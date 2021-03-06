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
import org.eclipse.swt.custom.StyledText;
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

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType.Designation;
import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
import com.raytheon.uf.common.bmh.request.StaticMsgValidationResult;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DateTimeFields;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.PeriodicitySelectionGroup;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Allows the user to create and update {@link StaticMessageType}s.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 11, 2015 4213       bkowal      Initial creation
 * Apr 08, 2015 4248       bkowal      Static Message Types are now only changed when the user saves
 *                                     the associated Transmitter Language.
 * Apr 28, 2015 4248       bkowal      Validate message contents before allowing people to
 *                                     confirm message alterations.
 * May 28, 2015 4490       bkowal      Use a proxy {@link StaticMessageType} for validation to
 *                                     prevent pass-by-reference overrides.
 * Jun 12, 2015 4482       rjpeter     Added DO_NOT_BLOCK.
 * Jul 06, 2015 4603       bkowal      Display a message dialog if message text fails validation.
 * Apr 05, 2016 5504       bkowal      Updates for compatibility with {@link DateTimeFields}.
 * Aug 01, 2016 5766       bkowal      Updates to support periodic cycles.
 * </pre>
 * 
 * @author bkowal
 */

public class CreateEditStaticMsgTypeDialog extends CaveSWTDialog {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateEditStaticMsgTypeDialog.class);

    private final String CREATE_TITLE = "Create Static Message Type";

    private final String EDIT_TITLE = "Edit Static Message Type";

    private final TransmitterLanguageDataManager tldm = new TransmitterLanguageDataManager();

    private final Language language;

    private final StaticMessageType staticMessageType;

    private Label txtMsg1Label;

    private StyledText txtMsg1Txt;

    private Label txtMsg2Label;

    private StyledText txtMsg2Txt;

    private PeriodicitySelectionGroup psg;

    public CreateEditStaticMsgTypeDialog(Shell parentShell,
            StaticMessageType staticMessageType, final Language language) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        this.staticMessageType = staticMessageType;
        this.language = language;
        this.setText(EDIT_TITLE);
    }

    public CreateEditStaticMsgTypeDialog(Shell parentShell,
            MessageType messageType, final Language language) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        this.staticMessageType = new StaticMessageType();
        this.staticMessageType.setMsgTypeSummary(messageType.getSummary());
        this.staticMessageType.setPeriodicity(messageType.getPeriodicity());
        this.language = language;
        this.setText(CREATE_TITLE);
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        this.createAttributeFields(shell);

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        this.createBottomButtons(shell);

        this.populateDialog();
    }

    private void createAttributeFields(final Shell shell) {
        /* Create the frame. */
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        Group attributesGroup = new Group(shell, SWT.BORDER);
        attributesGroup.setText(" Attributes ");
        attributesGroup.setLayout(gl);
        attributesGroup.setLayoutData(gd);

        /* The composite for the fields. */
        gl = new GridLayout(2, false);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        final Composite attributesComp = new Composite(attributesGroup,
                SWT.NONE);
        attributesComp.setLayout(gl);
        attributesComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label messageTypeLabel = new Label(attributesComp, SWT.NONE);
        messageTypeLabel.setText("Message Type: ");
        messageTypeLabel.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        Label afosIdLabel = new Label(attributesComp, SWT.NONE);
        afosIdLabel.setText(this.staticMessageType.getMsgTypeSummary()
                .getAfosid());
        afosIdLabel.setLayoutData(gd);

        /*
         * Text fields.
         */
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        this.txtMsg1Label = new Label(attributesComp, SWT.NONE);
        this.txtMsg1Label.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        gd.widthHint = 300;
        gd.heightHint = 65;
        this.txtMsg1Txt = new StyledText(attributesComp, SWT.BORDER | SWT.MULTI
                | SWT.V_SCROLL);
        this.txtMsg1Txt.setLayoutData(gd);
        this.txtMsg1Txt.setWordWrap(true);
        this.txtMsg1Txt.setTextLimit(StaticMessageType.MSG_LENGTH);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        this.txtMsg2Label = new Label(attributesComp, SWT.NONE);
        this.txtMsg2Label.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.verticalIndent = 5;
        gd.widthHint = 300;
        gd.heightHint = 65;
        this.txtMsg2Txt = new StyledText(attributesComp, SWT.BORDER | SWT.MULTI
                | SWT.V_SCROLL);
        this.txtMsg2Txt.setLayoutData(gd);
        this.txtMsg2Txt.setWordWrap(true);
        this.txtMsg2Txt.setTextLimit(StaticMessageType.MSG_LENGTH);

        psg = new PeriodicitySelectionGroup(attributesComp);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        psg.setLayoutData(gd);
        psg.populate(staticMessageType.getPeriodicity(),
                staticMessageType.getCycles());
    }

    private void createBottomButtons(final Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite comp = new Composite(getShell(), SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button saveUpdateBtn = new Button(comp, SWT.PUSH);
        saveUpdateBtn.setText("OK");
        saveUpdateBtn.setLayoutData(gd);
        saveUpdateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleConfirmAction();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(comp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    private void handleConfirmAction() {
        String msg1Text = null;
        String msg2Text = null;
        String periodicity = psg.getPeriodicityTime();

        // update the static message type that was being created or edited.
        msg1Text = this.txtMsg1Txt.getText().trim();
        if (this.staticMessageType.getMsgTypeSummary().getDesignation() == Designation.TimeAnnouncement) {
            msg2Text = this.txtMsg2Txt.getText().trim();
        }

        StaticMessageType validateStaticMsgType = new StaticMessageType();
        validateStaticMsgType.setMsgTypeSummary(this.staticMessageType
                .getMsgTypeSummary());
        validateStaticMsgType.setTextMsg1(msg1Text);
        validateStaticMsgType.setTextMsg2(msg2Text);

        /*
         * Verify that the message contents are allowed.
         */
        try {
            StaticMsgValidationResult result = this.tldm
                    .validateStaticMessageType(validateStaticMsgType,
                            this.language);
            if (result.isValid() == false) {
                DialogUtility.showMessageBox(shell, SWT.ICON_ERROR | SWT.OK,
                        "Validation Failed", result.getMessage());
                return;
            }
        } catch (Exception e) {
            statusHandler.error("Static Message validation has failed.", e);
            return;
        }

        this.staticMessageType.setTextMsg1(msg1Text);
        this.staticMessageType.setTextMsg2(msg2Text);
        this.staticMessageType.setPeriodicity(periodicity);
        staticMessageType.setCycles(psg.getPeriodicityCycles());

        setReturnValue(this.staticMessageType);

        close();
    }

    private void populateDialog() {
        if (this.staticMessageType.getMsgTypeSummary().getDesignation() == Designation.TimeAnnouncement) {
            this.txtMsg1Label.setText("Time Preamble: ");
            this.txtMsg2Label.setText("Time Postamble: ");
        } else {
            this.txtMsg1Label.setText("Station Id: ");
            this.txtMsg2Label.setVisible(false);
            this.txtMsg2Txt.setVisible(false);
        }

        if (this.staticMessageType.getTextMsg1() != null) {
            this.txtMsg1Txt.setText(this.staticMessageType.getTextMsg1());
        }
        if (this.staticMessageType.getTextMsg2() != null) {
            this.txtMsg2Txt.setText(this.staticMessageType.getTextMsg2());
        }
    }
}