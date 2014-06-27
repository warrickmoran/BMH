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
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.edex.bmh.dao.DictionaryDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.BMH_CATEGORY;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
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
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

/*
 * TODO: IN PROGRESS. This is representative of a shell implementation of the
 * Message Transformer. A final version of Word.java is required to fully finish
 * the Message Transformer.
 */
public class MessageTransformer {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(MessageTransformer.class);

    private static final String SENTENCE_REGEX = "[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)";

    private static final Pattern SENTENCE_PATTERN = Pattern
            .compile(SENTENCE_REGEX);

    /* Used to retrieve the Voice and Language */
    private final MessageTypeDao messageTypeDao;

    /* Used to retrieve the dictionary. */
    private final DictionaryDao dictionaryDao;

    /**
     * Constructor
     */
    public MessageTransformer() {
        messageTypeDao = new MessageTypeDao();
        dictionaryDao = new DictionaryDao();
        statusHandler.info("Message Transformer Ready ...");
    }

    /**
     * Creates a collection of {@link BroadcastMsg} based on the specified
     * {@link ValidatedMessage}
     * 
     * @param message
     *            the specified {@link ValidatedMessage}
     * @return TBD
     * @throws Exception
     *             when the transformation fails
     */
    public Object process(ValidatedMessage message) throws Exception {
        /* Verify that a message id has been provided. */
        if (message == null) {
            /* Do not send a NULL downstream. */
            throw new Exception(
                    "Receieved an uninitialized or incomplete Validated Message to process!");
        }

        /* Retrieve the validation message associated with the message id. */
        statusHandler.info("Transforming Message: " + message.getId() + ".");

        /* Retrieve the message type based on afos id. */
        MessageType messageType = this.getMessageType(message.getInputMessage()
                .getAfosid(), message.getId());

        /*
         * Iterate through the destination transmitters; determine which
         * dictionary to use.
         */
        for (TransmitterGroup group : message.getTransmitterGroups()) {
            Dictionary dictionary = this.getDictionary(group, messageType
                    .getVoice().getLanguage(), message.getId());
            this.transformText(message.getInputMessage(), dictionary, group,
                    messageType);
        }

        /* Transformation complete. */
        statusHandler.info("Transformation of message: " + message.getId()
                + " was successful. Generated " + "X"
                + " Broadcast Message(s).");

        return null;
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
        MessageType messageType = this.messageTypeDao.getByID(afosid);
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
                    "Failed to Transform Message: " + messageID + "!",
                    exception);

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
        /* First attempt to get the name of the dictionary. */
        if (group.getLanguages().containsKey(language) == false) {
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
        final String dictionaryName = group.getLanguages().get(language)
                .getDictionaryName();
        Dictionary dictionary = this.dictionaryDao.getByID(dictionaryName);
        if (dictionary == null) {
            StringBuilder stringBuilder = new StringBuilder(
                    "Unable to find a dictionary with the specified name: ");
            stringBuilder.append(dictionaryName);
            stringBuilder.append(" specified by transmitter group: ");
            stringBuilder.append(group.getName());
            stringBuilder.append("! [ Message = ");
            stringBuilder.append(messageID);
            stringBuilder.append("]");

            statusHandler.warn(BMH_CATEGORY.XFORM_MISSING_DICTIONARY,
                    stringBuilder.toString());
        }

        return dictionary;
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
     */
    private BroadcastMsg transformText(InputMessage inputMessage,
            Dictionary dictionary, TransmitterGroup group,
            MessageType messageType) {

        // TODO: verify that content is not empty.

        /* Initially all text is Free. */
        List<ITextRuling> transformationCandidates = new LinkedList<ITextRuling>();
        transformationCandidates.add(new RulingFreeText(inputMessage
                .getContent()));

        /* Create Transformation rules based on the dictionary. */
        List<ITextTransformation> textTransformations = new LinkedList<ITextTransformation>();
        if (dictionary != null) {
            for (Word word : dictionary.getWords()) {
                /*
                 * This is not representative of the final usage of
                 * ITextTransformation. The final usage is dependent on the
                 * final implementation of Word.java.
                 */
                textTransformations.add(new SimpleTextTransformation(word
                        .getWord()));
            }
        }

        /* Determine which text the transformations can be applied to. */
        transformationCandidates = this.setTransformations(textTransformations,
                transformationCandidates);

        /* Apply the transformations can build the SSML Document. */
        this.applyTransformations(transformationCandidates,
                inputMessage.getContent());

        /* Create the Broadcast Message - Note: Not Finished! */
        BroadcastMsg message = new BroadcastMsg();
        /* Message Header */
        message.setTransmitterGroup(group);
        message.setInputMessage(inputMessage);
        /* Message Body */
        message.setSsml(null);
        message.setVoice(messageType.getVoice());

        return message;
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
     */
    private void applyTransformations(
            List<ITextRuling> transformationCandidates,
            final String originalMessage) {
        /* Approximate the sentence divisions in the original message. */
        List<String> approximateSentences = new LinkedList<String>();
        Matcher sentenceMatcher = SENTENCE_PATTERN.matcher(originalMessage);
        while (sentenceMatcher.find()) {
            approximateSentences.add(sentenceMatcher.group(0));
        }

        // TODO: create first SSML sentence tag.
        String currentSentence = approximateSentences.remove(0);
        /*
         * Align the transformed text with the approximated sentences.
         */
        for (ITextRuling candidate : transformationCandidates) {
            // TODO: populate SSMLDocument during loop
            sentenceMatcher = SENTENCE_PATTERN.matcher(candidate.getText());
            while (sentenceMatcher.find()) {
                String textPart = sentenceMatcher.group(0);
                currentSentence = StringUtils.difference(textPart,
                        currentSentence).trim();
                if (currentSentence.isEmpty()
                        && approximateSentences.isEmpty() == false) {
                    // TODO: create new SSML sentence tag
                    currentSentence = approximateSentences.remove(0);
                }
            }
        }
    }
}