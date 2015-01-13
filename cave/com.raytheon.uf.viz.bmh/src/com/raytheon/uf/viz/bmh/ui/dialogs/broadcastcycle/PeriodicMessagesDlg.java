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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.MessageType;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Periodic Messages dialog. Launched from Broadcast Cycle dialog
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2014    3432    mpduff      Initial creation
 * Aug 14, 2014    3432    mpduff      Additional capabilities
 * Oct 10, 2014    3646    rferrel     Convert tableComp to GenericTable.
 * Dec 13, 2014    3843    mpduff      Implement periodic messages.
 * Jan 13, 2015    3843    bsteffen    Use playlist data to populate table correctly.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class PeriodicMessagesDlg extends CaveSWTDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PeriodicMessagesDlg.class);

    private GenericTable tableComp;

    private TableData tableData;

    /** Map of BroadcastMessage id to MessageDetailsDlg for that id */
    private final Map<Long, MessageDetailsDlg> detailsMap = new HashMap<>();

    private Button detailsBtn;

    private final String selectedTransmitterGrp;

    private final BroadcastCycleDataManager dataManager;

    private final PlaylistData playlistData;

    public PeriodicMessagesDlg(Shell parent,
            BroadcastCycleDataManager dataManager, PlaylistData playlistData,
            String selectedTransmitterGrp) {
        super(parent, CAVE.INDEPENDENT_SHELL | CAVE.PERSPECTIVE_INDEPENDENT);
        setText("Periodic Messages");
        this.dataManager = dataManager;
        this.playlistData = playlistData;
        this.selectedTransmitterGrp = selectedTransmitterGrp;
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.horizontalSpacing = 0;
        mainLayout.verticalSpacing = 0;
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createTable();
        createBottomButtons();
        populateTableData();
    }

    private void createTable() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 150;
        GridLayout gl = new GridLayout(1, false);
        gl.horizontalSpacing = 0;
        gl.marginWidth = 0;
        tableComp = new GenericTable(shell, SWT.BORDER | SWT.V_SCROLL);
        tableComp.setLayout(gl);
        tableComp.setLayoutData(gd);
    }

    public void populateTableData() {
        try {
            tableData = playlistData
                    .getPeriodicTableData(selectedTransmitterGrp);
            tableComp.populateTable(tableData);
            if (tableData.getTableRowCount() > 0) {
                detailsBtn.setEnabled(true);
            } else {
                detailsBtn.setEnabled(false);
            }
        } catch (Exception e) {
            statusHandler.error("Error accessing BMH Database.", e);
        }
    }

    private void createBottomButtons() {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        GridLayout gl = new GridLayout(2, false);
        Composite c = new Composite(shell, SWT.NONE);
        c.setLayout(gl);
        c.setLayoutData(gd);

        gd = new GridData(125, SWT.DEFAULT);
        detailsBtn = new Button(c, SWT.PUSH);
        detailsBtn.setText("Message Details...");
        detailsBtn.setLayoutData(gd);
        detailsBtn.setEnabled(false);
        detailsBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMessageDetails();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(c, SWT.PUSH);
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
     * Handler for Message Details button
     */
    private void handleMessageDetails() {
        try {
            List<TableRowData> selectionList = tableComp.getSelection();
            if (selectionList.isEmpty()) {
                return;
            }
            TableRowData selection = selectionList.get(0);
            PlaylistMessage playlistMessage = (PlaylistMessage) selection
                    .getData();
            String afosId = playlistMessage.getAfosid();
            MessageType messageType = dataManager.getMessageType(afosId);
            BroadcastMsg broadcastMsg = playlistMessage.getBroadcastMsg();
            long key = broadcastMsg.getId();
            MessageDetailsDlg dlg = detailsMap.get(key);
            if (dlg != null) {
                dlg.bringToTop();
            } else {
                MessageDetailsDlg detailsDlg = new MessageDetailsDlg(
                        getShell(), messageType, broadcastMsg);
                detailsDlg.setCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        long key = (long) returnValue;
                        detailsMap.remove(key);
                    }
                });
                detailsDlg.open();
                detailsMap.put(key, detailsDlg);
            }
        } catch (Exception e) {
            statusHandler.error("ERROR accessing BMH Database", e);
        }
    }
}
