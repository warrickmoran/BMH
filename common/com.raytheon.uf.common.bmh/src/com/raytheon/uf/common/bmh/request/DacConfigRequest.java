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
package com.raytheon.uf.common.bmh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * POJO used to request DAC configuration changes and/or DAC reboots.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2015 5113       bkowal      Initial creation
 * Nov 23, 2015 5113       bkowal      Added {@link #sync}.
 * May 09, 2016 5630       rjpeter     Remove DAC Sync.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class DacConfigRequest extends DacRequest {

    @DynamicSerializeElement
    private boolean reboot;

    @DynamicSerializeElement
    private String configAddress;

    public DacConfigRequest() {
    }

    /**
     * @return the reboot
     */
    public boolean isReboot() {
        return reboot;
    }

    /**
     * @param reboot
     *            the reboot to set
     */
    public void setReboot(boolean reboot) {
        this.reboot = reboot;
    }

    /**
     * @return the configAddress
     */
    public String getConfigAddress() {
        return configAddress;
    }

    /**
     * @param configAddress
     *            the configAddress to set
     */
    public void setConfigAddress(String configAddress) {
        this.configAddress = configAddress;
    }
}