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
package com.raytheon.uf.edex.bmh.msg.validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage.Origin;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHFileProcessException;
import com.raytheon.uf.edex.bmh.FileManager;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;

/**
 * 
 * Parses an {@link InputMessage} from a {@link CharSequence}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3283     bsteffen    Initial creation
 * Sep 25, 2014  3620     bsteffen    Add seconds to periodicity.
 * Nov 17, 2014  3793     bsteffen    Add same transmitters to input message.
 * Jan 05, 2015  3872     rjpeter     Add file name to stack trace.
 * Jan 05, 2015  3651     bkowal      Use {@link IMessageLogger} to log message errors.
 * Feb 18, 2015  4136     bkowal      Use the {@link Language} identifiers to determine
 *                                    the Language when parsing the input.
 * Feb 20, 2015  4158     bsteffen    Add support for optional polygons
 * Feb 27, 2015  4182     rferrel     Corrected spelling in messages.
 * Mar 05, 2015  4222     bkowal      Use null for messages that never expire.
 * Mar 05, 2015  4199     rferrel     The afosidPattern excepts valid spaces in the id.
 * Apr 27, 2015  4397     bkowal      Set the {@link InputMessage} update date.
 * May 13, 2015  4429     rferrel     Changes loggers for traceId.
 * May 21, 2015  4429     rjpeter     Added additional logging.
 * Jun 17, 2015  4482     rjpeter     Ignore all polygon data.
 * Jun 23, 2015  4572     bkowal      Extracted the afos id regex into {@link #AFOS_ID_REGEX}.
 * Jul 21, 2015  4671     bkowal      Ignore mrd follows.
 * Jul 29, 2015  4690     rjpeter     Set originalFile for rejection use case.
 * Aug 03, 2015  4350     bkowal      Fix spelling.
 * Aug 04, 2015  4671     bkowal      Throw a {@link ParseException} when mrd follows is
 *                                    encountered.
 * Sep 24, 2015  4916     bkowal      Trim the afos id.
 * Sep 24, 2015  4925     bkowal      Added {@link #parseUGCs(String, int)}.
 * Sep 30, 2015  4938     bkowal      Validate that the afos id meets the minimum length
 *                                    requirements when the same tone flag is set.
 * Nov 16, 2015  5127     rjpeter     Added logging of headers and archiving of original file.
 * Aug 04, 2016  5766     bkowal      Eliminate deprecated use of FileUtil.
 * Jan 19, 2017  6078     bkowal      Set origin on the {@link InputMessage}.
 * </pre>
 * 
 * @author bsteffen
 */
public class InputMessageParser {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(InputMessageParser.class);

    public static final String AFOS_ID_REGEX = "^(([A-Z0-9]{3}| {3})[A-Z0-9]{5}[A-Z0-9 ])";

    private static final ThreadLocal<SimpleDateFormat> dateParser = TimeUtil
            .buildThreadLocalSimpleDateFormat("yyMMddHHmm",
                    TimeZone.getTimeZone("GMT"));

    private static final Pattern startPattern = Pattern.compile("\\ea");

    private static final Pattern headerPattern = Pattern.compile("^.*$",
            Pattern.MULTILINE);

    private static final Pattern formatPattern = Pattern
            .compile("^([A-Z])_([A-Z]{3})");

    /**
     * Match CCCNNNXXX, sssNNNXXX, CCCNNNXXs and sssNNNXXs where s is spaces.
     */
    private static final Pattern afosidPattern = Pattern.compile(AFOS_ID_REGEX);

    private static final Pattern datePattern = Pattern.compile("^[0-9]{10}");

    private static final Pattern periodicityPattern = Pattern
            .compile("^[0-9]{8}| {8}");

    private static final Pattern mrdPattern = Pattern
            .compile("^[0-9]{3}(R([0-9]{3}){1,20})?(F([0-9]{3}){1,10})?");

    private static final Pattern ugcPattern = Pattern.compile("^([^c]*)c");

    private static final Pattern polygonPattern = Pattern
            .compile("^([-]?[0-9]{3,5} ?)");

