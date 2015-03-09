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
package com.raytheon.uf.viz.bmh.ui.dialogs.listening.areas;

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
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.same.SAMEStateCodes;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.widgets.duallist.DualList;
import com.raytheon.viz.ui.widgets.duallist.DualListConfig;
import com.raytheon.viz.ui.widgets.duallist.IUpdate;

/**
 * Create/Edit Listening Area dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014    3406    mpduff      Initial creation
 * Feb 09, 2015    4095    bsteffen    Remove Transmitter Name.
 * Mar 09, 2015    4247    rferrel     Now use SAMEStateCodes to validate state abbreviation.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class NewEditAreaDlg extends CaveSWTDialog implements IUpdate {

    /** Area Code text field */
    private Text areaCodeTxt;

    /** Area name text field */
    private Text nameTxt;

    /** Area to edit */
    private Area area;

    /** {@link DualList} control */
    private DualList dualList;

    /** List of {@link Transmitter}s */
    private final List<Transmitter> transmitterList;

    private final List<Area> allAreas;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent Shell
     * @param area
     *            The area to edit, or null if new area
     */
    public NewEditAreaDlg(Shell parentShell, Area area,
            List<Transmitter> transmitterList, List<Area> areaList) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE,
                CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        if (area != null) {
            setText("Edit Area");
        } else {
            setText("New Area");
        }
        this.area = area;
        this.transmitterList = transmitterList;
        this.allAreas = areaList;
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
        Label areaCodeLbl = new Label(comp1, SWT.NONE);
        areaCodeLbl.setText("Enter the Area Code:");
        areaCodeLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        areaCodeTxt = new Text(comp1, SWT.BORDER);
        areaCodeTxt.setLayoutData(gd);
        String tip = "Enter the desired 6-character area code in the Area Code field"
                + " in the form:\n\n  SSXNNN\n\nwhere \"SS\" indicates the state, \"X\" "
                + "where \"X\" is either a C for county code or a numeral (1-9) for a partial area code, and \"NNN\" "
                + "indicates the area code number.";

        areaCodeTxt.setToolTipText(tip);

        if (area != null) {
            areaCodeTxt.setText(area.getAreaCode());
        }
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label areaNameLbl = new Label(comp1, SWT.NONE);
        areaNameLbl.setText("Enter the Area Name:");
        areaNameLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        nameTxt = new Text(comp1, SWT.BORDER);
        nameTxt.setLayoutData(gd);
        nameTxt.setToolTipText("Enter area name, up to 60 characters");
        nameTxt.setTextLimit(60);

        if (area != null) {
            nameTxt.setText(area.getAreaName());
        }

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        Label includedTransmitterLbl = new Label(shell, SWT.NONE);
        includedTransmitterLbl
                .setText("Select the transmitters included in this area:");
        includedTransmitterLbl.setLayoutData(new GridData(SWT.CENTER,
                SWT.DEFAULT, false, false));

        DualListConfig dlc = getDualListConfig();
        if (area != null) {
            Set<Transmitter> transmitters = area.getTransmitters();
            List<String> selectedTranmitters = new ArrayList<String>(
                    transmitters.size());
            for (Transmitter t : transmitters) {
                selectedTranmitters.add(t.getMnemonic() + " - "
                        + t.getLocation());
            }
            dlc.setSelectedList(selectedTranmitters);
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
                    Area area = getArea();
                    setReturnValue(area);
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
     * Get the {@link DualList} configuration object
     * 
     * @return The DualListconfig object
     */
    private DualListConfig getDualListConfig() {
        DualListConfig dlc = new DualListConfig();
        List<String> fullList = new ArrayList<String>(transmitterList.size());
        for (Transmitter t : transmitterList) {
            fullList.add(t.getMnemonic().trim() + " - "
                    + t.getLocation().trim());
        }

        dlc.setAvailableListLabel("Available Transmitters:");
        dlc.setSelectedListLabel("Selected Transmitters:");
        dlc.setListWidth(150);
        dlc.setFullList(fullList);

        return dlc;
    }

    /**
     * Get Area
     * 
     * @return
     */
    private Area getArea() {
        if (area == null) {
            area = new Area();
        }

        area.setAreaCode(areaCodeTxt.getText().trim());
        area.setAreaName(nameTxt.getText().trim());

        String[] selectedTransmitters = dualList.getSelectedListItems();
        Set<Transmitter> transmitters = new HashSet<Transmitter>(
                selectedTransmitters.length);
        for (String s : selectedTransmitters) {
            String[] parts = s.split("-");
            for (Transmitter t : transmitterList) {
                if (t.getMnemonic().equals(parts[0].trim())) {
                    transmitters.add(t);
                    break;
                }
            }
        }

        area.setTransmitters(transmitters);

        return area;
    }

    /**
     * Validate the data
     * 
     * <pre>
     * SSXNNN - 6 digit UGC area code
     * 
     * SS - State
     * X - C for county code, and a numeral (i.e., 1 through 9) for a partial area code
     * NNN - county code number
     * 
     * @return true if valid
     */
    private boolean valid() {
        boolean valid = true;

        if (area == null) {
            // New area, verify it doesn't already exist.
            for (Area area : allAreas) {
                if (area.getAreaCode().equals(areaCodeTxt.getText().trim())) {
                    // If area exists offer user option to update
                    String msg = "This Area already exists.  Would you like to update the existing Area?";
                    int answer = DialogUtility.showMessageBox(getShell(),
                            SWT.YES | SWT.NO, "Area Exists", msg);
                    if (answer == SWT.YES) {
                        this.area = area;
                        return true;
                    }

                    return false;
                }
            }
        }

        // Verify area code is in correct format
        String areaCode = areaCodeTxt.getText().trim();
        String state = areaCode.substring(0, 2);

        String msg = "Invalid area code format\n\nArea code needs to be entered in this format:\n\n"
                + "SSXNNN - 6 digit UGC area code\nSS - State\nX - C for county code, and a numeral "
                + "(i.e., 1 through 9) for a partial area code\nNNN - county code number";

        // First check state
        if (SAMEStateCodes.DEFAULT.isValidState(state)) {
            // State is good, now check next char is either C or 1-9.
            if (areaCode.matches("\\w\\w[C1-9]{1}\\d{3}")) {
                return true;
            } else {
                valid = false;
                DialogUtility.showMessageBox(getShell(), SWT.OK | SWT.ERROR,
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
