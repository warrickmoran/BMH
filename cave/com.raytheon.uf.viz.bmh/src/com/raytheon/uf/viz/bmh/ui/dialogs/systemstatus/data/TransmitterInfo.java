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
package com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus.data;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;

/**
 * 
 * Class that hold the status information for a transmitter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 30, 2014  3349      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class TransmitterInfo implements Comparable<TransmitterInfo> {

    /** Mnemonic. */
    private String mnemonic = null;

    /** Transmitter name. */
    private String name = null;

    /** DAC port for the transmitter. */
    private Integer dacPort = null;

    /** Transmitter callsign. */
    private String callSign = null;

    /** Transmitter status. */
    private TxStatus txStatus;

    /** Transmitter ID. */
    private Integer id;

    /**
     * Constructor.
     */
    public TransmitterInfo() {

    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDacPort() {
        return dacPort;
    }

    public void setDacPort(Integer dacPort) {
        this.dacPort = dacPort;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    public TxStatus getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(TxStatus txStatus) {
        this.txStatus = txStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(TransmitterInfo o) {
        return this.mnemonic.compareTo(o.getMnemonic());
    }
}