    private static final Pattern endPattern = Pattern.compile("^(.*)\\eb",
            Pattern.DOTALL);

    private static final String UGC_SEPARATOR = "-";

    private static final String UGC_CONSECUTIVE = ">";

    private final IMessageLogger messageLogger;

    private final FileManager archiveFileManager;

    public InputMessageParser(final IMessageLogger messageLogger,
            final FileManager archiveFileManager) {
        this.messageLogger = messageLogger;
        this.archiveFileManager = archiveFileManager;
    }

    public InputMessage parse(@Body File file,
            @Headers Map<String, Object> headers) {
        InputMessage message = new InputMessage();

        String fileName = headers.get("CamelFileNameOnly").toString();
        message.setName(fileName);
        message.setOriginalFile(file);

        message.setValidHeader(true);
        messageLogger.logParseActivity(message);

        try {
            CharSequence text = new String(Files.readAllBytes(file.toPath()));
            int index = findStart(text);
            Matcher header = headerPattern.matcher(text);
            header.region(index, text.length());
            if (!header.find()) {
                throw new ParseException("Cannot find message header", index);
            }

            messageLogger.logParseHeader(message, header.group());

            index = parseMessageFormat(message, text, index);
            index = parseAfosId(message, text, index);
            index = parseCreationDate(message, text, index);
            index = parseEffectiveDate(message, text, index);
            index = parsePeriodicity(message, text, index);
            index = parseMrd(message, text, index);
            index = parseActive(message, text, index);
            index = parseSave(index);
            index = parseConfirmation(message, text, index);
            index = parseInterrupt(message, text, index);
            index = parseTone(message, text, index);
            index = parsePolygon(message, text, index);
            index = parseAreaCodes(message, text, index);
            index = parseExpirationDate(message, text, index);
            index = parseContent(message, text, index);
            deriveSameTransmitters(message);
        } catch (ParseException | IOException e) {
            statusHandler.error(BMH_CATEGORY.INPUT_MESSAGE_PARSE_ERROR,
                    fileName + " failed to parse", e);
            this.messageLogger.logError(null,
                    BMH_COMPONENT.INPUT_MESSAGE_PARSER,
                    BMH_ACTIVITY.MESSAGE_PARSING, message, e);
            message.setValidHeader(false);
        }

        message.setOrigin(Origin.EXTERNAL);
        return message;
    }

    /**
     * Archives the original file associated with the given input message.
     * 
     * @param msg
     */
    public void archive(InputMessage msg) {
        File file = msg.getOriginalFile();

        try {
            archiveFileManager.processFile(file.toPath(),
                    BMH_CATEGORY.MESSAGE_ARCHIVE_FAILED, true);
        } catch (BMHFileProcessException e) {
            statusHandler.error(BMH_CATEGORY.MESSAGE_ARCHIVE_FAILED,
                    "Failed to copy incoming file " + file.getName()
                            + " to archive");
        }
    }

    private int findStart(CharSequence text) throws ParseException {
        Matcher start = startPattern.matcher(text);
        if (!start.find()) {
            throw new ParseException("No Start Message Indicator.", 0);
        }
        return start.end();
    }

    private int parseMessageFormat(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher format = formatPattern.matcher(text);
        format.region(index, text.length());
        if (!format.find()) {
            throw new ParseException("Invalid Message Format.", index);
        }
        if (!("T".equals(format.group(1)))) {
            throw new ParseException("Unhandled format:" + format.group(1),
                    format.start(1));
        }
        if (Language.ENGLISH.getIdentifier().equals(format.group(2))) {
            message.setLanguage(Language.ENGLISH);
        } else if (Language.SPANISH.getIdentifier().equals(format.group(2))) {
            message.setLanguage(Language.SPANISH);
        } else {
            throw new ParseException("Unhandled language:" + format.group(2),
                    format.start(2));
        }

        return format.end();
    }

