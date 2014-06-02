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
package com.raytheon.uf.common.bmh.datamodel.transmitter;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Transmitter Language Primary Key
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Embeddable
@DynamicSerialize
public class TransmitterLanguagePK implements Serializable {
    private static final long serialVersionUID = 1L;

    // FK to transmitterGroup
    @Column(length = TransmitterGroup.NAME_LENGTH, nullable = false)
    @DynamicSerializeElement
    private String transmitterGroupName;

    // Language: 0-English, 1-Spanish
    @Enumerated(EnumType.STRING)
    @Column(length = Language.LENGTH, nullable = false)
    @DynamicSerializeElement
    private Language language;

    public String getTransmitterGroupName() {
        return transmitterGroupName;
    }

    public void setTransmitterGroupName(String transmitterGroupName) {
        this.transmitterGroupName = transmitterGroupName;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
