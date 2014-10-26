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
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields.DateFieldType;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

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
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class MessageScheduleDlg extends CaveSWTDialog {
    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(MessageScheduleDlg.class);

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
    private DateTimeFields creationDTF;

    /** Effective date/time field. */
    private DateTimeFields effectiveDTF;

    /** Expiration date/time field. */
    private DateTimeFields expirationDTF;

    /** Periodicity date/time field. */
    private DateTimeFields periodicityDTF;

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
        gd.verticalIndent = 5;
        Label msgNameLbl = new Label(controlComp, SWT.RIGHT);
        msgNameLbl.setText("Message Name: ");
        msgNameLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.widthHint = 175;
        gd.verticalIndent = 5;
        msgNameTF = new Text(controlComp, SWT.BORDER);
        msgNameTF.setLayoutData(gd);

        // Message Type
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label msgTypeDescLbl = new Label(controlComp, SWT.RIGHT);
        msgTypeDescLbl.setText("Message Type: ");
        msgTypeDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.verticalIndent = 5;
        msgTypeLbl = new Label(controlComp, SWT.BORDER);
        msgTypeLbl.setLayoutData(gd);

        // Message Title
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label msgTitleDescLbl = new Label(controlComp, SWT.RIGHT);
        msgTitleDescLbl.setText("Message Title: ");
        msgTitleDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.verticalIndent = 5;
        msgTitleLbl = new Label(controlComp, SWT.BORDER);
        msgTitleLbl.setLayoutData(gd);

        // Language
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label languageDescLbl = new Label(controlComp, SWT.RIGHT);
        languageDescLbl.setText("Language: ");
        languageDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.verticalIndent = 5;
        languageLbl = new Label(controlComp, SWT.NONE);
        languageLbl.setLayoutData(gd);

        // Designation
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label designationDescLbl = new Label(controlComp, SWT.RIGHT);
        designationDescLbl.setText("Designation: ");
        designationDescLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.verticalIndent = 5;
        designationLbl = new Label(controlComp, SWT.NONE);
        designationLbl.setLayoutData(gd);

        // Emergency Override
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.verticalIndent = 5;
        Label eoLbl = new Label(controlComp, SWT.RIGHT);
        eoLbl.setText("Emergency Override: ");
        eoLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.verticalIndent = 5;
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
        gd.verticalIndent = 15;
        Label creationLbl = new Label(controlComp, SWT.CENTER);
        creationLbl.setText("Creation Date/Time\n(YYMMDDHHmm): ");
        creationLbl.setLayoutData(gd);

        creationDTF = new DateTimeFields(controlComp, dateTimeMap, false,
                false, true);

        /*
         * Effective
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.verticalIndent = 5;
        Label effectiveLbl = new Label(controlComp, SWT.CENTER);
        effectiveLbl.setText("Effective Date/Time\n(YYMMDDHHmm): ");
        effectiveLbl.setLayoutData(gd);

        effectiveDTF = new DateTimeFields(controlComp, dateTimeMap, false,
                false, true);

        /*
         * Expiration
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        gd.verticalIndent = 5;
        Label expirationLbl = new Label(controlComp, SWT.CENTER);
        expirationLbl.setText("Expiration Date/Time\n(YYMMDDHHmm): ");
        expirationLbl.setLayoutData(gd);

        expirationDTF = new DateTimeFields(controlComp, dateTimeMap, false,
                false, true);

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
        gd.verticalIndent = 10;
        defaultsGrp.setLayoutData(gd);
        defaultsGrp.setText(" Defaults: ");

        // Periodicity
        Label periodicityLbl = new Label(defaultsGrp, SWT.RIGHT);
        periodicityLbl.setText("Periodicity\n(DDHHMMSS): ");

        Map<DateFieldType, Integer> periodicityMap = null;

        periodicityMap = generateDayHourMinuteSecondMap();

        periodicityDTF = new DateTimeFields(defaultsGrp, periodicityMap, false,
                false, true);

        // Interrupt, Alert, Confirm
        int hIndent = 15;
        gd = new GridData();
        gd.horizontalIndent = hIndent;
        gd.verticalIndent = 10;
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
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 75;

        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText("OK");

        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleOkAction();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
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
        this.userInputMessage.setCreationTime(this.updateCalFromDTF(
                this.userInputMessage.getCreationTime(),
                this.creationDTF.getCalDateTimeValues()));
        this.userInputMessage.setEffectiveTime(this.updateCalFromDTF(
                this.userInputMessage.getEffectiveTime(),
                this.effectiveDTF.getCalDateTimeValues()));
        this.userInputMessage.setExpirationTime(this.updateCalFromDTF(
                this.userInputMessage.getExpirationTime(),
                this.expirationDTF.getCalDateTimeValues()));
        if ("00000000".equals(this.periodicityDTF.getFormattedValue()) == false) {
            this.userInputMessage.setPeriodicity(this.periodicityDTF
                    .getFormattedValue());
        }
        this.userInputMessage.setConfirm(this.confirmChk.getSelection());
        setReturnValue(this.userInputMessage);

        // Note: if dialog contents are not valid, the dialog will not be
        // closed.
        close();
    }

    private Calendar updateCalFromDTF(Calendar currentCal,
            Map<Integer, Integer> fieldValuesMap) {
        for (Integer calField : fieldValuesMap.keySet()) {
            currentCal.set(calField, fieldValuesMap.get(calField));
        }

        return currentCal;
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
        this.creationDTF.setDateTimeSpinners(this.userInputMessage
                .getCreationTime());
        this.effectiveDTF.setDateTimeSpinners(this.userInputMessage
                .getEffectiveTime());
        this.expirationDTF.setDateTimeSpinners(this.userInputMessage
                .getExpirationTime());
        if (this.userInputMessage.getPeriodicity() != null) {
            Map<DateFieldType, Integer> dateTimeMap = BmhUtils
                    .generateDayHourMinuteSecondMap(this.userInputMessage
                            .getPeriodicity());
            this.periodicityDTF.setFieldValues(dateTimeMap);
        }
        this.confirmChk.setSelection(this.userInputMessage.getConfirm());
    }

    /**
     * Generate map for periodicity date/time controls.
     * 
     * @return Map of date/time types and initial values.
     */
    private Map<DateFieldType, Integer> generateDayHourMinuteSecondMap() {
        Map<DateFieldType, Integer> durMap = new LinkedHashMap<DateFieldType, Integer>();

        durMap.put(DateFieldType.DAY, 0);
        durMap.put(DateFieldType.HOUR, 0);
        durMap.put(DateFieldType.MINUTE, 0);
        durMap.put(DateFieldType.SECOND, 0);

        return durMap;
    }
}
