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
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
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
 * Jun 12, 2015    4482    rjpeter     Added DO_NOT_BLOCK.
 * Mar 10, 2016    5465    tgurney     Add missing trim button style
 * Apr 05, 2016    5504    bkowal      Fix GUI sizing issues.
 * May 13, 2016    5465    tgurney     Add minimize button in trim
 * Jul 25, 2016    5767    bkowal      Utilize {@link VizBroadcastCycleMessageExpirationUtil}
 *                                     and support message expiration.
 * </pre>
 * 
 * @author mpduff
 */

public class PeriodicMessagesDlg extends CaveSWTDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PeriodicMessagesDlg.class);

    private GenericTable tableComp;

    private TableData tableData;

    /** Map of BroadcastMessage id to MessageDetailsDlg for that id */
    private final Map<Long, MessageDetailsDlg> detailsMap = new HashMap<>();

    private Button detailsBtn;

    private Button expireBtn;

    private final String selectedTransmitterGrp;

    private final BroadcastCycleDataManager dataManager;

    private final PlaylistData playlistData;

    public PeriodicMessagesDlg(Shell parent,
            BroadcastCycleDataManager dataManager, PlaylistData playlistData,
            String selectedTransmitterGrp) {
        super(parent, SWT.DIALOG_TRIM | SWT.MIN, CAVE.INDEPENDENT_SHELL
                | CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
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
        GridLayout gl = new GridLayout(1, false);
        gl.horizontalSpacing = 0;
        gl.marginWidth = 0;
        tableComp = new GenericTable(shell, SWT.BORDER | SWT.V_SCROLL, 7);
        tableComp.setLayout(gl);
        tableComp.setLayoutData(gd);
    }

    public void populateTableData() {
        try {
            tableData = playlistData
                    .getPeriodicTableData(selectedTransmitterGrp);
            tableComp.populateTable(tableData);
            tableComp.setCallbackAction(new ITableActionCB() {
                @Override
                public void tableSelectionChange(int selectionCount) {
                    boolean enabled = (selectionCount == 1);

                    detailsBtn.setEnabled(enabled);
                    expireBtn.setEnabled(enabled);
                }
            });
        } catch (Exception e) {
            statusHandler.error("Error accessing BMH Database.", e);
        }
    }

    private void createBottomButtons() {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        GridLayout gl = new GridLayout(3, false);
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(gl);
        buttonComp.setLayoutData(gd);

        final int minimumButtonWidth = buttonComp.getDisplay().getDPI().x;

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.minimumWidth = minimumButtonWidth;
        detailsBtn = new Button(buttonComp, SWT.PUSH);
        detailsBtn.setText("Message Details...");
        detailsBtn.setLayoutData(gd);
        detailsBtn.setEnabled(false);
        detailsBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMessageDetails();
            }
        });

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.minimumWidth = minimumButtonWidth;
        expireBtn = new Button(buttonComp, SWT.PUSH);
        expireBtn.setText("Expire/Delete...");
        expireBtn.setLayoutData(gd);
        expireBtn.setEnabled(false);
        expireBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                handleExpireAction();
            }
        });

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.minimumWidth = minimumButtonWidth;
        Button closeBtn = new Button(buttonComp, SWT.PUSH);
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
            BroadcastMsg broadcastMsg = getSelectedBroadcastMsg();
            if (broadcastMsg == null) {
                /*
                 * should never happen because the buttons should only be
                 * enabled when a row is selected in the table.
                 */
                throw new IllegalStateException(
                        "No broadcast message is selected when handling the message details action.");
            }
            String afosId = broadcastMsg.getInputMessage().getAfosid();
            MessageType messageType = dataManager.getMessageType(afosId);
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

    private void handleExpireAction() {
        BroadcastMsg broadcastMsg = getSelectedBroadcastMsg();
        if (broadcastMsg == null) {
            /*
             * should never happen because the buttons should only be enabled
             * when a row is selected in the table.
             */
            throw new IllegalStateException(
                    "No broadcast message is selected when handling the expire/delete action.");
        }
        try {
            VizBroadcastCycleMessageExpirationUtil.initiateMessageExpiration(
                    broadcastMsg, dataManager, getShell(), statusHandler);
        } catch (Exception e) {
            statusHandler.error(
                    "Error expiring message: " + broadcastMsg.getId(), e);
        }
    }

    /**
     * Retrieves the {@link BroadcastMsg} associated with the selected row in
     * the table.
     * 
     * @return the selected {@link BroadcastMsg}
     */
    private BroadcastMsg getSelectedBroadcastMsg() {
        List<TableRowData> selectionList = tableComp.getSelection();
        if (selectionList.isEmpty()) {
            return null;
        }
        TableRowData selection = selectionList.iterator().next();
        return (BroadcastMsg) selection.getData();
    }
}