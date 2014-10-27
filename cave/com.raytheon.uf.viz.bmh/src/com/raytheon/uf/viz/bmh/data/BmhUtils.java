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
package com.raytheon.uf.viz.bmh.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

import com.raytheon.uf.common.auth.exception.AuthorizationException;
import com.raytheon.uf.common.auth.resp.SuccessfulExecution;
import com.raytheon.uf.common.bmh.request.AbstractBMHServerRequest;
import com.raytheon.uf.common.bmh.request.TextToSpeechRequest;
import com.raytheon.uf.common.serialization.comm.IServerRequest;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields.DateFieldType;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * BMH Viz utility class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 25, 2014   3355      mpduff      Initial creation
 * Aug 05, 2014   3414      rjpeter     Added BMH Thrift interface.
 * Oct 2, 2014    3642      bkowal      Specify the Synthesizer Timeout
 * Oct 13, 2014   3413      rferrel     Support of user roles in sendRequest.
 * Oct 16, 2014   3657      bkowal      Relocated duration parsing methods.
 * Oct 21, 2014   3728      lvenable    Make splitDateTimeString public.
 * Oct 27, 2014   3750      lvenable    change string length in parsing method
 *                                      generateDayHourMinuteSecondMap()
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BmhUtils {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BmhUtils.class);

    /**
     * Phoneme SSML open snippet
     */
    public static String PHONEME_OPEN = " <phoneme alphabet=\"x-cmu\" ph=\" ";

    /**
     * Phoneme SSML close snippet
     */
    public static String PHONEME_CLOSE = "\">  </phoneme>";

    /**
     * SayAs SSML open snippet
     */
    public static String SAYAS_OPEN = " <say-as interpret-as=\"";

    /**
     * SayAs SSML format snippet
     */
    public static String SAYAS_FORMAT = "\" format=\"";

    /**
     * SayAs SSML close snippet
     */
    public static String SAYAS_CLOSE = "</say-as>";

    /**
     * Convert the provided text to speech and play it
     * 
     * @param text
     *            The text to play
     */
    public static void playText(String text) {
        TextToSpeechRequest req = new TextToSpeechRequest();
        req.setPhoneme(text);
        req.setTimeout(10000);
        try {
            req = (TextToSpeechRequest) BmhUtils.sendRequest(req);
            playSound(req.getByteData(), req.getStatus());
        } catch (Exception e) {
            statusHandler.error("Error playing text", e);
        }
    }

    /**
     * Play the byte array data
     * 
     * @param data
     *            data to play
     */
    public static void playSound(byte[] data, String description) {
        if (data == null) {
            statusHandler.error(description);
            return;
        }

        AudioFormat audioFormat = new AudioFormat(Encoding.ULAW, 8000, 8, 1, 1,
                8000, true);

        AudioInputStream audioInputStream = new AudioInputStream(
                new ByteArrayInputStream(data), audioFormat, data.length);

        Clip line = null;
        try {
            line = AudioSystem.getClip();
            line.open(audioInputStream);
            line.start();
            line.drain();
        } catch (LineUnavailableException | IOException e1) {
            statusHandler.error(e1.getMessage());
        } finally {
            if (line != null) {
                line.close();
            }
        }
    }

    /**
     * Play a phoneme in the brief format
     * 
     * <pre>
     *  [K EY0 P K AA1 D]
     * </pre>
     * 
     * @param text
     *            The phoneme text
     */
    public static void playBriefPhoneme(String text) {
        text = text.replaceAll("\\[", PHONEME_OPEN);
        text = text.replaceAll("\\]", PHONEME_CLOSE);
        playText(text);
    }

    /**
     * Play the provided text as a phoneme. Surround text with the open/close
     * phoneme SSML tags.
     * 
     * @param text
     *            The bare phoneme text
     */
    public static void playAsPhoneme(String text) {
        String textToPlay = PHONEME_OPEN + text + PHONEME_CLOSE;
        playText(textToPlay);
    }

    /**
     * Convert the say as text to an SSML say-as snippet
     * 
     * @return the ssml snippet
     */
    public static String getSayAsSnippet(String sayAsType, String sayAsText) {
        int size = BmhUtils.SAYAS_OPEN.length() + BmhUtils.SAYAS_CLOSE.length()
                + sayAsText.length() + sayAsType.length() + 3;
        StringBuilder sb = new StringBuilder(size);
        sb.append(BmhUtils.SAYAS_OPEN);
        sb.append(sayAsType);
        sb.append("\"> ");
        sb.append(sayAsText);
        sb.append(BmhUtils.SAYAS_CLOSE);

        return sb.toString();
    }

    /**
     * Sends an {@link IServerRequest} to the BMH jvm for processing.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static Object sendRequest(AbstractBMHServerRequest request)
            throws Exception {
        request.setOperational(CAVEMode.getMode() == CAVEMode.OPERATIONAL);
        Object obj = RequestRouter.route(request, "bmh.server");
        if (obj instanceof SuccessfulExecution) {
            SuccessfulExecution se = (SuccessfulExecution) obj;
            obj = se.getResponse();
        } else {
            throw new AuthorizationException(
                    "User not authorized to perform request.");
        }
        return obj;
    }

    /**
     * Generate a Map of DateFieldType Day/Hour/Minute keys with values pulled
     * from the provided string.
     * 
     * @param dateTimeStr
     *            Date/Time string (DDHHMMSS).
     * @return Map of DateFieldTypes and the associated values.
     */
    public static Map<DateFieldType, Integer> generateDayHourMinuteSecondMap(
            String dateTimeStr) {
        Map<DateFieldType, Integer> dateTimeMap = new LinkedHashMap<DateFieldType, Integer>();

        if (dateTimeStr == null || dateTimeStr.length() != 8) {
            dateTimeMap.put(DateFieldType.DAY, 0);
            dateTimeMap.put(DateFieldType.HOUR, 0);
            dateTimeMap.put(DateFieldType.MINUTE, 0);
            dateTimeMap.put(DateFieldType.SECOND, 0);
        } else {
            int[] dtArray = splitDateTimeString(dateTimeStr);
            dateTimeMap.put(DateFieldType.DAY, dtArray[0]);
            dateTimeMap.put(DateFieldType.HOUR, dtArray[1]);
            dateTimeMap.put(DateFieldType.MINUTE, dtArray[2]);
            dateTimeMap.put(DateFieldType.SECOND, dtArray[3]);
        }

        return dateTimeMap;
    }

    /**
     * Generate a Map of DateFieldType Hour/Minute keys with values pulled from
     * the provided string.
     * 
     * @param timeStr
     *            Time string (HHMM).
     * @return Map of DateFieldTypes and the associated values.
     */
    public static Map<DateFieldType, Integer> generateHourMinuteMap(
            String timeStr) {
        Map<DateFieldType, Integer> durmap = new LinkedHashMap<DateFieldType, Integer>();

        if (timeStr == null || timeStr.length() != 4) {
            durmap.put(DateFieldType.HOUR, 0);
            durmap.put(DateFieldType.MINUTE, 0);
        } else {
            int[] dtArray = splitDateTimeString(timeStr);
            durmap.put(DateFieldType.HOUR, dtArray[0]);
            durmap.put(DateFieldType.MINUTE, dtArray[1]);
        }

        return durmap;
    }

    /**
     * This method will split the date/time string into an array of integers for
     * each element in the array.
     * 
     * If the string passed in is 013422, the return int array will contain 3
     * elements: 1, 34, 22
     * 
     * @param dateTimeStr
     *            Date/Time string.
     * @return Array of numbers.
     */
    public static int[] splitDateTimeString(String dateTimeStr) {
        int arraySize = dateTimeStr.length() / 2;
        int[] intArray = new int[arraySize];

        int idx = 0;
        for (int i = 0; i < arraySize; i++) {
            String subStr = dateTimeStr.substring(idx, idx + 2);

            try {
                intArray[i] = Integer.valueOf(subStr);
            } catch (NumberFormatException nfe) {
                intArray[i] = 0;
            }

            idx += 2;
        }

        return intArray;
    }
}