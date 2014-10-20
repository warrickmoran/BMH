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

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Contains the parsed message data exactly as it was received from NWRWAVES.
 * This is the starting point of messages within BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 16, 2014  3283     bsteffen    Initial creation
 * Aug 14, 2014  3432     mpduff      Added isPeriodic method
 * Sep 4, 2014   3568     bkowal      Added fields to differentiate between
 *                                    generated static messages and ingested messages.
 * Sep 09, 2014  2585     bsteffen    Implement MAT
 * Sep 25, 2014  3620     bsteffen    Add seconds to periodicity.
 * Oct 15, 2014  3728     lvenable    Added name column.
 * Oct 18, 2014  3728     lvenable    Added query to retrieve the Id, Name, Afos Id, and
 *                                    creation time.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = InputMessage.DUP_QUERY_NAME, query = InputMessage.DUP_QUERY),
        @NamedQuery(name = InputMessage.GET_INPUT_MSGS_ID_NAME_AFOS_CREATION, query = InputMessage.GET_INPUT_MSGS_ID_NAME_AFOS_CREATION_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "input_msg", schema = "bmh")
@SequenceGenerator(initialValue = 1, schema = "bmh", name = InputMessage.GEN, sequenceName = "input_msg_seq")
public class InputMessage {

    protected static final String GEN = "Input Messsage Id Generator";

    public static final String GET_INPUT_MSGS_ID_NAME_AFOS_CREATION = "getInputMsgIdNameAfosCreation";

    protected static final String GET_INPUT_MSGS_ID_NAME_AFOS_CREATION_QUERY = "select id, name, afosid, creationTime FROM InputMessage im";

    /**
     * Named query to pull all messages with a matching afosid and with a valid
     * time range encompassing a specified time range.
     */
    public static final String DUP_QUERY_NAME = "getDuplicateInputMessages";

    protected static final String DUP_QUERY = "FROM InputMessage m WHERE m.id != :id AND m.afosid = :afosid AND ((m.mrd = :mrd) OR (m.effectiveTime <= :effectiveTime AND m.expirationTime >= :expirationTime))";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    private int id;

    /**
     * Name for the input message.
     */
    @Column(length = 40, nullable = false)
    @DynamicSerializeElement
    private String name;

    @Column(length = Language.LENGTH)
    @Enumerated(EnumType.STRING)
    @DynamicSerializeElement
    private Language language;

    /** AFOS product identifier of the form CCNNNXXX **/
    @Column(length = 9)
    @DynamicSerializeElement
    private String afosid;

    /** The Date/Time that the product was created in AFOS. **/
    @Column
    @DynamicSerializeElement
    private Calendar creationTime;

    /** The Date/Time after which the message may be output. **/
    @Column
    @DynamicSerializeElement
    private Calendar effectiveTime;

    /**
     * The periodicity of message transmission for messages to be scheduled
     * based on time. This is stored in String representation with the format
     * 'DDHHMMSS'. This field will contain null for non-time-inserted messages.
     * 
     * @see InputMessage#getPeriodicityMinutes()
     **/
    @Column(length = 8)
    @DynamicSerializeElement
    private String periodicity;

    /**
     * This is an optional field containing three pieces of information. A
     * unique identifier, a list of identifiers that this message replaces, and
     * a list of identifiers this message follows. This field is stored unparsed
     * from the raw file although convenience methods are provided that can
     * parse it.
     * 
     * @see #getMrdId()
     * @see #getMrdReplacements()
     * @see #getMrdFollows()
     */
    @Column
    @DynamicSerializeElement
    private String mrd;

    /** This field is used by AFOS to direct messages into inactive storage . */
    @Column
    @DynamicSerializeElement
    private Boolean active;

    /**
     * This field is used to display a confirmation that this message was
     * transmitted.
     */
    @Column
    @DynamicSerializeElement
    private Boolean confirm;

    /**
     * This field is used to interrupt any message currently being broadcast on
     * the applicable transmitters with this message.
     */
    @Column
    @DynamicSerializeElement
    private Boolean interrupt;

    /**
     * This field is used to indicate an alert tone should be broadcast prior to
     * broadcast of this message for the first time.
     */
    @Column
    @DynamicSerializeElement
    private Boolean alertTone;

    /** Indicate an NWRSAME tone for this message. */
    @Column
    @DynamicSerializeElement
    private Boolean nwrsameTone;

    /**
     * The notion of a message's LISTENING AREA code is defined as a collection
     * of Universal Generic Codes (UGCs). These codes serve to specify
     * geographical areas to be served by the message's transmission. This field
     * consists of one or more UGCs separated by a dash.
     */
    @Column
    @DynamicSerializeElement
    private String areaCodes;

