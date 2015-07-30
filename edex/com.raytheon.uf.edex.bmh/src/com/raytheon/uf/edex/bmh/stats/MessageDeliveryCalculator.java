/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.edex.bmh.stats;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.raytheon.uf.common.bmh.stats.DeliveryPercentFiller;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.stats.AggregateRecord;
import com.raytheon.uf.common.stats.StatsGrouping;
import com.raytheon.uf.common.stats.StatsGroupingColumn;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterTask;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

/**
 * Used to periodically calculate message delivery statistics based on a cron
 * schedule.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 29, 2015 4686       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MessageDeliveryCalculator implements ApplicationContextAware {

    private static final String DATE_FORMAT = "MM-dd-yyy HH:mm";

    private static final DateFormat df = new SimpleDateFormat(DATE_FORMAT);

    private static final String LOCK_NAME = "msgDeliveryStatLock";

    private static final String LOCK_DETAILS = "calculateAggregate";

    private static final Pattern COLON_PATTERN = Pattern.compile(":");

    private static final String COLON_REPLACEMENT = Matcher
            .quoteReplacement("\\:");

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(MessageDeliveryCalculator.class);

    private static final String PROCEDURE_SQL = "classpath:res/scripts/message_delivery_stat.sql";

    private static final String METADATA_DB = "metadata";

    private final String groupingXml;

    private ApplicationContext context;

    private final CoreDao aggregateDao = new CoreDao(
            DaoConfig.forDatabase(METADATA_DB));

    private final BMHStatsDao bmhStatsDao = new BMHStatsDao();

    public MessageDeliveryCalculator() {
        final JAXBManager jaxbManager;

        try {
            jaxbManager = new JAXBManager(StatsGroupingColumn.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(
                    "Failed to initialize the JAXB Manager.", e);
        }

        final StatsGrouping statsGrouping = new StatsGrouping("summary",
                "Summary");
        StatsGroupingColumn statsGroupingColumn = StatsGroupingColumn
                .withGroupings(statsGrouping);

        try {
            groupingXml = jaxbManager.marshalToXml(statsGroupingColumn);
        } catch (JAXBException e) {
            throw new IllegalStateException("XML Marshalling failed.", e);
        }
    }

    public void initialize() {
        statusHandler.info("Attempting to load SQL Procedure: " + PROCEDURE_SQL
                + " ...");
        Resource resource = this.context.getResource(PROCEDURE_SQL);
        if (resource instanceof ClassPathResource) {
            ClassPathResource cpResource = (ClassPathResource) resource;
            if (cpResource.exists()) {
                StringBuffer sb = new StringBuffer();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(cpResource.getInputStream()))) {
                    String line = StringUtils.EMPTY;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Failed to read SQL Procedure: " + PROCEDURE_SQL
                                    + "!", e);
                }
                String sql = COLON_PATTERN.matcher(sb.toString()).replaceAll(
                        COLON_REPLACEMENT);
                try {
                    this.bmhStatsDao.importSQLProcedure(sql);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Failed to import SQL Procedure: " + PROCEDURE_SQL
                                    + "!", e);
                }
                statusHandler.info("Successfully loaded SQL Procedure: "
                        + PROCEDURE_SQL + ".");
                return;
            }
        }

        throw new IllegalStateException(
                "Unable to find the expected SQL Procedure: " + PROCEDURE_SQL
                        + "!");
    }

    public void calculateAggregate() {
        boolean existingLock = false;
        statusHandler.info("Completing aggregate calculations ...");

        /*
         * The end time is the current time rounded to a fifth minute.
         */
        Calendar currentRunTime = TimeUtil.newGmtCalendar();
        /*
         * 0 out the seconds and milliseconds.
         */
        currentRunTime.set(Calendar.SECOND, 0);
        currentRunTime.set(Calendar.MILLISECOND, 0);
        /*
         * ensure that we are on a fifth minute.
         */
        if (currentRunTime.get(Calendar.MINUTE) % 5 != 0) {
            int minute = currentRunTime.get(Calendar.MINUTE) - 1;
            while (minute % 5 != 0) {
                --minute;
            }
            currentRunTime.set(Calendar.MINUTE, minute);
        }

        /*
         * The end time will be five minutes ago.
         */
        Date endDate = DateUtils.addMinutes(currentRunTime.getTime(), -5);
        Date startDate = null;

        /*
         * Determine what time to start the aggregation.
         */
        final ClusterTask task = ClusterLockUtils.lookupLock(LOCK_NAME,
                LOCK_DETAILS);
        if (task != null && task.getExtraInfo() != null) {
            existingLock = true;
            try {
                synchronized (df) {
                    startDate = df.parse(task.getExtraInfo());
                }
                statusHandler.info("Setting start to [" + task.getExtraInfo()
                        + "].");
            } catch (ParseException e) {
                statusHandler.error(
                        "Failed to parse date stored in the cluster lock: "
                                + task.getExtraInfo() + ".", e);
            }
        }

        if (startDate == null) {
            /*
             * The default start time is five minutes prior to the end time.
             */
            startDate = DateUtils.addMinutes(endDate, -5);
        }

        while (DateUtils.isSameInstant(startDate, endDate) == false) {
            Calendar startTime = TimeUtil.newGmtCalendar(startDate);
            Calendar endTime = TimeUtil.newGmtCalendar(DateUtils.addMinutes(
                    startDate, 5));

            synchronized (df) {
                statusHandler.info("Generating stats for ["
                        + df.format(startTime.getTime()) + "] to ["
                        + df.format(endTime.getTime()) + "].");
            }

            final DeliveryStats stats = this.bmhStatsDao
                    .executeMessageDeliveryProcedure(startTime, endTime);
            if (stats.getExpectedCount() == 0) {
                statusHandler
                        .info("The aggregate calculation has been finished. There are no stats to record.");
            } else {
                /*
                 * Create the {@link AggregateRecord}.
                 */
                AggregateRecord record = new AggregateRecord(
                        DeliveryPercentFiller.class.getName(), startTime,
                        currentRunTime, this.groupingXml, "pctSuccess");
                record.setSum(stats.getActualCount());
                record.setMin(stats.getPercentage());
                record.setMax(stats.getPercentage());
                record.setCount(stats.getExpectedCount());
                this.aggregateDao.create(record);
            }

            startDate = DateUtils.addMinutes(startDate, 5);
        }

        final String extraInfo;
        synchronized (df) {
            extraInfo = df.format(endDate);
        }

        if (existingLock) {
            ClusterLockUtils
                    .updateExtraInfo(LOCK_NAME, LOCK_DETAILS, extraInfo);
        } else {
            ClusterLockUtils.lock(LOCK_NAME, LOCK_DETAILS, extraInfo, 30, true);
        }

        statusHandler.info("Successfully finished aggregate calculations.");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = applicationContext;
    }
}