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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.datamodel.PositionOrdered;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Static Message Type information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 9, 2015  4213       bkowal      Initial creation
 * Apr 02, 2015 4248       rjpeter     Implement PositionOrdered
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
@NamedQueries({ @NamedQuery(name = StaticMessageType.GET_STATIC_MSG_BY_MSG_TYPE_AND_GROUP, query = StaticMessageType.GET_STATIC_MSG_BY_MSG_TYPE_AND_GROUP_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "static_message_type", schema = "bmh", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "transmittergroup_id", "msgtype_id" }),
        @UniqueConstraint(columnNames = { "transmittergroup_id", "language",
                "position" }) })
public class StaticMessageType implements PositionOrdered {

    public static final int MSG_LENGTH = 4096;

    private static final String DEFAULT_NO_PERIODICITY = "00000000";

    public static final String GET_STATIC_MSG_BY_MSG_TYPE_AND_GROUP = "getStaticMsgTypeByMsgTypeAndGroup";

    protected static final String GET_STATIC_MSG_BY_MSG_TYPE_AND_GROUP_QUERY = "FROM StaticMessageType s WHERE s.id.transmitterLanguagePK.transmitterGroup = :transmitterGroup AND s.msgTypeSummary.afosid = :afosid";

    @EmbeddedId
    private StaticMessageTypePK id;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(referencedColumnName = "language", name = "language", insertable = false, updatable = false),
            @JoinColumn(referencedColumnName = "transmittergroup_id", name = "transmittergroup_id", insertable = false, updatable = false) })
    // avoid circular references for dynamic serialize.
    private TransmitterLanguage transmitterLanguage;

    @ManyToOne(optional = false)
    @MapsId("msgTypeId")
    @JoinColumn(name = "msgtype_id")
    @DynamicSerializeElement
    @ForeignKey(name = "static_message_type_to_message_type")
    private MessageTypeSummary msgTypeSummary;

    /*
     * exists for the station id msg and time preamble msg.
     */
    @Column(length = MSG_LENGTH)
    @DynamicSerializeElement
    private String textMsg1;

    /*
     * exists for time postamble msg.
     */
    @Column(length = MSG_LENGTH)
    @DynamicSerializeElement
    private String textMsg2;

    @Column(nullable = false)
    @DynamicSerializeElement
    private String periodicity = DEFAULT_NO_PERIODICITY;

    /*
     * Position within its parent.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private int position;

    private void checkId() {
        if (this.id == null) {
            this.id = new StaticMessageTypePK();
        }
    }

    /**
     * @return the id
     */
    public StaticMessageTypePK getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(StaticMessageTypePK id) {
        this.id = id;
    }

    /**
     * @return the transmitterLanguage
     */
    public TransmitterLanguage getTransmitterLanguage() {
        return transmitterLanguage;
    }

    /**
     * @param transmitterLanguage
     *            the transmitterLanguage to set
     */
    public void setTransmitterLanguage(TransmitterLanguage transmitterLanguage) {
        this.transmitterLanguage = transmitterLanguage;
        if (this.transmitterLanguage == null) {
            return;
        }
        this.checkId();
        this.id.setTransmitterLanguagePK(this.transmitterLanguage.getId());
    }

    /**
     * @return the msgTypeSummary
     */
    public MessageTypeSummary getMsgTypeSummary() {
        return msgTypeSummary;
    }

    /**
     * @param msgTypeSummary
     *            the msgTypeSummary to set
     */
    public void setMsgTypeSummary(MessageTypeSummary msgTypeSummary) {
        this.msgTypeSummary = msgTypeSummary;
        if (this.msgTypeSummary == null) {
            return;
        }
        this.checkId();
        this.id.setMsgTypeId(this.msgTypeSummary.getId());
    }

    /**
     * @return the textMsg1
     */
    public String getTextMsg1() {
        return textMsg1;
    }

    /**
     * @param textMsg1
     *            the textMsg1 to set
     */
    public void setTextMsg1(String textMsg1) {
        this.textMsg1 = textMsg1;
    }

    /**
     * @return the textMsg2
     */
    public String getTextMsg2() {
        return textMsg2;
    }

    /**
     * @param textMsg2
     *            the textMsg2 to set
     */
    public void setTextMsg2(String textMsg2) {
        this.textMsg2 = textMsg2;
    }

    /**
     * @return the periodicity
     */
    public String getPeriodicity() {
        return periodicity;
    }

    /**
     * @param periodicity
     *            the periodicity to set
     */
    public void setPeriodicity(String periodicity) {
        if ((periodicity == null)
                || periodicity.trim().isEmpty()
                || (periodicity.trim().length() != DEFAULT_NO_PERIODICITY
                        .length())) {
            /*
             * ensure that only valid and recognized values are provided for the
             * periodicity.
             */
            periodicity = DEFAULT_NO_PERIODICITY;
        }
        this.periodicity = periodicity.trim();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.bmh.datamodel.PositionOrdered#getPosition()
     */
    @Override
    public int getPosition() {
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.bmh.datamodel.PositionOrdered#setPosition(int)
     */
    @Override
    public void setPosition(int position) {
        this.position = position;
    }
}