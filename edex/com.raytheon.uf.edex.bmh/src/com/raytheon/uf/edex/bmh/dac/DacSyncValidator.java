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
package com.raytheon.uf.edex.bmh.dac;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.notify.config.DacNotSyncNotification;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.DacDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.core.EdexException;

/**
 * Verifies that all DACs and {@link Dac}s are in sync based on a scheduled
 * interval, the {@link DacSyncValidator} will verify that the information
 * associated with the DAC and {@link Dac} match. If an inconsistency is
 * discovered, a {@link BMH_CATEGORY#DAC_SYNC_ISSUE} notification will be sent
 * out to all CAVE.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2015 5113       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DacSyncValidator {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(DacSyncValidator.class);

    private final Set<Integer> desyncedDacsList = new HashSet<>();

    private final DacDao dacDao;

    public DacSyncValidator(DacDao dacDao) {
        this.dacDao = dacDao;
    }

    private void verify() throws Exception {
        statusHandler.info("Verifying that BMH and all DACs are in sync ...");
        /*
         * Alternatively, we could retrieve all {@link Dac}s in the system at
         * startup and just listen for DAC Configuration Notification updates
         * instead of constantly querying for all DACs.
         */
        List<Dac> allDacs = this.dacDao.getAll();
        if (allDacs.isEmpty()) {
            synchronized (this.desyncedDacsList) {
                /*
                 * No {@link Dac}s exists. So, there should not be any desycned
                 * {@link Dac}s.
                 */
                this.desyncedDacsList.clear();
            }
            return;
        }

        final List<Integer> allDacIds = new ArrayList<>(allDacs.size());
        for (Dac dac : allDacs) {
            allDacIds.add(dac.getId());
            statusHandler.info("Verifying that BMH Dac: " + dac.getId()
                    + " and DAC: " + dac.getAddress() + " are in sync ...");

            OrionPost orionPost = new OrionPost(dac, null);

            /*
             * First, verify connection to the DAC.
             */
            boolean available = true;
            Exception availableException = null;
            try {
                available = orionPost
                        .verifyDacAvailability(OrionPost.DEFAULT_TIMEOUT_SECONDS);
            } catch (Exception e) {
                available = false;
                availableException = e;
            }

            if (available == false) {
                StringBuilder sb = new StringBuilder(
                        "Failed to connect to DAC: ").append(dac.getAddress())
                        .append(". Unable to detrmine if BMH Dac: ")
                        .append(dac.getName()).append(" and DAC: ")
                        .append(dac.getAddress()).append(" are in sync.");

                if (availableException == null) {
                    statusHandler.error(BMH_CATEGORY.DAC_SYNC_ISSUE,
                            sb.toString());
                } else {
                    statusHandler.error(BMH_CATEGORY.DAC_SYNC_ISSUE,
                            sb.toString(), availableException);
                }

                synchronized (this.desyncedDacsList) {
                    this.desyncedDacsList.add(dac.getId());
                }
                /*
                 * Continue checking the other {@link Dac}s.
                 */
                continue;
            }

            final List<String> nonSyncedFields = orionPost.verifySync(false);
            if (nonSyncedFields.isEmpty() == false) {
                StringBuilder sb = new StringBuilder("BMH Dac: ");
                sb.append(dac.getName()).append(" and DAC: ")
                        .append(dac.getAddress())
                        .append(" are not in sync! Non-synced fields: ");
                sb.append(nonSyncedFields).append(" ...");

                statusHandler.error(BMH_CATEGORY.DAC_SYNC_ISSUE, sb.toString());
                synchronized (this.desyncedDacsList) {
                    this.desyncedDacsList.add(dac.getId());
                }
                continue;
            }

            synchronized (this.desyncedDacsList) {
                /*
                 * Verify that the {@link Dac} and DAC were not previously out
                 * of sync.
                 */
                this.desyncedDacsList.remove(dac.getId());
            }
            statusHandler.info("Successfully verified that BMH Dac: "
                    + dac.getId() + " and DAC: " + dac.getAddress()
                    + " are in sync.");
        }
        /*
         * Remove any {@link Dac}s that no longer exist from the desynced {@link
         * Dac}s list.
         */
        synchronized (this.desyncedDacsList) {
            Iterator<Integer> iterator = this.desyncedDacsList.iterator();
            while (iterator.hasNext()) {
                if (allDacIds.contains(iterator.next()) == false) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Returns a {@link List} containing the ids of {@link Dac}s that were found
     * to be out of sync when applicable.
     * 
     * @return a {@link List} of {@link Dac} ids.
     */
    public List<Integer> getOutOfSyncDacs() {
        synchronized (this.desyncedDacsList) {
            return new ArrayList<>(this.desyncedDacsList);
        }
    }

    public void run() {
        try {
            this.verify();
        } catch (Exception e) {
            statusHandler
                    .error(BMH_CATEGORY.DAC_SYNC_VERIFY_FAIL,
                            "Failed to verify whether or not the BMH Dacs and the DACS are in sync.",
                            e);
        }

        this.notifyNotSynced();
    }

    private void notifyNotSynced() {
        DacNotSyncNotification notification = new DacNotSyncNotification(
                this.getOutOfSyncDacs());
        try {
            BmhMessageProducer.sendConfigMessage(notification, true);
        } catch (EdexException | SerializationException e) {
            statusHandler
                    .error(BMH_CATEGORY.DAC_SYNC_VERIFY_FAIL,
                            "Failed to send an updated notification containing the out of sync DACs.",
                            e);
        }
    }
}