    /**
     * The Date/Time after which the message is ignored for transmission and may
     * be deleted .
     */
    @Column
    @DynamicSerializeElement
    private Calendar expirationTime;

    /** The text content of a message. */
    @Column(columnDefinition = "text")
    @DynamicSerializeElement
    private String content;

    /**
     * This field indicates if all other fields were parsed correctly. When
     * false some fields may not be populated and the content will be set to the
     * full file content including the invalid header.
     */
    @Column
    @DynamicSerializeElement
    private boolean validHeader;

    public InputMessage() {
        super();
    }

    public InputMessage(InputMessage other) {
        this.id = other.id;
        this.language = other.language;
        this.afosid = other.afosid;
        this.creationTime = other.creationTime;
        this.effectiveTime = other.effectiveTime;
        this.periodicity = other.periodicity;
        this.mrd = other.mrd;
        this.active = other.active;
        this.confirm = other.confirm;
        this.interrupt = other.interrupt;
        this.alertTone = other.alertTone;
        this.nwrsameTone = other.nwrsameTone;
        this.areaCodes = other.areaCodes;
        this.expirationTime = other.expirationTime;
        this.content = other.content;
        this.validHeader = other.validHeader;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name;
        } else {
            this.name = "";
        }
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getAfosid() {
        return afosid;
    }

    public void setAfosid(String afosid) {
        this.afosid = afosid;
    }

