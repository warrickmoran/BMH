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
package com.raytheon.uf.viz.bmh.ui.dialogs.emergencyoverride;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

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

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields.DateFieldType;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.PeriodicitySelectionGroup;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.widgets.DateTimeSpinner;

/**
 * Message Schedule dialog for the Emergency Override.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 5, 2014   #3700     lvenable     Initial creation
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 26, 2014  #3712     bkowal       Updated to populate dialog based on an InputMessage.
 *                                      Implemented Ok and Cancel.
 * Apr 05, 2016  #5504     bkowal       Fix GUI sizing issues. Updates for compatibility with 
 *                                      {@link DateTimeFields}.
 * May 04, 2016  #5602     bkowal       Use {@link DateTimeSpinner}.
 * Aug 11, 2016  #5766     bkowal       Use {@link PeriodicitySelectionGroup}.
 * 
 * </pre>
 * 
 * @author lvenable
 */

public class MessageScheduleDlg extends CaveSWTDialog {

    /** Message name. */
    private Text msgNameTF;

    /** Message Type label. */
    private Label msgTypeLbl;

    /** Message Title label. */
    private Label msgTitleLbl;

    /** Languages label. */
    private Label languageLbl;

    /** Designation label. */
    private Label designationLbl;

    /** Emergency Override label. */
    private Label emergenyOverrideLbl;

    /** Create date/time field. */
    private DateTimeSpinner creationDateTimeSpinner;

    /** Effective date/time field. */
    private DateTimeSpinner effectiveDateTimeSpinner;

    /** Expiration date/time field. */
    private DateTimeSpinner expirationDateTimeSpinner;

    private PeriodicitySelectionGroup psg;

    /** Confirm check box. */
    private Button confirmChk;

    /** Input message generated for and editable by the user. */
    private final InputMessage userInputMessage;

    private final MessageType selectedMessageType;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public MessageScheduleDlg(Shell parentShell, InputMessage userInputMessage,
            MessageType selectedMessageType) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        this.userInputMessage = userInputMessage;
        this.selectedMessageType = selectedMessageType;
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

    @Override
    protected void initializeComponents(Shell shell) {
        setText("Emergency Override - Message Schedule");
        createMainControls();
    }

    /**
     * Create the main control composite.
     */
    private void createMainControls() {
        Composite mainControlComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        mainControlComp.setLayout(gl);
        mainControlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        createMessageTypeControls(mainControlComp);
        createDefaultContentControls(mainControlComp);
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createBottomActionButtons();
        this.initialDialogPopulate();
    }

    /**
     * Create the message type controls.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createMessageTypeControls(Composite mainComp) {
        Composite controlComp = new Composite(mainComp, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        controlComp.setLayout(gl);
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        // Message Name
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, true);
        Label msgNameLbl = new Label(controlComp, SWT.RIGHT);
        msgNameLbl.setText("Message Name:");
        msgNameLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        msgNameTF = new Text(controlComp, SWT.BORDER);
        msgNameTF.setLayoutData(gd);

        // Message Type
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        Label msgTypeDescLbl = new Label(controlComp, SWT.RIGHT);
        msgTypeDescLbl.setText("Message Type:");
        msgTypeDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        msgTypeLbl = new Label(controlComp, SWT.BORDER);
        msgTypeLbl.setLayoutData(gd);

        // Message Title
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        Label msgTitleDescLbl = new Label(controlComp, SWT.RIGHT);
        msgTitleDescLbl.setText("Message Title:");
        msgTitleDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        msgTitleLbl = new Label(controlComp, SWT.BORDER);
        msgTitleLbl.setLayoutData(gd);

        // Language
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        Label languageDescLbl = new Label(controlComp, SWT.RIGHT);
        languageDescLbl.setText("Language:");
        languageDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        languageLbl = new Label(controlComp, SWT.NONE);
        languageLbl.setLayoutData(gd);

        // Designation
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        Label designationDescLbl = new Label(controlComp, SWT.RIGHT);
        designationDescLbl.setText("Designation:");
        designationDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        designationLbl = new Label(controlComp, SWT.NONE);
        designationLbl.setLayoutData(gd);

        // Emergency Override
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        Label eoLbl = new Label(controlComp, SWT.RIGHT);
        eoLbl.setText("Emergency Override: ");
        eoLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        emergenyOverrideLbl = new Label(controlComp, SWT.NONE);
        emergenyOverrideLbl.setLayoutData(gd);

        /*
         * Creation, Effective, Expiration Dates
         */