    private int parseAfosId(InputMessage message, CharSequence text, int index)
            throws ParseException {
        Matcher afosid = afosidPattern.matcher(text);
        afosid.region(index, text.length());
        if (!afosid.find()) {
            throw new ParseException("Invalid Afosid.", index);
        }
        message.setAfosid(afosid.group().trim());
        return afosid.end();
    }

    private int parseCreationDate(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher date = datePattern.matcher(text);
        date.region(index, text.length());
        if (!date.find()) {
            throw new ParseException("Invalid Creation Date.", index);
        }
        message.setCreationTime(parseDate(date));
        return date.end();
    }

    private int parseEffectiveDate(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher date = datePattern.matcher(text);
        date.region(index, text.length());
        if (!date.find()) {
            throw new ParseException("Invalid Effective Date.", index);
        }
        message.setEffectiveTime(parseDate(date));
        return date.end();
    }

    private int parsePeriodicity(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher periodicity = periodicityPattern.matcher(text);
        periodicity.region(index, text.length());
        if (!periodicity.find()) {
            throw new ParseException("Invalid Periodicity.", index);
        }
        if (!periodicity.group().trim().isEmpty()) {
            message.setPeriodicity(periodicity.group());
        }
        return periodicity.end();
    }

    private int parseMrd(InputMessage message, CharSequence text, int index)
            throws ParseException {
        Matcher mrd = mrdPattern.matcher(text);
        mrd.region(index, text.length());
        if (mrd.find()) {
            String mrdStr = mrd.group();
            /*
             * Determine if follows mrds are present.
             */
            if (mrdStr.contains("F")) {
                throw new ParseException(
                        "MRD Follows is no longer supported; discovered in mrd: "
                                + mrdStr,
                        index);
            }
            message.setMrd(mrdStr);
            return mrd.end();
        } else {
            return index;
        }
    }

    private int parseActive(InputMessage message, CharSequence text, int index)
            throws ParseException {
        char active = text.charAt(index);
        switch (active) {
        case 'A':
        case 'C':
        case 'X':
            message.setActive(true);
            break;
        case 'I':
            message.setActive(false);
            break;
        default:
            throw new ParseException(
                    "Unrecognized Active/Inactive Character: " + active, index);
        }
        return index + 1;
    }

    private int parseSave(int index) {
        /* Save is currently not stored so just skip a byte */
        return index + 1;
    }

    private int parseConfirmation(InputMessage message, CharSequence text,
            int index) throws ParseException {
        char active = text.charAt(index);
        switch (active) {
        case 'C':
            message.setConfirm(true);
            break;
        case ' ':
            message.setConfirm(false);
            break;
        default:
            throw new ParseException(
                    "Unrecognized Confirmation Character: " + active, index);
        }
        return index + 1;
    }

    private int parseInterrupt(InputMessage message, CharSequence text,
            int index) throws ParseException {
        char active = text.charAt(index);
        switch (active) {
        case 'I':
            message.setInterrupt(true);
            break;
        case ' ':
            message.setInterrupt(false);
            break;
        default:
            throw new ParseException(
                    "Unrecognized Interrupt Flag Character: " + active, index);
        }
        return index + 1;
    }

    private int parseTone(InputMessage message, CharSequence text, int index)
            throws ParseException {
        char active = text.charAt(index);
        switch (active) {
        case 'A':
            message.setAlertTone(true);
            message.setNwrsameTone(true);
            break;
        case ' ':
            message.setAlertTone(false);
            message.setNwrsameTone(true);
            break;
        case 'N':
            message.setAlertTone(false);
            message.setNwrsameTone(false);
            break;
        default:
            throw new ParseException(
                    "Unrecognized Alert Tone Character: " + active, index);
        }

        if (message.getNwrsameTone() && message.getAfosid().length() < 7) {
            throw new ParseException(
                    "Invalid message type. Message type length must be >= 7 characters.",
                    index);
        }

        return index + 1;
    }

