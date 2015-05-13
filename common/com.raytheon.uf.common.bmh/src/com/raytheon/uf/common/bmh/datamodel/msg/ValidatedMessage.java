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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.trace.ITraceable;
import com.raytheon.uf.common.bmh.trace.TraceableId;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Contains validation information about an {@link InputMessage}, any message
 * which has been accepted for further processing will have a status of
 * {@value #ACCEPTED}, any other status indicates a failure.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 16, 2014  3283     bsteffen    Initial creation
 * Jul 7, 2014   3302     bkowal      Use eager fetching to eliminate session closed
 *                                    errors with lazy loading.
 * Jul 17, 2014  3175     rjpeter     Updated query to match field name.
 * Sep 2, 2014   3568     bkowal      Added the getValidatedMsgForInputMsg named query.
 * Oct 21, 2014  3746     rjpeter     Hibernate upgrade.
 * Nov 02, 2014  3785     mpduff      Added DynamicSerialize annotations.
 * Nov 19, 2014  3385     bkowal      Added {@link LdadStatus#NONE}
 * Dec 02, 2014  3614     bsteffen    Add Unacceptable status.
 * Apr 16, 2015  4396     rferrel     Added {@link #ALL_UNEXPIRED_VALIDATED_MSGS_QUERY}.
 * May 12, 2015  4248     rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * May 13, 2016  4429     rferrel     Implement {@link ITraceable}.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = ValidatedMessage.GET_VALIDATED_MSG_FOR_INPUT_MSG, query = ValidatedMessage.GET_VALIDATED_MSG_FOR_INPUT_MSG_QUERY),
        @NamedQuery(name = ValidatedMessage.ALL_UNEXPIRED_VALIDATED_MSGS, query = ValidatedMessage.ALL_UNEXPIRED_VALIDATED_MSGS_QUERY) })
@Entity
@Table(name = "validated_msg")
@SequenceGenerator(initialValue = 1, name = ValidatedMessage.GEN, sequenceName = "validated_msg_seq")
@DynamicSerialize
public class ValidatedMessage implements ITraceable {

    public static final String GET_VALIDATED_MSG_FOR_INPUT_MSG = "getValidatedMsgForInputMsg";

    protected static final String GET_VALIDATED_MSG_FOR_INPUT_MSG_QUERY = "FROM ValidatedMessage vm WHERE vm.inputMessage = :inputMessage";

    public static final String ALL_UNEXPIRED_VALIDATED_MSGS = "getAllUnExpiredValidatedMsgs";

    protected static final String ALL_UNEXPIRED_VALIDATED_MSGS_QUERY = "SELECT vm FROM ValidatedMessage vm INNER JOIN vm.inputMessage  m  WHERE m.expirationTime IS NULL or m.expirationTime >= :currentTime";

    public static enum TransmissionStatus {
        /** This status must be set for a message to continue processing. */
        ACCEPTED,
        /** Message has expired and will not be transmitted */
        EXPIRED,
        /** The areas for the message cannot be mapped to a transmitter */
        UNPLAYABLE,
        /** The Message type is not in the configuration */
        UNDEFINED,
        /** The Message type is not assigned to any suite. */
        UNASSIGNED,
        /** An identical message has already been received */
        DUPLICATE,
        /** The message contents contains unnacceptable words. */
        UNACCEPTABLE,
        /** Validation did not complete successfully */
        ERROR;
    }

    public static enum LdadStatus {
        /** This status must be set for a message to continue processing. */
        ACCEPTED,
        /** This message does not have any associated ldad configuration **/
        NONE,
        /** The message contents contains unnacceptable words. */
        UNACCEPTABLE,
        /** Validation did not complete successfully */
        ERROR;
    }

    protected static final String GEN = "Validated Messsage Id Generator";

    @DynamicSerializeElement
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    private int id;

    @DynamicSerializeElement
    @OneToOne
    @JoinColumn(name = "input_msg_id")
    @ForeignKey(name = "fk_validated_msg_to_input_msg")
    private InputMessage inputMessage;

    @DynamicSerializeElement
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "validated_msg_transmitter_groups", joinColumns = @JoinColumn(name = "validated_msg_id"), inverseJoinColumns = @JoinColumn(name = "transmitter_group_id"))
    @ForeignKey(name = "fk_valid_msg_tx_groups_to_tx_group", inverseName = "fk_valid_msg_tx_groups_to_validated_msg")
    @Fetch(FetchMode.SUBSELECT)
    private Set<TransmitterGroup> transmitterGroups;

    @DynamicSerializeElement
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransmissionStatus transmissionStatus;

    @DynamicSerializeElement
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LdadStatus ldadStatus;

    @DynamicSerializeElement
    @Transient
    private String traceId;

    public ValidatedMessage() {
        this(null);
    }

    public ValidatedMessage(String traceId) {
        this.traceId = traceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InputMessage getInputMessage() {
        return inputMessage;
    }

    public void setInputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage;
    }

    public Set<TransmitterGroup> getTransmitterGroups() {
        return transmitterGroups;
    }

    public void setTransmitterGroups(Set<TransmitterGroup> transmitterGroups) {
        this.transmitterGroups = transmitterGroups;
    }

    public TransmissionStatus getTransmissionStatus() {
        return transmissionStatus;
    }

    public void setTransmissionStatus(TransmissionStatus transmissionStatus) {
        this.transmissionStatus = transmissionStatus;
    }

    public LdadStatus getLdadStatus() {
        return ldadStatus;
    }

    public void setLdadStatus(LdadStatus ldadStatus) {
        this.ldadStatus = ldadStatus;
    }

    public boolean isAccepted() {
        return LdadStatus.ACCEPTED.equals(ldadStatus)
                || TransmissionStatus.ACCEPTED.equals(transmissionStatus);
    }

    @Override
    public String toString() {
        return "ValidatedMessage [id=" + id + ", transmissionStatus="
                + transmissionStatus + ", ldadStatus=" + ldadStatus + "]";
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public TraceableId getTraceableId() {
        return new TraceableId(id, traceId);
    }
}
