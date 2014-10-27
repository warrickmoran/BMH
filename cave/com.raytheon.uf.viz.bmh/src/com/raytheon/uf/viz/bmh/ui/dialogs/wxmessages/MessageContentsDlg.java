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
import java.util.Iterator;
import java.util.List;

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

import com.raytheon.uf.common.bmh.request.InputMessageAudioData;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages;
import com.raytheon.uf.viz.bmh.ui.common.utility.RecordImages.RecordAction;
import com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages.WxMessagesContent.CONTENT_TYPE;
import com.raytheon.uf.viz.bmh.ui.recordplayback.RecordPlaybackDlg;
import com.raytheon.uf.viz.core.VizApp;
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
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class MessageContentsDlg extends CaveSWTDialogBase {

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

    /** Edit button. */
    private Button editBtn;

    /** Import button. */
    private Button importBtn;

    /** Record images. */
    private RecordImages recordImages;

    /** Record button. */
    private Button recordBtn;

    /** Audio data list. */
    private List<InputMessageAudioData> audioDataList;

    /** The original message text. **/
    private final String originalMessageContent;

    private CONTENT_TYPE contentType;

    /**
     * Will always be set when the contentType is audio.
     */
    private byte[] audio;

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
        messageSt.setEditable(false);
        if (this.originalMessageContent != null) {
            messageSt.setText(this.originalMessageContent);
        }

        Composite textActionBtnComp = new Composite(messageContentsGroup,
                SWT.NONE);
        textActionBtnComp.setLayout(new GridLayout(3, false));
        textActionBtnComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
                true, false));

        int buttonWidth = 80;
        gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        playBtn = new Button(textActionBtnComp, SWT.PUSH);
        playBtn.setText("Play");
        playBtn.setImage(recordImages.getImage(RecordAction.PLAY));
        playBtn.setLayoutData(gd);
        playBtn.setToolTipText("Play the test message");
        playBtn.setEnabled(false);
        playBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO : add play code
            }
        });

        gd = new GridData();
        gd.widthHint = buttonWidth;
        editBtn = new Button(textActionBtnComp, SWT.PUSH);
        editBtn.setText("Edit");
        editBtn.setLayoutData(gd);
        editBtn.setToolTipText("Enable editing of the message text");
        editBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder msg = new StringBuilder();

                msg.append("Editing the text will remove the audio files.\n\n");
                msg.append("Do you wish to continue?");

                int choice = DialogUtility.showMessageBox(getShell(),
                        SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL, "Edit Text",
                        msg.toString());

                if (choice == SWT.OK) {
                    messageSt.setEditable(true);
                    msgAudioComp.removeAllAudioControls();
                }
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        importBtn = new Button(textActionBtnComp, SWT.PUSH);
        importBtn.setText("Import...");
        importBtn.setToolTipText("Import text from file");
        importBtn.setLayoutData(gd);
        importBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                if (msgAudioComp.hasAudioControls()
                        || messageSt.getEditable() == false) {
                    StringBuilder msg = new StringBuilder();

                    if (msgAudioComp.hasAudioControls()) {
                        msg.append("Importing text will wipe the audio files and enable text editing.\n");
                    } else if (messageSt.getEditable() == false) {
                        msg.append("Importing text will enable text editing.\n\n");
                    }

                    msg.append("Do you wish to continue?");

                    int choice = DialogUtility.showMessageBox(getShell(),
                            SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL,
                            "Import Text", msg.toString());

                    if (choice == SWT.CANCEL) {
                        return;
                    }
                }

                if (handleContentsFromFileAction()) {
                    messageSt.setEditable(true);
                    playBtn.setEnabled(true);
                    msgAudioComp.removeAllAudioControls();
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
        audioComp.setLayout(new GridLayout(2, true));
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
        // TODO : is validation needed?
        WxMessagesContent content = new WxMessagesContent(this.contentType);
        content.setText(this.messageSt.getText());
        content.setAudio(this.audio);
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

                if (sb.toString().length() > 0) {
                    playBtn.setEnabled(true);
                }

                return true;

            } catch (IOException e) {
                // statusHandler.error("Error reading data from file: " + fn
                // + " --- ", e);
                //
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

    /**
     * For the microphone contents, display the record/playback dialog.
     */
    private void handleRecordAction() {
        RecordPlaybackDlg recPlaybackDlg = new RecordPlaybackDlg(shell, 600);
        recPlaybackDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                if (returnValue instanceof ByteBuffer == false) {
                    return;
                }

                audioRecorded(((ByteBuffer) returnValue).array());
            }
        });
        recPlaybackDlg.open();
    }

    private void audioRecorded(byte[] messageAudio) {
        /*
         * if we reach this point, the user will have confirmed purging the
         * current information and the user has successfully recorded new audio.
         */
        StringBuilder recordedByMsg = new StringBuilder("Recorded by ");
        recordedByMsg.append(VizApp.getWsId().getUserName());
        recordedByMsg.append(" on ");
        // TODO: apply date/time formatting.
        recordedByMsg.append(TimeUtil.newCalendar().getTime().toString());
        recordedByMsg.append(".");
        this.playBtn.setEnabled(false);
        this.messageSt.setText(recordedByMsg.toString());
        this.messageSt.setEditable(false);

        msgAudioComp.removeAllAudioControls();
        // create a new record to display in the audio list
        InputMessageAudioData recordedInputAudio = new InputMessageAudioData();
        recordedInputAudio.setTransmitterGroupName("User Recording"); // agreed.
                                                                      // rename
        // variable.
        recordedInputAudio.setSuccess(true);
        // TODO: need to centralize audio playback time calculation.
        /* calculate the duration in seconds */
        final long playbackTimeMS = messageAudio.length / 160L * 20L;
        // swt component expects an Integer
        final int playbackTimeS = (int) playbackTimeMS / 1000;
        recordedInputAudio.setAudioDuration(playbackTimeS);
        recordedInputAudio.setAudio(messageAudio);
        this.audio = messageAudio;
        // TODO: format duration string if it end up being used.
        this.msgAudioComp.addAudioControl(recordedInputAudio);

        this.contentType = CONTENT_TYPE.AUDIO;
    }
}