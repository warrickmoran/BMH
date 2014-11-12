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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.ldad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeTable;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.SelectMessageTypeDlg;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * LDAD (Data Push) configuration dialog
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 11, 2014    3381    mpduff      Initial creation
 * Nov 10, 2014    3381    bkowal      Dialog redesign.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class CreateEditLdadConfigDlg extends CaveSWTDialog {

    /**
     * Data manager
     */
    private final LdadConfigDataManager dataManager = new LdadConfigDataManager();

    /**
     * Name text field
     */
    private Text nameTxt;

    /**
     * Host text field
     */
    private Text hostTxt;

    /**
     * Directory text field
     */
    private Text directoryTxt;

    /**
     * Voice selection combo box
     */
    private Combo voiceCbo;

    /**
     * Dictionary selection combo box
     */
    private Combo dictCbo;

    /**
     * Encoding selection combo box
     */
    private Combo encodingCbo;

    private Button removeMsgTypeButton;

    private MsgTypeTable selectedMsgTableComp;

    /**
     * Construtor.
     * 
     * @param parentShell
     *            The parent
     */
    public CreateEditLdadConfigDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);
        setText("Create New LDAD Configuration");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayout()
     */
    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayoutData()
     */
    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
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
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite selectionComp = new Composite(shell, SWT.NONE);
        selectionComp.setLayout(gl);
        selectionComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label hostLbl = new Label(selectionComp, SWT.NONE);
        hostLbl.setText("Configuration:");
        hostLbl.setLayoutData(gd);

        createConfigComp(shell);

        createBottomButtons();

        populateDictCombo();
    }

    /**
     * Create the configuration composite.
     * 
     * @param shell
     *            The shell
     */
    private void createConfigComp(Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite configComp = new Composite(shell, SWT.BORDER);
        configComp.setLayout(gl);
        configComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label nameLbl = new Label(configComp, SWT.NONE);
        nameLbl.setText("Name:");
        nameLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        nameTxt = new Text(configComp, SWT.BORDER);
        nameTxt.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label hostLbl = new Label(configComp, SWT.NONE);
        hostLbl.setText("Host:");
        hostLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        hostTxt = new Text(configComp, SWT.BORDER);
        hostTxt.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label directoryLbl = new Label(configComp, SWT.NONE);
        directoryLbl.setText("Directory:");
        directoryLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        directoryTxt = new Text(configComp, SWT.BORDER);
        directoryTxt.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label encodingLbl = new Label(configComp, SWT.NONE);
        encodingLbl.setText("Encoding:");
        encodingLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        encodingCbo = new Combo(configComp, SWT.SINGLE | SWT.READ_ONLY);
        encodingCbo.setLayoutData(gd);
        encodingCbo.setItems(dataManager.getEncodingOptions());
        encodingCbo.select(0);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label voiceLbl = new Label(configComp, SWT.NONE);
        voiceLbl.setText("Voice:");
        voiceLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        voiceCbo = new Combo(configComp, SWT.SINGLE | SWT.READ_ONLY);
        voiceCbo.setLayoutData(gd);
        voiceCbo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                populateDictCombo();
            }
        });

        List<TtsVoice> voices = this.dataManager.getTtsVoices();
        if (voices != null) {
            List<String> voiceList = new ArrayList<String>(voices.size());
            for (TtsVoice voice : voices) {
                voiceList.add(voice.getVoiceName());
            }

            Collections.sort(voiceList);

            voiceCbo.setItems(voiceList.toArray(new String[voiceList.size()]));
            voiceCbo.select(0);
        }

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label dictLbl = new Label(configComp, SWT.NONE);
        dictLbl.setText("Dictionary:");
        dictLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalAlignment = SWT.LEFT;
        dictCbo = new Combo(configComp, SWT.SINGLE);
        dictCbo.setLayoutData(gd);

        /* selected message types */
        this.createSelectedMsgTypesGroup();
    }

    private void createSelectedMsgTypesGroup() {
        final Group selectedMsgTypesGroup = new Group(this.shell,
                SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        selectedMsgTypesGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        selectedMsgTypesGroup.setLayoutData(gd);
        selectedMsgTypesGroup.setText("Selected Message Type(s):");

        /* Controls */
        Composite selectedMsgTypeComposite = new Composite(
                selectedMsgTypesGroup, SWT.NONE);
        gl = new GridLayout(2, false);
        selectedMsgTypeComposite.setLayout(gl);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        selectedMsgTypeComposite.setLayoutData(gd);

        /* Add Button */
        Button addMsgTypeButton = new Button(selectedMsgTypeComposite, SWT.PUSH);
        addMsgTypeButton.setText("Add...");
        gd = new GridData();
        gd.widthHint = 80;
        addMsgTypeButton.setLayoutData(gd);
        addMsgTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMsgTypeAddAction();
            }
        });

        /* Remove Button */
        removeMsgTypeButton = new Button(selectedMsgTypeComposite, SWT.PUSH);
        removeMsgTypeButton.setText("Remove...");
        gd = new GridData();
        gd.widthHint = 80;
        removeMsgTypeButton.setLayoutData(gd);
        removeMsgTypeButton.setEnabled(false);

        /* Table */
        selectedMsgTableComp = new MsgTypeTable(selectedMsgTypesGroup, 450, 100);
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>(2);
        TableColumnData tcd = new TableColumnData("Message Type", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Message Title", 250);
        columnNames.add(tcd);
        TableData selectedMsgTypeTableData = new TableData(columnNames);
        selectedMsgTableComp.populateTable(selectedMsgTypeTableData);
    }

    /**
     * Create the OK, Apply, Cancel buttons
     */
    private void createBottomButtons() {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite comp = new Composite(getShell(), SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button saveUpdateBtn = new Button(comp, SWT.PUSH);
        saveUpdateBtn.setText("Save");
        saveUpdateBtn.setLayoutData(gd);
        saveUpdateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(comp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Populate the dictionary selection combo
     */
    private void populateDictCombo() {
        // TODO: implement
    }

    private void handleMsgTypeAddAction() {
        SelectMessageTypeDlg dlg = new SelectMessageTypeDlg(this.shell, true);
        dlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                if (returnValue instanceof List<?> == false) {
                    return;
                }

                addSelectedMsgType((List<?>) returnValue);
            }
        });
        dlg.open();
    }

    private void addSelectedMsgType(List<?> messageTypes) {
        TableData tableData = this.selectedMsgTableComp.getTableData();
        for (Object object : messageTypes) {
            if (object instanceof MessageType == false) {
                continue;
            }
            MessageType messageType = (MessageType) object;
            TableRowData trd = new TableRowData();
            trd.addTableCellData(new TableCellData(messageType.getAfosid()));
            trd.addTableCellData(new TableCellData(messageType.getTitle()));
            trd.setData(messageType);
            tableData.addDataRow(trd);
        }

        this.selectedMsgTableComp.populateTable(tableData);
    }

    /**
     * Validate user's data
     * 
     * @return true if valid
     */
    private boolean valid() {
        // TODO: validate that message type(s) have been associated with the
        // ldad config.
        boolean valid = true;
        Color red = getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
        if (nameTxt.getText().trim().length() <= 0) {
            nameTxt.setBackground(red);
            valid = false;
        } else {
            nameTxt.setBackground(null);
        }

        if (hostTxt.getText().trim().length() <= 0) {
            hostTxt.setBackground(red);
            valid = false;
        } else {
            hostTxt.setBackground(null);
        }

        if (directoryTxt.getText().trim().length() <= 0) {
            directoryTxt.setBackground(red);
            valid = false;
        } else {
            directoryTxt.setBackground(null);
        }

        if (valid) {
            nameTxt.setBackground(null);
            hostTxt.setBackground(null);
            directoryTxt.setBackground(null);
        }
        return valid;
    }
}
