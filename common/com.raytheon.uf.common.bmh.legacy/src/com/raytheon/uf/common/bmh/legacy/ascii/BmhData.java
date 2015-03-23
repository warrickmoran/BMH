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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
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
 * Nov 18, 2014  3746      rjpeter     Refactored MessageTypeReplacement.
 * Mar 13, 2015 4213       bkowal      Store {@link TransmitterLanguage}s mapped to
 *                                     a {@link TransmitterGroup} and {@link Language}.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class BmhData {
    private Map<String, Dictionary> dictionaries;

    private Map<String, TransmitterGroup> transmitters;

    private Map<String, Map<Language, TransmitterLanguage>> transmitterLanguages;

    private final Map<String, TransmitterMessages> englishTransmitterMessages = new HashMap<>();

    private final Map<String, TransmitterMessages> spanishTransmitterMessages = new HashMap<>();

    private Map<String, Area> areas;

    private Map<String, Zone> zones;

    private Map<String, MessageType> msgTypes;

    private Map<String, Suite> suites;

    private Map<String, Program> programs;

    private Map<MessageType, Set<MessageTypeSummary>> replaceMap;

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

    /**
     * @return the transmitterLanguages
     */
    public Map<String, Map<Language, TransmitterLanguage>> getTransmitterLanguages() {
        return transmitterLanguages;
    }

    /**
     * @param transmitterLanguages
     *            the transmitterLanguages to set
     */
    public void setTransmitterLanguages(
            Map<String, Map<Language, TransmitterLanguage>> transmitterLanguages) {
        this.transmitterLanguages = transmitterLanguages;
    }

    public List<TransmitterLanguage> getAllTransmitterLanguages() {
        if (this.transmitterLanguages == null
                || this.transmitterLanguages.isEmpty()) {
            return Collections.emptyList();
        }

        List<TransmitterLanguage> languages = new ArrayList<>();
        for (Map<Language, TransmitterLanguage> tlMap : this.transmitterLanguages
                .values()) {
            languages.addAll(tlMap.values());
        }
        return languages;
    }

    public void addTransmitterMessages(
            final TransmitterMessages transmitterMessages) {
        switch (transmitterMessages.getLanguage()) {
        case ENGLISH:
            this.englishTransmitterMessages.put(
                    transmitterMessages.getTransmitterGroupName(),
                    transmitterMessages);
            break;
        case SPANISH:
            this.spanishTransmitterMessages.put(
                    transmitterMessages.getTransmitterGroupName(),
                    transmitterMessages);
            break;
        }
    }

    public TransmitterMessages getTransmitterMessages(
            final String transmitterGroup, final Language language) {
        switch (language) {
        case ENGLISH:
            return this.englishTransmitterMessages.get(transmitterGroup);
        case SPANISH:
            return this.spanishTransmitterMessages.get(transmitterGroup);
        default:
            return null;
        }
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
     * @return the replaceMap
     */
    public Map<MessageType, Set<MessageTypeSummary>> getReplaceMap() {
        return replaceMap;
    }

    /**
     * @param replaceMap
     *            the replaceMap to set
     */
    public void setReplaceMap(
            Map<MessageType, Set<MessageTypeSummary>> replaceMap) {
        this.replaceMap = replaceMap;
    }

    /**
     * Add a replacement message.
     * 
     * @param replacement
     */
    public void addReplacementMsg(MessageType msg, MessageTypeSummary replaced) {
        if (replaceMap == null) {
            replaceMap = new HashMap<>();
        }

        Set<MessageTypeSummary> replaceSet = replaceMap.get(msg);
        if (replaceSet == null) {
            replaceSet = new HashSet<>();
            replaceMap.put(msg, replaceSet);
        }
        replaceSet.add(replaced);
    }

}
