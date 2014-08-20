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
package com.raytheon.uf.common.bmh.legacy.ascii;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeReplacement;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;

/**
 * Container for bmh database objects that were generated from the legacy ascii
 * dictionary.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014 3175       rjpeter     Initial creation
 * Aug 19, 2014 3411       mpduff      Handle {@link MessageTypeReplacement}
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class BmhData {
    private Map<String, Dictionary> dictionaries;

    private Map<String, TransmitterGroup> transmitters;

    private List<TransmitterLanguage> transmitterLanguages;

    private Map<String, Area> areas;

    private Map<String, Zone> zones;

    private Map<String, MessageType> msgTypes;

    private Map<String, Suite> suites;

    private Map<String, Program> programs;

    private List<MessageTypeReplacement> replaceList;

    public Map<String, Dictionary> getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(Map<String, Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }

    public Map<String, TransmitterGroup> getTransmitters() {
        return transmitters;
    }

    public void setTransmitters(Map<String, TransmitterGroup> transmitters) {
        this.transmitters = transmitters;
    }

    public List<TransmitterLanguage> getTransmitterLanguages() {
        return transmitterLanguages;
    }

    public void setTransmitterLanguages(
            List<TransmitterLanguage> transmitterLanguages) {
        this.transmitterLanguages = transmitterLanguages;
    }

    public Map<String, Area> getAreas() {
        return areas;
    }

    public void setAreas(Map<String, Area> areas) {
        this.areas = areas;
    }

    public Map<String, Zone> getZones() {
        return zones;
    }

    public void setZones(Map<String, Zone> zones) {
        this.zones = zones;
    }

    public Map<String, MessageType> getMsgTypes() {
        return msgTypes;
    }

    public void setMsgTypes(Map<String, MessageType> msgTypes) {
        this.msgTypes = msgTypes;
    }

    public Map<String, Suite> getSuites() {
        return suites;
    }

    public void setSuites(Map<String, Suite> suites) {
        this.suites = suites;
    }

    public Map<String, Program> getPrograms() {
        return programs;
    }

    public void setPrograms(Map<String, Program> programs) {
        this.programs = programs;
    }

    /**
     * @return the replaceList
     */
    public List<MessageTypeReplacement> getReplaceList() {
        return replaceList;
    }

    /**
     * @param replaceList
     *            the replaceList to set
     */
    public void setReplaceList(List<MessageTypeReplacement> replaceList) {
        this.replaceList = replaceList;
    }

    /**
     * Add a replacement message.
     * 
     * @param replacement
     */
    public void addReplacementMsg(MessageTypeReplacement replacement) {
        if (replaceList == null) {
            replaceList = new ArrayList<>();
        }

        replaceList.add(replacement);
    }

}
