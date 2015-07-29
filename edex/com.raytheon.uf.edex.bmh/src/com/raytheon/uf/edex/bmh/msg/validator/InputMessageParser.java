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

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.FileUtil;
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
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class InputMessageParser {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(InputMessageParser.class);

    public static final String AFOS_ID_REGEX = "^(([A-Z0-9]{3}| {3})[A-Z0-9]{5}[A-Z0-9 ])";

    private static final ThreadLocal<SimpleDateFormat> dateParser = TimeUtil
            .buildThreadLocalSimpleDateFormat("yyMMddHHmm",
                    TimeZone.getTimeZone("GMT"));

    private static final Pattern startPattern = Pattern.compile("\\ea");

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

    private final IMessageLogger messageLogger;

    public InputMessageParser(final IMessageLogger messageLogger) {
        this.messageLogger = messageLogger;
    }

    public InputMessage parse(@Body File file,
            @Headers Map<String, Object> headers) {
        InputMessage message = new InputMessage();

        message.setUpdateDate(TimeUtil.newGmtCalendar());
        String fileName = headers.get("CamelFileNameOnly").toString();
        message.setName(fileName);
        message.setOriginalFile(file);

        message.setValidHeader(true);
        messageLogger.logParseActivity(message);

        try {
            CharSequence text = FileUtil.file2String(file);
            int index = findStart(text);
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

        return message;
    }

    private int findStart(CharSequence text) throws ParseException {
        Matcher start = startPattern.matcher(text);
        if (start.find() == false) {
            throw new ParseException("No Start Message Indicator.", 0);
        }
        return start.end();
    }

    private int parseMessageFormat(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher format = formatPattern.matcher(text);
        format.region(index, text.length());
        if (format.find() == false) {
            throw new ParseException("Invalid Message Format.", index);
        }
        if ("T".equals(format.group(1)) == false) {
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
        if (afosid.find() == false) {
            throw new ParseException("Invalid Afosid.", index);
        }
        message.setAfosid(afosid.group());
        return afosid.end();
    }

    private int parseCreationDate(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher date = datePattern.matcher(text);
        date.region(index, text.length());
        if (date.find() == false) {
            throw new ParseException("Invalid Creation Date.", index);
        }
        message.setCreationTime(parseDate(date));
        return date.end();
    }

    private int parseEffectiveDate(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher date = datePattern.matcher(text);
        date.region(index, text.length());
        if (date.find() == false) {
            throw new ParseException("Invalid Effective Date.", index);
        }
        message.setEffectiveTime(parseDate(date));
        return date.end();
    }

    private int parsePeriodicity(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher periodicity = periodicityPattern.matcher(text);
        periodicity.region(index, text.length());
        if (periodicity.find() == false) {
            throw new ParseException("Invalid Periodicity.", index);
        }
        if (!periodicity.group().trim().isEmpty()) {
            message.setPeriodicity(periodicity.group());
        }
        return periodicity.end();
    }

    private int parseMrd(InputMessage message, CharSequence text, int index) {
        Matcher mrd = mrdPattern.matcher(text);
        mrd.region(index, text.length());
        if (mrd.find()) {
            String mrdStr = mrd.group();
            /*
             * Determine if follows mrds are present.
             */
            int followsIdx = mrdStr.indexOf('F');
            if (followsIdx != -1) {
                final String mrdFollows = mrdStr.substring(followsIdx);
                mrdStr = mrdStr.substring(0, followsIdx);

                StringBuilder sb = new StringBuilder(
                        "Found and ignored mrd follows (");
                sb.append(mrdFollows)
                        .append(") information in input message (");
                sb.append(message.getName()).append(").");

                statusHandler.info(sb.toString());
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
            throw new ParseException("Unrecognized Active/Inactive Character: "
                    + active, index);
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
            throw new ParseException("Unrecognized Confirmation Character: "
                    + active, index);
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
            throw new ParseException("Unrecognized Interrupt Flag Character: "
                    + active, index);
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
            throw new ParseException("Unrecognized Alert Tone Character: "
                    + active, index);
        }
        return index + 1;
    }

    private int parsePolygon(InputMessage message, CharSequence text, int index) {
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
                    .append(") inforamtion in input message(")
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
            message.setAreaCodes(ugc.group(1));
            return ugc.end();
        }
        throw new ParseException("Invalid Listening Area Codes.", index);
    }

    private int parseExpirationDate(InputMessage message, CharSequence text,
            int index) throws ParseException {
        Matcher date = datePattern.matcher(text);
        date.region(index, text.length());
        if (date.find() == false) {
            throw new ParseException("Invalid Expiration Date.", index);
        }
        if (date.group().equals("9999999999")) {
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
        if (end.find() == false) {
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