    private int parsePolygon(InputMessage message, CharSequence text,
            int index) {
        Matcher polygon = polygonPattern.matcher(text);
        polygon.region(index, text.length());
        StringBuilder polygonText = new StringBuilder();
        int end = index;
        String validationMsg = null;
        int polyCount = 0;
        while (polygon.find()) {
            // lat
            polygonText.append(polygon.group());
            end = polygon.end();
            polygon.region(end, text.length());

            if (polygon.find()) {
                // lon
                polygonText.append(polygon.group());
                end = polygon.end();
                polygon.region(end, text.length());
            } else {
                validationMsg = " Incomplete vertex detected.";
            }
            polyCount++;
        }

        if (polygonText.length() > 0) {
            StringBuilder msg = new StringBuilder(160);
            msg.append("Found and ignored polygon(").append(polygonText)
                    .append(") information in input message(")
                    .append(message.getName()).append(").");
            if (validationMsg != null || polyCount > 20) {
                msg.append(" Polygon issues detected:");
                if (validationMsg != null) {
                    msg.append(validationMsg);
                }
                if (polyCount > 20) {
                    msg.append(" ").append(polyCount)
                            .append(" vertices detected, only 20 allowed.");
                }
            }

            statusHandler.info(msg.toString());
        }
        return end;
    }

    private int parseAreaCodes(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher ugc = ugcPattern.matcher(text);
        ugc.region(index, text.length());
        if (ugc.find()) {
            message.setAreaCodes(
                    this.parseUGCs(ugc.group(1), index, message.getName()));
            return ugc.end();
        }
        throw new ParseException("Invalid Listening Area Codes.", index);
    }

    /**
     * Examines the specified ugc {@link String} and performs the required
     * substitutions in accordance with the specification:
     * http://www.nws.noaa.gov/directives/sym/pd01017002curr.pdf (page 5).
     * 
     * @param ugcString
     *            the specified ugc {@link String}
     * @param index
     *            index in header where the ugc String started. Only used for
     *            exception reporting.
     * @param name
     *            the name of the {@link InputMessage}. Only used for logging
     *            purposes.
     * @return the ugc {@link String} with all applicable substitutions
     * @throws ParseException
     */
    private String parseUGCs(String ugcString, final int index,
            final String name) throws ParseException {
        /*
         * We will resolve all substitutions that should take place based on the
         * specification so that the substitution handling will not need to be
         * propagated throughout the entire BMH code base.
         */

        StringBuilder sb = new StringBuilder();
        String[] ugcComponents = ugcString.split(UGC_SEPARATOR);
        if (ugcComponents.length == 1) {
            return ugcComponents[0];
        }

        boolean first = true;
        String lastSSF = null;
        for (String ugcComponent : ugcComponents) {
            if (first) {
                first = false;
            } else {
                sb.append(UGC_SEPARATOR);
            }

            if (ugcComponent.contains(UGC_CONSECUTIVE)) {
                String[] consecutiveComponents = ugcComponent
                        .split(UGC_CONSECUTIVE);
                if (consecutiveComponents.length != 2) {
                    throw new ParseException(
                            "UGC consecutive sequence: " + ugcComponent
                                    + " was not in the expected [SSF]NNN>NNN format.",
                            index);
                }
                String consecutive0 = consecutiveComponents[0];
                final String consecutive1 = consecutiveComponents[1];
                if (lastSSF == null && consecutive0.length() != 6) {
                    /*
                     * This component MUST contain a SSF.
                     */
                    throw new ParseException(
                            "First ugc: " + ugcComponent
                                    + " was not in the expected SSFNNN format.",
                            index);
                }

                if (consecutive0.length() == 6) {
                    lastSSF = consecutive0.substring(0, 3);
                    consecutive0 = consecutive0.substring(3);
                }

                if (consecutive0.length() != 3) {
                    throw new ParseException("Component: " + consecutive0
                            + " of ugc consecutive sequence: " + ugcComponent
                            + " was not in the expected NNN format.", index);
                }
                if (consecutive1.length() != 3) {
                    throw new ParseException("Component: " + consecutive1
                            + " of ugc consecutive sequence: " + ugcComponent
                            + " was not in the expected NNN format.", index);
                }

                /*
                 * Verify the sequence.
                 */
                int sequenceStart;
                int sequenceEnd;
                try {
                    sequenceStart = Integer.parseInt(consecutive0);
                    sequenceEnd = Integer.parseInt(consecutive1);
                } catch (NumberFormatException e) {
                    throw new ParseException(
                            "UGC consecutive sequence: " + ugcComponent
                                    + " contains invalid numeric reference.",
                            index);
                }

                if (sequenceStart > sequenceEnd) {
                    throw new ParseException("UGC consecutive sequence: "
                            + ugcComponent + " start is before end.", index);
                }

                /*
                 * Now we are finally fairly confident that we may be able to
                 * reasonably work with the specified ugc sequence.
                 */
                boolean firstInSequence = true;
                for (int j = sequenceStart; j <= sequenceEnd; j++) {
                    if (firstInSequence) {
                        firstInSequence = false;
                    } else {
                        sb.append(UGC_SEPARATOR);
                    }

                    /*
                     * 0-Padding.
                     */
                    String sequenceNum = StringUtils
                            .leftPad(Integer.toString(j), 3, "0");
                    sb.append(lastSSF).append(sequenceNum);
                }
            } else {
                if (lastSSF == null && ugcComponent.length() != 6) {
                    /*
                     * This component MUST contain a SSF.
                     */
                    throw new ParseException(
                            "First ugc: " + ugcComponent
                                    + " was not in the expected SSFNNN format.",
                            index);
                }

                if (ugcComponent.length() == 6) {
                    /*
                     * Extract the SSF.
                     */
                    lastSSF = ugcComponent.substring(0, 3);
                    sb.append(ugcComponent);
                } else {
                    if (ugcComponent.length() != 3) {
                        throw new ParseException(
                                "UGC component: " + ugcComponent
                                        + " was not in the expected NNN format.",
                                index);
                    }

                    /*
                     * Just the county, territory, parish, or independent city
                     * number.
                     */
                    sb.append(lastSSF).append(ugcComponent);
                }
            }
        }

        if (sb.length() != ugcString.length()) {
            StringBuilder msg = new StringBuilder("Expanded the ugc string: ");
            msg.append(ugcString).append(" for input message: ").append(name)
                    .append(" to: ").append(sb.toString()).append(".");

            statusHandler.info(msg.toString());
        }

        return sb.toString();
    }

