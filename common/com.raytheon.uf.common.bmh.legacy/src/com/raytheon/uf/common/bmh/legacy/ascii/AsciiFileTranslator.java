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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageTypeSummary;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSuite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite.SuiteType;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.BMHTimeZone;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.legacy.ascii.data.Ascii;
import com.raytheon.uf.common.bmh.legacy.ascii.data.MessageGroupData;
import com.raytheon.uf.common.bmh.legacy.ascii.data.StationIdData;

/**
 * Translates a supplied file from the ascii dictionary format in to BMH objects
 * for storage.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014 3175       rjpeter     Initial creation
 * Aug 05, 2014 3175       rjpeter     Added set verification of message type in suite.
 * Aug 19, 2014 3411       mpduff      Implement {@link MessageTypeReplacement}
 * Sep 16, 2014 3587       bkowal      Updated to only allow trigger assignment for {Program, Suite}
 * Sep 25, 2014 3620       bsteffen    Add seconds to periodicity and duration.
 * Oct 07, 2014 3642       bkowal      Set a default time message preamble for {@link TransmitterLanguage}
 * Oct 08, 2014 3687       bsteffen    Remove ProgramTrigger.
 * Oct 13, 2014 3654       rjpeter     Updated to use MessageTypeSummary.
 * Oct 24, 2014 3617       dgilling    Support unified time zone field for transmitter groups.
 * Nov 18, 2014 3746       rjpeter     Refactored MessageTypeReplacement.
 * Dec 10, 2014 3824       rferrel     Remove warning on missing voices exclude Spanish when no Spanish voices.
 *                                       Added reader/source constructor.
 * Jan 22, 2015 3995       rjpeter     Removed importing of amplitudes.
 * Feb 09, 2015 4095       bsteffen    Remove Transmitter Name.
 * Mar 03, 2015 4175       bkowal      Cleanup.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class AsciiFileTranslator {

    private static final Map<Language, String> defaultTimeMsgs;

    static {
        defaultTimeMsgs = new HashMap<>(Language.values().length);
        defaultTimeMsgs.put(Language.ENGLISH, "The current time is");
        defaultTimeMsgs.put(Language.SPANISH, "La hora actual es");
    }

    /**
     * Pattern to match the Block header marker.
     */
    private static final Pattern BLOCK_HEADER = Pattern
            .compile("^:BLOCK (\\d+)(\\w+)?:");

    /**
     * Pattern to match the start of a new Transmitter Section.
     */
    private static final Pattern TRANSMITTER_START = Pattern
            .compile("^#\\s+-+\\s+Transmitter Number");

    /**
     * Pattern to designate the end of all transmitter sections.
     */
    private static final Pattern TRANSMITTER_END = Pattern
            .compile("^#\\s+AFOS/AWIPS Configuration");

    /**
     * Pattern to match the period/duration settings.
     */
    private static final Pattern PERIOD = Pattern
            .compile("(\\d\\d).(\\d\\d):(\\d\\d):(\\d\\d)");

    /**
     * Set of block sections that will be ignored.
     */
    private static final Set<Integer> IGNORE_BLOCKS;

    /**
     * Set of Transmitter Mnemonics that will be skipped.
     */
    private static final Set<String> PLAYBACK_TRANSMITTERS;

    static {
        int[] ignore = new int[] { 3, 4, 6, 7, 12, 13, 18 };
        Set<Integer> blocks = new HashSet<>();

        for (int val : ignore) {
            blocks.add(new Integer(val));
        }

        IGNORE_BLOCKS = Collections.unmodifiableSet(blocks);
        PLAYBACK_TRANSMITTERS = Collections.unmodifiableSet(new HashSet<>(
                Arrays.asList(new String[] { "PB1", "PB2" })));
    }

    /**
     * Container for any ascii data that is specified before the associated bmh
     * objects are ready.
     */
    private final Ascii ascii = new Ascii();

    /**
     * Container for the parsed bmh data.
     */
    private final BmhData bmhData = new BmhData();

    /**
     * Boolean to control whether errors are logged or throw exception.
     */
    private final boolean strict;

    /**
     * If strict = false, this will contain all validation messages for the
     * file.
     */
    private final List<String> validationMessages = new ArrayList<>();

    /**
     * Mapping of voices available in the system keys are Language followed by
     * Gender.
     */
    private final Map<Language, Map<Boolean, TtsVoice>> voices = new HashMap<>(
            4);

    private final boolean includeSpanish;

    /**
     * Set to true when data is parsed.
     */
    protected final AtomicBoolean parsedData = new AtomicBoolean(false);

    private final String voiceMsg;

    /**
     * Translates the given file as a legacy ascii dictionary. If strict is
     * true, then any parsing/validation issues will throw an exception,
     * otherwise they will accumulate as validation messages that can be
     * retrieved. The current known voices must also be passed. If the legacy
     * dictionary specifies a setting for which there is no voice then it will
     * be mapped to the closest acceptable voice.
     * 
     * @param file
     * @param strict
     * @param definedVoices
     * @throws IOException
     * @throws ParseException
     */
    public AsciiFileTranslator(File file, boolean strict,
            List<TtsVoice> definedVoices) throws IOException, ParseException {
        this(new BufferedReader(new FileReader(file)), file.getAbsolutePath(),
                strict, definedVoices);
    }

    public AsciiFileTranslator(BufferedReader buffer, String source,
            boolean strict, List<TtsVoice> definedVoices) throws IOException,
            ParseException {
        this.strict = strict;

        if ((definedVoices == null) || (definedVoices.size() == 0)) {
            throw new IllegalArgumentException(
                    "No voices configured for BMH, cannot parse legacy database");
        }

        includeSpanish = containsSpanishVoice(definedVoices);

        if (includeSpanish) {
            voiceMsg = "Including Spanish information.";
        } else {
            voiceMsg = "Excluding Spanish information.";
        }

        // put voices in to map for look up
        for (TtsVoice voice : definedVoices) {
            Language lang = voice.getLanguage();
            Map<Boolean, TtsVoice> langMap = voices.get(lang);
            if (langMap == null) {
                langMap = new HashMap<>(4);
                voices.put(lang, langMap);
            }

            langMap.put(voice.isMale(), voice);
        }

        // scan the buffer
        try (AsciiFileParser reader = new AsciiFileParser(buffer, source)) {
            reader.setStartOfSectionPattern(BLOCK_HEADER);
            reader.setEndOfSectionPattern(BLOCK_HEADER);
            for (Matcher blockMatcher = reader.scanToNextSection(true); blockMatcher != null; blockMatcher = reader
                    .scanToNextSection(true)) {
                int blockNumber = Integer.parseInt(blockMatcher.group(1));
                String blockExtra = (blockMatcher.groupCount() > 1 ? blockMatcher
                        .group(2) : null);
                parseBlock(blockNumber, blockExtra, reader);

                /*
                 * reset block section patterns in case parseBlock had to do sub
                 * section parsing
                 */
                reader.setStartOfSectionPattern(BLOCK_HEADER);
                reader.setEndOfSectionPattern(BLOCK_HEADER);
            }
        }
    }

    /**
     * Returns the bmh database objects that represent the data in the legacy
     * ascii database.
     * 
     * @return
     */
    public BmhData getTranslatedData() {
        return bmhData;
    }

    /**
     * Handles parsing the sections as identified by the passed number.
     * 
     * @param blockNumber
     * @param blockExtra
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseBlock(int blockNumber, String blockExtra,
            AsciiFileParser reader) throws ParseException, IOException {
        if ((blockExtra != null) && (blockNumber != 7)) {
            throw new ParseException("Unknown block " + blockNumber
                    + blockExtra, reader.getCurrentLineNumber() - 1,
                    reader.getSourceFile());
        }

        switch (blockNumber) {
        case 1:
            parseDictionaryData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        case 2:
            parseStationIdData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        // BLOCK 3 - Keep Alive Data not used in BMH
        // BLOCK 4 - Interrupt Announcement Data not used in BMH
        case 5:
            /*
             * BLOCK 5 has multiple sections. Simplest detection is to scan
             * block for the specific comment header for each section.
             */
            reader.setStartOfSectionPattern(TRANSMITTER_START);
            reader.setEndOfSectionPattern(TRANSMITTER_END);
            int scanStart = reader.getCurrentLineNumber();
            Matcher matcher = reader.scanToNextSection(true);

            if (matcher != null) {
                parseTransmitterData(reader);
            } else {
                throw new ParseException(
                        "Could not find transmitter start section in block "
                                + blockNumber, scanStart,
                        reader.getSourceFile());
            }

            Pattern roamsPortConfig = Pattern
                    .compile("#\\s*ROAMS PORT Configuration");
            Pattern roamsSiteConfig = Pattern
                    .compile("#\\s*ROAMS Site Configuration");
            reader.setStartOfSectionPattern(roamsPortConfig);
            reader.setEndOfSectionPattern(roamsSiteConfig);
            scanStart = reader.getCurrentLineNumber();
            matcher = reader.scanToNextSection(true);

            if (matcher != null) {
                // skip next 7 fields
                for (int i = 0; i < 7; i++) {
                    reader.nextField();
                }
                ascii.setOfficeOrigin(reader.nextField());
            } else {
                throw new ParseException(
                        "Could not find ROAMS PORT Configuration to get office origin",
                        scanStart, reader.getSourceFile());
            }

            Pattern siteSpecificConfig = Pattern
                    .compile("#\\s*Site Specific Configuration");
            reader.setStartOfSectionPattern(siteSpecificConfig);
            reader.setEndOfSectionPattern(BLOCK_HEADER);
            scanStart = reader.getCurrentLineNumber();
            matcher = reader.scanToNextSection(true);
            if (matcher != null) {
                ascii.setTimeZone(parseTimeZone(reader).getID());
            } else {
                throw new ParseException(
                        "Could not find Site Specific Configuration to get time zone",
                        scanStart, reader.getSourceFile());
            }
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        // BLOCK 6 - Lead-In Data not used in BMH
        // BLOCK 7/7B - Call To Action Data not used in BMH
        case 8:
            parseAreaData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        case 9:
            parseZoneData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        case 10:
            parseMessageTypeData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        case 11:
            parseMessageTypeReplaceData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        // BLOCK 12 - Message Type Follow not used in BMH
        // BLOCK 13 - Message Type CTA not used in BMH
        case 14:
            parseMessageGroupData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        case 15:
            parseSuiteData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        case 16:
            parseProgramData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        case 17:
            parseTransmitterProgramData(reader);
            AsciiFileTranslator.this.parsedData.set(true);
            break;
        // BLOCK 18 - Data not used in BMH
        default:
            if (!IGNORE_BLOCKS.contains(blockNumber)) {
                throw new ParseException("Unknown block " + blockNumber
                        + (blockExtra == null ? "" : blockExtra),
                        reader.getCurrentLineNumber() - 1,
                        reader.getSourceFile());
            }
        }
    }

    /**
     * Parses the dictionary settings.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseDictionaryData(AsciiFileParser reader)
            throws ParseException, IOException {
        Map<String, Dictionary> map = new HashMap<>();

        while (reader.hasNextField()) {
            String name = reader.nextField();

            if (map.containsKey(name)) {
                StringBuilder msg = new StringBuilder(
                        "Found duplicate dictionary name: ").append(name);
                handleError(msg, reader.getCurrentLineNumber(),
                        reader.getSourceFile());
            }

            Dictionary data = new Dictionary();
            data.setName(name);
            data.setLanguage(parseLanguage(reader));
            map.put(name, data);
        }

        bmhData.setDictionaries(map);
    }

    /**
     * Parses station id data.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseStationIdData(AsciiFileParser reader)
            throws ParseException, IOException {
        Map<String, StationIdData> map = new HashMap<>();

        while (reader.hasNextField()) {
            String name = reader.nextField();
            if (map.containsKey(name)) {
                StringBuilder msg = new StringBuilder(
                        "Found duplicate station id name: ").append(name);
                handleError(msg, reader.getCurrentLineNumber(),
                        reader.getSourceFile());
            }

            StationIdData data = new StationIdData();
            data.setName(name);
            data.setLanguage(parseLanguage(reader));
            data.setText(reader.nextField());

            map.put(name, data);
        }

        ascii.setStationIdData(map);
    }

    /**
     * Parses transmitter data in to BMH objects.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseTransmitterData(AsciiFileParser reader)
            throws ParseException, IOException {
        Map<String, TransmitterGroup> map = new LinkedHashMap<>(16, 1);
        List<TransmitterLanguage> langs = new ArrayList<>();
        int position = 0;

        while (reader.hasNextField()) {
            TransmitterGroup group = new TransmitterGroup();
            Transmitter trans = new Transmitter();

            reader.nextField(); // skip tranx number
            boolean isTransmitter = 1 == parseInt(reader);
            reader.nextField();// skip Proc ID
            reader.nextField();// skip Proc Slot
            trans.setTxStatus(parseBool(reader) ? TxStatus.ENABLED
                    : TxStatus.DISABLED);
            TimeZone baseTZ = parseTimeZone(reader);

            reader.nextField(); // skip alert tone amplitude
            reader.nextField(); // skip transfer tone amplitude
            reader.nextField(); // skip SAME Validation
            reader.nextField(); // skip same tone amplitude
            reader.nextField(); // skip voice amplitude
            reader.nextField(); // skip long pause setting

            boolean observesDST = !parseBool(reader);
            TimeZone transmitterTZ = BMHTimeZone.getTimeZone(baseTZ,
                    observesDST);
            group.setTimeZone(transmitterTZ.getID());

            Dictionary engDict = parseTransmitterDictionary(Language.ENGLISH,
                    reader);
            Dictionary spaDict = parseTransmitterDictionary(Language.SPANISH,
                    reader);
            reader.nextField();// skip unused dict name
            reader.nextField();// skip unused dict name

            String engStatIdMsg = parseTransmitterStationIdMsg(
                    Language.ENGLISH, reader);
            String spaStatIdMsg = parseTransmitterStationIdMsg(
                    Language.SPANISH, reader);

            if (isTransmitter) {
                TransmitterLanguage lang = parseTransmitterLanguage(
                        Language.ENGLISH, group, engDict, engStatIdMsg, reader);
                if (lang != null) {
                    langs.add(lang);
                }

                lang = parseTransmitterLanguage(Language.SPANISH, group,
                        spaDict, spaStatIdMsg, reader);
                if (includeSpanish && (lang != null)) {
                    langs.add(lang);
                }
            }

            reader.nextField();// skip unused station id
            reader.nextField();// skip unused station id
            reader.nextField();// skip keep alive
            reader.nextField();// skip keep alive
            reader.nextField();// skip keep alive
            reader.nextField();// skip keep alive
            reader.nextField();// skip interrupt
            reader.nextField();// skip interrupt
            reader.nextField();// skip interrupt
            reader.nextField();// skip interrupt

            String mnemonic = reader.nextField();
            group.setName(mnemonic);
            trans.setMnemonic(mnemonic);
            group.addTransmitter(trans);
            trans.setCallSign(reader.nextField());
            String freq = reader.nextField();

            if (freq != null) {
                trans.setFrequency(parseFloat(freq, reader));
            }

            trans.setLocation(reader.nextField());
            trans.setServiceArea(reader.nextField());
            reader.nextField();// skip voice type
            reader.nextField();// skip rate flag
            reader.nextField();// skip baseline fall flag
            reader.nextField();// skip hatRise flag
            reader.nextField();// skip stress rise flag
            reader.nextField();// skip rate
            reader.nextField();// skip baseline fall
            reader.nextField();// skip hat rise
            reader.nextField();// skip stress rise
            reader.nextField();// skip amplitude

            if (isTransmitter) {
                group.setPosition(position++);
                map.put(mnemonic, group);
            }
        }

        bmhData.setTransmitters(map);
        bmhData.setTransmitterLanguages(langs);
    }

    /**
     * Parses area data and adds linking to transmitters for an area.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseAreaData(AsciiFileParser reader) throws ParseException,
            IOException {
        Map<String, Area> map = new HashMap<>(128, 1);

        while (reader.hasNextField()) {
            Area area = new Area();
            if (map.size() == 0) {
                reader.nextField();// skip first >
            }

            area.setAreaCode(reader.nextField());
            area.setAreaName(reader.nextField());
            map.put(area.getAreaCode(), area);

            String mnemonic = null;
            Map<String, TransmitterGroup> transmitters = bmhData
                    .getTransmitters();

            while (reader.hasNextField()) {
                mnemonic = reader.nextField();
                if (">".equals(mnemonic)) {
                    break;
                } else {
                    // group has same mnemonic as transmitter initially
                    TransmitterGroup group = transmitters.get(mnemonic);
                    if (group == null) {
                        StringBuilder msg = new StringBuilder(64);
                        msg.append("Uknown Transmitter mnemonic [")
                                .append(mnemonic)
                                .append("] assigned to area [")
                                .append(area.getAreaCode()).append("]");
                        handleError(msg, reader.getCurrentLineNumber(),
                                reader.getSourceFile());
                    } else {
                        // one to one mapping as part of initial load
                        area.addTransmitter(group.getTransmitters().iterator()
                                .next());
                    }
                }
            }
        }

        bmhData.setAreas(map);
    }

    /**
     * Parses zone information and links in to previously parsed area data.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseZoneData(AsciiFileParser reader) throws ParseException,
            IOException {
        Map<String, Zone> map = new HashMap<>(64, 1);

        while (reader.hasNextField()) {
            Zone zone = new Zone();
            if (map.size() == 0) {
                reader.nextField();// skip first >
            }

            zone.setZoneCode(reader.nextField());
            zone.setZoneName(reader.nextField());
            Map<String, Area> areas = bmhData.getAreas();

            while (reader.hasNextField()) {
                String areaCode = reader.nextField();
                if (">".equals(areaCode)) {
                    break;
                } else {
                    Area area = areas.get(areaCode);
                    if (area != null) {
                        zone.addArea(area);
                    } else {
                        StringBuilder msg = new StringBuilder(64);
                        msg.append("Uknown area code [").append(areaCode)
                                .append("] assigned to zone [")
                                .append(zone.getZoneCode()).append("]");
                        handleError(msg, reader.getCurrentLineNumber(),
                                reader.getSourceFile());
                    }
                }
            }

            map.put(zone.getZoneCode(), zone);
        }

        bmhData.setZones(map);
    }

    /**
     * Parses message type data, links in to previously parsed areas, zones, and
     * transmitters.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseMessageTypeData(AsciiFileParser reader)
            throws ParseException, IOException {
        Map<String, MessageType> map = new HashMap<>(2048);

        Pattern delimiter = Pattern
                .compile("^\\s*(:BLOCK |#MsgType|#indicator)");
        reader.setStartOfSectionPattern(delimiter);
        reader.setEndOfSectionPattern(delimiter);
        Matcher matcher = reader.scanToNextSection(false);
        String section = matcher.group(1);
        MessageType data = null;
        Map<String, Area> areas = bmhData.getAreas();
        Map<String, Zone> zones = bmhData.getZones();
        Map<String, TransmitterGroup> transmitters = bmhData.getTransmitters();

        while (!":BLOCK ".equals(section)) {
            if ("#MsgType".equals(section)) {
                matcher = reader.scanToNextSection(true);
                data = new MessageType();
                String afosId = reader.nextField();
                if (afosId.startsWith(">")) {
                    afosId = afosId.substring(1);
                }

                data.setAfosid(afosId);
                map.put(afosId, data);
                data.setTitle(reader.nextField());
                parseInt(reader); // skip listening area override
                data.setDesignation(parseDesignation(reader));
                data.setEmergencyOverride(parseInt(reader) == 2);
                Language lang = parseLanguage(reader);
                boolean isMale = !parseBool(reader);
                TtsVoice voice = getTtsVoice(lang, isMale,
                        reader.getCurrentLineNumber(), reader.getSourceFile());
                data.setVoice(voice);

                if (reader.hasNextField()) {
                    throw new ParseException("Unknown field ["
                            + reader.nextField() + "] in message type section",
                            reader.getCurrentLineNumber(),
                            reader.getSourceFile());
                }
            } else if ("#indicator".equals(section)) {
                matcher = reader.scanToNextSection(true);
                String type = reader.nextField();
                if ("area".equals(type)) {
                    while (reader.hasNextField()) {
                        String field = reader.nextField();
                        if (field != null) {
                            Area area = areas.get(field);
                            boolean found = false;
                            if (area != null) {
                                data.addDefaultArea(area);
                                found = true;
                            } else if ((field.length() > 3)
                                    && (field.charAt(2) == 'Z')) {
                                // Parse as zone
                                Zone zone = zones.get(field);
                                if (zone != null) {
                                    data.addDefaultZone(zone);
                                    found = true;
                                }
                            }

                            if (!found) {
                                // check if its a transmitter
                                TransmitterGroup tg = transmitters.get(field);

                                if (tg != null) {
                                    data.addDefaultTransmitterGroup(tg);
                                } else {
                                    StringBuilder msg = new StringBuilder(64);
                                    msg.append("Uknown area [")
                                            .append(field)
                                            .append("] in area section for msgType [")
                                            .append(data.getAfosid())
                                            .append("]");
                                    handleError(msg,
                                            reader.getCurrentLineNumber(),
                                            reader.getSourceFile());
                                }
                            }
                        }
                    }
                } else if ("SAME".equals(type)) {
                    while (reader.hasNextField()) {
                        String field = reader.nextField();
                        if (field != null) {
                            TransmitterGroup group = transmitters.get(field);
                            if (group != null) {
                                data.addSameTransmitter(group.getTransmitters()
                                        .iterator().next());
                            } else {
                                StringBuilder msg = new StringBuilder(64);
                                msg.append("Uknown transmitter [")
                                        .append(field)
                                        .append("] in same section for msgType [")
                                        .append(data.getAfosid()).append("]");
                                handleError(msg, reader.getCurrentLineNumber(),
                                        reader.getSourceFile());
                            }
                        }
                    }
                } else if ("default".equals(type)) {
                    data.setDuration(parsePeriod(reader));
                    data.setPeriodicity(parsePeriod(reader));
                    parseBool(reader); // ignore default save
                    data.setConfirm(parseInt(reader) > 0);
                    // 0-none, 1-interrupt, 2-interrupt with announcement
                    data.setInterrupt(parseInt(reader) > 0);
                    data.setAlert(parseBool(reader));

                    if (reader.hasNextField()) {
                        throw new ParseException(
                                "Unknown field in default indicator ["
                                        + reader.nextField()
                                        + "] in message type section",
                                reader.getCurrentLineNumber(),
                                reader.getSourceFile());
                    }
                } else if ("data".equals(type) || "voice".equals(type)
                        || "trailer".equals(type)) {
                    // skip all items in these sections
                    reader.scanToNextSection(false);
                } else {
                    throw new ParseException(
                            "Error parsing MessageType data.  Unknown indicator section",
                            reader.getCurrentLineNumber(), reader
                                    .getSourceFile());
                }
            }

            matcher = delimiter.matcher(reader.getCurrentLine());
            if (matcher.find()) {
                section = matcher.group(1);
            } else {
                throw new ParseException(
                        "Error parsing MessageType data.  Could not find required delimiter",
                        reader.getCurrentLineNumber(), reader.getSourceFile());
            }
        }

        bmhData.setMsgTypes(map);
    }

    /**
     * Parses message replacement data and populates previously parsed messages
     * type data.
     * 
     * @param reader
     * @throws IOException
     * @throws ParseException
     */
    private void parseMessageTypeReplaceData(AsciiFileParser reader)
            throws IOException, ParseException {
        Map<String, MessageType> msgTypes = bmhData.getMsgTypes();
        MessageType current = null;

        while (reader.hasNextField()) {
            String field = reader.nextField();
            if (field.startsWith(">")) {
                // skip leading >
                field = field.substring(1);
                current = msgTypes.get(field);
                if (current == null) {
                    StringBuilder msg = new StringBuilder(64);
                    msg.append("Unknown message type [").append(field)
                            .append("] in message type replace block");
                    handleError(msg, reader.getCurrentLineNumber(),
                            reader.getSourceFile());
                }
            } else {
                MessageType replaces = msgTypes.get(field);
                if (replaces == null) {
                    StringBuilder msg = new StringBuilder(64);
                    msg.append("Unknown message type [").append(field)
                            .append("] in message type replace block");
                    handleError(msg, reader.getCurrentLineNumber(),
                            reader.getSourceFile());
                }

                bmhData.addReplacementMsg(current, replaces.getSummary());
            }
        }
    }

    /**
     * Parses message group data. Group data is linked in to previously parsed
     * messages. This data is not used directly by BMH and as group information
     * is found in legacy database the group data is expanded to the individual
     * message types.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseMessageGroupData(AsciiFileParser reader)
            throws ParseException, IOException {
        Map<String, MessageGroupData> map = new HashMap<>();
        MessageGroupData data = null;
        boolean isMsgType = false;
        Map<String, TransmitterGroup> groups = bmhData.getTransmitters();
        Map<String, MessageType> msgTypes = bmhData.getMsgTypes();

        while (reader.hasNextField()) {
            String field = reader.nextField();
            if (field == null) {
                continue;
            }

            if (field.startsWith(">")) {
                data = new MessageGroupData();
                data.setName(field.substring(1));
                map.put(data.getName(), data);
            } else if (field.equals("MessageType")) {
                isMsgType = true;
            } else if (field.equals("SAME")) {
                isMsgType = false;
            } else if (isMsgType) {
                MessageType msgType = msgTypes.get(field);
                if (msgType != null) {
                    data.addMessageType(msgType);
                } else {
                    StringBuilder msg = new StringBuilder(64);
                    msg.append("Unknown message type [").append(field)
                            .append("] in message group [")
                            .append(data.getName()).append("]");
                    handleError(msg, reader.getCurrentLineNumber(),
                            reader.getSourceFile());
                }
            } else {
                TransmitterGroup group = groups.get(field);
                if (group != null) {
                    data.addSameTransmitter(group.getTransmitters().iterator()
                            .next());
                } else {
                    StringBuilder msg = new StringBuilder(64);
                    msg.append("Unknown transmitter [").append(field)
                            .append("] in message group [")
                            .append(data.getName()).append("]");
                    handleError(msg, reader.getCurrentLineNumber(),
                            reader.getSourceFile());
                }
            }
        }

        ascii.setMessageGroupData(map);
    }

    /**
     * Parses suite information and links in to previously parsed message types.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseSuiteData(AsciiFileParser reader) throws ParseException,
            IOException {
        Map<String, Suite> rval = new HashMap<>();
        Suite suite = null;
        Map<String, MessageType> msgTypes = bmhData.getMsgTypes();
        Map<String, MessageGroupData> msgGroups = ascii.getMessageGroupData();
        Set<MessageType> msgTypesInSuite = new HashSet<>();

        while (reader.hasNextField()) {
            String field = reader.nextField();
            if (field.startsWith(">")) {
                msgTypesInSuite.clear();
                suite = new Suite();
                suite.setName(field.substring(1));
                rval.put(suite.getName(), suite);
            } else if (field.equals("group")) {
                field = reader.nextField();
                MessageGroupData group = msgGroups.get(field);
                if (group != null) {
                    for (MessageType msgType : group.getMessageTypes()) {
                        if (!msgTypesInSuite.contains(msgType)) {
                            SuiteMessage msg = new SuiteMessage();
                            msg.setMsgTypeSummary(msgType.getSummary());
                            suite.addSuiteMessage(msg);
                            msgTypesInSuite.add(msgType);
                        }
                    }
                } else {
                    StringBuilder msg = new StringBuilder(64);
                    msg.append("Unknown message group [").append(field)
                            .append("] in suite [").append(suite.getName())
                            .append("]");
                    handleError(msg, reader.getCurrentLineNumber(),
                            reader.getSourceFile());
                }
            } else {
                MessageType msgType = msgTypes.get(field);
                if (msgType != null) {
                    if (!msgTypesInSuite.contains(msgType)) {
                        SuiteMessage msg = new SuiteMessage();
                        msg.setMsgTypeSummary(msgType.getSummary());
                        suite.addSuiteMessage(msg);
                        msgTypesInSuite.add(msgType);
                    }
                } else {
                    StringBuilder msg = new StringBuilder(64);
                    msg.append("Unknown message type [").append(field)
                            .append("] in suite [").append(suite.getName())
                            .append("]");
                    handleError(msg, reader.getCurrentLineNumber(),
                            reader.getSourceFile());
                }
            }
        }

        bmhData.setSuites(rval);
    }

    /**
     * Parses program data and links in to previously parsed message types and
     * suite information.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseProgramData(AsciiFileParser reader)
            throws ParseException, IOException {
        // maps of previously parsed data
        Map<String, Suite> suites = bmhData.getSuites();
        Map<String, MessageGroupData> msgGroups = ascii.getMessageGroupData();
        Map<String, MessageType> msgTypes = bmhData.getMsgTypes();

        Map<String, Program> rval = new HashMap<>();

        // objects representing current parsers
        Program program = null;
        Suite suite = null;
        List<String> triggers = new ArrayList<>();

        // optimized look up for triggers
        Map<MessageTypeSummary, SuiteMessage> suiteMessages = new HashMap<>();

        // validation set for suite types
        Set<String> suiteTypes = new HashSet<>();
        for (SuiteType type : SuiteType.values()) {
            suiteTypes.add(type.name().toLowerCase());
        }

        while (reader.hasNextField()) {
            String field = reader.nextField();

            if (field.startsWith(">")) {
                // validate last suite
                validateProgramSuite(program, suite, triggers, msgTypes, reader);
                // validate previous program
                program = validateProgram(program, reader);

                if (program != null) {
                    rval.put(program.getName(), program);
                }

                program = new Program();
                suite = null;
                triggers.clear();

                String programName = field.substring(1);
                program.setName(programName);
                rval.put(programName, program);
            } else {
                if (suiteTypes.contains(field)) {
                    // validate last suite
                    validateProgramSuite(program, suite, triggers, msgTypes,
                            reader);
                    triggers.clear();

                    String type = field;
                    String name = reader.nextField();
                    suite = suites.get(name);
                    if (suite != null) {
                        suiteMessages.clear();

                        List<SuiteMessage> suiteMessList = suite
                                .getSuiteMessages();
                        if (suiteMessList != null) {
                            // optimize look up of messages for triggers
                            for (SuiteMessage suiteMsg : suite
                                    .getSuiteMessages()) {
                                suiteMessages.put(suiteMsg.getMsgTypeSummary(),
                                        suiteMsg);
                            }
                        }

                        suite.setType(SuiteType.valueOf(type.toUpperCase()));
                        parseBool(reader); // skip OrderByTime
                        parseInt(reader); // skip timeOut
                        reader.nextField(); // skip start time
                        reader.nextField(); // skip periodicity
                    } else {
                        StringBuilder msg = new StringBuilder(64);
                        msg.append("Unknown suite [").append(name)
                                .append("] in program [")
                                .append(program.getName()).append("]");
                        handleError(msg, reader.getCurrentLineNumber(),
                                reader.getSourceFile());
                    }
                } else if (field.equals("group")) {
                    // legacy system can trigger on a group
                    String triggerGroup = reader.nextField();
                    MessageGroupData group = msgGroups.get(triggerGroup);
                    if (group != null) {
                        for (MessageType msgType : group.getMessageTypes()) {
                            SuiteMessage suiteMsg = suiteMessages.get(msgType
                                    .getSummary());
                            if (suiteMsg != null) {
                                triggers.add(triggerGroup);
                            } else {
                                StringBuilder msg = new StringBuilder(80);
                                msg.append("Program [")
                                        .append(program.getName())
                                        .append("], Suite [")
                                        .append(suite.getName())
                                        .append("], Group [")
                                        .append(triggerGroup)
                                        .append("] has MessagType [")
                                        .append(msgType.getAfosid())
                                        .append("] set as a trigger but the message type is not in the suite");
                                handleError(msg, reader.getCurrentLineNumber(),
                                        reader.getSourceFile());
                            }
                        }
                    }
                } else {
                    MessageType msgType = msgTypes.get(field);
                    if (msgType != null) {
                        SuiteMessage suiteMsg = suiteMessages.get(msgType
                                .getSummary());
                        if (suiteMsg != null) {
                            triggers.add(field);
                        } else {
                            StringBuilder msg = new StringBuilder(80);
                            msg.append("Program [")
                                    .append(program.getName())
                                    .append("], Suite [")
                                    .append(suite.getName())
                                    .append("] has MessagType [")
                                    .append(msgType.getAfosid())
                                    .append("] set as a trigger but the message type is not in the suite");
                            handleError(msg, reader.getCurrentLineNumber(),
                                    reader.getSourceFile());
                        }
                    } else {
                        StringBuilder msg = new StringBuilder(64);
                        msg.append("Unknown message type trigger [")
                                .append(field).append("] in suite [")
                                .append(suite.getName()).append("] program [")
                                .append(program.getName()).append("]");
                        handleError(msg, reader.getCurrentLineNumber(),
                                reader.getSourceFile());
                    }
                }
            }
        }

        // validate last suite
        validateProgramSuite(program, suite, triggers, msgTypes, reader);
        // validate previous program
        program = validateProgram(program, reader);

        if (program != null) {
            rval.put(program.getName(), program);
        }

        bmhData.setPrograms(rval);

        // validate programs have a defined suite
        Iterator<Program> progIter = rval.values().iterator();
        while (progIter.hasNext()) {
            Program prg = progIter.next();
            List<ProgramSuite> prgSuites = prg.getProgramSuites();
            if ((prgSuites == null) || (prgSuites.size() == 0)) {
                // remove programs that have no suites
                progIter.remove();
                continue;
            }

        }
    }

    /**
     * Validates programs.
     * 
     * @param program
     * @param reader
     * @return
     * @throws ParseException
     */
    private Program validateProgram(Program program, AsciiFileParser reader)
            throws ParseException {
        if (program != null) {
            List<ProgramSuite> suites = program.getProgramSuites();
            if ((suites == null) || (suites.size() == 0)) {
                // no suites, don't keep program
                return null;
            }
        }

        return program;
    }

    /**
     * Validates a suite for a given program.
     * 
     * @param program
     * @param suite
     * @param triggers
     * @param reader
     * @throws ParseException
     */
    private void validateProgramSuite(Program program, Suite suite,
            List<String> triggers, Map<String, MessageType> msgTypes,
            AsciiFileParser reader) throws ParseException {
        if (suite == null) {
            return;
        }

        List<SuiteMessage> messages = suite.getSuiteMessages();
        ProgramSuite programSuite = new ProgramSuite();
        programSuite.setSuite(suite);
        program.addProgramSuite(programSuite);

        // only keep suites that have message
        if ((messages == null) || (messages.size() == 0)) {
            return;
        }

        // determine if there are any triggers associated with the suite.
        if (!SuiteType.GENERAL.equals(suite.getType())) {
            for (String trigger : triggers) {
                MessageType msgType = msgTypes.get(trigger);
                if (msgType != null) {
                    programSuite.addTrigger(msgType.getSummary());
                }
            }
        }
    }

    /**
     * Parses the program assignment for a transmitter linking previously parsed
     * data together.
     * 
     * @param reader
     * @throws ParseException
     * @throws IOException
     */
    private void parseTransmitterProgramData(AsciiFileParser reader)
            throws ParseException, IOException {
        Map<String, TransmitterGroup> transGroups = bmhData.getTransmitters();
        Map<String, Program> programs = bmhData.getPrograms();

        while (reader.hasNextField()) {
            String mnemonic = reader.nextField();
            TransmitterGroup transGroup = transGroups.get(mnemonic);

            if (transGroup != null) {
                String programName = reader.nextField();
                Program prg = programs.get(programName);
                if (prg != null) {
                    prg.addTransmitterGroup(transGroup);
                } else {
                    StringBuilder msg = new StringBuilder(80);
                    msg.append("Unknown program [").append(programName)
                            .append("] assigned to transmitter [")
                            .append(mnemonic).append("]");
                    handleError(msg, reader.getCurrentLineNumber(),
                            reader.getSourceFile());
                }
            } else if (PLAYBACK_TRANSMITTERS.contains(mnemonic)) {
                // skip playback transmitters
                reader.nextField();
            } else {
                StringBuilder msg = new StringBuilder(80);
                msg.append("Unknown transmitter [").append(mnemonic)
                        .append("] in program assignment");
                handleError(msg, reader.getCurrentLineNumber(),
                        reader.getSourceFile());
                // skip the program name
                reader.nextField();
            }
        }
    }

    private Language parseLanguage(AsciiFileParser reader) throws IOException,
            ParseException {
        String field = reader.nextField();

        try {
            switch (Integer.parseInt(field)) {
            case 0:
                return Language.ENGLISH;
            case 1:
                return Language.SPANISH;
            case 2:
                System.err
                        .println("English and Spanish Language found.  Ignoring and setting to English. Entry found at line "
                                + reader.getCurrentLineNumber()
                                + " of file "
                                + reader.getSourceFile());
                return Language.ENGLISH;
            default:
                throw new ParseException(
                        "Unrecognized language.  Expected 0 or 1, received ["
                                + field + "]", reader.getCurrentLineNumber(),
                        reader.getSourceFile());
            }
        } catch (NumberFormatException e) {
            throw new ParseException(
                    "Unrecognized language.  Expected 0 or 1, received ["
                            + field + "]", reader.getCurrentLineNumber(),
                    reader.getSourceFile());
        }
    }

    private int parseInt(AsciiFileParser reader) throws IOException,
            ParseException {
        String field = reader.nextField();

        try {
            return Integer.parseInt(field);
        } catch (NumberFormatException e) {
            throw new ParseException(
                    "Error occurred, expected integer field, received ["
                            + field + "]", reader.getCurrentLineNumber(),
                    reader.getSourceFile());
        }
    }

    private float parseFloat(String freq, AsciiFileParser reader)
            throws ParseException {
        if (freq != null) {
            try {
                return Float.parseFloat(freq);
            } catch (NumberFormatException e) {
                throw new ParseException(
                        "Error occurred, expected float field, received ["
                                + freq + "]", reader.getCurrentLineNumber(),
                        reader.getSourceFile());
            }
        } else {
            throw new ParseException(
                    "Error occurred, expected float field, received [" + freq
                            + "]", reader.getCurrentLineNumber(),
                    reader.getSourceFile());

        }
    }

    private boolean parseBool(AsciiFileParser reader) throws IOException,
            ParseException {
        String field = reader.nextField();

        try {
            switch (Integer.parseInt(field)) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                throw new ParseException(
                        "Unrecognized boolean.  Expected 0 or 1, received ["
                                + field + "]", reader.getCurrentLineNumber(),
                        reader.getSourceFile());
            }
        } catch (NumberFormatException e) {
            throw new ParseException(
                    "Unrecognized boolean.  Expected 0 or 1, received ["
                            + field + "]", reader.getCurrentLineNumber(),
                    reader.getSourceFile());
        }
    }

    private TimeZone parseTimeZone(AsciiFileParser reader) throws IOException,
            ParseException {
        String field = reader.nextField();

        try {
            int legacyCode = Integer.parseInt(field);
            TimeZone tz = BMHTimeZone.getLegacyTimeZone(legacyCode);
            return tz;
        } catch (IllegalArgumentException e) {
            throw new ParseException(
                    "Unrecognized time zone.  Expected 0, 1, 3, 5, 7, 9, 11, 13, or 14 received ["
                            + field + "]", reader.getCurrentLineNumber(),
                    reader.getSourceFile());
        }
    }

    private Dictionary parseTransmitterDictionary(Language lang,
            AsciiFileParser reader) throws IOException, ParseException {
        String dictName = reader.nextField();

        if (dictName != null) {
            Dictionary dict = bmhData.getDictionaries().get(dictName);
            if (dict == null) {
                throw new ParseException("Unknown " + lang + " dictionary ["
                        + dictName + "]", reader.getCurrentLineNumber(),
                        reader.getSourceFile());
            } else if (!lang.equals(dict.getLanguage())) {
                throw new ParseException("Expected dictionary [" + dictName
                        + "] to be " + lang + " instead is "
                        + dict.getLanguage(), reader.getCurrentLineNumber(),
                        reader.getSourceFile());
            }

            return dict;
        }

        return null;
    }

    private String parseTransmitterStationIdMsg(Language lang,
            AsciiFileParser reader) throws IOException, ParseException {
        String stationId = reader.nextField();

        if (stationId != null) {
            StationIdData statData = ascii.getStationIdData().get(stationId);
            if (statData == null) {
                throw new ParseException("Unknown " + lang + " station id ["
                        + stationId + "]", reader.getCurrentLineNumber(),
                        reader.getSourceFile());
            } else if (!lang.equals(statData.getLanguage())) {
                throw new ParseException("Expected " + lang + " Station Id ["
                        + stationId + "] received " + statData.getLanguage(),
                        reader.getCurrentLineNumber(), reader.getSourceFile());
            }

            return statData.getText();
        }

        return null;
    }

    private TransmitterLanguage parseTransmitterLanguage(Language lang,
            TransmitterGroup group, Dictionary dict, String stationMessage,
            AsciiFileParser reader) throws ParseException {
        // if (dict != null) {
        if (stationMessage != null) {
            TransmitterLanguage transLang = new TransmitterLanguage();
            transLang.setLanguage(lang);
            transLang.setTimeMsgPreamble(defaultTimeMsgs.get(lang));
            // transLang.setDictionaryName(dict.getName());
            transLang.setStationIdMsg(stationMessage);

            TtsVoice voice = getTtsVoice(lang, true,
                    reader.getCurrentLineNumber(), reader.getSourceFile());
            transLang.setVoice(voice);
            transLang.setTransmitterGroup(group);
            return transLang;
        }

        return null;
    }

    private Designation parseDesignation(AsciiFileParser reader)
            throws IOException, ParseException {
        // from legacy MP/db_include/MessageTypeTable.h and
        // MP/db_include/DBEnums.h

        String field = reader.nextField();

        try {
            switch (Integer.parseInt(field)) {
            case 0:
                return Designation.StationID;
            case 1:
                return Designation.TimeAnnouncement;
            case 2:
                return Designation.Observation;
            case 3:
                return Designation.Outlook;
            case 4:
                return Designation.Forecast;
            case 5:
                return Designation.Advisory;
            case 6:
                return Designation.Watch;
            case 7:
                return Designation.Warning;
            case 8:
                return Designation.Other;
            default:
                throw new ParseException(
                        "Unrecognized designation.  Expected 0-8, received ["
                                + field + "]", reader.getCurrentLineNumber(),
                        reader.getSourceFile());
            }
        } catch (NumberFormatException e) {
            throw new ParseException(
                    "Unrecognized designation.  Expected 0-8, received ["
                            + field + "]", reader.getCurrentLineNumber(),
                    reader.getSourceFile());
        }
    }

    private String parsePeriod(AsciiFileParser reader) throws IOException,
            ParseException {
        String period = reader.nextField();
        Matcher matcher = PERIOD.matcher(period);
        if (matcher.matches()) {
            return matcher.group(1) + matcher.group(2) + matcher.group(3)
                    + matcher.group(4);
        } else {
            throw new ParseException("Invalid time format, found [" + period
                    + "], expected [00.00:00:00]",
                    reader.getCurrentLineNumber(), reader.getSourceFile());
        }
    }

    /**
     * Handles strict check to either log message or throw an exception
     * 
     * @param msg
     * @param currentLineNumber
     * @param sourceFile
     * @throws ParseException
     */
    private void handleError(StringBuilder msg, int currentLineNumber,
            String sourceFile) throws ParseException {
        if (strict) {
            throw new ParseException(msg.toString(), currentLineNumber,
                    sourceFile);
        } else {
            msg.append(". Line: ").append(currentLineNumber).append(", File: ")
                    .append(sourceFile);
            validationMessages.add(msg.toString());
        }
    }

    private TtsVoice getTtsVoice(Language lang, boolean isMale,
            int currentLineNumber, String sourceFile) throws ParseException {
        if (!includeSpanish && (lang == Language.SPANISH)) {
            return null;
        }
        Map<Boolean, TtsVoice> langMap = voices.get(lang);
        if (langMap == null) {
            StringBuilder msg = new StringBuilder(80);
            msg.append("No ").append(lang)
                    .append(" voices are configured for BMH");
            handleError(msg, currentLineNumber, sourceFile);
            langMap = voices.values().iterator().next();
        }

        TtsVoice voice = langMap.get(isMale);
        if (voice == null) {
            voice = langMap.values().iterator().next();
        }
        return voice;
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }

    private boolean containsSpanishVoice(List<TtsVoice> voices) {
        for (TtsVoice voice : voices) {
            if (voice.getLanguage() == Language.SPANISH) {
                return true;
            }
        }
        return false;
    }

    public String getVoiceMsg() {
        return voiceMsg;
    }

    public boolean parsedData() {
        return this.parsedData.get();
    }
}