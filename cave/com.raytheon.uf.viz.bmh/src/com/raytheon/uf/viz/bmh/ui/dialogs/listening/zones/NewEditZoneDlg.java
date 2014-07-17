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
package com.raytheon.uf.viz.bmh.ui.dialogs.listening.zones;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.listening.ZonesAreasDataManager;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.widgets.duallist.DualList;
import com.raytheon.viz.ui.widgets.duallist.DualListConfig;
import com.raytheon.viz.ui.widgets.duallist.IUpdate;

/**
 * Create/Edit Listening Zone dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 14, 2014    3406    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class NewEditZoneDlg extends CaveSWTDialog implements IUpdate {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NewEditZoneDlg.class);

    /** Zone Code text field */
    private Text zoneCodeTxt;

    /** Zone name text field */
    private Text nameTxt;

    /** Zone to edit */
    private Zone zone;

    /** {@link DualList} Control */
    private DualList dualList;

    /** List of {@link Area} objects */
    private final List<Area> areaList;

    /** List of {@link Zone} objects */
    private final List<Zone> zoneList;

    /**
     * Constructor
     * 
     * @param parentShell
     *            parent shell
     * @param zone
     *            Zone to populate, or null for new Zone
     * @param zones
     */
    public NewEditZoneDlg(Shell parentShell, Zone zone, List<Area> areaList,
            List<Zone> zoneList) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE,
                CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        if (zone != null) {
            setText("Edit Zone");
        } else {
            setText("New Zone");
        }
        this.zone = zone;
        this.areaList = areaList;
        this.zoneList = zoneList;
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
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(2, false);
        Composite comp1 = new Composite(shell, SWT.NONE);
        comp1.setLayout(gl);
        comp1.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label zoneCodeLbl = new Label(comp1, SWT.NONE);
        zoneCodeLbl.setText("Enter the Zone Code:");
        zoneCodeLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        zoneCodeTxt = new Text(comp1, SWT.BORDER);
        zoneCodeTxt.setLayoutData(gd);
        String tip = "Enter the desired 6-character zone code in the Zone Code field"
                + " in the form:\n\n  SSZNNN\n\nwhere \"SS\" indicates the state, \"Z\" "
                + "indicates zone (and thus must always be a \"Z\"), and \"NNN\" "
                + "indicates the zone code number.";

        zoneCodeTxt.setToolTipText(tip);

        if (zone != null) {
            zoneCodeTxt.setEditable(false);
            zoneCodeTxt.setText(zone.getZoneCode());
        }
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label zoneNameLbl = new Label(comp1, SWT.NONE);
        zoneNameLbl.setText("Enter the Zone Name:");
        zoneNameLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        nameTxt = new Text(comp1, SWT.BORDER);
        nameTxt.setLayoutData(gd);
        nameTxt.setToolTipText("Enter zone name, up to 60 characters");
        nameTxt.setTextLimit(60);

        if (zone != null) {
            nameTxt.setText(zone.getZoneName());
        }

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        Label includedAreaLbl = new Label(shell, SWT.NONE);
        includedAreaLbl.setText("Select the Areas included in this zone:");
        includedAreaLbl.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT,
                false, false));

        DualListConfig dlc = getDualListConfig();
        if (zone != null) {
            Set<Area> areas = zone.getAreas();
            List<String> selectedAreas = new ArrayList<String>(areas.size());
            for (Area a : areas) {
                selectedAreas.add(a.getAreaCode().trim() + " - "
                        + a.getAreaName().trim());
            }
            dlc.setSelectedList(selectedAreas);
        }

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        gl = new GridLayout(1, false);
        Composite dlComp = new Composite(shell, SWT.NONE);
        dlComp.setLayout(gl);
        dlComp.setLayoutData(gd);
        dualList = new DualList(dlComp, SWT.NONE, dlc, this);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gl = new GridLayout(2, false);
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.RIGHT;
        Button saveBtn = new Button(comp, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.setLayoutData(gd);
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (valid()) {
                    Zone zone = getZone();
                    setReturnValue(zone);
                    close();
                }
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.LEFT;
        Button cancelBtn = new Button(comp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        shell.setDefaultButton(saveBtn);
    }

    /**
     * Get a {@link DualListConfig} object
     * 
     * @return The populated DualListConfig object
     */
    private DualListConfig getDualListConfig() {
        DualListConfig dlc = new DualListConfig();
        List<String> fullList = new ArrayList<String>(areaList.size());
        for (Area a : areaList) {
            fullList.add(a.getAreaCode().trim() + " - "
                    + a.getAreaName().trim());
        }

        dlc.setAvailableListLabel("Available Areas:");
        dlc.setSelectedListLabel("Selected Areas:");
        dlc.setListWidth(150);
        dlc.setFullList(fullList);

        return dlc;
    }

    /**
     * Get the {@link Zone} to save.
     * 
     * @return The Zone
     */
    private Zone getZone() {
        if (this.zone == null) {
            zone = new Zone();
        }
        zone.setZoneCode(zoneCodeTxt.getText().trim());
        zone.setZoneName(nameTxt.getText().trim());

        String[] selectedArea = dualList.getSelectedListItems();
        Set<Area> areas = new HashSet<Area>(selectedArea.length);
        for (String s : selectedArea) {
            String[] parts = s.split("-");
            for (Area a : this.areaList) {
                if (parts[0].trim().equals(a.getAreaCode())) {
                    areas.add(a);
                    break;
                }
            }
        }

        zone.setAreas(areas);

        return zone;
    }

    /**
     * Validate the data
     * 
     * <pre>
     * SSZNNN - 6 digit UGC Zone code
     * 
     * SS - State
     * Z - Always Z for zone
     * NNN - zone code number
     * </pre>
     * 
     * @return true if valid
     */
    private boolean valid() {
        boolean valid = true;

        if (zone == null) {
            // New zone, verify it doesn't already exist.
            for (Zone z : zoneList) {
                if (z.getZoneCode().equals(zoneCodeTxt.getText().trim())) {
                    // If Zone exists offer user option to update
                    String msg = "This Zone already exists.  Would you like to update the existing Zone?";
                    int answer = DialogUtility.showMessageBox(getShell(),
                            SWT.YES | SWT.NO, "Zone Exists", msg);
                    if (answer == SWT.YES) {
                        this.zone = z;
                        return true;
                    }

                    return false;
                }
            }
        }

        // Verify zone code is in correct format
        String zoneCode = zoneCodeTxt.getText().trim();
        String state = zoneCode.substring(0, 2);

        String msg = "Invalid Zone code format\n\nZone code needs to be entered in this format:\n\n"
                + "SSZNNN - 6 digit UGC Zone code\nSS - State\nZ - always Z for Zone "
                + "\nNNN - Zone code number";

        // First check state
        if (new ZonesAreasDataManager().getStateAbbreviations().contains(state)) {
            // State is good, now check next char is Z.
            if (zoneCode.matches("\\w\\w[Z]{1}\\d{3}")) {
                return true;
            } else {
                valid = false;
                DialogUtility.showMessageBox(getShell(), SWT.OK,
                        "Invalid Format", msg);
            }
        } else {
            valid = false;
            DialogUtility.showMessageBox(getShell(), SWT.OK | SWT.ERROR,
                    "Invalid Format", msg);
        }

        return valid;
    }

    @Override
    public void hasEntries(boolean entries) {
        // No-op
    }

    @Override
    public void selectionChanged() {
        // No-op
    }
}