    private int parseExpirationDate(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher date = datePattern.matcher(text);
        date.region(index, text.length());
        if (!date.find()) {
            throw new ParseException("Invalid Expiration Date.", index);
        }
        if ("9999999999".equals(date.group())) {
            /*
             * The message never expires.
             */
            message.setExpirationTime(null);
        } else {
            message.setExpirationTime(parseDate(date));
        }
        return date.end();
    }

    private int parseContent(InputMessage message, CharSequence text, int index)
            throws ParseException {
        Matcher end = endPattern.matcher(text);
        end.region(index, text.length());
        if (!end.find()) {
            throw new ParseException("No End Message indicator", index);
        }
        message.setContent(end.group(1).trim());
        return end.end();
    }

    private void deriveSameTransmitters(InputMessage message) {
        if (Boolean.TRUE.equals(message.getNwrsameTone())) {
            MessageTypeDao messageTypeDao = new MessageTypeDao();
            MessageType type = messageTypeDao.getByAfosId(message.getAfosid());
            if (type != null) {
                Set<Transmitter> transmitters = type.getSameTransmitters();
                if ((transmitters != null) && !transmitters.isEmpty()) {
                    StringBuilder transmittersBuilder = new StringBuilder();
                    for (Transmitter transmitter : transmitters) {
                        if (transmittersBuilder.length() > 0) {
                            transmittersBuilder.append("-");
                        }
                        transmittersBuilder.append(transmitter.getMnemonic());
                    }
                    message.setSameTransmitters(transmittersBuilder.toString());
                }
            }
        }
    }

    private static Calendar parseDate(Matcher date) throws ParseException {
        Date d = dateParser.get().parse(date.group());
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }
}
