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
package com.raytheon.uf.viz.bmh.ui.dialogs.demo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.broadcast.NewBroadcastMsgRequest;
import com.raytheon.uf.common.bmh.datamodel.PositionComparator;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterLanguage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.broadcast.BroadcastLiveDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.TransmitterLanguageDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeDataManager;
import com.raytheon.uf.viz.core.localization.LocalizationManager;

/**
 * Dialog for submitting a demo message for a particular
 * {@link TransmitterGroup}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Feb 19, 2015  4143     bsteffen    Initial creation
 * Mar 23, 2015  4309     rferrel     Alert Tone now set to false when submitting the message.
 * Mar 26, 2015  4322     rferrel     Added cancel button.
 * Mar 31, 2015  4342     bkowal      Ensure that the generated afos id matches the required
 *                                    format.
 * Mar 31, 2015  4248     rjpeter     Use PositionComparator.
 * Apr 27, 2015  4397     bkowal      Set the {@link InputMessage} update date.
 * May 06, 2015  4471     bkowal      Set the SAME Tone Flag in {@link InputMessage} to
 *                                    {@link Boolean#TRUE}.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class DemoMessageDialog extends AbstractBMHDialog {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BroadcastLiveDlg.class);

    private final Map<Language, String> defaultMessages = new EnumMap<>(
            Language.class);

    private org.eclipse.swt.widgets.List transmitterSelectionList;

    /**
     * List of groups displayed in the dialog, in the exact same order as the
     * {@link #transmitterSelectionList}
     */
    private List<TransmitterGroup> availableTransmittersList;

    private StyledText messageText;

    private Combo voiceSelectionCombo;

    /**
     * List of voices displayed in the dialog, in the exact same order as the
     * {@link #voiceSelectionCombo}
     */
    private List<TtsVoice> availableVoices;

    public DemoMessageDialog(Map<AbstractBMHDialog, String> map,
            Shell parentShell) {
        super(map, DlgInfo.DEMO_MESSAGE.getTitle(), parentShell,
                SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE, CAVE.DO_NOT_BLOCK
                        | CAVE.PERSPECTIVE_INDEPENDENT);
        defaultMessages
                .put(Language.ENGLISH,
                        "Interrupting broadcast for a demonstration of the national weather radio system.");
        defaultMessages
                .put(Language.SPANISH,
                        "La interrupción de transmisión para una demostración del sistema de radio nacional de meteorología.");
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(2, false);
    }

    @Override
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        super.initializeComponents(shell);
        this.setText(DlgInfo.DEMO_MESSAGE.getTitle());

        Composite transmitterAndVoiceComp = new Composite(shell, SWT.NONE);
        GridData gd = new GridData(SWT.NONE, SWT.FILL, false, true);
        transmitterAndVoiceComp.setLayoutData(gd);
        transmitterAndVoiceComp.setLayout(new GridLayout(1, false));
        this.createTransmitterSelection(transmitterAndVoiceComp);
        this.createVoiceSelection(transmitterAndVoiceComp);
        this.createMessageEntry();
        this.createButtons();
        this.configureListeners();

        new QueryTransmitterGroupsJob().schedule();
        populateMessageText();
    }

    protected void createTransmitterSelection(Composite parent) {
        Group tranmistterGroup = new Group(parent, SWT.SHADOW_OUT);
        tranmistterGroup.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(SWT.NONE, SWT.FILL, false, true);
        tranmistterGroup.setLayoutData(gd);
        tranmistterGroup.setText("Transmitter:");
        transmitterSelectionList = new org.eclipse.swt.widgets.List(
                tranmistterGroup, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        transmitterSelectionList.setLayoutData(gd);
    }

    protected void createVoiceSelection(Composite parent) {
        Group voiceGroup = new Group(parent, SWT.SHADOW_OUT);
        voiceGroup.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        voiceGroup.setLayoutData(gd);
        voiceGroup.setText("Voice:");
        voiceSelectionCombo = new Combo(voiceGroup, SWT.BORDER | SWT.DROP_DOWN
                | SWT.READ_ONLY);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        voiceSelectionCombo.setLayoutData(gd);
    }

    protected void createMessageEntry() {
        Group messagesGroup = new Group(this.shell, SWT.SHADOW_OUT);
        messagesGroup.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        messagesGroup.setLayoutData(gd);
        messagesGroup.setText("Message:");

        messageText = new StyledText(messagesGroup, SWT.BORDER | SWT.MULTI
                | SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 500;
        gd.heightHint = 160;
        messageText.setLayoutData(gd);
        messageText.setWordWrap(true);
        messageText.setAlwaysShowScrollBars(false);
    }

    protected void configureListeners() {
        transmitterSelectionList.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                voiceSelectionCombo.setEnabled(false);
                new QueryVoicesJob(getSelectedTransmitterGroup()).schedule();
            }

        });
        voiceSelectionCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                populateMessageText();
            }

        });
    }

    protected void populateTransmitters(List<TransmitterGroup> groups) {
        availableTransmittersList = new ArrayList<>(groups.size());
        for (TransmitterGroup group : groups) {
            if (group.isEnabled()) {
                availableTransmittersList.add(group);
                transmitterSelectionList.add(group.getName());
            }
        }
        transmitterSelectionList.select(0);
        new QueryVoicesJob(getSelectedTransmitterGroup()).schedule();
    }

    protected void populateVoices(List<TransmitterLanguage> languages) {
        TtsVoice previousSelection = getSelectedVoice();
        voiceSelectionCombo.removeAll();
        availableVoices = new ArrayList<>(languages.size());
        for (TransmitterLanguage language : languages) {
            availableVoices.add(language.getVoice());
            voiceSelectionCombo.add(language.getVoice().getVoiceName());
        }

        voiceSelectionCombo.select(0);
        if (previousSelection != null) {
            for (int index = 0; index < availableVoices.size(); index += 1) {
                if (availableVoices.get(index).getLanguage() == previousSelection
                        .getLanguage()) {
                    voiceSelectionCombo.select(index);
                }
            }
        }
        voiceSelectionCombo.setEnabled(true);
        populateMessageText();
    }

    protected void populateMessageText() {
        String currentText = messageText.getText();
        if (currentText.isEmpty()
                || defaultMessages.values().contains(currentText)) {
            TtsVoice voice = getSelectedVoice();
            Language language = Language.ENGLISH;
            if (voice != null) {
                language = voice.getLanguage();
            }
            messageText.setText(defaultMessages.get(language));
        }
    }

    protected void createButtons() {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1);
        Label sepLbl = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        sepLbl.setLayoutData(gd);

        Composite buttonComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        buttonComp.setLayout(gl);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false, 2, 1);
        buttonComp.setLayoutData(gd);

        int btnWidth = 112;
        Button submitMsgBtn = new Button(buttonComp, SWT.PUSH);
        submitMsgBtn.setText("Submit Message");
        gd = new GridData();
        gd.widthHint = btnWidth;
        submitMsgBtn.setLayoutData(gd);
        submitMsgBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSubmitAction();
            }
        });

        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        gd = new GridData();
        gd.widthHint = btnWidth;
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (okToClose()) {
                    close();
                }
            }
        });
    }

    protected TransmitterGroup getSelectedTransmitterGroup() {
        if (availableTransmittersList == null) {
            return null;
        }
        int idx = transmitterSelectionList.getSelectionIndex();
        if (idx < 0) {
            return null;
        }
        return availableTransmittersList.get(idx);
    }

    protected TtsVoice getSelectedVoice() {
        if (availableVoices == null) {
            return null;
        }
        int idx = voiceSelectionCombo.getSelectionIndex();
        if (idx < 0) {
            return null;
        }
        return availableVoices.get(idx);
    }

    protected String getMessage() {
        return messageText.getText();
    }

    protected boolean validate() {
        String failureMessage = null;
        if (getSelectedTransmitterGroup() == null) {
            failureMessage = "Please select a transmitter.";
        } else if (getSelectedVoice() == null) {
            failureMessage = "Please select a voice.";
        } else if (getMessage().isEmpty()) {
            failureMessage = "Please enter a message.";
        }
        if (failureMessage != null) {
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setText("Unable to submit message.");
            mb.setMessage("Unable to submit message. " + failureMessage);
            mb.open();
            return false;
        }
        return true;
    }

    protected void handleSubmitAction() {
        if (!validate()) {
            return;
        }
        NewBroadcastMsgRequest request = new NewBroadcastMsgRequest();
        TransmitterGroup selection = getSelectedTransmitterGroup();
        request.setSelectedTransmitters(selection.getTransmitterList());
        InputMessage inputMessage = new InputMessage();
        inputMessage.setUpdateDate(TimeUtil.newGmtCalendar());
        MessageType messageType = null;
        try {
            messageType = getMessageType(selection);
        } catch (Exception e) {
            statusHandler.handle(Priority.WARN,
                    "Failed to submit the weather message.", e);
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setText("Demo message Failure.");
            mb.setMessage("Unable to send demo message. Failed to find or create message type.");
            mb.open();
            return;
        }
        inputMessage.setName(messageType.getTitle());
        inputMessage.setLanguage(messageType.getVoice().getLanguage());
        inputMessage.setAfosid(messageType.getAfosid());
        inputMessage.setCreationTime(TimeUtil.newGmtCalendar());
        inputMessage.setEffectiveTime(TimeUtil.newGmtCalendar());
        inputMessage.setInterrupt(true);
        inputMessage.setAlertTone(false);
        inputMessage.setNwrsameTone(Boolean.TRUE);
        StringBuilder transmittersBuilder = new StringBuilder();
        for (Transmitter transmitter : selection.getTransmitters()) {
            if (transmittersBuilder.length() > 0) {
                transmittersBuilder.append("-");
            }
            transmittersBuilder.append(transmitter.getMnemonic());
        }
        inputMessage.setSameTransmitters(transmittersBuilder.toString());
        inputMessage.setSelectedTransmitters(selection.getTransmitters());
        Calendar expire = TimeUtil.newGmtCalendar();
        expire.add(Calendar.HOUR, 1);
        inputMessage.setExpirationTime(expire);
        inputMessage.setContent(getMessage());
        request.setInputMessage(inputMessage);
        try {
            BmhUtils.sendRequest(request);
        } catch (Exception e) {
            statusHandler.handle(Priority.WARN,
                    "Failed to submit the weather message.", e);
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setText("Demo message Failure.");
            mb.setMessage("Failed to submit the demo message.");
            mb.open();
            return;
        }
        MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        mb.setText("Demo message Success.");
        mb.setMessage("Successfully submited the demo message.");
        mb.open();
    }

    protected MessageType getMessageType(TransmitterGroup transmitterGroup)
            throws Exception {
        String site = LocalizationManager.getInstance().getCurrentSite();
        String afosid = this.constructAfosId(site, transmitterGroup.getName());
        MessageTypeDataManager dataManager = new MessageTypeDataManager();

        MessageType mType = dataManager.getMessageType(afosid);
        if (mType == null) {
            statusHandler.info("Creating a new message type for " + afosid);
            mType = new MessageType();
            mType.setAfosid(afosid);
            mType.setTitle("Demo message for " + transmitterGroup.getName());
            mType.setAlert(false);
            mType.setConfirm(false);
            mType.setInterrupt(true);
            mType.setDesignation(Designation.Other);
            mType.setDuration("00010000");
            mType.setPeriodicity("00000000");
            mType.setVoice(getSelectedVoice());

            mType.setSameTransmitters(transmitterGroup.getTransmitters());
            mType.setDefaultTransmitterGroups(Collections
                    .singleton(transmitterGroup));
            dataManager.saveMessageType(mType);
        } else if (mType.getVoice() != getSelectedVoice()) {
            mType.setVoice(getSelectedVoice());
            dataManager.saveMessageType(mType);
        }
        return mType;
    }

    private String constructAfosId(String site, String transmitterGroupName) {
        /*
         * the afos id format is CCCNNNXXX.
         */

        /*
         * Each element of the afos id can only be a maximum of 3 characters.
         */
        final int defaultLength = 3;
        // ensure that the afos id is a maximum of three characters. pad it if
        // necessary.
        if (site.length() > defaultLength) {
            site = site.substring(0, defaultLength);
        } else {
            site = StringUtils.rightPad(site, defaultLength);
        }

        // ensure that the transmitter group name is a maximum of three
        // characters.
        if (transmitterGroupName.length() > defaultLength) {
            transmitterGroupName = transmitterGroupName.substring(0,
                    defaultLength);
        }

        return String.format("%sDMO%s", site, transmitterGroupName);
    }

    @Override
    public boolean okToClose() {
        return true;
    }

    private class QueryTransmitterGroupsJob extends Job {

        public QueryTransmitterGroupsJob() {
            super("Retrieving Transmitter Groups");

        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                TransmitterDataManager tdm = new TransmitterDataManager();
                List<TransmitterGroup> groups = tdm
                        .getTransmitterGroups(new PositionComparator());
                getDisplay().asyncExec(
                        new PopulateTransmitterGroupsTask(groups));
            } catch (Throwable e) {
                statusHandler.error("Unable to retrieve list of transmitters.",
                        e);
            }
            return Status.OK_STATUS;
        }

    }

    private class PopulateTransmitterGroupsTask implements Runnable {

        private final List<TransmitterGroup> transmitterGroups;

        public PopulateTransmitterGroupsTask(
                List<TransmitterGroup> transmitterGroups) {
            this.transmitterGroups = transmitterGroups;
        }

        @Override
        public void run() {
            populateTransmitters(transmitterGroups);
        }

    }

    private class QueryVoicesJob extends Job {

        private final TransmitterGroup transmitterGroup;

        public QueryVoicesJob(TransmitterGroup transmitterGroup) {
            super("Retrieving Voices");
            this.transmitterGroup = transmitterGroup;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            if (transmitterGroup != null) {
                try {
                    TransmitterLanguageDataManager tldm = new TransmitterLanguageDataManager();
                    List<TransmitterLanguage> languages = tldm
                            .getLanguagesForGroup(transmitterGroup);
                    getDisplay().asyncExec(new PopulateVoicesTask(languages));
                } catch (Throwable e) {
                    statusHandler.error(
                            "Unable to retrieve list of transmitters.", e);
                }
            }
            return Status.OK_STATUS;
        }

    }

    private class PopulateVoicesTask implements Runnable {

        private final List<TransmitterLanguage> languages;

        public PopulateVoicesTask(List<TransmitterLanguage> languages) {
            this.languages = languages;
        }

        @Override
        public void run() {
            populateVoices(languages);
        }

    }

}
