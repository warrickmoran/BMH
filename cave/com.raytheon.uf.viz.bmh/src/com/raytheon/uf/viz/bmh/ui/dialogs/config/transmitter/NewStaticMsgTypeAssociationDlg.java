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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeDataManager;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Allows the user to create message types with the station id or time
 * announcement designations to associate with static message types.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 16, 2015 4213       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class NewStaticMsgTypeAssociationDlg extends CaveSWTDialog {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NewStaticMsgTypeAssociationDlg.class);

    private final MessageTypeDataManager mtdm = new MessageTypeDataManager();

    private final String DEFAULT_DURATION = "00000000";

    private final String DEFAULT_TIME_PERIODICTY = "00003000";

    private final String DEFAULT_STATION_PERIODICITY = "00010000";

    private final TtsVoice voice;

    private Text messageTypeTF;

    private Text titleTF;

    private Button stationIdOption;

    private Button timeAnnouncementOption;

    /**
     * @param parentShell
     */
    public NewStaticMsgTypeAssociationDlg(Shell parentShell,
            final TtsVoice voice) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        this.voice = voice;
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);

        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#initializeComponents(org
     * .eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {
        setText("New Message Type");

        this.createAttributesComposite();

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        this.createSaveButtons();
    }

    private void createAttributesComposite() {
        /* Create the frame. */
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        Group attributesGroup = new Group(shell, SWT.BORDER);
        attributesGroup.setText(" Attributes ");
        attributesGroup.setLayout(gl);
        attributesGroup.setLayoutData(gd);

        /* The composite for the fields. */
        gl = new GridLayout(3, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        final Composite attributesComp = new Composite(attributesGroup,
                SWT.NONE);
        attributesComp.setLayout(gl);
        attributesComp.setLayoutData(gd);

        /*
         * Message Type afosid field.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label msgTypeLabel = new Label(attributesComp, SWT.NONE);
        msgTypeLabel.setText("Message Type: ");
        msgTypeLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.widthHint = 200;
        gd.horizontalSpan = 2;
        messageTypeTF = new Text(attributesComp, SWT.BORDER);
        messageTypeTF.setLayoutData(gd);
        messageTypeTF.setTextLimit(9);

        /*
         * Message Type title field.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label titleLabel = new Label(attributesComp, SWT.NONE);
        titleLabel.setText("Title: ");
        titleLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.widthHint = 200;
        gd.horizontalSpan = 2;
        titleTF = new Text(attributesComp, SWT.BORDER);
        titleTF.setLayoutData(gd);
        titleTF.setTextLimit(40);

        /*
         * Message Type designation options.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label designationLabel = new Label(attributesComp, SWT.NONE);
        designationLabel.setText("Designation: ");
        designationLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        stationIdOption = new Button(attributesComp, SWT.RADIO);
        stationIdOption.setText(Designation.StationID.name());
        stationIdOption.setLayoutData(gd);
        stationIdOption.setSelection(true);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        timeAnnouncementOption = new Button(attributesComp, SWT.RADIO);
        timeAnnouncementOption.setText(Designation.TimeAnnouncement.name());
        timeAnnouncementOption.setLayoutData(gd);
    }

    /**
     * Create Save & Cancel buttons.
     */
    protected void createSaveButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, true));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 80;
        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button saveBtn = new Button(buttonComp, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.setLayoutData(gd);
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSaveAction();
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

    private boolean validate(MessageType mt) {
        /*
         * verify that a message designation has been selected.
         */
        if (mt.getDesignation() == null) {
            DialogUtility
                    .showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                            "Message Type - Designation",
                            "Designation is a required field. Please select a Designation.");
            return false;
        }

        /*
         * verify that an afos id has been set.
         */
        if (mt.getAfosid() == null || mt.getAfosid().isEmpty()) {
            DialogUtility
                    .showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                            "Message Type - Message Type",
                            "Message Type is a required field. Please enter a Message Type.");
            return false;
        }

        /*
         * verify that a title has been set.
         */
        if (mt.getTitle() == null || mt.getTitle().isEmpty()) {
            DialogUtility.showMessageBox(this.shell, SWT.ICON_ERROR | SWT.OK,
                    "Message Type - Title",
                    "Title is a required field. Please enter a Title.");
            return false;
        }

        /*
         * verify that the afos id is unique.
         */
        try {
            MessageType existingMessageType = this.mtdm.getMessageType(mt
                    .getAfosid());
            if (existingMessageType != null) {
                DialogUtility
                        .showMessageBox(
                                this.shell,
                                SWT.ICON_ERROR | SWT.OK,
                                "Message Type - Message Type",
                                "Message Type "
                                        + mt.getAfosid()
                                        + " already exists. Please enter a unique Message Type.");
                return false;
            }
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to determine if Message Type " + mt.getAfosid()
                            + " is unique.", e);
            return false;
        }

        return true;
    }

    private void handleSaveAction() {
        MessageType mt = this.createAssociatedMessageType();

        if (this.validate(mt) == false) {
            return;
        }

        try {
            mt = this.mtdm.saveMessageType(mt);
        } catch (Exception e) {
            statusHandler.error("Failed to save Message Type " + mt.getAfosid()
                    + ".", e);
            return;
        }

        setReturnValue(mt);
        close();
    }

    private MessageType createAssociatedMessageType() {
        MessageType mt = new MessageType();
        mt.setAfosid(this.messageTypeTF.getText().trim());
        mt.setTitle(this.titleTF.getText().trim());
        Designation mtDesignation = this.getSelectedDesignation();
        mt.setDesignation(mtDesignation);
        mt.setDuration(DEFAULT_DURATION);
        if (mtDesignation == Designation.StationID) {
            mt.setPeriodicity(DEFAULT_STATION_PERIODICITY);
        } else if (mtDesignation == Designation.TimeAnnouncement) {
            mt.setPeriodicity(DEFAULT_TIME_PERIODICTY);
        }
        mt.setVoice(this.voice);
        /*
         * Every other field will remain with its associated default, not-set
         * value.
         */

        return mt;
    }

    private Designation getSelectedDesignation() {
        if (this.timeAnnouncementOption.getSelection()) {
            return Designation.TimeAnnouncement;
        }
        if (this.stationIdOption.getSelection()) {
            return Designation.StationID;
        }

        return null;
    }
}