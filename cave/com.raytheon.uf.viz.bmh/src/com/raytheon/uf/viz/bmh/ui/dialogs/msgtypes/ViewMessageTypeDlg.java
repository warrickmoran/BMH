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
package com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.Suite;
import com.raytheon.uf.common.bmh.datamodel.msg.SuiteMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.suites.SuiteDataManager;
import com.raytheon.uf.viz.bmh.ui.program.ProgramDataManager;
import com.raytheon.uf.viz.bmh.ui.program.ProgramTable;
import com.raytheon.uf.viz.bmh.ui.program.SuiteTable;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * 
 * Dialog to show the Message Type's associated Programs and Suites.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 29, 2014  #3420     lvenable     Initial creation
 * Aug 12, 2014  #3490     lvenable    Updated to view message type relationships.
 * Aug 15, 2014  #3490     lvenable    Updated to use Program and Suite data managers.
 * Aug 22, 2014  #3490     lvenable    Added resize and minimum size.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class ViewMessageTypeDlg extends CaveSWTDialog {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ViewMessageTypeDlg.class);

    /** Program table. */
    private ProgramTable programTable;

    /** Suite table. */
    private SuiteTable suiteTable;

    /** The selected message type. */
    private MessageType msgType;

    /** Programs associated with the message type. */
    List<Program> assocProgs = new ArrayList<Program>();

    /** List of Suites associate with the message type. */
    List<Suite> assocSuites = new ArrayList<Suite>();

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     */
    public ViewMessageTypeDlg(Shell parentShell, MessageType msgType) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE,
                CAVE.DO_NOT_BLOCK | CAVE.MODE_INDEPENDENT);

        this.msgType = msgType;
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 2;
        mainLayout.marginWidth = 2;

        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void disposed() {
    }

    @Override
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText("Message Type Information");

        retrieveDataFromDB();

        createMessageTypeInfoControls();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createProgramControls();
        createSuiteControls();
        createBottomButton();

        populateProgramTable();
        populateSuiteTable();
    }

    /**
     * Create the controls for the message type information.
     */
    private void createMessageTypeInfoControls() {
        Composite msgTypeInfoComp = new Composite(shell, SWT.NONE);
        msgTypeInfoComp.setLayout(new GridLayout(2, false));
        msgTypeInfoComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        Label nameLbl = new Label(msgTypeInfoComp, SWT.NONE);
        nameLbl.setText("Message Type: ");

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label messageTypeNameLbl = new Label(msgTypeInfoComp, SWT.NONE);
        messageTypeNameLbl.setText(msgType.getAfosid());
        messageTypeNameLbl.setLayoutData(gd);

        Label titleLbl = new Label(msgTypeInfoComp, SWT.NONE);
        titleLbl.setText("Title: ");

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label msgTypeTitleLbl = new Label(msgTypeInfoComp, SWT.NONE);
        msgTypeTitleLbl.setText(msgType.getTitle());
        msgTypeTitleLbl.setLayoutData(gd);
    }

    /**
     * Create program controls.
     */
    private void createProgramControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(1, false));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData();
        gd.horizontalIndent = 5;
        Label nameLbl = new Label(controlComp, SWT.NONE);
        nameLbl.setText("Associated Programs:");
        nameLbl.setLayoutData(gd);

        programTable = new ProgramTable(controlComp, 500, 100);
    }

    /**
     * Create the Suite controls.
     */
    private void createSuiteControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(1, false));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData();
        gd.verticalIndent = 10;
        gd.horizontalIndent = 5;
        Label nameLbl = new Label(controlComp, SWT.NONE);
        nameLbl.setText("Associated Suites:");
        nameLbl.setLayoutData(gd);

        suiteTable = new SuiteTable(controlComp, 500, 100);
    }

    /**
     * Create the bottom action buttons.
     */
    private void createBottomButton() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(1, false));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setText(" Close ");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Retrieve the data from the database.
     */
    private void retrieveDataFromDB() {

        // Programs and Suites
        ProgramDataManager pdm = new ProgramDataManager();
        List<Program> allProgramsArray = null;

        try {
            allProgramsArray = pdm.getProgramSuites();
        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving program data from the database: ", e);
            allProgramsArray = Collections.emptyList();
        }

        // Suites and Message Types
        SuiteDataManager sdm = new SuiteDataManager();
        List<Suite> allSuitesArray = null;

        try {
            allSuitesArray = sdm.getSuitesMsgTypes();

        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving suite data from the database: ", e);
            allSuitesArray = Collections.emptyList();
        }

        findAssocProgsSuites(allSuitesArray, allProgramsArray);
    }

    /**
     * Populate the program table.
     */
    private void populateProgramTable() {

        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Program Name");
        columnNames.add(tcd);

        TableData td = new TableData(columnNames);

        for (Program p : assocProgs) {
            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(p.getName()));
            td.addDataRow(trd);
        }

        programTable.populateTable(td);
    }

    /**
     * Populate the suite table.
     */
    private void populateSuiteTable() {
        List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
        TableColumnData tcd = new TableColumnData("Suite Name", 150);
        columnNames.add(tcd);
        tcd = new TableColumnData("Category");
        columnNames.add(tcd);

        TableData td = new TableData(columnNames);

        for (Suite s : assocSuites) {
            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(s.getName()));
            trd.addTableCellData(new TableCellData(s.getType().name()));
            td.addDataRow(trd);
        }

        suiteTable.populateTable(td);
    }

    /**
     * Find the all of the suites that contain the message type and then find
     * all of the programs that contain the suites that were found. This has to
     * be done separately because suites may not be associated to a program.
     * 
     * @param allSuitesArray
     *            List of suites.
     * @param allProgramsArray
     *            List of programs.
     */
    private void findAssocProgsSuites(List<Suite> allSuitesArray,
            List<Program> allProgramsArray) {
        // TODO : All of this could be done at the database to just return you
        // the programs and suites that this message belongs to. Just need to
        // pass the messageType to the first query and get exactly what you
        // want. Look into updating for performance.

        /*
         * Loop through all of the suites and find the ones that contain the
         * message type.
         */
        for (Suite s : allSuitesArray) {
            List<SuiteMessage> msgTypesInSuite = s.getSuiteMessages();
            for (SuiteMessage sm : msgTypesInSuite) {
                if (sm.getMsgType().getAfosid().equals(msgType.getAfosid())) {
                    assocSuites.add(s);
                    break;
                }
            }
        }

        /*
         * Loop through all of the programs and find the ones that contain the
         * associated suites that contain the message type.
         */
        ProgramFor: for (Program p : allProgramsArray) {
            List<Suite> suitesInProgram = p.getSuites();
            for (Suite progSuite : suitesInProgram) {
                for (Suite s : assocSuites) {
                    // If a Suite is found, add the program and continue to the
                    // next program.
                    if (progSuite.getId() == s.getId()) {
                        assocProgs.add(p);
                        continue ProgramFor;
                    }
                }
            }
        }
    }
}
