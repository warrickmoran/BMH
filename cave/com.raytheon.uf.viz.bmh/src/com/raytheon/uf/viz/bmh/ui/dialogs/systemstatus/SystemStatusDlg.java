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
package com.raytheon.uf.viz.bmh.ui.dialogs.systemstatus;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * BMH main system status dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 23, 2014    3349    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class SystemStatusDlg extends CaveSWTDialog {
    private static final String PLUGIN = "com.raytheon.uf.viz.bmh";

    /** List of transmitters */
    private List<String> transmitters;

    /** Normal transmitter icon */
    private Image transmitterNormalIcon;

    /** Checked icon */
    private Image checkedIcon;

    /** Alarm icon */
    private Image alarmIcon;

    /** Process up icon */
    private Image processUpIcon;

    /** Green arrow up icon */
    private Image greenArrowUpIcon;

    /**
     * Constructor.
     * 
     * @param parentShell
     */
    public SystemStatusDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT
                | CAVE.DO_NOT_BLOCK);
        setText("System Status");
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
        getTestTransmitters(); // TODO delete
        loadImages();

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(2, false);
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(gl);
        mainComp.setLayoutData(gd);

        createTransmitterGroup(mainComp);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(1, false);
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        Composite rightComp = new Composite(mainComp, SWT.NONE);
        rightComp.setLayout(gl);
        rightComp.setLayoutData(gd);

        createProcessGroup(rightComp);
        createDacGroup(rightComp);

        gd = new GridData(75, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        Button closeBtn = new Button(shell, SWT.PUSH);
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
     * Create the transmitter group.
     * 
     * @param comp
     */
    private void createTransmitterGroup(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(1, false);
        Group transGrp = new Group(comp, SWT.BORDER);
        transGrp.setText(" Transmitters ");
        transGrp.setLayout(gl);
        transGrp.setLayoutData(gd);

        for (String s : transmitters) {
            gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
            gl = new GridLayout(3, false);
            Composite c = new Composite(transGrp, SWT.NONE);
            c.setLayout(gl);
            c.setLayoutData(gd);

            gd = new GridData(12, 12);
            Label l = new Label(c, SWT.BORDER);
            l.setImage(checkedIcon);
            l.setLayoutData(gd);

            Label l2 = new Label(c, SWT.NONE);
            l2.setImage(transmitterNormalIcon);

            Label l3 = new Label(c, SWT.NONE);
            l3.setText(s);
        }
    }

    /**
     * Create the process group.
     * 
     * @param comp
     */
    private void createProcessGroup(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginHeight = 0;
        Group processGrp = new Group(comp, SWT.BORDER);
        processGrp.setLayout(gl);
        processGrp.setLayoutData(gd);
        processGrp.setText(" BMH Processes ");

        String[] processes = new String[] { "Msg Validator", "Msg Transformer",
                "TTS Manager", "Playlist Manager", "Comm's Manager" };
        for (String s : processes) {
            gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
            gl = new GridLayout(3, false);
            Composite c = new Composite(processGrp, SWT.NONE);
            c.setLayout(gl);
            c.setLayoutData(gd);

            Label l = new Label(c, SWT.NONE);
            l.setImage(alarmIcon);

            Label l2 = new Label(c, SWT.NONE);
            l2.setImage(processUpIcon);

            Label l3 = new Label(c, SWT.NONE);
            l3.setText(s);
        }
    }

    /**
     * Create the dac group.
     * 
     * @param comp
     */
    private void createDacGroup(Composite comp) {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(1, false);
        Group dacGrp = new Group(comp, SWT.BORDER);
        dacGrp.setLayout(gl);
        dacGrp.setLayoutData(gd);
        dacGrp.setText("DACs");

        for (int i = 0; i < 3; i++) {
            gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
            gl = new GridLayout(3, false);
            Composite c = new Composite(dacGrp, SWT.NONE);
            c.setLayout(gl);
            c.setLayoutData(gd);

            Label l = new Label(c, SWT.NONE);
            l.setImage(alarmIcon);

            Label l2 = new Label(c, SWT.NONE);
            l2.setImage(greenArrowUpIcon);

            Label l3 = new Label(c, SWT.NONE);
            l3.setText("DAC #" + 1);
        }

    }

    /**
     * Read in the icons.
     */
    private void loadImages() {
        ImageDescriptor id;
        id = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN,
                "icons/checked.xbm");
        checkedIcon = id.createImage();

        id = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN,
                "icons/xmit_normal.xpm");
        transmitterNormalIcon = id.createImage();

        id = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN,
                "icons/alarm_label.xpm");
        alarmIcon = id.createImage();

        id = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN,
                "icons/fep_1.xpm");
        processUpIcon = id.createImage();

        id = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN,
                "icons/process_up_label.xpm");
        greenArrowUpIcon = id.createImage();
    }

    @Override
    protected void disposed() {
        // transmitterNormalIcon.dispose();
        checkedIcon.dispose();
        transmitterNormalIcon.dispose();
        alarmIcon.dispose();
        processUpIcon.dispose();
        greenArrowUpIcon.dispose();
    }

    // TODO delete
    private void getTestTransmitters() {
        transmitters = new ArrayList<String>(15);

        for (int i = 0; i < 11; i++) {
            transmitters.add("TR" + i);
        }
    }
}
