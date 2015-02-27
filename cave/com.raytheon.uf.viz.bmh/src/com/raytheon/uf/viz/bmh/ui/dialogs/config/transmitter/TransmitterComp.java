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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.raytheon.uf.common.bmh.broadcast.OnDemandBroadcastConstants.MSGSOURCE;
import com.raytheon.uf.common.bmh.broadcast.TransmitterMaintenanceCommand;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.msg.Program;
import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter.TxMode;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroupPositionComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterPositionComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.bmh.request.TransferToneRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.DetailsDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.NewEditTransmitterDlg.TransmitterEditType;
import com.raytheon.uf.viz.bmh.ui.dialogs.dac.DacDataManager;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Class holding the Tree/table view of the transmitter groups and the
 * transmitters in those groups.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 30, 2014    3173    mpduff      Initial creation
 * Aug 18, 2014    3173    mpduff      Remove "Move To Group" menu item.
 * Aug 24, 2014    3432    mpduff      Handle null ports
 * Sep 23, 2014    3649    rferrel     Changes to handle all types of groups.
 * Oct 23, 2014    3687    bsteffen    Display dac name instead of id.
 * Nov 21, 2014    3838    rferrel     Enable transmitter added check to see if its
 *                                      group's program contains a GENERAL suite.
 * Dec 12, 2014    3603    bsteffen    Implement transfer tone.
 * Jan 08, 2015    3963    bkowal      Allow a user to complete or undo a transmitter decommission.
 * Jan 13, 2015    3995    rjpeter     Do not pass stand alone transmitters to new transmitter dialog.
 * Feb 09, 2015    4095    bsteffen    Remove Transmitter Name.
 * Feb 26, 2015    4156    bkowal      Menu item to commission a Transmitter is now Disabled. Status
 *                                     change confirmations are now asked in the form from state to state.
 * Feb 26, 2015    4166    bkowal      Check for decomm state instead of active state when determining if
 *                                     the user should be able to commission a transmitter in a group.
 * Feb 27, 2015    3962    rferrel     Move decomm menu item and disable when unable to change status to decomm.
 *                                      Change color of text when using a dark background color.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */
