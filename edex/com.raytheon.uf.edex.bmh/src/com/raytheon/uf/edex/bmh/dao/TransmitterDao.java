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
package com.raytheon.uf.edex.bmh.dao;

import java.util.List;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;

/**
 * Dao class for transmitter groups.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * May 30, 2014  3175     rjpeter     Initial creation
 * Jul 17, 2014  3406     mpduff      Added getAllTransmitters()
 * Sep 20, 2014 3640       rferrel     saveTransmitterDeleteGroup no longer deletes transmitter.
 * Oct 06, 2014  3687     bsteffen    Add operational flag to constructor.
 * Mar 25, 2015  4305     rferrel     Added {@link #getTransmitterByFips(String)}.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class TransmitterDao extends AbstractBMHDao<Transmitter, Integer> {
    public TransmitterDao() {
        super(Transmitter.class);
    }

    public TransmitterDao(boolean operational) {
        super(operational, Transmitter.class);
    }

    /**
     * Get all the data on all the transmitters.
     * 
     * @return transmitters
     */
    public List<Transmitter> getAllTransmitters() {
        return this.loadAll();
    }

    /**
     * Update transmitter, remove it from the transmitter group then delete the
     * group.
     * 
     * @param transmitter
     * @param transmitterGroup
     * @return transmitter
     */
    public Transmitter saveTransmitterDeleteGroup(
            final Transmitter transmitter,
            final TransmitterGroup transmitterGroup) {
        Transmitter xmit = txTemplate
                .execute(new TransactionCallback<Transmitter>() {
                    @Override
                    public Transmitter doInTransaction(TransactionStatus status) {
                        saveOrUpdate(transmitter);

                        /*
                         * Remove transmitter from the group to prevent deleting
                         * it along with the group.
                         */
                        transmitterGroup.getTransmitters().remove(transmitter);
                        delete(transmitterGroup);
                        return transmitter;
                    }
                });

        return xmit;
    }

    /**
     * Get list of transmitters with the FIPS Code. Normally should only be zero
     * or one but if data base corrupt could have more that need to be dealt
     * with.
     * 
     * @param FipsCode
     * @return transmitters
     */
    @SuppressWarnings("unchecked")
    public List<Transmitter> getTransmitterByFips(String fipsCode) {
        return (List<Transmitter>) findByNamedQueryAndNamedParam(
                Transmitter.GET_TRANSMITTERS_FOR_FIPS, "fipscode", fipsCode);
    }
}