        Map<DateFieldType, Integer> dateTimeMap = new LinkedHashMap<DateFieldType, Integer>();
        dateTimeMap.put(DateFieldType.YEAR, null);
        dateTimeMap.put(DateFieldType.MONTH, null);
        dateTimeMap.put(DateFieldType.DAY, null);
        dateTimeMap.put(DateFieldType.HOUR, null);
        dateTimeMap.put(DateFieldType.MINUTE, null);

        /*
         * Creation
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label creationLbl = new Label(controlComp, SWT.CENTER);
        creationLbl.setText("Creation Date/Time\n(YYMMDDHHmm): ");
        creationLbl.setLayoutData(gd);

        Calendar defaultDateTime = TimeUtil.newGmtCalendar();
        creationDateTimeSpinner = new DateTimeSpinner(controlComp,
                defaultDateTime, 5, true);

        /*
         * Effective
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label effectiveLbl = new Label(controlComp, SWT.CENTER);
        effectiveLbl.setText("Effective Date/Time\n(YYMMDDHHmm): ");
        effectiveLbl.setLayoutData(gd);

        effectiveDateTimeSpinner = new DateTimeSpinner(controlComp,
                defaultDateTime, 5, true);

        /*
         * Expiration
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        Label expirationLbl = new Label(controlComp, SWT.CENTER);
        expirationLbl.setText("Expiration Date/Time\n(YYMMDDHHmm): ");
        expirationLbl.setLayoutData(gd);

        expirationDateTimeSpinner = new DateTimeSpinner(controlComp,
                defaultDateTime, 5, true);
    }

    /**
     * Create the default content controls.
     * 
     * @param mainComp
     *            Main composite.
     */
    private void createDefaultContentControls(Composite mainComp) {

        /*
         * Defaults Group
         */
        Group defaultsGrp = new Group(mainComp, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(2, false);
        defaultsGrp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        defaultsGrp.setLayoutData(gd);
        defaultsGrp.setText(" Defaults: ");

        // Periodicity
        psg = new PeriodicitySelectionGroup(defaultsGrp);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        psg.setLayoutData(gd);
        psg.populate(null, null);

        // Interrupt, Alert, Confirm
        gd = new GridData();
        gd.horizontalSpan = 2;
        confirmChk = new Button(defaultsGrp, SWT.CHECK);
        confirmChk.setText("Confirm");
        confirmChk.setLayoutData(gd);
    }

    /**
     * Create the save/create & cancel actions buttons.
     */
    private void createBottomActionButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, true));
        buttonComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false));

        final int minimumButtonWidth = buttonComp.getDisplay().getDPI().x;

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = minimumButtonWidth;
        Button okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText("OK");

        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleOkAction();
            }
        });

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = minimumButtonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText(" Cancel ");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    private void handleOkAction() {
        /*
         * Update the input message based on the dialog contents.
         */

        // TODO: validate fields - date / time spinners self validate.

        this.userInputMessage.setName(this.msgNameTF.getText());
        userInputMessage
                .setCreationTime(creationDateTimeSpinner.getSelection());
        userInputMessage.setEffectiveTime(effectiveDateTimeSpinner
                .getSelection());
        userInputMessage.setExpirationTime(expirationDateTimeSpinner
                .getSelection());
        if (!"00000000".equals(psg.getPeriodicityTime())) {
            this.userInputMessage.setPeriodicity(psg.getPeriodicityTime());
        }
        userInputMessage.setCycles(psg.getPeriodicityCycles());
        this.userInputMessage.setConfirm(this.confirmChk.getSelection());
        setReturnValue(this.userInputMessage);

        // Note: if dialog contents are not valid, the dialog will not be
        // closed.
        close();
    }

    /**
     * Populates the dialog fields based on the specified {@link InputMessage}.
     */
    private void initialDialogPopulate() {
        this.msgNameTF.setText(this.userInputMessage.getName());
        this.msgTypeLbl.setText(this.userInputMessage.getAfosid());
        this.msgTitleLbl.setText(this.selectedMessageType.getTitle());
        this.languageLbl
                .setText(this.userInputMessage.getLanguage().toString());
        this.designationLbl.setText(this.selectedMessageType.getDesignation()
                .name());
        this.emergenyOverrideLbl.setText(this.selectedMessageType
                .isEmergencyOverride() ? "YES" : "NO");
        creationDateTimeSpinner
                .setSelection(userInputMessage.getCreationTime());
        effectiveDateTimeSpinner.setSelection(userInputMessage
                .getEffectiveTime());
        expirationDateTimeSpinner.setSelection(userInputMessage
                .getExpirationTime());
        psg.populate(userInputMessage.getPeriodicity(),
                userInputMessage.getCycles());
        this.confirmChk.setSelection(this.userInputMessage.getConfirm());
    }
}