    public Calendar getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    public Calendar getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(Calendar effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    /**
     * @return the parsed number of seconds represented by the periodicty field
     *         of this message.
     */
    public int getPeriodicitySeconds() {
        if (this.periodicity != null) {
            int days = Integer.parseInt(this.periodicity.substring(0, 2));
            int hours = Integer.parseInt(this.periodicity.substring(2, 4));
            int minutes = Integer.parseInt(this.periodicity.substring(4, 6));
            int seconds = Integer.parseInt(this.periodicity.substring(6, 8));
            return seconds + (60 * minutes + (60 * (hours + (24 * days))));
        }
        return -1;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public String getMrd() {
        return mrd;
    }

    /**
     * @return The parsed unique id for this message(between 0 and 999) from the
     *         mrd, or -1 if no id was included
     */
    public int getMrdId() {
        if (mrd != null) {
            return Integer.parseInt(mrd.substring(0, 3));
        }
        return -1;
    }

    /**
     * @return The parsed unique ids that this message should replace, will
     *         return an empty array if no replacements were specified.
     */
    public int[] getMrdReplacements() {
        if (mrd == null) {
            return new int[0];
        }
        int start = mrd.indexOf('R') + 1;
        if (start == 0) {
            return new int[0];
        }
        int end = mrd.indexOf('F');
        if (end == -1) {
            end = mrd.length();
        }
        int[] result = new int[(end - start) / 3];
        for (int i = 0; i < result.length; i += 1) {
            int s = start + (i * 3);
            result[i] = Integer.parseInt(mrd.substring(s, s + 3));
        }
        return result;
    }

    /**
     * @return The parsed unique ids that this message should follow, will
     *         return an empty array if no follows were specified.
     */
    public int[] getMrdFollows() {
        if (mrd == null) {
            return new int[0];
        }
        int start = mrd.indexOf('F') + 1;
        if (start == 0) {
            return new int[0];
        }
        int[] result = new int[(mrd.length() - start) / 3];
        for (int i = 0; i < result.length; i += 1) {
            int s = start + (i * 3);
            result[i] = Integer.parseInt(mrd.substring(s, s + 3));
        }
        return result;
    }

    public void setMrd(String mrd) {
        this.mrd = mrd;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getConfirm() {
        return confirm;
    }

    public void setConfirm(Boolean confirm) {
        this.confirm = confirm;
    }

    public Boolean getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(Boolean interrupt) {
        this.interrupt = interrupt;
    }

    public Boolean getAlertTone() {
        return alertTone;
    }

    public void setAlertTone(Boolean alertTone) {
        this.alertTone = alertTone;
    }

    public Boolean getNwrsameTone() {
        return nwrsameTone;
    }

    public void setNwrsameTone(Boolean nwrsameTone) {
        this.nwrsameTone = nwrsameTone;
    }

    public String getAreaCodes() {
        return areaCodes;
    }

    public List<String> getAreaCodeList() {
        return Arrays.asList(areaCodes.split("-"));
    }

    public void setAreaCodes(String areaCodes) {
        this.areaCodes = areaCodes;
    }

    public Calendar getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Calendar expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isValidHeader() {
        return validHeader;
    }

    public void setValidHeader(boolean validHeader) {
        this.validHeader = validHeader;
    }

    /**
     * Check if this message is periodic or not.
     * 
     * @return true if periodic, false if not
     */
    public boolean isPeriodic() {
        try {
            if (periodicity != null && Integer.parseInt(periodicity) > 0) {
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((active == null) ? 0 : active.hashCode());
        result = (prime * result) + ((afosid == null) ? 0 : afosid.hashCode());
        result = (prime * result)
                + ((alertTone == null) ? 0 : alertTone.hashCode());
        result = (prime * result)
                + ((areaCodes == null) ? 0 : areaCodes.hashCode());
        result = (prime * result)
                + ((confirm == null) ? 0 : confirm.hashCode());
        result = (prime * result)
                + ((content == null) ? 0 : content.hashCode());
        result = (prime * result)
                + ((creationTime == null) ? 0 : creationTime.hashCode());
        result = (prime * result)
                + ((effectiveTime == null) ? 0 : effectiveTime.hashCode());
        result = (prime * result)
                + ((expirationTime == null) ? 0 : expirationTime.hashCode());
        result = (prime * result) + id;
        result = (prime * result)
                + ((interrupt == null) ? 0 : interrupt.hashCode());
        result = (prime * result)
                + ((language == null) ? 0 : language.hashCode());
        result = (prime * result) + ((mrd == null) ? 0 : mrd.hashCode());
        result = (prime * result)
                + ((nwrsameTone == null) ? 0 : nwrsameTone.hashCode());
        result = (prime * result)
                + ((periodicity == null) ? 0 : periodicity.hashCode());
        result = (prime * result) + (validHeader ? 1231 : 1237);
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
        InputMessage other = (InputMessage) obj;
        if (active == null) {
            if (other.active != null) {
                return false;
            }
        } else if (!active.equals(other.active)) {
            return false;
        }
        if (afosid == null) {
            if (other.afosid != null) {
                return false;
            }
        } else if (!afosid.equals(other.afosid)) {
            return false;
        }
        if (alertTone == null) {
            if (other.alertTone != null) {
                return false;
            }
        } else if (!alertTone.equals(other.alertTone)) {
            return false;
        }
        if (areaCodes == null) {
            if (other.areaCodes != null) {
                return false;
            }
        } else if (!areaCodes.equals(other.areaCodes)) {
            return false;
        }
        if (confirm == null) {
            if (other.confirm != null) {
                return false;
            }
        } else if (!confirm.equals(other.confirm)) {
            return false;
        }
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        if (creationTime == null) {
            if (other.creationTime != null) {
                return false;
            }
        } else if (!creationTime.equals(other.creationTime)) {
            return false;
        }
        if (effectiveTime == null) {
            if (other.effectiveTime != null) {
                return false;
            }
        } else if (!effectiveTime.equals(other.effectiveTime)) {
            return false;
        }
        if (expirationTime == null) {
            if (other.expirationTime != null) {
                return false;
            }
        } else if (!expirationTime.equals(other.expirationTime)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (interrupt == null) {
            if (other.interrupt != null) {
                return false;
            }
        } else if (!interrupt.equals(other.interrupt)) {
            return false;
        }
        if (language != other.language) {
            return false;
        }
        if (mrd == null) {
            if (other.mrd != null) {
                return false;
            }
        } else if (!mrd.equals(other.mrd)) {
            return false;
        }
        if (nwrsameTone == null) {
            if (other.nwrsameTone != null) {
                return false;
            }
        } else if (!nwrsameTone.equals(other.nwrsameTone)) {
            return false;
        }
        if (periodicity == null) {
            if (other.periodicity != null) {
                return false;
            }
        } else if (!periodicity.equals(other.periodicity)) {
            return false;
        }
        if (validHeader != other.validHeader) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String content = this.content;
        if (content != null) {
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    content = line;
                    break;
                }
            }
            if (content.length() > 70) {
                content = content.substring(0, 70);
            }
            content = content + "...";
        }
        return "InputMessage [\n  id=" + id + "\n  language=" + language
                + "\n  afosid=" + afosid + "\n  creationTime="
                + creationTime.getTime() + "\n  effectiveTime="
                + effectiveTime.getTime() + "\n  periodicity=" + periodicity
                + "\n  mrd=" + mrd + "\n  active=" + active + "\n  confirm="
                + confirm + "\n  interrupt=" + interrupt + "\n  alertTone="
                + alertTone + "\n  nwrsameTone=" + nwrsameTone
                + "\n  areaCodes=" + areaCodes + "\n  expirationTime="
                + expirationTime.getTime() + "\n  content=" + content
                + "\n  validHeader=" + validHeader + "\n]";
    }

}
