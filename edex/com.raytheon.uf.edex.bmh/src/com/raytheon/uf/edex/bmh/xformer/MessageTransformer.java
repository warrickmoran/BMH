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
package com.raytheon.uf.edex.bmh.xformer;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguagePK;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.Sentence;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.bmh.tts.StaticMessageIdentifierUtil;
import com.raytheon.uf.edex.bmh.xformer.data.DynamicNumericTextTransformation;
import com.raytheon.uf.edex.bmh.xformer.data.IBoundText;
import com.raytheon.uf.edex.bmh.xformer.data.IFreeText;
import com.raytheon.uf.edex.bmh.xformer.data.ITextRuling;
import com.raytheon.uf.edex.bmh.xformer.data.ITextTransformation;
import com.raytheon.uf.edex.bmh.xformer.data.RulingFreeText;
import com.raytheon.uf.edex.bmh.xformer.data.SimpleTextTransformation;

/**
 * The Message Transformer is responsible for generating Broadcast Message(s)
 * based on a Validated Message. All generated Broadcast Message(s) will be
 * persisted and placed on a queue for other, downstream BMH components.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2014 3302       bkowal      Initial creation
 * Jul 7, 2014  3302       bkowal      Finished the SSML Document generation and the
 *                                     Broadcast Message generation.
 * Aug 26, 2014 3559       bkowal      Remove newline characters from the text and
 *                                     standardize capitalization before applying
 *                                     the transformation (at least so that EVERY
 *                                     letter in the message is not capitalized).
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MessageTransformer {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(MessageTransformer.class);

    private static final String PLATFORM_AGNOSTIC_NEWLINE_REGEX = "\\r\\n|\\r|\\n";

    private static final String SENTENCE_REGEX = "[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)";

    private static final Pattern SENTENCE_PATTERN = Pattern
            .compile(SENTENCE_REGEX);

    /* Used to retrieve the Voice and Language */
    private final MessageTypeDao messageTypeDao;

    /* Used to retrieve the dictionary. */
    private final TransmitterLanguageDao transmitterLanguageDao;

    /**
     * Constructor
     */
    public MessageTransformer() {
        messageTypeDao = new MessageTypeDao();
        transmitterLanguageDao = new TransmitterLanguageDao();
        statusHandler.info("Message Transformer Ready ...");
    }

    /**
     * Creates a collection of {@link BroadcastMsg} based on the specified
     * {@link ValidatedMessage}
     * 
     * @param message
     *            the specified {@link ValidatedMessage}
     * @return the list of broadcast messages that were generated
     * @throws Exception
     *             when the transformation fails
     */
    public List<BroadcastMsg> process(ValidatedMessage message)
            throws Exception {
        final String completionError = "Receieved an uninitialized or incomplete Validated Message to process!";

        if (message == null) {
            /* Do not send a NULL downstream. */
            throw new Exception(completionError);
        }

        /* Verify that the message is complete. */
        if (message.getInputMessage() == null) {
            throw new Exception(completionError + " Missing Input Message.");
        }

        if (message.getInputMessage().getAfosid() == null) {
            throw new Exception(completionError + " Missing Afos ID.");
        }

        if (message.getInputMessage().getContent() == null
                || message.getInputMessage().getContent().trim().isEmpty()) {
            throw new Exception(completionError + " Missing Text to Transform.");
        }

        /* Retrieve the validation message associated with the message id. */
        statusHandler.info("Transforming Message: " + message.getId() + ".");

        /*
         * Format the text. Remove extra newlines and standardize
         * capitalization.
         */
        final String formattedText = this.formatText(message.getInputMessage()
                .getContent().trim());

        /* Retrieve the message type based on afos id. */
        MessageType messageType = this.getMessageType(message.getInputMessage()
                .getAfosid(), message.getId());

        /*
         * Iterate through the destination transmitters; determine which
         * dictionary to use.
         */
        List<BroadcastMsg> generatedMessages = new LinkedList<>();
        for (TransmitterGroup group : message.getTransmitterGroups()) {
            Dictionary dictionary = this.getDictionary(group, messageType
                    .getVoice().getLanguage(), message.getId());
            BroadcastMsg msg = null;
            try {
                msg = this.transformText(message.getInputMessage(),
                        formattedText, dictionary, group, messageType);
            } catch (SSMLConversionException e) {
                StringBuilder errorString = new StringBuilder(
                        "Failed to generate a Broadcast Msg for Input Message: ");
                errorString.append(message.getId());
                if (dictionary == null) {
                    errorString.append("!");
                } else {
                    errorString.append(" with dictionary: ");
                    errorString.append(dictionary.getName());
                    errorString.append("!");
                }
                errorString.append(" Skipping ...");

                statusHandler.error(BMH_CATEGORY.XFORM_SSML_GENERATION_FAILED,
                        errorString.toString(), e);
                continue;
            }

            generatedMessages.add(msg);
        }

        if (generatedMessages.isEmpty()) {
            /*
             * Errors would have already been generated for each individual
             * broadcast msg that could not be successfully generated. So,
             * terminate the camel route.
             */
            throw new Exception(
                    "Failed to generate Broadcast Messages associated with Input Message: "
                            + message.getId() + "!");
        }

        /* Transformation complete. */
        statusHandler.info("Transformation of message: " + message.getId()
                + " was successful. Generated " + generatedMessages.size()
                + " Broadcast Message(s).");

        return generatedMessages;
    }

    /**
     * Retrieves the message type associated with the specified afosid
     * 
     * @param afosid
     *            the specified afosid
     * @param messageID
     *            id of the Validated Message for auditing purposes
     * @return the {@link MessageType} that was retrieved
     * @throws Exception
     *             when an associated {@link MessageType} cannot be found
     */
    private MessageType getMessageType(String afosid, int messageID)
            throws Exception {
        MessageType messageType = this.messageTypeDao.getByAfosId(afosid);
        if (messageType == null) {
            StringBuilder exceptionText = new StringBuilder(
                    "Unable to find a Message Type associated with afos id:");
            exceptionText.append(afosid);
            exceptionText.append("!");
            Exception exception = new Exception(exceptionText.toString());

            /*
             * Unlike the previous 2 error conditions, the expectation is that a
             * message would have been transformed at this point. So, initiate
             * the standard error handling procedure ...
             */
            statusHandler.error(BMH_CATEGORY.XFORM_MISSING_MSG_TYPE,
                    "Failed to Transform Message: SENTENCE_PUNCTUATION"
                            + messageID + "!", exception);

            /*
             * But, at the same time there is nothing to send downstream -
             * sending NULL or an empty BroadcastMsg downstream would just fail
             * at the next component. So, halt the camel route execution.
             */
            throw exception;
        }

        return messageType;
    }

    /**
     * Retrieves the dictionary associated with the specified Transmitter Group
     * and Language
     * 
     * @param group
     *            the specified {@link TransmitterGroup}
     * @param language
     *            the specified {@link Language}
     * @param messageID
     *            id of the Validated Message for auditing purposes
     * @return
     */
    private Dictionary getDictionary(TransmitterGroup group, Language language,
            int messageID) {
        TransmitterLanguagePK pk = new TransmitterLanguagePK();
        pk.setTransmitterGroup(group);
        pk.setLanguage(language);
        TransmitterLanguage lang = transmitterLanguageDao.getByID(pk);

        // lookup dictionary
        if ((lang == null) || (lang.getDictionary() == null)) {
            StringBuilder stringBuilder = new StringBuilder(
                    "No dictionary has been defined for language: ");
            stringBuilder.append(language.toString());
            stringBuilder.append(" in transmitter group: ");
            stringBuilder.append(group.getName());
            stringBuilder.append("! [ Message = ");
            stringBuilder.append(messageID);
            stringBuilder.append("]");

            statusHandler.warn(BMH_CATEGORY.XFORM_MISSING_DICTIONARY,
                    stringBuilder.toString());
            return null;
        }

        return lang.getDictionary();
    }

    /**
     * Transform the text and build a broadcast message using the provided
     * information.
     * 
     * @param inputMessage
     *            the {@link InputMessage} associated with the Validated Message
     *            that will be transformed.
     * @param dictionary
     *            the {@link Dictionary} associated with the specified
     *            {@link TransmitterGroup}
     * @param group
     *            the {@link TransmitterGroup} associated with the Validated
     *            Message; a collection of the destination transmitters
     * @param messageType
     *            the {@link MessageType} associated with the Validated Message
     *            based on afosid
     * @return the broadcast message that was built.
     * @throws SSMLConversionException
     */
    private BroadcastMsg transformText(InputMessage inputMessage,
            final String formattedContent, Dictionary dictionary,
            TransmitterGroup group, MessageType messageType)
            throws SSMLConversionException {

        /* Initially all text is Free. */
        List<ITextRuling> transformationCandidates = new LinkedList<ITextRuling>();
        transformationCandidates.add(new RulingFreeText(formattedContent));

        /* Create Transformation rules based on the dictionary. */
        List<ITextTransformation> textTransformations = new LinkedList<ITextTransformation>();
        if (dictionary != null) {
            for (Word word : dictionary.getWords()) {
                if (word.isDynamic()) {
                    textTransformations
                            .add(new DynamicNumericTextTransformation(word
                                    .getWord(), word.getSubstitute()));
                } else {
                    textTransformations.add(new SimpleTextTransformation(word
                            .getWord(), word.getSubstitute()));
                }
            }
        }

        /* Determine which text the transformations can be applied to. */
        transformationCandidates = this.setTransformations(textTransformations,
                transformationCandidates);

        /* Apply the transformations and build the SSML Document. */
        SSMLDocument ssmlDocument = this.applyTransformations(
                transformationCandidates, formattedContent);

        /* Create the Broadcast Message */
        BroadcastMsg message = new BroadcastMsg();
        /* Message Header */
        message.setTransmitterGroup(group);
        message.setInputMessage(inputMessage);

        /*
         * Static message types define their own voice based on the id of the
         * transmitter group. "Special Case" for static message types.
         */
        message.setVoice(messageType.getVoice());
        if (StaticMessageIdentifierUtil.isStaticMsgType(messageType)) {
            statusHandler
                    .info("Afos Id "
                            + inputMessage.getAfosid()
                            + " is associated with a static message type. Retrieving voice associated with transmitter language for transmitter group "
                            + group.getId() + " and language "
                            + inputMessage.getLanguage().toString() + ".");
            final TransmitterLanguagePK key = new TransmitterLanguagePK();
            key.setLanguage(inputMessage.getLanguage());
            key.setTransmitterGroup(group);
            TransmitterLanguage transmitterLanguage = this.transmitterLanguageDao
                    .getByID(key);
            if (transmitterLanguage != null) {
                message.setVoice(transmitterLanguage.getVoice());
            } else {
                statusHandler
                        .info("No transmitter language is associated with transmitter group "
                                + group.getId()
                                + " and language "
                                + inputMessage.getLanguage().toString()
                                + ". Using default voice associated with message type "
                                + inputMessage.getAfosid() + ".");
            }
        }
        message.setSsml(ssmlDocument.toSSML());

        return message;
    }

    private String formatText(String content) {
        /* First replace the new line characters. */
        content = content.replaceAll(PLATFORM_AGNOSTIC_NEWLINE_REGEX,
                StringUtils.EMPTY);

        return WordUtils.capitalizeFully(content);
    }

    /**
     * Determines which text is eligible for transformation and which text
     * should be used as is. The goal is to segregate text that a transformation
     * can be applied to so that it will be possible to guarantee that multiple
     * transformations will not be applied to the same block of text.
     * 
     * @param textTransformations
     *            a list of the available transformations
     * @param transformationCandidates
     *            a list of text that should be evaluated
     * @return a list of text that is eligible for transformation intermixed
     *         with text that should be used as is.
     */
    private List<ITextRuling> setTransformations(
            List<ITextTransformation> textTransformations,
            List<ITextRuling> transformationCandidates) {

        for (ITextTransformation textTransformation : textTransformations) {
            List<IFreeText> transformCandidatesToUpdate = new LinkedList<IFreeText>();
            for (ITextRuling candidate : transformationCandidates) {
                if (candidate instanceof IBoundText) {
                    /*
                     * a transformation will already be applied to this block of
                     * text.
                     */
                    continue;
                }

                // determine if the current rule applies.
                if (textTransformation
                        .determineTransformationApplicability((IFreeText) candidate)) {
                    transformCandidatesToUpdate.add((IFreeText) candidate);
                }
            }

            /* Attach the transformations */
            for (IFreeText freeCandidate : transformCandidatesToUpdate) {
                Deque<ITextRuling> updatedText = freeCandidate
                        .applyRuling(textTransformation);
                int index = transformationCandidates.indexOf(freeCandidate);
                transformationCandidates.remove(index);
                transformationCandidates.addAll(index, updatedText);
            }
        }

        return transformationCandidates;
    }

    /**
     * Builds the SSML Document based on the Transformed Text. Note: the final
     * version of this method will not be 'void'
     * 
     * @param transformationCandidates
     *            the transformed text
     * @param originalMessage
     *            the text associated with the original message.
     * @throws SSMLConversionException
     */
    private SSMLDocument applyTransformations(
            List<ITextRuling> transformationCandidates,
            final String originalMessage) throws SSMLConversionException {
        /* Approximate the sentence divisions in the original message. */
        List<String> approximateSentences = new LinkedList<String>();
        Matcher sentenceMatcher = SENTENCE_PATTERN.matcher(originalMessage);
        while (sentenceMatcher.find()) {
            approximateSentences.add(sentenceMatcher.group(0));
        }

        // Create the SSML Document.
        SSMLDocument ssmlDocument = new SSMLDocument();
        // Start the first sentence.
        Sentence ssmlSentence = ssmlDocument.getFactory().createSentence();

        String currentSentence = approximateSentences.remove(0);
        /*
         * Align the transformed text with the approximated sentences.
         */
        for (ITextRuling candidate : transformationCandidates) {
            if (candidate instanceof IBoundText) {
                /*
                 * Bound text is added in its entirety; however, we still want
                 * to perform the sentence alignment.
                 */
                IBoundText boundText = (IBoundText) candidate;
                ssmlSentence.getContent().addAll(
                        boundText.getTransformation().applyTransformation(
                                candidate.getText()));
            }
            sentenceMatcher = SENTENCE_PATTERN.matcher(candidate.getText());
            while (sentenceMatcher.find()) {
                String textPart = sentenceMatcher.group(0);
                if (candidate instanceof IFreeText) {
                    /*
                     * Add free text fragments to the current sentence as they
                     * are discovered.
                     */
                    ssmlSentence.getContent().add(textPart);
                }
                currentSentence = StringUtils.difference(textPart,
                        currentSentence).trim();
                if (currentSentence.isEmpty()
                        && (approximateSentences.isEmpty() == false)) {
                    ssmlDocument.getRootTag().getContent().add(ssmlSentence);
                    ssmlSentence = ssmlDocument.getFactory().createSentence();
                    currentSentence = approximateSentences.remove(0);
                }
            }
        }

        /*
         * Add the final sentence to the SSML document.
         */
        ssmlDocument.getRootTag().getContent().add(ssmlSentence);

        return ssmlDocument;
    }
}