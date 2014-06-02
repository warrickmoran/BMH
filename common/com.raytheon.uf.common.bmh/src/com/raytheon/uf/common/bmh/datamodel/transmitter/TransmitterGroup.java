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

import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Transmitter Group information. A transmitter group of one is usually named
 * after the mnemonic of the transmitter.
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
// @NamedQueries({
// @NamedQuery(name = "getTransmitterGroupNames", query =
// "SELECT t.name FROM TransmitterGroup"),
// @NamedQuery(name = TransmitterGroup.GET_TRANSMITTER_GROUPS, query =
// "SELECT t FROM TransmitterGroup") })
@Entity
@Table(name = "transmitter_group", schema = "bmh")
@DynamicSerialize
public class TransmitterGroup {
    public static final String GET_TRANSMITTER_GROUPS = "getTransmitterGroups";

    public static final int NAME_LENGTH = 20;

    public enum TxStatus {
        ENABLED, DISABLED
    };

    public enum TxMode {
        PRIMARY, SECONDARY
    };

    @Id
    @Column(length = NAME_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 8, nullable = false)
    private TxStatus txStatus = TxStatus.ENABLED;

    @Enumerated(EnumType.STRING)
    @Column(length = 9, nullable = false)
    private TxMode txMode = TxMode.PRIMARY;

    // @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @PrimaryKeyJoinColumn
    // @DynamicSerializeElement
    // private Program program;
    // ForeignKey to Program enforced at table creation via scripts.
    @Column(length = 20, nullable = false)
    @DynamicSerializeElement
    private String programName;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "transmitterGroupName")
    @MapKey(name = "id.language")
    @DynamicSerializeElement
    private Map<Language, TransmitterLanguage> languages;

    @Embedded
    @DynamicSerializeElement
    private Tone tone;

    @Column
    @DynamicSerializeElement
    private Integer rcs;

    @Column
    @DynamicSerializeElement
    private Integer rcsPort;

    @Column(length = 15)
    @DynamicSerializeElement
    private String timeZone;

    @Column
    @DynamicSerializeElement
    private Boolean silenceAlarm;

    @Column
    @DynamicSerializeElement
    private Boolean daylightSaving;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "transmitterGroup")
    @DynamicSerializeElement
    Set<Transmitter> transmitters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TxStatus getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(TxStatus txStatus) {
        this.txStatus = txStatus;
    }

    public TxMode getTxMode() {
        return txMode;
    }

    public void setTxMode(TxMode txMode) {
        this.txMode = txMode;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public Tone getTone() {
        return tone;
    }

    public void setTone(Tone tone) {
        this.tone = tone;
    }

    public Integer getRcs() {
        return rcs;
    }

    public void setRcs(Integer rcs) {
        this.rcs = rcs;
    }

    public Integer getRcsPort() {
        return rcsPort;
    }

    public void setRcsPort(Integer rcsPort) {
        this.rcsPort = rcsPort;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Boolean getSilenceAlarm() {
        return silenceAlarm;
    }

    public void setSilenceAlarm(Boolean silenceAlarm) {
        this.silenceAlarm = silenceAlarm;
    }

    public Boolean getDaylightSaving() {
        return daylightSaving;
    }

    public void setDaylightSaving(Boolean daylightSaving) {
        this.daylightSaving = daylightSaving;
    }
}
