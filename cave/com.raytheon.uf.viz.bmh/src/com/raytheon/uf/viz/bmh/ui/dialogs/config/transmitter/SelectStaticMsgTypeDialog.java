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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.StaticMessageIdentifier;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.transmitter.StaticMessageType;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MsgTypeAfosComparator;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.SelectMessageTypeDlg;
import com.raytheon.uf.viz.bmh.ui.program.ProgramDataManager;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Extension of {@link SelectMessageTypeDlg} that limits the {@link MessageType}
 * s that can be selected to static message types that are in a suite associated
 * with a specific transmitter group. Additionally, the user can also optionally
 * view all available transmitters languages in the system.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 11, 2015 4213       bkowal     Initial creation
 * Apr 04, 2016 5504       bkowal     Fix GUI sizing issues.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class SelectStaticMsgTypeDialog extends SelectMessageTypeDlg {

    private final ProgramDataManager pdm = new ProgramDataManager();

    private TtsVoice voice;

    private ProgramSummary selectedProgram;

    private Button filterChk;

    private List<MessageType> unfilteredMessageTypes;

    private List<TableRowData> unfilteredMessageTypeRows;

    private List<MessageType> programMessageTypes;

    private List<TableRowData> programMessageTypeRows;

    /**
     * @param parentShell
     */
    public SelectStaticMsgTypeDialog(Shell parentShell,
            List<StaticMessageType> staticMessageTypes, final TtsVoice voice,
            final ProgramSummary selectedProgram) {
        super(parentShell);
        this.voice = voice;
        /*
         * Note: if the Transmitter Group does not exist yet, it will not be
         * possible to add static message types to the language associated with
         * the transmitter group yet because no programs and suites can be or
         * have been assigned to the transmitter group which automatically
         * ensures that the Transmitter Group will ignore messages of every type
         * that are sent to it.
         */
        this.selectedProgram = selectedProgram;
        if (staticMessageTypes.isEmpty() == false) {
            Set<String> existingStaticMsgAfosIds = new HashSet<>(
                    staticMessageTypes.size(), 1.0f);
            for (StaticMessageType staticMsgType : staticMessageTypes) {
                existingStaticMsgAfosIds.add(staticMsgType.getMsgTypeSummary()
                        .getAfosid());
            }
            this.setFilteredMessageTypes(existingStaticMsgAfosIds);
        }
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText("Select Message Type");

        this.createMessageTypesTable();

        this.createFilterAndNewControls();

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        this.createOkCancelButtons();

        this.populateMsgTypeTable();
    }

    private void createFilterAndNewControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(2, true));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        filterChk = new Button(controlComp, SWT.CHECK);
        filterChk.setText("Show All Types");
        filterChk.setLayoutData(gd);
        filterChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAllAction();
            }
        });

        Button newButton = new Button(controlComp, SWT.PUSH);
        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gd.minimumWidth = newButton.getDisplay().getDPI().x;
        newButton.setText("New ...");
        newButton.setLayoutData(gd);
        newButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleNewAction();
            }
        });
    }

    @Override
    protected List<MessageType> retrieveMessageTypes() throws Exception {
        MessageTypeDataManager msgTypeDataMgr = new MessageTypeDataManager();

        List<MessageType> stationIdMsgTypes = msgTypeDataMgr
                .getMessageTypesByDesignationAndLanguage(
                        StaticMessageIdentifier.stationIdDesignation,
                        this.voice.getLanguage());
        List<MessageType> timeAnnouncementMsgTypes = msgTypeDataMgr
                .getMessageTypesByDesignationAndLanguage(
                        StaticMessageIdentifier.timeDesignation,
                        this.voice.getLanguage());

        List<MessageType> allStaticMsgTypes = new ArrayList<>(
                stationIdMsgTypes.size() + timeAnnouncementMsgTypes.size());
        allStaticMsgTypes.addAll(stationIdMsgTypes);
        allStaticMsgTypes.addAll(timeAnnouncementMsgTypes);

        if (allStaticMsgTypes.isEmpty() == false) {
            Collections.sort(allStaticMsgTypes, new MsgTypeAfosComparator());
        }

        /*
         * If a program has been selected, create a filtered list of message
         * type(s) that will be returned instead of the full list.
         */
        this.unfilteredMessageTypes = allStaticMsgTypes;
        this.programMessageTypes = Collections.emptyList();
        if (this.selectedProgram == null) {
            /*
             * Only all static message types will be displayed.
             */
            this.filterChk.setSelection(true);
            this.filterChk.setEnabled(false);
            return this.unfilteredMessageTypes;
        } else {
            /*
             * Reduce the message type(s) that are initially displayed to
             * message type(s) associated with the selected program.
             */
            this.filterMessageTypesByProgram();
            return this.programMessageTypes;
        }
    }

    private void filterMessageTypesByProgram() {
        /*
         * start with an empty list. if our query below fails, no message types
         * will be displayed initially.
         */
        List<String> staticAfosIds = null;
        try {
            staticAfosIds = this.pdm
                    .getStaticAfosIdsForProgram(this.selectedProgram);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to determine the static message type(s) associated with Program: "
                            + this.selectedProgram.getName() + ".", e);
            return;
        }

        if (staticAfosIds.isEmpty()) {
            return;
        }

        this.programMessageTypes = new LinkedList<>(this.unfilteredMessageTypes);
        Iterator<MessageType> iter = this.programMessageTypes.iterator();
        while (iter.hasNext()) {
            if (staticAfosIds.contains(iter.next().getAfosid()) == false) {
                iter.remove();
            }
        }
    }

    private void handleAllAction() {
        if (this.filterChk.getSelection()) {
            /*
             * show all message types.
             */
            if (this.unfilteredMessageTypeRows == null) {
                this.unfilteredMessageTypeRows = this
                        .constructTableRowList(this.unfilteredMessageTypes);
            }
            this.replaceMsgTypeTableDataRows(this.unfilteredMessageTypeRows);
        } else {
            /*
             * show filtered message types.
             */
            if (this.programMessageTypeRows == null) {
                this.programMessageTypeRows = this
                        .constructTableRowList(this.programMessageTypes);
            }
            this.replaceMsgTypeTableDataRows(this.programMessageTypeRows);
        }
    }

    private List<TableRowData> constructTableRowList(
            List<MessageType> sourceMsgTypeList) {
        if (sourceMsgTypeList.isEmpty()) {
            return Collections.emptyList();
        }

        List<TableRowData> destinationRowsList = new LinkedList<>();
        for (MessageType mt : sourceMsgTypeList) {
            TableRowData trd = new TableRowData();

            trd.addTableCellData(new TableCellData(mt.getAfosid()));
            trd.addTableCellData(new TableCellData(mt.getTitle()));
            trd.setData(mt.getAfosid());

            destinationRowsList.add(trd);
        }

        return destinationRowsList;
    }

    private void handleNewAction() {
        NewStaticMsgTypeAssociationDlg newStaticMsgTypeDlg = new NewStaticMsgTypeAssociationDlg(
                this.shell, this.voice);
        newStaticMsgTypeDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue instanceof MessageType) {
                    handleNewMsgTypeCreated((MessageType) returnValue);
                }
            }
        });
        newStaticMsgTypeDlg.open();
    }

    private void handleNewMsgTypeCreated(MessageType messageType) {
        setReturnValue(messageType);

        close();
    }
}