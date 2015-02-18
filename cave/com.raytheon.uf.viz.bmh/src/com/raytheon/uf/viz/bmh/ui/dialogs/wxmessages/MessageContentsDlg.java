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
package com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.request.InputMessageAudioData;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ImportedByUtils;
import com.raytheon.uf.viz.bmh.RecordedByUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages.RecordAction;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.ldad.LdadConfigDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages.WxMessagesContent.CONTENT_TYPE;
import com.raytheon.uf.viz.bmh.ui.recordplayback.AudioRecordPlaybackNotification;
import com.raytheon.uf.viz.bmh.ui.recordplayback.RecordPlaybackDlg;
import com.raytheon.viz.ui.dialogs.CaveSWTDialogBase;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * 
 * This creates the message contents dialog that will allow the user to select
 * text files or record audio files.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 26, 2014  #3728     lvenable     Initial creation
 * Oct 26, 2014  #3748     bkowal       Implement content interactions.
 * Nov 18, 2014  #3829     bkowal       Set content type back to TEXT when
 *                                      applicable.
 * Dec 2, 2014   #3874     bkowal       Implemented the play action.
 * Dec 09, 2014  #3909     bkowal       Use {@link RecordedByUtils}.
 * Dec 9, 2014   #3904     bkowal       Publish a {@link AudioRecordPlaybackNotification}
 *                                      prior to the start of audio playback.
 * Feb 11, 2015  #3908     bkowal       Removed the "Edit" button.
 * Feb 16, 2015  #4118     bkowal       Added an option to import audio.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class MessageContentsDlg extends CaveSWTDialogBase {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(MessageContentsDlg.class);

    private final LdadConfigDataManager ldadDataMgr = new LdadConfigDataManager();

    private final int MAX_AUDIO_SECONDS = 600;

    private final int MAX_AUDIO_BYTES = ((MAX_AUDIO_SECONDS * (int) TimeUtil.MILLIS_PER_SECOND) / 20) * 160;

    /** Message text styled text control. */
    private StyledText messageSt;

    /** Scrolled composite containing the audio information. */
    private ScrolledComposite scrolledComp;

    /**
     * Composite containing the audio information that is used with the scrolled
     * composite.
     */
    private MessageAudioComp msgAudioComp;

    /** Play button. */
    private Button playBtn;

    private TextAudioPlaybackDelegate delegate;

    /** Import message text button. */
    private Button importTextBtn;

    /** Record images. */
    private RecordImages recordImages;

    /** Record button. */
    private Button recordBtn;

    /** Import audio button. */
    private Button importAudioBtn;

    /** Audio data list. */
    private List<InputMessageAudioData> audioDataList;

    /** The original message text. **/
    private final String originalMessageContent;

    private CONTENT_TYPE contentType;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public MessageContentsDlg(Shell parentShell,
            List<InputMessageAudioData> audioDataList,
            final String existingMessageContent, final CONTENT_TYPE contentType) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL
                | SWT.RESIZE, CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);

        this.audioDataList = audioDataList;
        this.originalMessageContent = existingMessageContent;
        this.contentType = contentType;
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {

        setText("Message Contents");

        recordImages = new RecordImages(shell);

        Group messageContentsGroup = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        messageContentsGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        messageContentsGroup.setLayoutData(gd);
        messageContentsGroup.setText("Message Contents:");

        createMessageTextControl(messageContentsGroup);
        createAudioControls(messageContentsGroup);

        createRecordAudioControls();

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createOkCancelButtons();
    }

    @Override
    protected void disposed() {
        this.delegate.dispose();
    }

    /**
     * Create the styled text control that displays the message content text.
     * 
     * @param messageContentsGroup
     *            Group container.
     */
    private void createMessageTextControl(Group messageContentsGroup) {

        Label messageLbl = new Label(messageContentsGroup, SWT.NONE);
        messageLbl.setText("Message Text:");

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 600;
        gd.heightHint = 150;

        messageSt = new StyledText(messageContentsGroup, SWT.BORDER | SWT.MULTI
                | SWT.H_SCROLL | SWT.V_SCROLL);
        messageSt.setWordWrap(true);
        messageSt.setLayoutData(gd);

        Composite textActionBtnComp = new Composite(messageContentsGroup,
                SWT.NONE);
        textActionBtnComp.setLayout(new GridLayout(4, false));
        textActionBtnComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
                true, false));

        final int buttonWidth = 80;
        gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        playBtn = new Button(textActionBtnComp, SWT.PUSH);
        playBtn.setText("Play");
        playBtn.setImage(recordImages.getImage(RecordAction.PLAY));
        playBtn.setLayoutData(gd);
        playBtn.setToolTipText("Play the test message");

        gd = new GridData();
        gd.widthHint = buttonWidth;
        Button stopBtn = new Button(textActionBtnComp, SWT.PUSH);
        stopBtn.setText("Stop");
        stopBtn.setImage(this.recordImages.getImage(RecordAction.STOP));
        stopBtn.setLayoutData(gd);
        stopBtn.setToolTipText("Stop message playback");

        delegate = new TextAudioPlaybackDelegate(this.getShell(), this.playBtn,
                stopBtn, this.recordImages);

        /*
         * add text change listener and set the text now that the play button
         * exists.
         */
        messageSt.addModifyListener(delegate);
        if (this.originalMessageContent != null) {
            messageSt.setText(this.originalMessageContent);
        }

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        importTextBtn = new Button(textActionBtnComp, SWT.PUSH);
        importTextBtn.setText("Import...");
        importTextBtn.setToolTipText("Import text from file");
        importTextBtn.setLayoutData(gd);
        importTextBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (msgAudioComp.hasAudioControls()) {
                    int choice = DialogUtility
                            .showMessageBox(getShell(), SWT.ICON_QUESTION
                                    | SWT.OK | SWT.CANCEL, "Import Text",
                                    "Importing text will wipe the audio files. Do you wish to continue?");

                    if (choice == SWT.CANCEL) {
                        return;
                    }
                }

                if (handleContentsFromFileAction()) {
                    msgAudioComp.removeAllAudioControls();
                    contentType = CONTENT_TYPE.TEXT;
                }
            }
        });
    }

    /**
     * Create the scrolled composite that will contain all of the audio files.
     * 
     * @param messageContentsGroup
     *            Group container.
     */
    private void createAudioControls(Group messageContentsGroup) {
        GridData gd = new GridData();
        gd.horizontalIndent = 5;
        Label audioLbl = new Label(messageContentsGroup, SWT.NONE);
        audioLbl.setText("Audio:");
        audioLbl.setLayoutData(gd);

        Composite comp = new Composite(messageContentsGroup, SWT.NONE);
        comp.setLayout(new GridLayout(1, false));
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        comp.setLayoutData(gd);

        scrolledComp = new ScrolledComposite(comp, SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL);
        GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = 1;
        scrolledComp.setLayout(gl);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 400;
        gd.heightHint = 150;
        scrolledComp.setLayoutData(gd);

        msgAudioComp = new MessageAudioComp(scrolledComp, audioDataList,
                recordImages);

        scrolledComp.setExpandHorizontal(true);
        scrolledComp.setExpandVertical(true);
        scrolledComp.setContent(msgAudioComp);
        scrolledComp.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                scrolledComp.setMinSize(msgAudioComp.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT));
            }
        });
        scrolledComp.layout();
    }

    /**
     * Create the record audio controls.
     */
    private void createRecordAudioControls() {
        Composite audioComp = new Composite(shell, SWT.NONE);
        audioComp.setLayout(new GridLayout(3, false));
        audioComp
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label recordLbl = new Label(audioComp, SWT.NONE);
        recordLbl.setText("Record new audio with microphone:");

        GridData gd = new GridData(80, SWT.DEFAULT);
        recordBtn = new Button(audioComp, SWT.PUSH);
        recordBtn.setText("Rec");
        recordBtn.setImage(recordImages.getImage(RecordAction.RECORD));
        recordBtn.setLayoutData(gd);
        recordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder msg = new StringBuilder();

                msg.append("Recording new audio will remove the existing text and audio files.\n\n");
                msg.append("Do you wish to continue?");

                int choice = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL,
                        "Record Audio", msg.toString());

                if (choice == SWT.OK) {
                    handleRecordAction();
                }
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        gd.widthHint = 80;
        importAudioBtn = new Button(audioComp, SWT.PUSH);
        importAudioBtn.setText("Import...");
        importAudioBtn.setToolTipText("Import audio from file");
        importAudioBtn.setLayoutData(gd);
        importAudioBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder msg = new StringBuilder();

                msg.append("Importing audio will remove the existing text and audio files.\n\n");
                msg.append("Do you wish to continue?");

                int choice = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL,
                        "Import Audio", msg.toString());

                if (choice == SWT.OK) {
                    handleAudioImportAction();
                }
            }
        });
    }

    /**
     * Create the OK and Cancel buttons.
     */
    private void createOkCancelButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, true));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 80;
        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleOkayAction();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    /**
     * Handle the OK action.
     */
    private void handleOkayAction() {
        /*
         * Determine the content type.
         */
        // getText should never return null according to the JavaDoc.
        final String currentMsg = this.messageSt.getText().trim();

        if (RecordedByUtils.isMessage(currentMsg)
                || ImportedByUtils.isMessage(currentMsg)) {
            this.contentType = CONTENT_TYPE.AUDIO;
        } else {
            this.contentType = CONTENT_TYPE.TEXT;
        }

        /*
         * if the content type is audio, verify that audio actually exists.
         */
        if (this.contentType == CONTENT_TYPE.AUDIO
                && (this.msgAudioComp.getAudioDataList() == null || this.msgAudioComp
                        .getAudioDataList().isEmpty())) {
            // do not allow the user to use the content.
            DialogUtility
                    .showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                            "Weather Messages - Contents",
                            "Invalid Message Contents - Please record or import audio for the message.");
            return;
        }

        WxMessagesContent content = new WxMessagesContent(this.contentType);
        content.setText(currentMsg);
        if (this.contentType == CONTENT_TYPE.AUDIO) {
            content.setAudioDataList(this.msgAudioComp.getAudioDataList());
        }
        setReturnValue(content);

        close();
    }

    /**
     * Display a file browser and allow the user to select a message file.
     * 
     * @return True if a file was chosen and successfully read in.
     */
    private boolean handleContentsFromFileAction() {

        String[] filterNames = { "Text (*.txt)", "All Extensions (*.*)",
                "All Files (*)" };

        // These filter extensions are used to filter which files are displayed.
        String[] filterExtentions = { "*.txt", "*.*", "*" };

        FileDialog dlg = new FileDialog(shell, SWT.OPEN);
        dlg.setFilterNames(filterNames);
        dlg.setFilterExtensions(filterExtentions);
        String fn = dlg.open();
        if (fn != null) {
            File f = new File(fn);

            try {
                List<String> fileContents = Files.readAllLines(f.toPath(),
                        Charset.defaultCharset());

                if (fileContents.isEmpty()) {
                    StringBuilder msg = new StringBuilder();

                    msg.append("The file: \n");
                    msg.append(fn).append("\n");
                    msg.append("does not have any text to read in.");

                    DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING
                            | SWT.OK, "No Text Error", msg.toString());
                    return false;
                }

                StringBuilder sb = new StringBuilder();
                Iterator<String> iter = fileContents.iterator();

                while (iter.hasNext()) {
                    sb.append(iter.next());

                    if (iter.hasNext()) {
                        sb.append("\n");
                    }
                }

                messageSt.setText(sb.toString());

                return true;

            } catch (IOException e) {
                StringBuilder msg = new StringBuilder();

                msg.append("The file: \n");
                msg.append(fn).append("\n");
                msg.append("could not be read in.  The file must be an ASCII text file.");

                DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING
                        | SWT.OK, "File Read Error", msg.toString());
            }
        }
        return false;
    }

    private void handleAudioImportAction() {
        /*
         * Get a list of supported formats from the server and build a file
         * selection filter with them.
         */
        final Set<BMHAudioFormat> supportedEncodings;
        try {
            supportedEncodings = this.ldadDataMgr.getSupportedLdadEncodings();
        } catch (Exception e) {
            statusHandler.error("Failed to retrieve the supported encodings.",
                    e);
            return;
        }

        Iterator<BMHAudioFormat> formatIterator = supportedEncodings.iterator();
        /*
         * remove the mp3 audio format, if present. the default pre-compiled and
         * pre-packaged libmp3lame provides support for encoding. however, it
         * does not provide support for decoding => decoding requires royalty
         * payments: http://www.mp3licensing.com/royalty/software.html.
         */
        while (formatIterator.hasNext()) {
            if (formatIterator.next() == BMHAudioFormat.MP3) {
                formatIterator.remove();
            }
        }
        final String[] filterNames = new String[supportedEncodings.size()];
        final String[] filterExtensions = new String[supportedEncodings.size()];
        int index = 0;
        for (BMHAudioFormat format : supportedEncodings) {
            filterNames[index] = format.getName();
            filterExtensions[index] = "*" + format.getExtension();
            ++index;
        }

        FileDialog dlg = new FileDialog(shell, SWT.OPEN);
        dlg.setFilterNames(filterNames);
        dlg.setFilterExtensions(filterExtensions);
        String fn = dlg.open();
        if (fn == null) {
            /*
             * dialog closed.
             */
            return;
        }

        Path filePath = Paths.get(fn);
        if (Files.exists(filePath) == false) {
            /*
             * file does not exist.
             */
            StringBuilder sb = new StringBuilder("The specified file: ");
            sb.append(filePath.toString()).append(" does not exist.");

            DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING | SWT.OK,
                    "File Read Error", sb.toString());
            return;
        }

        /*
         * determine the input audio format based on the extension of the
         * selected file.
         */
        final String fileName = filePath.getFileName().toString();
        final String[] fileParts = fileName.split("\\.");
        final String fileExtension = (fileParts.length >= 1) ? "."
                + fileParts[fileParts.length - 1] : null;
        if (fileExtension == null
                || BMHAudioFormat.isValidExtension(fileExtension) == false) {
            StringBuilder sb = new StringBuilder(
                    "Invalid file type specified: ");
            sb.append(filePath.toString()).append(".");

            DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING | SWT.OK,
                    "File Read Error", sb.toString());
            return;
        }

        BMHAudioFormat selectedAudioFormat = BMHAudioFormat
                .lookupByExtension(fileExtension);
        /*
         * Read the file bytes.
         */
        byte[] audioData;
        try {
            audioData = Files.readAllBytes(filePath);
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Failed to read audio file: ");
            sb.append(filePath.toString()).append(".");

            DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING | SWT.OK,
                    "File Read Error", sb.toString());
            return;
        }
        // TODO: should we limit file size?

        /*
         * If the file is already a ulaw file, it will not need to be converted.
         */
        if (selectedAudioFormat != BMHAudioFormat.ULAW) {
            try {
                audioData = this.ldadDataMgr.convertAudio(selectedAudioFormat,
                        audioData);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder(
                        "Failed to convert audio in file: ");
                sb.append(filePath.toString()).append(" from format ")
                        .append(selectedAudioFormat.name());
                sb.append(" to format ").append(BMHAudioFormat.ULAW.name())
                        .append(".");

                DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING
                        | SWT.OK, "File Conversion Error", sb.toString());
                return;
            }
        }

        /*
         * Determine if the audio needs to be truncated.
         */
        int currentAudioDurationSeconds = this
                .getAudioPlaybackLengthSeconds(audioData);
        if (currentAudioDurationSeconds > MAX_AUDIO_SECONDS) {
            final byte[] truncatedAudio = Arrays.copyOf(audioData,
                    MAX_AUDIO_BYTES);
            audioData = truncatedAudio;

            /*
             * Confirm that the user approves of the audio truncation.
             */
            StringBuilder sb = new StringBuilder(
                    "The imported audio will need to be truncated from ");
            sb.append(currentAudioDurationSeconds).append(" seconds to ");
            sb.append(MAX_AUDIO_SECONDS).append(
                    " seconds. Would you like to proceed?");

            int option = DialogUtility.showMessageBox(getShell(),
                    SWT.ICON_QUESTION | SWT.YES | SWT.NO,
                    "Audio Truncation Required", sb.toString());
            if (option != SWT.YES) {
                return;
            }
        }

        /*
         * Process the audio as though it were recorded audio.
         */
        this.audioLoaded(audioData, ImportedByUtils.getMessage(filePath),
                "User Import");
    }

    /**
     * For the microphone contents, display the record/playback dialog.
     */
    private void handleRecordAction() {
        RecordPlaybackDlg recPlaybackDlg = new RecordPlaybackDlg(shell,
                MAX_AUDIO_SECONDS);
        recPlaybackDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                if (returnValue instanceof ByteBuffer == false) {
                    return;
                }

                audioLoaded(((ByteBuffer) returnValue).array(),
                        RecordedByUtils.getMessage(), "User Recording");
            }
        });
        recPlaybackDlg.open();
    }

    private void audioLoaded(byte[] messageAudio, final String messageText,
            final String displayName) {
        /*
         * if we reach this point, the user will have confirmed purging the
         * current information and the user has successfully recorded new audio.
         */
        this.playBtn.setEnabled(false);
        this.messageSt.setText(messageText);

        msgAudioComp.removeAllAudioControls();
        // create a new record to display in the audio list
        InputMessageAudioData recordedInputAudio = new InputMessageAudioData();
        recordedInputAudio.setTransmitterGroupName(displayName); // agreed.
                                                                 // rename
        // variable.
        recordedInputAudio.setSuccess(true);
        recordedInputAudio.setAudioDuration(this
                .getAudioPlaybackLengthSeconds(messageAudio));
        recordedInputAudio.setAudio(messageAudio);
        // TODO: format duration string if it end up being used.
        this.msgAudioComp.addAudioControl(recordedInputAudio);

        this.contentType = CONTENT_TYPE.AUDIO;
    }

    private int getAudioPlaybackLengthSeconds(final byte[] audio) {
        // TODO: need to centralize audio playback time calculation.
        /* calculate the duration in seconds */
        final long playbackTimeMS = audio.length / 160L * 20L;
        // swt component expects an Integer
        final int playbackTimeS = (int) playbackTimeMS / 1000;

        return playbackTimeS;
    }
}