public class TransmitterComp extends Composite implements
        ITransmitterStatusChange {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TransmitterComp.class);

    private final String TRANSMITTER = "Transmitter";

    private final String GROUP = "Group";

    private enum TreeTableColumn {
        GROUP_TRANSMITTER("Group/Transmitter", 125, SWT.LEFT), NAME(
                "Name/Location", 125, SWT.LEFT), MNEMONIC("Mnemonic", 125,
                SWT.CENTER), SERVICE_AREA("Service Area", 125, SWT.LEFT), DAC_PORT(
                "DAC/Port", 125, SWT.CENTER), STATUS("Status", 85, SWT.LEFT), MODE(
                "Mode", 85, SWT.LEFT);

        private String text;

        private int colWidth;

        private int alignment;

        TreeTableColumn(String text, int colWidth, int alignment) {
            this.text = text;
            this.colWidth = colWidth;
            this.alignment = alignment;
        }

        /**
         * @return the text
         */
        public String getText() {
            return text;
        }

        /**
         * @return the colWidth
         */
        public int getColWidth() {
            return colWidth;
        }

        /**
         * @return the alignment
         */
        public int getAlignment() {
            return alignment;
        }
    }

    /** The tree */
    private Tree tree;

    /** List of Transmitter groups */
    private List<TransmitterGroup> groups;

    /** Popup menu */
    private Menu menu;

    /** New/Edit transmitter and transmitter group dialog */
    private NewEditTransmitterDlg newEditDlg;

    /** Data manager */
    private TransmitterDataManager dataManager;

    private final Map<String, TransmitterGroup> groupMap = new HashMap<>();

    private final Map<String, Transmitter> transmitterMap = new HashMap<>();

    private List<String> displayStrings;

    /**
     * Call back for NewEditTranmitterDlg.
     */
    private final ICloseCallback callback = new ICloseCallback() {
        @Override
        public void dialogClosed(Object returnValue) {
            if (returnValue != null) {
                populateTree(returnValue);
            }
            getShell().setCursor(null);
        }
    };

    /**
     * Constructor.
     * 
     * @param parent
     */
    public TransmitterComp(Composite parent) {
        super(parent, SWT.NONE);
        init();
    }

    private void init() {
        dataManager = new TransmitterDataManager();

        createTree();
        createPopupMenu();
        populateTree();
    }

    private void createPopupMenu() {
        menu = new Menu(tree);
        tree.setMenu(menu);
        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {
                handleMenuShown();
            }
        });
    }

    /**
     * Build the popup menu
     */
    private void handleMenuShown() {
        MenuItem[] items = menu.getItems();
        for (MenuItem item : items) {
            item.dispose();
        }

        MenuItem newGroupItem = new MenuItem(menu, SWT.PUSH);
        newGroupItem.setText("New Group...");
        newGroupItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                groupMenuAction(true);
            }
        });

        MenuItem newTransmitterItem = new MenuItem(menu, SWT.PUSH);
        newTransmitterItem.setText("New Transmitter...");
        newTransmitterItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                transmitterMenuAction(true);
            }
        });
        new MenuItem(menu, SWT.SEPARATOR);

        TransmitterGroup group = null;
        TransmitterGroup standaloneGroup = null;
        Transmitter groupTransmitter = null;
        boolean transmitterEnabled = false;
        boolean transmitterDecomissioned = false;
        boolean transmitterPrimary = false;

        if (tree.getSelectionCount() > 0) {
            Object o = tree.getSelection()[0].getData();

            if (o instanceof TransmitterGroup) {
                group = (TransmitterGroup) o;
                if (group.isStandalone()) {
                    standaloneGroup = group;
                    group = null;
                }
            } else if (o instanceof Transmitter) {
                groupTransmitter = (Transmitter) o;
            }
            MenuItem editItem = new MenuItem(menu, SWT.PUSH);

            if (group != null) {
                editItem.setText("Edit Group...");
                editItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        groupMenuAction(false);
                    }
                });

                MenuItem deleteItem = new MenuItem(menu, SWT.PUSH);
                deleteItem.setText("Delete Group");
                deleteItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        deleteGroup();
                    }
                });
                deleteItem.setEnabled(group.getTransmitters().size() == 0);
            } else {
                editItem.setText("Edit Transmitter...");
                editItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        transmitterMenuAction(false);
                    }
                });

                transmitterEnabled = ((groupTransmitter != null) && (groupTransmitter
                        .getTxStatus() == TxStatus.ENABLED))
                        || ((standaloneGroup != null) && (standaloneGroup
                                .getTransmitterList().get(0).getTxStatus() == TxStatus.ENABLED));
                if (transmitterEnabled == false) {
                    transmitterDecomissioned = ((groupTransmitter != null) && (groupTransmitter
                            .getTxStatus() == TxStatus.DECOMM))
                            || ((standaloneGroup != null) && (standaloneGroup
                                    .getTransmitterList().get(0).getTxStatus() == TxStatus.DECOMM));
                }

                transmitterPrimary = ((groupTransmitter != null) && (groupTransmitter
                        .getTxMode() == TxMode.PRIMARY))
                        || ((standaloneGroup != null) && (standaloneGroup
                                .getTransmitterList().get(0).getTxMode() == TxMode.PRIMARY));

                new MenuItem(menu, SWT.SEPARATOR);
                /*
                 * Ensure that the user will not be able to enable/disable or
                 * change the mode of a decommissioned transmitter.
                 */
                MenuItem statusMenuItem = new MenuItem(menu, SWT.CASCADE);
                statusMenuItem.setText("Transmitter Status");
                Menu statusMenu = new Menu(menu);
                statusMenuItem.setMenu(statusMenu);

                MenuItem enableStatusItem = new MenuItem(statusMenu, SWT.RADIO);
                enableStatusItem.setText("Enable Transmitter");
                enableStatusItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        MenuItem item = (MenuItem) e.widget;
                        if (item.getSelection()) {
                            changeTxStatus(TxStatus.ENABLED);
                        }
                    }
                });
                enableStatusItem.setSelection(transmitterEnabled);

                /*
                 * Enable only when transmitter has a DAC, DAC port, group has a
                 * program and the program contains a GENERAL suite.
                 */
                enableStatusItem
                        .setEnabled(((standaloneGroup != null)
                                && (standaloneGroup.getDac() != null)
                                && (standaloneGroup.getTransmitterList().get(0)
                                        .getDacPort() != null) && containsGeneralSuite(standaloneGroup
                                    .getProgramSummary()))
                                || ((groupTransmitter != null)
                                        && (groupTransmitter.getDacPort() != null)
                                        && (groupTransmitter
                                                .getTransmitterGroup().getDac() != null) && containsGeneralSuite(groupTransmitter
                                        .getTransmitterGroup()
                                        .getProgramSummary())));

                MenuItem disableStatusItem = new MenuItem(statusMenu, SWT.RADIO);
                disableStatusItem.setText("Disable Transmitter");
                disableStatusItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        MenuItem item = (MenuItem) e.widget;
                        if (item.getSelection()) {
                            changeTxStatus(TxStatus.DISABLED);
                        }
                    }
                });
                disableStatusItem
                        .setSelection(!(transmitterEnabled || transmitterDecomissioned));

                MenuItem decommissionTransmitterItem = new MenuItem(statusMenu,
                        SWT.RADIO);
                decommissionTransmitterItem.setText("Decommission Transmitter");
                decommissionTransmitterItem
                        .addSelectionListener(new SelectionAdapter() {
                            @Override
                            public void widgetSelected(SelectionEvent e) {
                                decommissionTransmitter();
                            }
                        });

                decommissionTransmitterItem
                        .setSelection(transmitterDecomissioned);
                decommissionTransmitterItem.setEnabled(transmitterDecomissioned
                        || !transmitterEnabled);

                MenuItem modeMenuItem = new MenuItem(menu, SWT.CASCADE);
                modeMenuItem.setText("Transmitter Mode");
                Menu modeMenu = new Menu(menu);
                modeMenuItem.setMenu(modeMenu);
                modeMenuItem.setEnabled(!transmitterDecomissioned);

                MenuItem primaryModeItem = new MenuItem(modeMenu, SWT.RADIO);
                primaryModeItem.setText("PRIMARY Mode");
                primaryModeItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        MenuItem item = (MenuItem) e.widget;
                        if (item.getSelection()) {
                            changeTxMode(TxMode.PRIMARY);
                        }
                    }
                });
                primaryModeItem.setSelection(transmitterPrimary);
                primaryModeItem.setEnabled(!transmitterEnabled);

                MenuItem secondaryModeItem = new MenuItem(modeMenu, SWT.RADIO);
                secondaryModeItem.setText("SECONDARY Mode");
                secondaryModeItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        MenuItem item = (MenuItem) e.widget;
                        if (item.getSelection()) {
                            changeTxMode(TxMode.SECONDARY);
                        }
                    }
                });
                secondaryModeItem.setSelection(!transmitterPrimary);
                secondaryModeItem.setEnabled(!transmitterEnabled);

                new MenuItem(menu, SWT.SEPARATOR);

                MenuItem deleteItem = new MenuItem(menu, SWT.PUSH);
                deleteItem.setText("Delete Transmitter");
                deleteItem.setEnabled(!transmitterEnabled);
                deleteItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        deleteTransmitter();
                    }
                });
            }
        }

        MenuItem reorderMenuItem = new MenuItem(menu, SWT.PUSH);
        if (groupTransmitter != null) {
            reorderMenuItem.setText("Order Transmitters...");
            reorderMenuItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    reorderTransmitters();
                }
            });
        } else {
            reorderMenuItem.setText("Order Groups/Transmitters...");
            reorderMenuItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    reorderGroups();
                }
            });
        }

        if (tree.getSelectionCount() > 0) {
            new MenuItem(menu, SWT.SEPARATOR);

            MenuItem detailsItem = new MenuItem(menu, SWT.PUSH);
            detailsItem.setText("Details...");
            detailsItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    showDetails();
                }
            });
        }

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem expandAllItem = new MenuItem(menu, SWT.PUSH);
        expandAllItem.setText("Expand All Groups");
        expandAllItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                expandAll(true);
            }
        });

        MenuItem collapseAllItem = new MenuItem(menu, SWT.PUSH);
        collapseAllItem.setText("Collapse All Groups");
        collapseAllItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                expandAll(false);
            }
        });
    }

    /**
     * Expand/Collapse all
     * 
     * @param expanded
     *            true to expand, false to collapse
     */
    protected void expandAll(boolean expanded) {
        for (TreeItem item : tree.getItems()) {
            item.setExpanded(expanded);
        }
    }

    /**
     * New/Edit Transmitter menu action.
     * 
     * @param newTransmitter
     *            true for a new transmitter, false for an edit
     */
    private void transmitterMenuAction(boolean newTransmitter) {
        Shell shell = getShell();
        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
        if (newTransmitter) {
            TransmitterGroup group = null;

            if (tree.getSelectionCount() > 0) {
                TreeItem item = tree.getSelection()[0];
                Object o = item.getData();
                if (o instanceof Transmitter) {
                    o = item.getParentItem().getData();
                }
                group = (TransmitterGroup) o;
                if (group.isStandalone()) {
                    group = null;
                }
            }

            newEditDlg = new NewEditTransmitterDlg(getShell(), null, group,
                    TransmitterEditType.NEW_TRANSMITTER, callback, this);
        } else {
            TreeItem selectedItem = tree.getSelection()[0];
            Transmitter t = null;
            TransmitterGroup group;
            if (selectedItem.getData() instanceof Transmitter) {
                t = (Transmitter) selectedItem.getData();
                group = (TransmitterGroup) selectedItem.getParentItem()
                        .getData();
            } else {
                group = (TransmitterGroup) selectedItem.getData();
                // in this case there is only one transmitter in the group
                t = group.getTransmitters().iterator().next();
            }

            newEditDlg = new NewEditTransmitterDlg(getShell(), t, group,
                    TransmitterEditType.EDIT_TRANSMITTER, callback, this);
        }
        newEditDlg.open();
    }

    /**
     * New/Edit TransmitterGroup menu action.
     * 
     * @param newGroup
     *            true for a new TransmitterGroup, false for an edit
     */
    private void groupMenuAction(boolean newGroup) {
        Shell shell = getShell();
        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
        if (newGroup) {
            newEditDlg = new NewEditTransmitterDlg(getShell(),
                    TransmitterEditType.NEW_TRANSMITTER_GROUP, callback, this);
        } else {
            TreeItem selectedItem = tree.getSelection()[0];
            TransmitterGroup group = (TransmitterGroup) selectedItem.getData();
            newEditDlg = new NewEditTransmitterDlg(getShell(), null, group,
                    TransmitterEditType.EDIT_TRANSMITTER_GROUP, callback, this);
        }

        newEditDlg.open();
    }

    /**
     * Show Details menu action.
     */
    private void showDetails() {
        TransmitterUtils tu = new TransmitterUtils();
        TreeItem selection = tree.getSelection()[0];
        Object obj = selection.getData();
        String details = null;
        if (obj instanceof TransmitterGroup) {
            details = tu.getTransmitterGroupDetails((TransmitterGroup) obj);
        } else if (obj instanceof Transmitter) {
            details = tu.getTransmitterDetails((Transmitter) obj);
        } else {
            return;
        }

        // Replace "null" with empty String
        details = details.replace("null", "");
        DetailsDlg dd = new DetailsDlg(getShell(), details, "Details");
        dd.open();
    }

    /**
     * Reorder the groups menu action.
     */
    private void reorderGroups() {
        displayStrings = new ArrayList<>(groups.size());

        for (TransmitterGroup g : groups) {
            if (g.isStandalone()) {
                String item = g.getTransmitterList().get(0).getLocation();
                displayStrings.add(item);
                groupMap.put(item, g);
            } else {
                String item = g.getName();
                displayStrings.add(item);
                groupMap.put(item, g);
            }
        }

        GroupTransmitterOrderDlg orderDlg = new GroupTransmitterOrderDlg(
                this.getShell(), "Reorder Groups/Transmitters", displayStrings);
        String[] groupNameArray = (String[]) orderDlg.open();
        if (groupNameArray == null) {
            return;
        }

        for (int i = 0; i < groupNameArray.length; i++) {
            String s = groupNameArray[i];
            TransmitterGroup g = groupMap.get(s);
            g.setPosition(i);
        }

        try {
            dataManager.saveTransmitterGroups(groups);
            populateTree();
        } catch (Exception e) {
            statusHandler.error("Error saving group reorder", e);
            return;
        }
    }

    /**
     * Reorder Transmitters menu action
     */
    private void reorderTransmitters() {
        if (tree.getSelectionCount() > 0) {
            TreeItem parent = tree.getSelection()[0].getParentItem();
            if (parent == null) {
                reorderGroups();
                return;
            }

            TransmitterGroup group = (TransmitterGroup) parent.getData();

            List<String> itemList = new ArrayList<String>();
            for (TreeItem item : parent.getItems()) {
                Transmitter t = (Transmitter) item.getData();
                String s = t.getMnemonic() + " - " + t.getLocation();
                itemList.add(s);
                transmitterMap.put(s, t);
            }

            GroupTransmitterOrderDlg orderDlg = new GroupTransmitterOrderDlg(
                    this.getShell(), "Reorder Transmitters", itemList);
            String[] transmitterNameList = (String[]) orderDlg.open();

            if (transmitterNameList == null) {
                return;
            }

            for (int i = 0; i < transmitterNameList.length; i++) {
                Transmitter t = transmitterMap.get(transmitterNameList[i]);
                t.setPosition(i);
            }

            try {
                dataManager.saveTransmitterGroup(group);
                populateTree();
            } catch (Exception e) {
                statusHandler.error("Error saving reorder", e);
            }
        }
    }

    /**
     * Delete TransmitterGroup menu action. Assumes menu item is displayed only
     * when the group has no transmitters.
     */
    private void deleteGroup() {
        TreeItem selectedItem = tree.getSelection()[0];
        TransmitterGroup toDelete = (TransmitterGroup) selectedItem.getData();
        int answer = DialogUtility.showMessageBox(this.getShell(),
                SWT.ICON_QUESTION | SWT.YES | SWT.NO, "Confirm Delete",
                "Are you sure you want to permenantly delete Transmitter Group "
                        + toDelete.getName() + "?");

        if (answer == SWT.YES) {

            try {
                dataManager.deleteTransmitterGroup(toDelete);
                populateTree();
            } catch (Exception e) {
                statusHandler.error("Error deleting Transmitter Group: "
                        + toDelete.getName(), e);
            }
        }
    }

    /**
     * Delete Transmitter menu action. Assumes transmitter is disabled.
     */
    private void deleteTransmitter() {
        Transmitter transmitter = getSelectedTransmitter();

        if (confirmDeleteTransmitter(transmitter)) {
            try {
                if (transmitter.getTransmitterGroup().isStandalone()) {
                    dataManager.deleteTransmitterGroup(transmitter
                            .getTransmitterGroup());
                } else {
                    dataManager.deleteTransmitter(transmitter);
                }
                populateTree();
            } catch (Exception e) {
                statusHandler.handle(
                        Priority.PROBLEM,
                        "Error deleting transmitter "
                                + transmitter.getMnemonic(), e);
            }
        }
    }

    /**
     * @param toDelete
     * @return true to perform delete
     */
    private boolean confirmDeleteTransmitter(Transmitter toDelete) {
        return SWT.YES == DialogUtility.showMessageBox(this.getShell(),
                SWT.ICON_QUESTION | SWT.YES | SWT.NO, "Confirm Delete",
                "Are you sure you want to permenantly delete Transmitter "
                        + toDelete.getLocation() + "?");
    }

    /**
     * Change the selected transmitter's TxStatus menu action.
     */
    private void changeTxStatus(TxStatus status) {
        Transmitter transmitter = getSelectedTransmitter();
        Object data = tree.getSelection()[0].getData();
        if (confirmChangeTxStatus(transmitter, status)) {
            transmitter.setTxStatus(status);
            if (status == TxStatus.DECOMM) {
                /**
                 * Return the dac port so that other transmitters will be able
                 * to use it when when the transmitter is decommissioned.
                 */
                transmitter.setDacPort(null);
            }
            try {
                dataManager.saveTransmitter(transmitter);
                populateTree(data);
            } catch (Exception e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        }
    }

    private boolean confirmChangeTxStatus(Transmitter toChange, TxStatus status) {
        String statusName = status.name();

        StringBuilder sb = new StringBuilder(
                "Are you sure you want to change the status of Transmitter ");
        sb.append(toChange.getLocation()).append(" from ")
                .append(toChange.getTxStatus().name());
        sb.append(" to ").append(status.name()).append("?");

        return SWT.YES == DialogUtility.showMessageBox(this.getShell(),
                SWT.ICON_QUESTION | SWT.YES | SWT.NO, "Confirm " + statusName,
                sb.toString());
    }

    /**
     * Change the selected transmitter's TxMode menu action.
     */
    private void changeTxMode(TxMode mode) {
        Transmitter transmitter = getSelectedTransmitter();
        Object data = tree.getSelection()[0].getData();
        if (confirmChangeTxMode(transmitter, mode)) {
            TransferToneRequest request = new TransferToneRequest(
                    transmitter.getId(), mode);
            TransmitterGroup transmitterGroup = transmitter
                    .getTransmitterGroup();
            try {
                String inputAudioFile = (String) BmhUtils.sendRequest(request);
                DacDataManager dacDataManager = new DacDataManager();
                Dac dac = dacDataManager.getDacById(transmitterGroup.getDac());

                TransmitterMaintenanceCommand command = new TransmitterMaintenanceCommand();
                command.setMaintenanceDetails("Transfer Tone");
                command.setMsgSource(MSGSOURCE.VIZ);
                command.addTransmitterGroup(transmitterGroup);
                command.setDacHostname(dac.getAddress());
                command.setAllowedDataPorts(dac.getDataPorts());
                command.setRadios(new int[] { transmitter.getDacPort() });
                command.setDecibelTarget(transmitterGroup.getAudioDBTarget());
                command.setInputAudioFile(inputAudioFile);
                command.setBroadcastDuration(-1);
                TransmitterMaintenanceThread.runAndReportResult(statusHandler,
                        this.getShell(), command);
                populateTree(data);
            } catch (Exception e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        }
    }

    private boolean confirmChangeTxMode(Transmitter toChange, TxMode mode) {
        String modeName = mode.name();
        return SWT.YES == DialogUtility.showMessageBox(
                this.getShell(),
                SWT.ICON_QUESTION | SWT.YES | SWT.NO,
                "Confirm " + modeName,
                "Are you sure you want to set Transmitter "
                        + toChange.getMnemonic() + " as " + modeName + "?");
    }

    /**
     * Get the transmitter for the tree's selected item.
     * 
     * @return transmitter - null when selected item is not a transmitter or
     *         standalone group
     */
    private Transmitter getSelectedTransmitter() {
        TreeItem selectedItem = tree.getSelection()[0];
        Object obj = selectedItem.getData();
        Transmitter transmitter = null;

        if (obj instanceof Transmitter) {
            transmitter = (Transmitter) obj;
        } else if (obj instanceof TransmitterGroup) {
            TransmitterGroup group = (TransmitterGroup) selectedItem.getData();
            if (group.isStandalone()) {
                transmitter = group.getTransmitterList().get(0);
            }
        }
        return transmitter;
    }

    /**
     * Do transmitter decommission operation.
     * 
     */
    private void decommissionTransmitter() {
        /*
         * Verify that the user has the required permissions to decommission a
         * transmitter.
         */
        if (BmhUtils.isAuthorized(this.getShell(),
                DlgInfo.TRANSMITTER_CONFIGURATION_DECOMMISSION)) {
            this.changeTxStatus(TxStatus.DECOMM);
        }
    }

    /**
     * Create the tree table
     */
    private void createTree() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        tree = new Tree(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setHeaderVisible(true);
        tree.setLayoutData(gd);
        tree.setToolTipText("Transmitters/Groups\nRight-click for menu options");
        tree.addListener(SWT.PaintItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                paintAction(event);
            }
        });

        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                mouseDoubleClickAction();
            }
        });

        for (TreeTableColumn column : TreeTableColumn.values()) {
            TreeColumn col = new TreeColumn(tree, column.getAlignment());
            col.setText(column.getText());
            col.setWidth(column.getColWidth());
        }
    }

    /**
     * Mouse action to edit selected item.
     */
    private void mouseDoubleClickAction() {
        if (tree.getSelectionCount() > 0) {
            Object o = tree.getSelection()[0].getData();
            if (o instanceof Transmitter) {
                transmitterMenuAction(false);
            } else if (o instanceof TransmitterGroup) {
                TransmitterGroup group = (TransmitterGroup) o;
                if (group.isStandalone()) {
                    transmitterMenuAction(false);
                } else {
                    groupMenuAction(false);
                }
            }
        }
    }

    /**
     * Get List of TransmitterGroups
     * 
     * @return List of TransmitterGroup objects
     */
    private List<TransmitterGroup> getTransmitterGroups() {
        try {
            return dataManager.getTransmitterGroups();
        } catch (Exception e) {
            statusHandler.error("Error retrieving Transmitter information", e);
        }

        return Collections.emptyList();
    }

    /**
     * Paint listener action. Called to draw the status colors on the tree
     * table.
     * 
     * @param event
     *            The Event
     */
    private void paintAction(Event event) {
        if (event.index == TreeTableColumn.STATUS.ordinal()) {
            TreeItem treeItem = (TreeItem) event.item;

            // Return for a group entry
            if (treeItem.getText(0).equals(GROUP)) {
                return;
            }

            Display display = getParent().getDisplay();
            String text = treeItem.getText(TreeTableColumn.STATUS.ordinal());

            switch (TxStatus.valueOf(text)) {
            case ENABLED:
                event.gc.setForeground(display
                        .getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                event.gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
                break;
            case DISABLED:
                event.gc.setForeground(display
                        .getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                event.gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
                break;
            case DECOMM:
                Color brown = new Color(display, 139, 69, 19);
                event.gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
                event.gc.setBackground(brown);
                brown.dispose();
                break;
            default:
                statusHandler.error("Color not set for TxStatus: "
                        + TxStatus.valueOf(text));
            }

            event.gc.fillRectangle(
                    event.x,
                    event.y,
                    tree.getColumn(TreeTableColumn.STATUS.ordinal()).getWidth(),
                    event.height);
            Rectangle rect = new Rectangle(event.x, event.y, tree.getColumn(
                    TreeTableColumn.STATUS.ordinal()).getWidth(), event.height);
            event.gc.drawRectangle(rect);

            Point size = event.gc.textExtent(text);
            int offset = Math.max(0, (event.height - size.y) / 2);
            event.gc.drawText(text, event.x + 2, event.y + offset, true);
        }
    }

    private void populateTree() {
        populateTree(null);
    }

    private void populateTree(Object selectData) {
        groups = getTransmitterGroups();
        List<Integer> expandedList = new ArrayList<Integer>();
        TreeItem[] items = tree.getItems();
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item.getExpanded()) {
                expandedList.add(i);
            }
        }
        tree.removeAll();

        DacDataManager dacDataManager = new DacDataManager();

        Collections.sort(groups, new TransmitterGroupPositionComparator());
        for (TransmitterGroup group : groups) {
            boolean standAlone = false;
            if ((group.getTransmitters() != null)
                    && (group.getTransmitters().size() == 1)) {
                // Check to see if group and transmitter have the same name
                for (Transmitter t : group.getTransmitters()) {
                    if (group.getName().equals(t.getMnemonic())) {
                        standAlone = true;
                        break;
                    }
                }
            }
            String dacStr = "N/A";
            if (group.getDac() != null) {
                try {
                    dacStr = dacDataManager.getDacNameById(group.getDac());
                } catch (Exception e) {
                    statusHandler.error("Error retreiving DAC information", e);
                    dacStr = "<error>";
                }
            }

            TreeItem groupItem = new TreeItem(tree, SWT.NONE);
            StringBuffer sb = new StringBuffer("DAC ");

            if (group.getDac() == null) {
                sb.append("N/A");
            } else {
                sb.append(dacStr);
            }
            groupItem.setText(new String[] { "Group", group.getName(), "", "",
                    sb.toString() });
            groupItem.setData(group);
            groupItem.setBackground(getDisplay().getSystemColor(
                    SWT.COLOR_WIDGET_LIGHT_SHADOW));
            if (group.equals(selectData)) {
                tree.setSelection(groupItem);
            }

            List<Transmitter> transmitterList = group.getTransmitterList();
            if (transmitterList != null) {
                Collections.sort(transmitterList,
                        new TransmitterPositionComparator());
                for (Transmitter t : transmitterList) {
                    String dacPortStr = "N/A";
                    if (t.getDacPort() != null) {
                        dacPortStr = String.valueOf(t.getDacPort());
                    }
                    if (standAlone) {
                        groupItem.setText(new String[] { TRANSMITTER,
                                t.getLocation(), t.getMnemonic(),
                                t.getServiceArea(),
                                dacStr + " / " + dacPortStr,
                                t.getTxStatus().name(), t.getTxMode().name() });
                        if (group.getTransmitterList().get(0)
                                .equals(selectData)) {
                            tree.setSelection(groupItem);
                        }
                    } else {
                        TreeItem transItem = new TreeItem(groupItem, SWT.NONE);
                        transItem.setText(new String[] { TRANSMITTER,
                                t.getLocation(), t.getMnemonic(),
                                t.getServiceArea(), "Port #" + dacPortStr,
                                t.getTxStatus().name(), t.getTxMode().name() });
                        transItem.setData(t);
                        if (t.equals(selectData)) {
                            tree.setSelection(transItem);
                        }
                    }
                }
            }
        }

        for (Integer i : expandedList) {
            if (tree.getItemCount() > i) {
                tree.getItem(i).setExpanded(true);
            }
        }
    }

    /**
     * Determine if a program contains a suite of type GENERAL.
     * 
     * @param ps
     * @return false if no program or program does not contain a GENERAL suite.
     */
    private boolean containsGeneralSuite(ProgramSummary ps) {
        if (ps == null) {
            return false;
        }
        Boolean value = null;
        try {
            Program program = new Program();
            program.setId(ps.getId());
            value = BmhUtils.containsGeneralSuite(program);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to get program information.", e);
        }
        return value;
    }

    /**
     * Check if ok to close.
     * 
     * @return true if ok (new/edit dialog not open)
     */
    public boolean okToClose() {
        return (newEditDlg == null) || newEditDlg.isDisposed();
    }

    @Override
    public void statusChanged() {
        populateTree();
    }
}
