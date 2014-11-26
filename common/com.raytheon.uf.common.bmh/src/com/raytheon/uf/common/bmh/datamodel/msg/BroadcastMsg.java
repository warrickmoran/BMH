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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@NamedQueries({
        @NamedQuery(name = BroadcastMsg.GET_MSGS_BY_AFOS_ID, query = BroadcastMsg.GET_MSGS_BY_AFOS_ID_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE, query = BroadcastMsg.GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSGS_BY_INPUT_MSG, query = BroadcastMsg.GET_MSGS_BY_INPUT_MSG_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_UNEXPIRED_MSGS_BY_AFOS_ID_AND_GROUP, query = "FROM BroadcastMsg m WHERE m.inputMessage.afosid = :afosID AND m.inputMessage.expirationTime > :expirationTime AND m.transmitterGroup = :group"),
        @NamedQuery(name = BroadcastMsg.GET_MSG_BY_FRAGMENT_PATH, query = BroadcastMsg.GET_MSG_BY_FRAGMENT_PATH_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "broadcast_msg", schema = "bmh")
@SequenceGenerator(initialValue = 1, name = BroadcastMsg.GEN, sequenceName = "broadcast_msg_seq")
public class BroadcastMsg {
    public static final String GEN = "Broadcast Msg Generator";

    public static final String GET_MSGS_BY_AFOS_ID = "getBroadcastMsgsByAfosId";

    protected static final String GET_MSGS_BY_AFOS_ID_QUERY = "FROM BroadcastMsg m WHERE m.inputMessage.afosid = :afosId";

    public static final String GET_UNEXPIRED_MSGS_BY_AFOS_ID_AND_GROUP = "getUnexpiredBroadcastMsgsByAfosIDAndGroup";

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
    @OrderColumn(name = "position", nullable = false)
    @Fetch(FetchMode.SELECT)
    @DynamicSerializeElement
    private List<BroadcastFragment> fragments;

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

    public List<BroadcastFragment> getFragments() {
        return fragments;
    }

    public void setFragments(List<BroadcastFragment> fragments) {
        this.fragments = fragments;
        for (BroadcastFragment fragment : fragments) {
            fragment.setMessage(this);
        }
    }

    public void addFragment(BroadcastFragment fragment) {
        if (this.fragments == null) {
            this.fragments = new ArrayList<>(1);
        }
        this.fragments.add(fragment);
        fragment.setMessage(this);
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
}