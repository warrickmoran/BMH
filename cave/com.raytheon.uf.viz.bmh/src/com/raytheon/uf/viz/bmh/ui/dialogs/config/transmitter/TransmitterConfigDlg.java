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

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.viz.bmh.Activator;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;

/**
 * Transmitter configuration dialog
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 30, 2014    3173    mpduff      Initial creation
 * Nov 11, 2014  3413      rferrel     Use DlgInfo to get title.
 * Mar 12, 2015  4249      rferrel     Load/dispose disableImage used by TransmitterComp.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterConfigDlg extends AbstractBMHDialog {
    /** Transmitter Composite Tree */
    private TransmitterComp tComp;

    private Image disableImage;

    /**
     * Constructor.
     * 
     * @param parentShell
     * @param dlgMap
     */
    public TransmitterConfigDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, DlgInfo.TRANSMITTER_CONFIGURATION.getTitle(),
                parentShell, SWT.DIALOG_TRIM | SWT.RESIZE,
                CAVE.PERSPECTIVE_INDEPENDENT | CAVE.DO_NOT_BLOCK);
        setText(DlgInfo.TRANSMITTER_CONFIGURATION.getTitle());
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
        ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, "icons/radio_disabled.png");
        disableImage = id.createImage();
        createTree();
        createBottomButtons();
        shell.setMinimumSize(650, 350);
    }

    private void createTree() {
        GridLayout gl = new GridLayout();
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 400;
        tComp = new TransmitterComp(shell, disableImage);
        tComp.setLayout(gl);
        tComp.setLayoutData(gd);
    }

    private void createBottomButtons() {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button closeBtn = new Button(btnComp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    @Override
    public boolean okToClose() {
        return tComp.okToClose();
    }

    @Override
    protected void disposed() {
        if (disableImage != null) {
            disableImage.dispose();
        }
        super.disposed();
    }
}
