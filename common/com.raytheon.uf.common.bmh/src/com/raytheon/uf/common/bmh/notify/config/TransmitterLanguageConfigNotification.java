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
package com.raytheon.uf.common.bmh.notify.config;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguagePK;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Notification that is used when a {@link TransmitterLanguage} is created,
 * updated, or deleted.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 8, 2014  3568       bkowal      Initial creation
 * Jan 19, 2015 4011       bkowal      Added {@link #transmitterGroup}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@DynamicSerialize
public class TransmitterLanguageConfigNotification extends ConfigNotification {

    @DynamicSerializeElement
    private TransmitterLanguagePK key;

    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    /**
     * 
     */
    public TransmitterLanguageConfigNotification() {
        super();
    }

    /**
     * @param type
     */
    public TransmitterLanguageConfigNotification(ConfigChangeType type,
            TransmitterLanguage tl) {
        super(type);
        this.setKey(tl);
        this.transmitterGroup = tl.getTransmitterGroup();
    }

    /**
     * @return the key
     */
    public TransmitterLanguagePK getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(TransmitterLanguagePK key) {
        this.key = key;
    }

    public void setKey(TransmitterLanguage tl) {
        this.key = tl.getId();
    }

    /**
     * @return the transmitterGroup
     */
    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }
}