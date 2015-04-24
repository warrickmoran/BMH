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
package com.raytheon.uf.viz.bmh;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;

import com.raytheon.uf.common.bmh.notify.config.AbstractTraceableSystemConfigNotification;
import com.raytheon.uf.common.bmh.request.AbstractBMHSystemConfigRequest;
import com.raytheon.uf.common.bmh.request.MessageTypeRequest;
import com.raytheon.uf.common.bmh.request.ProgramRequest;
import com.raytheon.uf.common.bmh.request.SuiteRequest;
import com.raytheon.uf.common.bmh.request.ZoneAreaRequest;
import com.raytheon.uf.common.bmh.stats.SystemConfigProcessingTimeEvent;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.stats.collector.StatsCollector;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Utility to generate {@link SystemConfigProcessingTimeEvent} based on a
 * {@link AbstractBMHSystemConfigRequest} and the generated
 * {@link AbstractTraceableSystemConfigNotification}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 21, 2015 4397       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHConfigStatisticsGenerator {

    private static enum ConfigurationType {
        MESSAGE_TYPE, AREAS_AND_ZONES, SUITE, PROGRAM;

        private final String text;

        private ConfigurationType() {
            this.text = WordUtils
                    .capitalizeFully(this.name().replace("_", " "));
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    private static final Map<Class<? extends AbstractBMHSystemConfigRequest>, ConfigurationType> typeLookupMap;
    static {
        typeLookupMap = new HashMap<>(ConfigurationType.values().length, 1.0f);
        typeLookupMap.put(MessageTypeRequest.class,
                ConfigurationType.MESSAGE_TYPE);
        typeLookupMap.put(ZoneAreaRequest.class,
                ConfigurationType.AREAS_AND_ZONES);
        typeLookupMap.put(ProgramRequest.class, ConfigurationType.PROGRAM);
        typeLookupMap.put(SuiteRequest.class, ConfigurationType.SUITE);
    }

    /**
     * 
     */
    protected BMHConfigStatisticsGenerator() {
    }

    public static synchronized AbstractBMHSystemConfigRequest prepareStatistic(
            AbstractBMHSystemConfigRequest request) {

        if (request.isSystemConfigChange() == false) {
            /*
             * Do not generate a statistic if this request will not alter the
             * system configuration.
             */
            return request;
        }

        /*
         * If we are in practice mode, do not generate any statistics.
         */
        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            return request;
        }

        ConfigurationType configurationType = typeLookupMap.get(request
                .getClass());
        if (configurationType == null) {
            /*
             * Unlikely or developer mistake.
             */
            throw new IllegalArgumentException(
                    "Failed to find a configuration type associated with: "
                            + request.getClass().getName() + ".");
        }

        /*
         * Construct the statistic.
         */
        final String statisticKey = VizApp.getWsId().toString() + "-"
                + UUID.randomUUID().toString();
        request.setStatisticKey(statisticKey);

        StatsCollector.start(statisticKey, new SystemConfigProcessingTimeEvent(
                configurationType.toString()));

        return request;
    }

    public static void publishStatistic(
            final AbstractTraceableSystemConfigNotification notification) {

        /*
         * If we are in practice mode, do not generate any statistics.
         */
        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            return;
        }

        StatsCollector.stop(notification.getStatisticKey());
    }
}