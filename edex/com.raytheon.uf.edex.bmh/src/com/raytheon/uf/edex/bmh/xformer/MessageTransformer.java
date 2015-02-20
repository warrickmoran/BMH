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

import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.TIME_MSG_TOKENS;
import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastFragment;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.ValidatedMessage.LdadStatus;
import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguagePK;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.NationalDictionaryConfigNotification;
import com.raytheon.uf.common.bmh.notify.config.TransmitterLanguageConfigNotification;
import com.raytheon.uf.common.bmh.schemas.ssml.Prosody;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLConversionException;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.Sentence;
import com.raytheon.uf.common.bmh.schemas.ssml.SpeechRateFormatter;
import com.raytheon.uf.common.time.util.ITimer;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.bmh.BMHConfigurationException;
import com.raytheon.uf.edex.bmh.dao.DictionaryDao;
import com.raytheon.uf.edex.bmh.dao.LdadConfigDao;
import com.raytheon.uf.edex.bmh.dao.MessageTypeDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterLanguageDao;
import com.raytheon.uf.edex.bmh.ldad.LdadMsg;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.staticmsg.StaticMessageIdentifierUtil;
import com.raytheon.uf.edex.bmh.staticmsg.TimeMessagesGenerator;
import com.raytheon.uf.edex.bmh.staticmsg.TimeTextFragment;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.bmh.xformer.data.DynamicNumericTextTransformation;
import com.raytheon.uf.edex.bmh.xformer.data.IBoundText;
import com.raytheon.uf.edex.bmh.xformer.data.IFreeText;
import com.raytheon.uf.edex.bmh.xformer.data.ITextRuling;
import com.raytheon.uf.edex.bmh.xformer.data.ITextTransformation;
import com.raytheon.uf.edex.bmh.xformer.data.RulingFreeText;
import com.raytheon.uf.edex.bmh.xformer.data.SimpleTextTransformation;
import com.raytheon.uf.edex.core.IContextStateProcessor;

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
 * Sep 12, 2014 3588       bsteffen    Support audio fragments.
 * Oct 2, 2014  3642       bkowal      Updated to recognize and handle static time fragments.
 * Oct 22, 2014 3747       bkowal      Set creation / update time manually.
 * Oct 27, 2014 3759       bkowal      Update to support practice mode.
 * Nov 5, 2014  3759       bkowal      Practice Mode Support for time messages.
 * Nov 19, 2014 3385       bkowal      Implemented ldad support.
 * Nov 20, 2014 3385       bkowal      Set afos id, voice number, and encoding on
 *                                     {@link LdadMsg}.
 * Dec 11, 2014 3618       bkowal      Handle tiered {@link Dictionary}(ies).
 * Dec 15, 2014 3618       bkowal      Improved {@link Dictionary} caching.
 * Dec 16, 2014 3618       bkowal      Check for empty dictionary word lists.
 * Jan 05, 2015 3651       bkowal      Use {@link IMessageLogger} to log message errors.
 * Jan 05, 2015 3618       bkowal      Do not attempt to insert {@code null} values
 *                                     into a Google {@link Table}. Merge {@link Dictionary}(ies}
 *                                     by {@link Word} regex.
 * Jan 07, 2015 3899       bkowal      Skip {@link LdadConfig}s that have been disabled.
 * Jan 07, 2015 3958       bkowal      The ldad configs returned by the dao will never be {@code null}.
 * Feb 19, 2015 4142       bkowal      Include a prosody tag with the generated SSML to influence
 *                                     the rate of speech.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class MessageTransformer implements IContextStateProcessor {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(MessageTransformer.class);

    private static final String PLATFORM_AGNOSTIC_NEWLINE_REGEX = "\\r\\n|\\r|\\n";

    private static final String SENTENCE_REGEX = "[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)";

    private static final Pattern SENTENCE_PATTERN = Pattern
            .compile(SENTENCE_REGEX);

    /* Used to retrieve the Voice and Language */
    private MessageTypeDao messageTypeDao;

    /* Used to retrieve the dictionary. */
    private TransmitterLanguageDao transmitterLanguageDao;

    /* Used to retrieve ldad configuration(s). */
    private LdadConfigDao ldadConfigDao;

    /* Used to retrieve the national dictionary */
    private DictionaryDao dictionaryDao;

    /* Used to retrieve directory paths associated with time audio. */
    private final TimeMessagesGenerator tmGenerator;

    /* Cached national dictionaries */
    private ConcurrentMap<Language, Dictionary> nationalDictionaryLanguageMap = new ConcurrentHashMap<>(
            Language.values().length, 1.0f);

    private Table<TransmitterGroup, Language, TransmitterLanguage> transmitterLanguageTableCache = HashBasedTable
            .create();

    /* Used to fullfil the message logging requirement. */
    private final IMessageLogger messageLogger;

    /**
     * Constructor
     */
    public MessageTransformer(final TimeMessagesGenerator tmGenerator,
            final IMessageLogger messageLogger) {
        this.tmGenerator = tmGenerator;
        this.messageLogger = messageLogger;
        statusHandler.info("Message Transformer Ready ...");
    }

    /**
     * Creates a collection of {@link BroadcastMsg} and, possibly,
     * {@link LdadMsg} based on the specified {@link ValidatedMessage}
     * 
     * @param message
     *            the specified {@link ValidatedMessage}
     * @return the list of {@link BroadcastMsg}s and, when applicable,
     *         {@link LdadMsg}s that were generated.
     * @throws Exception
     *             when the transformation fails
     */
    public List<Object> process(ValidatedMessage message) throws Exception {
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
        MessageType messageType = this.getMessageType(message);

        /*
         * Iterate through the destination transmitters; determine which
         * dictionary to use.
         */
        /*
         * TODO: not sure if we will leave it as an object or if we will create
         * some type of generic interface for the BroadcastMsg and LdadMsg to
         * extend?
         */
        List<Object> generatedMessages = new LinkedList<>();
        for (TransmitterGroup group : message.getTransmitterGroups()) {
            /* Get the transmitter level dictionary */
            Dictionary transmitterDictionary = null;
            synchronized (this.transmitterLanguageTableCache) {
                if (this.transmitterLanguageTableCache.get(group, messageType
                        .getVoice().getLanguage()) == null) {
                    this.cacheTransmitterLanguageInformation(group, messageType
                            .getVoice().getLanguage());
                }
                transmitterDictionary = this.transmitterLanguageTableCache.get(
                        group, messageType.getVoice().getLanguage())
                        .getDictionary();
                if (transmitterDictionary == null) {
                    StringBuilder stringBuilder = new StringBuilder(
                            "No dictionary has been defined for language: ");
                    stringBuilder.append(messageType.getVoice().getLanguage()
                            .toString());
                    stringBuilder.append(" in transmitter group: ");
                    stringBuilder.append(group.getName());
                    stringBuilder.append("! [ Message = ");
                    stringBuilder.append(message.getId());
                    stringBuilder.append("]");

                    statusHandler.warn(BMH_CATEGORY.XFORM_MISSING_DICTIONARY,
                            stringBuilder.toString());
                }
            }
            BroadcastMsg msg = null;
            try {
                msg = this.transformText(message.getInputMessage(),
                        formattedText, transmitterDictionary, group,
                        messageType);
            } catch (SSMLConversionException e) {
                StringBuilder errorString = new StringBuilder(
                        "Failed to generate a Broadcast Msg for Input Message: ");
                errorString.append(message.getId());
                if (transmitterDictionary == null) {
                    errorString.append("!");
                } else {
                    errorString.append(" with dictionary: ");
                    errorString.append(transmitterDictionary.getName());
                    errorString.append("!");
                }
                errorString.append(" Skipping ...");

                statusHandler.error(BMH_CATEGORY.XFORM_SSML_GENERATION_FAILED,
                        errorString.toString(), e);
                this.messageLogger.logError(BMH_COMPONENT.MESSAGE_TRANSFORMER,
                        BMH_ACTIVITY.SSML_GENERATION, message, e);
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

        if (message.getLdadStatus() == LdadStatus.ACCEPTED) {
            statusHandler.info("Building ldad message(s) for message: "
                    + message.getId() + "...");
            try {
                generatedMessages.addAll(this.processLdad(messageType,
                        formattedText));
            } catch (SSMLConversionException e) {
                StringBuilder errorString = new StringBuilder(
                        "Failed to generate ldad message(s) for message: ");
                errorString.append(message.getId());
                errorString.append(".");

                /*
                 * Currently the generated broadcast message(s) will still be
                 * sent through the processing routes.
                 */
                statusHandler.error(BMH_CATEGORY.XFORM_SSML_GENERATION_FAILED,
                        errorString.toString(), e);
                this.messageLogger.logError(BMH_COMPONENT.MESSAGE_TRANSFORMER,
                        BMH_ACTIVITY.SSML_GENERATION, message, e);
            }
        }

        return generatedMessages;
    }

    private List<LdadMsg> processLdad(final MessageType messageType,
            final String formattedText) throws SSMLConversionException {
        /*
         * Retrieve all ldad configuration(s) associated with the specified
         * message type.
         */
        List<LdadConfig> ldadConfigurations = this.ldadConfigDao
                .getLdadConfigsForMsgType(messageType.getAfosid());
        if (ldadConfigurations.isEmpty()) {
            /*
             * in the rare case that an ldad configuration exists during
             * validation; but, it is removed before the message is processed.
             */
            return Collections.emptyList();
        }

        statusHandler.info("Found " + ldadConfigurations.size()
                + " ldad configuration(s) for message type: "
                + messageType.getAfosid() + ".");
        List<LdadMsg> ldadMessages = new ArrayList<>(ldadConfigurations.size());

        /*
         * Generate the default case. Transformed text/ssml without any
         * dictionary.
         */
        final List<ITextRuling> transformationCandidates = new LinkedList<ITextRuling>();
        transformationCandidates.add(new RulingFreeText(formattedText));

        SSMLDocument defaultSSMLDocument = this.applyTransformations(
                new LinkedList<>(transformationCandidates), formattedText,
                SpeechRateFormatter
                        .formatSpeechRate(SpeechRateFormatter.DEFAULT_RATE));
        for (LdadConfig ldadConfig : ldadConfigurations) {
            if (ldadConfig.isEnabled() == false) {
                /**
                 * This {@link LdadConfig} is currently disabled. Skip it.
                 */
                statusHandler.info("Skipping disabled ldad configuration: "
                        + ldadConfig.getName() + " (id = " + ldadConfig.getId()
                        + ") for message type: " + messageType.getAfosid()
                        + ".");
                continue;
            }
            LdadMsg ldadMsg = new LdadMsg();
            ldadMsg.setLdadId(ldadConfig.getId());
            ldadMsg.setAfosid(messageType.getAfosid());
            ldadMsg.setVoiceNumber(ldadConfig.getVoice().getVoiceNumber());
            ldadMsg.setEncoding(ldadConfig.getEncoding());
            if (ldadConfig.getDictionary() == null
                    && ldadConfig.getSpeechRate() == SpeechRateFormatter.DEFAULT_RATE) {
                /*
                 * No dictionary or custom speech rate have been defined, use
                 * the default ssml.
                 */
                ldadMsg.setSsml(defaultSSMLDocument.toSSML());
                ldadMessages.add(ldadMsg);
                continue;
            }

            /*
             * Generate ssml based on text transformed using the specified
             * dictionary.
             */
            // Generate the transformation rules.
            List<ITextTransformation> textTransformations = this
                    .buildDictionaryTransformationList(ldadConfig
                            .getDictionary());

            // Apply the rules.
            List<ITextRuling> transformedCandidates = this.setTransformations(
                    textTransformations, new LinkedList<>(
                            transformationCandidates));

            // Generate the Transformed SSML.
            SSMLDocument ssmlDocument = this.applyTransformations(
                    transformedCandidates, formattedText, SpeechRateFormatter
                            .formatSpeechRate(ldadConfig.getSpeechRate()));

            ldadMsg.setSsml(ssmlDocument.toSSML());
            ldadMessages.add(ldadMsg);
        }

        return ldadMessages;
    }

    /**
     * Retrieves the message type associated with the specified
     * {@link ValidatedMessage} based on the associated afosid.
     * 
     * @param message
     *            the specified {@link ValidatedMessage}
     * @return the {@link MessageType} that was retrieved
     * @throws Exception
     *             when an associated {@link MessageType} cannot be found
     */
    private MessageType getMessageType(ValidatedMessage message)
            throws Exception {
        final String afosid = message.getInputMessage().getAfosid();
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
                    "Failed to Transform Message: " + message.getId() + "!",
                    exception);
            this.messageLogger.logError(BMH_COMPONENT.MESSAGE_TRANSFORMER,
                    BMH_ACTIVITY.DATA_RETRIEVAL, message, exception);

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
     * Retrieves the {@link TransmitterLanguage} associated with the specified
     * {@link TransmitterGroup} and {@link Language}. Caches the associated
     * {@link Dictionary} and Speech Rate, when applicable.
     * 
     * @param group
     * @param language
     */
    private void cacheTransmitterLanguageInformation(TransmitterGroup group,
            Language language) {
        ITimer tlCacheTimer = TimeUtil.getTimer();
        tlCacheTimer.start();

        /**
         * Attempt to retrieve the {@link TransmitterLanguage}.
         */
        TransmitterLanguagePK pk = new TransmitterLanguagePK();
        pk.setTransmitterGroup(group);
        pk.setLanguage(language);
        TransmitterLanguage lang = transmitterLanguageDao.getByID(pk);
        if (lang == null) {
            /*
             * No transmitter language was found - nothing to cache.
             */
            StringBuilder sb = new StringBuilder(
                    "No Transmitter Language was found for Transmitter Group ");
            sb.append(group.getName()).append(" and language ")
                    .append(language.toString()).append(".");
            statusHandler.info(sb.toString());
            tlCacheTimer.stop(); // not logging non-retrievals
        }

        /**
         * A {@link TransmitterLanguage} was successfully retrieved. Add it to
         * the cache.
         */
        this.transmitterLanguageTableCache.put(group, language, lang);

        tlCacheTimer.stop();
        StringBuilder sb = new StringBuilder(
                "Successfully cached the Transmitter Language for Transmitter Group ");
        sb.append(group.getName()).append(" and language ")
                .append(language.toString()).append(" in ")
                .append(TimeUtil.prettyDuration(tlCacheTimer.getElapsedTime()))
                .append(".");
        statusHandler.info(sb.toString());
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
     * @throws BMHConfigurationException
     */
    private BroadcastMsg transformText(InputMessage inputMessage,
            final String formattedContent, Dictionary dictionary,
            TransmitterGroup group, MessageType messageType)
            throws SSMLConversionException, BMHConfigurationException {

        /* Create Transformation rules based on the dictionary. */
        List<ITextTransformation> textTransformations = this.mergeDictionaries(
                inputMessage.getLanguage(), dictionary, messageType.getVoice()
                        .getDictionary());

        /*
         * Handle the static message type special case.
         */
        TtsVoice fragmentVoice = messageType.getVoice();
        TransmitterLanguage transmitterLanguage = null;
        if (StaticMessageIdentifierUtil.isStaticMsgType(messageType)) {
            statusHandler
                    .info("Afos Id "
                            + inputMessage.getAfosid()
                            + " is associated with a static message type. Retrieving associated transmitter language for transmitter group "
                            + group.getId() + " and language "
                            + inputMessage.getLanguage().toString() + ".");
            final TransmitterLanguagePK key = new TransmitterLanguagePK();
            key.setLanguage(inputMessage.getLanguage());
            key.setTransmitterGroup(group);
            transmitterLanguage = this.transmitterLanguageDao.getByID(key);
            if (transmitterLanguage == null) {
                BMHConfigurationException configException = new BMHConfigurationException(
                        "Unable to find a transmitter language associated with transmitter group "
                                + group.getId() + " and language "
                                + inputMessage.getLanguage().toString() + "!");
                this.messageLogger.logError(BMH_COMPONENT.MESSAGE_TRANSFORMER,
                        BMH_ACTIVITY.DATA_RETRIEVAL, inputMessage);
                throw configException;
            }
            fragmentVoice = transmitterLanguage.getVoice();
        }

        /*
         * Handle the Time Announcement static message type special case.
         */
        List<BroadcastFragment> broadcastFragments = new LinkedList<>();
        if (messageType.getDesignation() == Designation.TimeAnnouncement) {
            /*
             * There will be a fragment for each portion of the time message.
             */
            List<TimeTextFragment> timeFragments = StaticMessageIdentifierUtil
                    .getTimeMsgFragments(transmitterLanguage);
            int index = 0;
            for (TimeTextFragment timeFragment : timeFragments) {
                if (timeFragment.isTimePlaceholder() == false) {
                    final String formattedTimeText = this
                            .formatText(timeFragment.getText());

                    /* Initially all text is Free. */
                    List<ITextRuling> transformationCandidates = new LinkedList<ITextRuling>();
                    transformationCandidates.add(new RulingFreeText(
                            formattedTimeText));

                    /*
                     * Determine which text the transformations can be applied
                     * to.
                     */
                    transformationCandidates = this.setTransformations(
                            textTransformations, transformationCandidates);
                    /*
                     * Just a standard part of the message. Will only change
                     * when the message is changed triggering an absolute
                     * regeneration.
                     */
                    SSMLDocument ssmlDocument = this.applyTransformations(
                            transformationCandidates, formattedTimeText,
                            SpeechRateFormatter
                                    .formatSpeechRate(transmitterLanguage
                                            .getSpeechRate()));
                    BroadcastFragment fragment = new BroadcastFragment();
                    fragment.setSsml(ssmlDocument.toSSML());
                    fragment.setPosition(index);
                    broadcastFragments.add(fragment);
                    ++index;
                } else {
                    for (TIME_MSG_TOKENS token : StaticMessageIdentifierUtil.timeContentFormat) {
                        BroadcastFragment fragment = new BroadcastFragment();
                        /*
                         * Dynamic audio that will need to be swapped in based
                         * on the current time. No need to actually set text for
                         * audio synthesis because all of the time messages have
                         * been pre-generated.
                         */
                        fragment.setSsml(StringUtils.EMPTY);
                        /*
                         * Set the location of the file. Dac Transmit will be
                         * responsible for building the full path to the file
                         * based on the hour and minute at the time.
                         */
                        fragment.setOutputName(Paths.get(
                                this.tmGenerator.getTimeVoiceDirectory(
                                        fragmentVoice).toString(),
                                token.getIdentifier()).toString());
                        /*
                         * Ensure that TTS Manager skips over this fragment
                         * during synthesis. If the TTS Manager still attempts
                         * to process the message at this point, something may
                         * have happened to the pre-generated time audio files.
                         */
                        fragment.setSuccess(true);
                        fragment.setPosition(index);
                        broadcastFragments.add(fragment);
                        ++index;
                    }
                }
            }
        } else {
            /* Initially all text is Free. */
            List<ITextRuling> transformationCandidates = new LinkedList<ITextRuling>();
            transformationCandidates.add(new RulingFreeText(formattedContent));

            /* Determine which text the transformations can be applied to. */
            transformationCandidates = this.setTransformations(
                    textTransformations, transformationCandidates);

            /*
             * retrieve the cached speed rate if one is available.
             */
            final int speechRate;
            synchronized (this.transmitterLanguageTableCache) {
                if (this.transmitterLanguageTableCache.get(group,
                        inputMessage.getLanguage()) == null) {
                    this.cacheTransmitterLanguageInformation(group,
                            inputMessage.getLanguage());
                }
                speechRate = (this.transmitterLanguageTableCache.get(group,
                        inputMessage.getLanguage()) == null) ? SpeechRateFormatter.DEFAULT_RATE
                        : this.transmitterLanguageTableCache.get(group,
                                inputMessage.getLanguage()).getSpeechRate();
            }

            /*
             * There will only be one fragment for all text.
             */
            SSMLDocument ssmlDocument = this.applyTransformations(
                    transformationCandidates, formattedContent,
                    SpeechRateFormatter.formatSpeechRate(speechRate));
            BroadcastFragment fragment = new BroadcastFragment();
            fragment.setSsml(ssmlDocument.toSSML());
            broadcastFragments.add(fragment);
        }

        /* Create the Broadcast Message */
        BroadcastMsg message = new BroadcastMsg();
        final Calendar current = TimeUtil.newGmtCalendar();
        message.setCreationDate(current);
        message.setUpdateDate(current);
        /* Message Header */
        message.setTransmitterGroup(group);
        message.setInputMessage(inputMessage);
        for (BroadcastFragment fragment : broadcastFragments) {
            fragment.setVoice(fragmentVoice);
            message.addFragment(fragment);
        }
        return message;
    }

    private String formatText(String content) {
        /* First replace the new line characters. */
        content = content.replaceAll(PLATFORM_AGNOSTIC_NEWLINE_REGEX,
                StringUtils.EMPTY);

        return WordUtils.capitalizeFully(content);
    }

    /**
     * Merges the National {@link Dictionary}, the Voice {@link Dictionary}, and
     * the Transmitter {@link Dictionary} into a single {@link List} of
     * {@link ITextTransformation}s.
     * 
     * @param transmitterDictionary
     *            the Transmitter {@link Dictionary}
     * @param voiceDictionary
     *            the Voice {@link Dictionary}
     * @return the merged list of {@link ITextTransformation} rules.
     * @throws SSMLConversionException
     */
    private List<ITextTransformation> mergeDictionaries(Language language,
            final Dictionary transmitterDictionary,
            final Dictionary voiceDictionary) throws SSMLConversionException {
        ITimer dictionaryTimer = TimeUtil.getTimer();
        dictionaryTimer.start();
        final Dictionary nationalDictionary = this.nationalDictionaryLanguageMap
                .get(language);
        // no dictionaries exist?
        if (nationalDictionary == null && transmitterDictionary == null
                && voiceDictionary == null) {
            dictionaryTimer.stop();
            statusHandler.info("Successfully merged the dictionaries in "
                    + TimeUtil.prettyDuration(dictionaryTimer.getElapsedTime())
                    + ".");
            return Collections.emptyList();
        }

        Map<String, ITextTransformation> mergedDictionaryMap = new LinkedHashMap<>();
        this.mergeDictionary(nationalDictionary, mergedDictionaryMap);
        this.mergeDictionary(voiceDictionary, mergedDictionaryMap);
        this.mergeDictionary(transmitterDictionary, mergedDictionaryMap);
        if (mergedDictionaryMap.isEmpty()) {
            return Collections.emptyList();
        }

        dictionaryTimer.stop();
        statusHandler.info("Successfully merged the dictionaries in "
                + TimeUtil.prettyDuration(dictionaryTimer.getElapsedTime())
                + ".");

        return new LinkedList<ITextTransformation>(mergedDictionaryMap.values());
    }

    /**
     * Merges the {@link Word}s from the specified {@link Dictionary} into the
     * specified {@link Word} {@link Map}.
     * 
     * @param dictionary
     *            the specified {@link Dictionary}
     * @param mergedDictionaryMap
     *            the specified {@link Word} {@link Map}
     * @throws SSMLConversionException
     */
    private void mergeDictionary(final Dictionary dictionary,
            Map<String, ITextTransformation> mergedDictionaryMap)
            throws SSMLConversionException {
        if (dictionary == null || dictionary.getWords() == null) {
            return;
        }

        for (Word word : dictionary.getWords()) {
            /**
             * Regex pattern is converted to lowercase; therefore, the
             * {@link Word} will also be converted to lower-case for identifying
             * overriding rules.
             */
            mergedDictionaryMap.put(word.getWord().toLowerCase(),
                    this.buildTransformationRule(word));
        }
    }

    /**
     * Creates the text transformation rules based on the specified dictionary.
     * When no dictionary is specified, an empty list will be returned. Used for
     * building the Ldad {@link ITextTransformation} {@link List}.
     * 
     * @param dictionary
     *            the specified dictionary
     * @return a list of text transformation rules; an empty list of rules is
     *         possible.
     * @throws SSMLConversionException
     */
    private List<ITextTransformation> buildDictionaryTransformationList(
            final Dictionary dictionary) throws SSMLConversionException {
        if (dictionary == null) {
            return Collections.emptyList();
        }
        /* Create Transformation rules based on the dictionaries. */
        List<ITextTransformation> textTransformations = new LinkedList<ITextTransformation>();
        for (Word word : dictionary.getWords()) {
            textTransformations.add(this.buildTransformationRule(word));
        }

        return textTransformations;
    }

    /**
     * Constructs a {@link ITextTransformation} rule based on the specified
     * {@link Word}.
     * 
     * @param word
     *            the specified {@link Word}
     * @return the constructed {@link ITextTransformation} rule
     * @throws SSMLConversionException
     */
    private ITextTransformation buildTransformationRule(Word word)
            throws SSMLConversionException {
        if (word.isDynamic()) {
            return new DynamicNumericTextTransformation(word.getWord(),
                    word.getSubstitute());
        } else {
            return new SimpleTextTransformation(word.getWord(),
                    word.getSubstitute());
        }
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
     * @param speechRate
     *            the formatted prosody speech rate.
     * @throws SSMLConversionException
     */
    private SSMLDocument applyTransformations(
            List<ITextRuling> transformationCandidates,
            final String originalMessage, final String speechRate)
            throws SSMLConversionException {
        /* Approximate the sentence divisions in the original message. */
        List<String> approximateSentences = new LinkedList<String>();
        Matcher sentenceMatcher = SENTENCE_PATTERN.matcher(originalMessage);
        while (sentenceMatcher.find()) {
            approximateSentences.add(sentenceMatcher.group(0));
        }

        // Create the SSML Document.
        SSMLDocument ssmlDocument = new SSMLDocument();

        // Construct the prosody.
        Prosody prosody = ssmlDocument.getFactory().createProsody();
        /*
         * initialize the prosody rate to the default rate.
         */
        prosody.setRate(speechRate);

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
                    prosody.getContent().add(ssmlSentence);
                    ssmlSentence = ssmlDocument.getFactory().createSentence();
                    currentSentence = approximateSentences.remove(0);
                }
            }
        }

        /*
         * Add the final sentence to the SSML document.
         */
        prosody.getContent().add(ssmlSentence);
        ssmlDocument.getRootTag().getContent().add(prosody);

        return ssmlDocument;
    }

    /**
     * Responds to changes to the National {@link Dictionary}.
     * 
     * @param notification
     *            a notification of the {@link Dictionary} change
     */
    public void updateNationalDictionary(
            NationalDictionaryConfigNotification notification) {
        if (notification.getType() == ConfigChangeType.Update) {
            this.retrieveNationalDictionaryForLanguage(notification
                    .getLanguage());
        } else {
            /*
             * The National {@link Dictionary} no longer exists.
             */
            this.nationalDictionaryLanguageMap.remove(notification
                    .getLanguage());
        }
    }

    /**
     * Removes the cached {@link TransmitterLanguage} associated with a
     * {@link TransmitterGroup} and {@link Language} from the cache to ensure
     * that the most recent version of the {@link TransmitterLanguage} will be
     * retrieved the next time it is needed.
     * 
     * @param notification
     *            a {@link TransmitterLanguageConfigNotification} containing
     *            identifying information about the {@link TransmitterLanguage}
     *            that was updated.
     */
    public void updateTransmitterDictionary(
            TransmitterLanguageConfigNotification notification) {
        synchronized (this.transmitterLanguageTableCache) {
            this.transmitterLanguageTableCache
                    .remove(notification.getKey().getTransmitterGroup(),
                            notification.getKey().getLanguage());
        }
    }

    /**
     * Retrieves the national {@Dictionary} for every
     * {@link Language}. Used during Spring bean initialization.
     */
    private void retrieveNationalDictionaries() {
        ITimer dictionaryTimer = TimeUtil.getTimer();
        dictionaryTimer.start();
        List<Dictionary> nationalDictionaries = this.dictionaryDao
                .getNationalDictionaries();
        if (nationalDictionaries.isEmpty()) {
            statusHandler
                    .info("No National Dictionary currently exists in the system.");
            dictionaryTimer.stop();
            return;
        }
        for (Dictionary dictionary : nationalDictionaries) {
            this.nationalDictionaryLanguageMap.put(dictionary.getLanguage(),
                    dictionary);
            statusHandler.info("Using National Dictionary: "
                    + dictionary.getName() + " for Language: "
                    + dictionary.getLanguage().toString() + ".");
        }
        dictionaryTimer.stop();

        statusHandler
                .info("Successfully retrieved all national dictionaries in "
                        + TimeUtil.prettyDuration(dictionaryTimer
                                .getElapsedTime()) + ".");
    }

    /**
     * Retrieves the national {@link Dictionary} associated with the specified
     * {@link Language}. Used to retrieve an updated national {@link Dictionary}
     * for a specific {@link Language} whenever a
     * {@link NationalDictionaryConfigNotification} is received.
     * 
     * @param language
     *            the specified {@link Language}.
     */
    private void retrieveNationalDictionaryForLanguage(Language language) {
        ITimer dictionaryTimer = TimeUtil.getTimer();
        dictionaryTimer.start();
        Dictionary nationalDictionary = this.dictionaryDao
                .getNationalDictionaryForLanguage(language);
        if (nationalDictionary == null) {
            dictionaryTimer.stop();
            return;
        }

        this.nationalDictionaryLanguageMap.put(language, nationalDictionary);
        statusHandler.info("Using National Dictionary: "
                + nationalDictionary.getName() + " for Language: "
                + nationalDictionary.getLanguage().toString() + ".");
        dictionaryTimer.stop();

        statusHandler
                .info("Successfully retrieved the national dictionary for language: "
                        + language.toString()
                        + " in "
                        + TimeUtil.prettyDuration(dictionaryTimer
                                .getElapsedTime()) + ".");
    }

    /**
     * @return the messageTypeDao
     */
    public MessageTypeDao getMessageTypeDao() {
        return messageTypeDao;
    }

    /**
     * @param messageTypeDao
     *            the messageTypeDao to set
     */
    public void setMessageTypeDao(MessageTypeDao messageTypeDao) {
        this.messageTypeDao = messageTypeDao;
    }

    /**
     * @return the transmitterLanguageDao
     */
    public TransmitterLanguageDao getTransmitterLanguageDao() {
        return transmitterLanguageDao;
    }

    /**
     * @param transmitterLanguageDao
     *            the transmitterLanguageDao to set
     */
    public void setTransmitterLanguageDao(
            TransmitterLanguageDao transmitterLanguageDao) {
        this.transmitterLanguageDao = transmitterLanguageDao;
    }

    /**
     * @return the ldadConfigDao
     */
    public LdadConfigDao getLdadConfigDao() {
        return ldadConfigDao;
    }

    /**
     * @param ldadConfigDao
     *            the ldadConfigDao to set
     */
    public void setLdadConfigDao(LdadConfigDao ldadConfigDao) {
        this.ldadConfigDao = ldadConfigDao;
    }

    /**
     * @return the dictionaryDao
     */
    public DictionaryDao getDictionaryDao() {
        return dictionaryDao;
    }

    /**
     * @param dictionaryDao
     *            the dictionaryDao to set
     */
    public void setDictionaryDao(DictionaryDao dictionaryDao) {
        this.dictionaryDao = dictionaryDao;
    }

    /**
     * Validate all DAOs are set correctly and throw an exception if any are not
     * set.
     * 
     * @throws IllegalStateException
     */
    private void validateDaos() throws IllegalStateException {
        if (this.messageTypeDao == null) {
            throw new IllegalStateException(
                    "MessageTypeDao has not been set on the MessageTransformer");
        } else if (this.transmitterLanguageDao == null) {
            throw new IllegalStateException(
                    "TransmitterLanguageDao has not been set on the MessageTransformer");
        } else if (this.ldadConfigDao == null) {
            throw new IllegalStateException(
                    "LdadConfigDao has not been set on the MessageTransformer");
        } else if (this.dictionaryDao == null) {
            throw new IllegalStateException(
                    "DictionaryDao has not been set on the MessageTransformer");
        }
    }

    @Override
    public void preStart() {
        this.validateDaos();

        /* Attempt to retrieve the national dictionary. */
        this.retrieveNationalDictionaries();
    }

    @Override
    public void postStart() {
        // Do Nothing.
    }

    @Override
    public void preStop() {
        // Do Nothing.
    }

    @Override
    public void postStop() {
        // Do Nothing.
    }
}
