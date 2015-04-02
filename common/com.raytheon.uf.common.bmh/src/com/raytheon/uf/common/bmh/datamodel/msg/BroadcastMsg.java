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
package com.raytheon.uf.common.bmh.datamodel.msg;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.datamodel.PositionUtil;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage.ReplacementType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Broadcast Message Record Object. Used to transform text to audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3302     bkowal      Initial creation
 * Jul 10, 2014  3285     bsteffen    Add getAfosid()
 * Aug 29, 2014  3568     bkowal      Added query to retrieve broadcast msg by transmitter
 *                                    group, language and afos id.
 * Sep 03, 2014  3554     bsteffen    Add getUnexpiredBroadcastMsgsByAfosIDAndGroup
 * Sep 10, 2014  2585     bsteffen    Implement MAT
 * Sep 12, 2014  3588     bsteffen    Support audio fragments.
 * Oct 21, 2014  3746     rjpeter     Hibernate upgrade.
 * Oct 23, 2014  3748     bkowal      Added getBroadcastMsgsByInputMsg.
 * Nov 18, 2014  3746     rjpeter     Labeled foreign key.
 * Nov 26, 2014  3613     bsteffen    Add getBroadcastMsgsByFragmentPath
 * Dec 08, 2014  3864     bsteffen    Redo some of the playlist manager queries.
 * Mar 05, 2015  4222     bkowal      Include messages that never expire when retrieving
 *                                    unexpired messages.
 * Mar 13, 2015  4213     bkowal      Fixed {@link #GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE_QUERY}.
 * Mar 17, 2015  4160     bsteffen    Add booleans for tone status.
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Apr 02, 2015  4248     rjpeter     Made BroadcastFragment database relation a set and add ordered return methods.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@NamedQueries({
        @NamedQuery(name = BroadcastMsg.GET_UNEXPIRED_MSGS_BY_AFOS_IDS_AND_GROUP, query = BroadcastMsg.GET_UNEXPIRED_MSGS_BY_AFOS_IDS_AND_GROUP_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE, query = BroadcastMsg.GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSGS_BY_INPUT_MSG, query = BroadcastMsg.GET_MSGS_BY_INPUT_MSG_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSG_BY_FRAGMENT_PATH, query = BroadcastMsg.GET_MSG_BY_FRAGMENT_PATH_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "broadcast_msg", schema = "bmh")
@SequenceGenerator(initialValue = 1, name = BroadcastMsg.GEN, sequenceName = "broadcast_msg_seq")
public class BroadcastMsg {
    public static final String GEN = "Broadcast Msg Generator";

    public static final String GET_UNEXPIRED_MSGS_BY_AFOS_IDS_AND_GROUP = "getBroadcastMsgsByAfosIdsAndGroup";

    protected static final String GET_UNEXPIRED_MSGS_BY_AFOS_IDS_AND_GROUP_QUERY = "FROM BroadcastMsg m WHERE m.inputMessage.afosid IN :afosIDs AND (m.inputMessage.expirationTime > :expirationTime OR (m.inputMessage.expirationTime is null AND m.inputMessage.validHeader = true)) AND m.transmitterGroup = :group ORDER BY m.inputMessage.creationTime DESC";

    public static final String GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE = "getBroadcastMsgsByAfosIdGroupAndLanguage";

    protected static final String GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE_QUERY = "FROM BroadcastMsg m WHERE m.inputMessage.afosid = :afosId AND m.transmitterGroup = :group AND m.inputMessage.language = :language ORDER BY m.inputMessage.creationTime DESC";

    public static final String GET_MSGS_BY_INPUT_MSG = "getBroadcastMsgsByInputMsg";

    protected static final String GET_MSGS_BY_INPUT_MSG_QUERY = "FROM BroadcastMsg m WHERE m.inputMessage.id = :inputMsgId";

    public static final String GET_MSG_BY_FRAGMENT_PATH = "getBroadcastMsgsByFragmentPath";

    protected static final String GET_MSG_BY_FRAGMENT_PATH_QUERY = "SELECT m FROM BroadcastMsg m inner join m.fragments fragment where fragment.outputName = :path";

    /* A unique auto-generated numerical id. Long = SQL BIGINT */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    private long id;

    /* The date that the record was created; mainly for auditing purposes. */
    @Column(nullable = false)
    @DynamicSerializeElement
    private Calendar creationDate;

    /* The date the record was last updated; mainly for auditing purposes. */
    @Column(nullable = false)
    @DynamicSerializeElement
    private Calendar updateDate;

    /* ===== Message Header ===== */

    /*
     * The transmitter group associated with the record. Transmitter Groups will
     * have a dictionary associated with them based on the language (voice
     * determines the language). So, a separate broadcast message will be
     * generated for each { transmitter group, dictionary } combination and this
     * field is set to the transmitter group in that combination.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "transmitter_group_id")
    @ForeignKey(name = "broadcast_msg_to_tx_group")
    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "input_message_id")
    @DynamicSerializeElement
    private InputMessage inputMessage;

    @OneToMany(mappedBy = "message", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @DynamicSerializeElement
    private Set<BroadcastFragment> fragments;

    @Column
    @DynamicSerializeElement
    private boolean playedSameTone = false;

    @Column
    @DynamicSerializeElement
    private boolean playedAlertTone = false;

    /**
     * 
     */
    public BroadcastMsg() {
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the creationDate
     */
    public Calendar getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the updateDate
     */
    public Calendar getUpdateDate() {
        return updateDate;
    }

    /**
     * @param updateDate
     *            the updateDate to set
     */
    public void setUpdateDate(Calendar updateDate) {
        this.updateDate = updateDate;
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

    /**
     * @return the inputMessage
     */
    public InputMessage getInputMessage() {
        return inputMessage;
    }

    /**
     * @param inputMessage
     *            the inputMessage to set
     */
    public void setInputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage;
    }

    /**
     * Returns BroadcastFragments in position order.
     * 
     * @return
     */
    public List<BroadcastFragment> getOrderedFragments() {
        if (fragments == null) {
            return Collections.emptyList();
        }

        return PositionUtil.order(fragments);
    }

    /**
     * Sets the BroadcastFragments in the specified position order.
     * 
     * @param fragments
     */
    public void setOrderedFragments(List<BroadcastFragment> fragments) {
        if (fragments == null) {
            this.fragments = null;
            return;
        }

        PositionUtil.updatePositions(fragments);
        setFragments(new HashSet<>(fragments));
    }

    /**
     * Returns the BroadcastFragments
     * 
     * @return
     */
    public Set<BroadcastFragment> getFragments() {
        return fragments;
    }

    /**
     * 
     * @param fragments
     */
    public void setFragments(Set<BroadcastFragment> fragments) {
        this.fragments = fragments;

        if (this.fragments != null) {
            for (BroadcastFragment fragment : this.fragments) {
                fragment.setMessage(this);
            }
        }
    }

    public void addFragment(BroadcastFragment fragment) {
        if (this.fragments == null) {
            this.fragments = new HashSet<>(2, 1);
        }

        PositionUtil.updatePositions(fragments);
        fragment.setPosition(this.fragments.size());
        fragment.setMessage(this);
        this.fragments.add(fragment);
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        for (BroadcastFragment fragment : fragments) {
            if (!fragment.isSuccess()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convenience method, equivalent to getInput().getAfosid();
     * 
     * @return the afosid
     */
    public String getAfosid() {
        if (inputMessage == null) {
            return null;
        } else {
            return inputMessage.getAfosid();
        }
    }

    public boolean isPlayedSameTone() {
        return playedSameTone;
    }

    public void setPlayedSameTone(boolean playedSameTone) {
        this.playedSameTone = playedSameTone;
    }

    public boolean isPlayedAlertTone() {
        return playedAlertTone;
    }

    public void setPlayedAlertTone(boolean playedAlertTone) {
        this.playedAlertTone = playedAlertTone;
    }

    public Calendar getEffectiveTime() {
        return inputMessage.getEffectiveTime();
    }

    public Calendar getExpirationTime() {
        return inputMessage.getExpirationTime();
    }

    public boolean isActive() {
        return inputMessage.getActive();
    }

    public ReplacementType getReplacementType() {
        return inputMessage.getReplacementType();
    }

    public boolean isPeriodic() {
        return inputMessage.isPeriodic();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = (prime * result) + (int) (id ^ (id >>> 32));
        result = (prime * result)
                + ((inputMessage == null) ? 0 : inputMessage.hashCode());
        result = (prime * result)
                + ((transmitterGroup == null) ? 0 : transmitterGroup.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BroadcastMsg other = (BroadcastMsg) obj;
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (inputMessage == null) {
            if (other.inputMessage != null) {
                return false;
            }
        } else if (!inputMessage.equals(other.inputMessage)) {
            return false;
        }
        if (transmitterGroup == null) {
            if (other.transmitterGroup != null) {
                return false;
            }
        } else if (!transmitterGroup.equals(other.transmitterGroup)) {
            return false;
        }
        return true;
    }

}