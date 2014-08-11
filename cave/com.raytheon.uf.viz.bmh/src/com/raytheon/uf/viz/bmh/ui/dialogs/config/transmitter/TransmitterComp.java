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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroupPositionComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterPositionComparator;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TxStatus;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.DetailsDlg;
import com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter.NewEditTransmitterDlg.TransmitterEditType;
import com.raytheon.viz.ui.dialogs.ICloseCallback;
import com.raytheon.viz.ui.dialogs.ListSelectionDlg;
import com.raytheon.viz.ui.dialogs.ListSelectionDlg.ReturnArray;

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
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterComp extends Composite {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(TransmitterComp.class);

    private final String TRANSMITTER = "Transmitter";

    private final String GROUP = "Group";

    private enum TreeTableColumn {
        GROUP_TRANSMITTER("Group/Transmitter", 125, SWT.LEFT), NAME("Name",
                125, SWT.LEFT), MNEMONIC("Mnemonic", 125, SWT.CENTER), SERVICE_AREA(
                "Service Area", 125, SWT.LEFT), DAC_PORT("DAC/Port", 75,
                SWT.CENTER), STATUS("Status", 85, SWT.LEFT), MODE("Mode", 85,
                SWT.LEFT);

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
        for (int i = 0; i < items.length; i++) {
            items[i].dispose();
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

        MenuItem editItem = new MenuItem(menu, SWT.PUSH);

        if (tree.getSelectionCount() > 0
                && tree.getSelection()[0].getText(0).equals(TRANSMITTER)) {
            // Zero children items, Transmitter
            editItem.setText("Edit Transmitter...");
            editItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    transmitterMenuAction(false);
                }
            });

            MenuItem deleteItem = new MenuItem(menu, SWT.PUSH);
            deleteItem.setText("Delete Transmitter");
            deleteItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    deleteTransmitter();
                }
            });

            MenuItem moveToGroupItem = new MenuItem(menu, SWT.PUSH);
            moveToGroupItem.setText("Move To Group...");
            moveToGroupItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    moveToGroupAction();
                }
            });
            MenuItem reorderMenuItem = new MenuItem(menu, SWT.PUSH);
            reorderMenuItem.setText("Order Transmitters...");
            reorderMenuItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    reorderTransmitters();
                }
            });
            MenuItem decommissionTransmitterItem = new MenuItem(menu, SWT.PUSH);
            decommissionTransmitterItem.setText("Decommission Transmitter");
            decommissionTransmitterItem
                    .addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            decommissionTransmitter();
                        }
                    });
        } else {
            // Group
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

            MenuItem reorderMenuItem = new MenuItem(menu, SWT.PUSH);
            reorderMenuItem.setText("Order Groups/Transmitters...");
            reorderMenuItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    reorderGroups();
                }
            });
        }

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem detailsItem = new MenuItem(menu, SWT.PUSH);
        detailsItem.setText("Details...");
        detailsItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                showDetails();
            }
        });

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
        ICloseCallback callback = new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue != null) {
                    Boolean reload = (Boolean) returnValue;
                    if (reload) {
                        populateTree();
                    }
                }
            }
        };

        if (newTransmitter) {
            newEditDlg = new NewEditTransmitterDlg(getShell(),
                    TransmitterEditType.NEW_TRANSMITTER, callback);
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
                for (Transmitter trans : group.getTransmitters()) {
                    t = trans;
                    break;
                }
            }

            newEditDlg = new NewEditTransmitterDlg(getShell(), t, group,
                    TransmitterEditType.EDIT_TRANSMITTER, callback);
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
        ICloseCallback callback = new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue != null) {
                    Boolean reload = (Boolean) returnValue;
                    if (reload) {
                        populateTree();
                    }
                }
            }
        };

        if (newGroup) {
            newEditDlg = new NewEditTransmitterDlg(getShell(),
                    TransmitterEditType.NEW_TRANSMITTER_GROUP, callback);
        } else {
            TreeItem selectedItem = tree.getSelection()[0];
            TransmitterGroup group = (TransmitterGroup) selectedItem.getData();
            newEditDlg = new NewEditTransmitterDlg(getShell(), null, group,
                    TransmitterEditType.EDIT_TRANSMITTER_GROUP, callback);
        }

        newEditDlg.open();
    }

    /**
     * Show Details menu action
     */
    private void showDetails() {
        if (tree.getSelectionCount() == 0) {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_WARNING,
                    "Select", "A Group/Transmitter must be selected");
            return;
        }
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
        List<String> itemList = new ArrayList<String>();
        for (TreeItem item : tree.getItems()) {
            itemList.add(item.getText(1));
        }

        GroupTransmitterOrderDlg orderDlg = new GroupTransmitterOrderDlg(
                this.getShell(), "Reorder Groups/Transmitters", itemList);
        String[] groupNameArray = (String[]) orderDlg.open();
        if (groupNameArray == null) {
            return;
        }

        List<String> groupNameList = Arrays.asList(groupNameArray);

        List<TransmitterGroup> groupList = new ArrayList<TransmitterGroup>(
                groupNameList.size());
        for (TreeItem item : tree.getItems()) {
            groupList.add((TransmitterGroup) item.getData());
        }

        try {
            for (TransmitterGroup group : groupList) {
                group.setPosition(groupNameList.indexOf(group.getName()));
            }
            dataManager.saveTransmitterGroups(groupList);
        } catch (Exception e) {
            statusHandler.error("Error saving group reorder", e);
            return;
        }

        populateTree();
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

            List<String> itemList = new ArrayList<String>();
            for (TreeItem item : parent.getItems()) {
                itemList.add(item.getText(1));
            }
            GroupTransmitterOrderDlg orderDlg = new GroupTransmitterOrderDlg(
                    this.getShell(), "Reorder Transmitters", itemList);
            String[] transmitterNameList = (String[]) orderDlg.open();

            if (transmitterNameList == null) {
                return;
            }

            List<String> transmitterList = Arrays.asList(transmitterNameList);
            TransmitterGroup group = (TransmitterGroup) parent.getData();
            for (Transmitter trans : group.getTransmitters()) {
                trans.setPosition(transmitterList.indexOf(trans.getName()));
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
     * Delete TransmitterGroup menu action
     */
    private void deleteGroup() {
        TreeItem selectedItem = tree.getSelection()[0];
        TransmitterGroup toDelete = (TransmitterGroup) selectedItem.getData();
        if (toDelete.getTransmitters() == null
                || toDelete.getTransmitters().size() == 0) {
            int answer = DialogUtility.showMessageBox(this.getShell(),
                    SWT.ICON_QUESTION | SWT.YES | SWT.NO, "Confirm Delete",
                    "Are you sure you want to permenantly delete Transmitter Group "
                            + toDelete.getName() + ".");

            if (answer == SWT.YES) {

                try {
                    dataManager.deleteTransmitterGroup(toDelete);
                    populateTree();
                } catch (Exception e) {
                    statusHandler.error("Error deleting Transmitter Group "
                            + toDelete.getName(), e);
                }
            }
        } else {
            DialogUtility.showMessageBox(this.getShell(), SWT.ICON_WARNING
                    | SWT.OK, "Group Not Empty",
                    "To Delete a Group all Transmitters must be removed.");
        }
    }

    /**
     * Delete Transmitter menu action
     */
    private void deleteTransmitter() {
        TreeItem selectedItem = tree.getSelection()[0];
        Object obj = selectedItem.getData();
        if (obj instanceof Transmitter) {
            Transmitter toDelete = (Transmitter) selectedItem.getData();
            int answer = DialogUtility.showMessageBox(this.getShell(),
                    SWT.ICON_QUESTION | SWT.YES | SWT.NO, "Confirm Delete",
                    "Are you sure you want to permenantly delete Transmitter "
                            + toDelete.getName() + ".");

            if (answer == SWT.YES) {
                try {
                    dataManager.deleteTransmitter(toDelete);
                    populateTree();
                } catch (Exception e) {
                    statusHandler.error("Error deleting Transmitter "
                            + toDelete.getName(), e);
                }
            }
        } else if (obj instanceof TransmitterGroup) {
            // A stand alone transmitter
            TransmitterGroup toDelete = (TransmitterGroup) selectedItem
                    .getData();
            int answer = DialogUtility.showMessageBox(this.getShell(),
                    SWT.ICON_QUESTION | SWT.YES | SWT.NO, "Confirm Delete",
                    "Are you sure you want to permenantly delete Transmitter "
                            + toDelete.getName() + ".");

            if (answer == SWT.YES) {
                try {
                    dataManager.deleteTransmitterGroup(toDelete);
                    populateTree();
                } catch (Exception e) {
                    statusHandler.error("Error deleting Transmitter "
                            + toDelete.getName(), e);
                }
            }
        }
    }

    /**
     * Decommission menu action
     */
    private void decommissionTransmitter() {
        // TODO implement when ready
        System.out.println("Decommission");
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

        for (TreeTableColumn column : TreeTableColumn.values()) {
            TreeColumn col = new TreeColumn(tree, column.getAlignment());
            col.setText(column.getText());
            col.setWidth(column.getColWidth());
        }
    }

    /**
     * Move Transmitter to group menu action, launches the dialog
     */
    private void moveToGroupAction() {
        List<String> nameList = new ArrayList<String>();
        for (int i = 0; i < groups.size(); i++) {
            TransmitterGroup group = groups.get(i);
            if (!group.isStandalone()) {
                nameList.add(group.getName());
            }
        }

        TreeItem item = tree.getSelection()[0];
        TransmitterGroup tg = null;
        if (item.getData() instanceof TransmitterGroup) {
            tg = (TransmitterGroup) item.getData();
        } else {
            Transmitter t = (Transmitter) item.getData();
            tg = t.getTransmitterGroup();
        }
        nameList.remove(tg.getName());

        String[] names = nameList.toArray(new String[0]);
        ListSelectionDlg dlg = new ListSelectionDlg(getShell(), names, true,
                ReturnArray.ARRAY_STRING_ITEMS, "Select", "Destination Group",
                "Select the destination group", new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        if (returnValue != null) {
                            String[] results = (String[]) returnValue;
                            moveToGroup(results[0]);
                        }
                    }
                });
        dlg.open();
    }

    /**
     * Move the selected transmitter to the destinationGroup.
     * 
     * @param destinationGroup
     *            The Group to move to
     */
    private void moveToGroup(String destinationGroup) {
        TreeItem selectedItem = tree.getSelection()[0];
        Transmitter t = null;
        TransmitterGroup toDelete = null;

        if (selectedItem.getData() instanceof TransmitterGroup) {
            TransmitterGroup tg = (TransmitterGroup) selectedItem.getData();
            if (tg.isStandalone()) {
                t = tg.getTransmitterList().get(0);
                toDelete = tg;
            }
        } else if (selectedItem.getData() instanceof Transmitter) {
            t = (Transmitter) selectedItem.getData();
        }

        for (TransmitterGroup tg : groups) {
            if (tg.getName().equals(destinationGroup)) {
                if (tg.getTransmitterList().size() > 0) {
                    // A FIPS code is required for each transmitter in the group
                    if (t.getFipsCode() == null
                            || t.getFipsCode().length() == 0) {
                        DialogUtility.showMessageBox(getShell(),
                                SWT.ICON_WARNING | SWT.OK,
                                "FIPS Code Required",
                                "A FIPS code is required for all transmitters "
                                        + "in a group of 2 or more.");
                        return;
                    }
                }

                // Check port numbers
                for (Transmitter trans : tg.getTransmitters()) {
                    if (trans.getDacPort() == t.getDacPort()) {
                        // TODO look into the move to group functionality.
                        // DialogUtility
                        // .showMessageBox(getShell(), SWT.ICON_WARNING
                        // | SWT.YES | SWT.NO, "Port Conflict",
                        // "This port number is already in use in the group " +
                        // tg.getName() + ".  ");
                    }
                }
                t.setTransmitterGroup(tg);
                try {
                    if (toDelete != null) {
                        toDelete.setTransmitters(null);
                        dataManager.saveTransmitterDeleteGroup(t, toDelete);
                    } else {
                        dataManager.saveTransmitter(t);
                    }
                    populateTree();
                } catch (Exception e) {
                    statusHandler.error(
                            "Error saving Transmitter " + t.getName(), e);
                }
                break;
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

            Color green = getParent().getDisplay().getSystemColor(
                    SWT.COLOR_GREEN);
            Color red = getParent().getDisplay().getSystemColor(SWT.COLOR_RED);
            String text = treeItem.getText(TreeTableColumn.STATUS.ordinal());

            if (TxStatus.valueOf(text) == TxStatus.ENABLED) {
                event.gc.setForeground(green);
                event.gc.setBackground(green);
            } else {
                event.gc.setForeground(red);
                event.gc.setBackground(red);
            }

            event.gc.fillRectangle(
                    event.x,
                    event.y,
                    tree.getColumn(TreeTableColumn.STATUS.ordinal()).getWidth(),
                    event.height);
            Rectangle rect = new Rectangle(event.x, event.y, tree.getColumn(
                    TreeTableColumn.STATUS.ordinal()).getWidth(), event.height);
            event.gc.drawRectangle(rect);

            event.gc.setForeground(getParent().getDisplay().getSystemColor(
                    SWT.COLOR_LIST_FOREGROUND));
            Point size = event.gc.textExtent(text);
            int offset = Math.max(0, (event.height - size.y) / 2);
            event.gc.drawText(text, event.x + 2, event.y + offset, true);
        }
    }

    private void populateTree() {
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

        Collections.sort(groups, new TransmitterGroupPositionComparator());
        for (TransmitterGroup group : groups) {
            boolean standAlone = false;
            if (group.getTransmitters() != null
                    && group.getTransmitters().size() == 1) {
                // Check to see if group and transmitter have the same name
                for (Transmitter t : group.getTransmitters()) {
                    if (group.getName().equals(t.getMnemonic())) {
                        standAlone = true;
                        break;
                    }
                }
            }
            TreeItem groupItem = new TreeItem(tree, SWT.NONE);
            groupItem.setText(new String[] { "Group", group.getName(), "", "",
                    "DAC #" + group.getDac() });
            groupItem.setData(group);
            groupItem.setBackground(getDisplay().getSystemColor(
                    SWT.COLOR_WIDGET_LIGHT_SHADOW));

            List<Transmitter> transmitterList = group.getTransmitterList();
            if (transmitterList != null) {
                Collections.sort(transmitterList,
                        new TransmitterPositionComparator());
                for (Transmitter t : transmitterList) {
                    if (standAlone) {
                        groupItem.setText(new String[] { "Transmitter",
                                t.getName(), t.getMnemonic(),
                                t.getServiceArea(), "Port #" + t.getDacPort(),
                                t.getTxStatus().name(), t.getTxMode().name() });
                    } else {
                        TreeItem transItem = new TreeItem(groupItem, SWT.NONE);
                        transItem.setText(new String[] { "Transmitter",
                                t.getName(), t.getMnemonic(),
                                t.getServiceArea(), "Port #" + t.getDacPort(),
                                t.getTxStatus().name(), t.getTxMode().name() });
                        transItem.setData(t);
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
     * Check if ok to close.
     * 
     * @return true if ok (new/edit dialog not open)
     */
    public boolean okToClose() {
        return newEditDlg == null || newEditDlg.isDisposed();
    }
}
