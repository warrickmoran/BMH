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
package com.raytheon.uf.viz.bmh.ui.dialogs.dac;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.dac.DacSyncFields;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.dac.DacChannel;
import com.raytheon.uf.common.bmh.request.DacConfigResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.common.utility.InputTextDlg;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog for creating a new DAC configuration or editing an existing DAC
 * configuration.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 24, 2014  #3660     lvenable     Initial creation
 * Oct 08, 2014  #3479     lvenable     Changed MODE_INDEPENDENT to PERSPECTIVE_INDEPENDENT.
 * Oct 19, 2014  #3699     mpduff       Implement dialog
 * Feb 03, 2015  4056      bsteffen     Auto populate new dac and validate all fields for duplicates.
 * Mar 09, 2015  4235      rferrel      Check for duplicate channel numbers.
 * Jul 01, 2015  4602      rjpeter      Port order now matters.
 * Nov 05, 2015  5092      bkowal       Use {@link DacChannel}.
 * Nov 09, 2015  5113      bkowal       Updated to support the new {@link Dac} fields as well
 *                                      as {@link DacChannel}s.
 * Nov 11, 2015  5114      rjpeter      Update port numbering scheme.
 * Nov 12, 2015  5113      bkowal       Support executing DAC configuration changes.
 * Nov 23, 2015  5113      bkowal       Display a common status indicating whether or not a {@link Dac}
 *                                      has sync. Allow the user to sync a {@link Dac}.
 * Dec 01, 2015  5113      bkowal       Allow for Enter -> ... -> Enter creation for new
 *                                      DACs using the generated configuration.
 * Mar 25, 2016  5504      bkowal       Remove extra margin around the channels composite for a
 *                                      consistent look.
 * Mar 30, 2016  5504      bkowal       Fix GUI sizing issues.
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class CreateEditDacConfigDlg extends CaveSWTDialog {

    private final String CHANNEL_BASE_PORT = DacSyncFields.FIELD_DAC_CHANNEL_FMT
            + ":";

    private final String NET_MASK_LABEL = DacSyncFields.FIELD_DAC_NET_MASK
            + ":";

    private final String GATEWAY_LABEL = DacSyncFields.FIELD_DAC_GATEWAY + ":";

    private final String BROADCAST_BUFFER_LABEL = DacSyncFields.FIELD_BROADCAST_BUFFER
            + ":";

    private final String LEVEL_LABEL = "Level:";

    private final String REBOOT_TEXT = "Reboot";

    private final String AUTO_CONFIGURE_TEXT = "Auto-Configure (Requires DAC Reboot)";

    private final String CONFIGURE_TEXT = "Configure...";

    private final String SAVE_TEXT = "Save";

    private final String SYNC_TEXT = "Sync";

    private static final String SYNC_MESSAGE = "DAC and BMH are in sync.";

    private static final String NO_SYNC_MESSAGE = "DAC and BMH are NOT in sync.";

    private static final String NO_EXIST_MESSAGE = "DAC sync status unknown.";

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateEditDacConfigDlg.class);

    /**
     * Label indicating whether the DAC and {@link Dac} are out of sync or in
     * sync.
     */
    private Label dacSyncLabel;

    /** DAC Name text field */
    private Text dacNameTF;

    /** DAC IP text control. */
    private Text dacIpTF;

    /** DAC Net Mask text control. */
    private Text dacNetMaskTF;

    /** DAC Gateway text control. */
    private Text dacGatewayTF;

    /** DAC Broadcast Buffer text control. */
    private Text dacBroadcastBufferTF;

    /** Receive Port text control. */
    private Text receivePortTF;

    /** Receive address text control. */
    private Text receiveAddressTF;

    /** Channel 1 text control. */
    private Text channel1TF;

    /** Channel 1 level text field. */
    private Text channel1LvlTF;

    /** Channel 2 text control. */
    private Text channel2TF;

    /** Channel 2 level text field. */
    private Text channel2LvlTF;

    /** Channel 3 text control. */
    private Text channel3TF;

    /** Channel 3 level text field. */
    private Text channel3LvlTF;

    /** Channel 4 text control. */
    private Text channel4TF;

    /** Channel 4 level text field. */
    private Text channel4LvlTF;

    /** The Reboot / Auto-Configure checkbox */
    private Button rebootAutoConfigureChk;

    private Button createSaveBtn;

    /** The new/edited dac */
    private Dac dac;

    /**
     * boolean flag indicating whether the edited {@link Dac} is in our out of
     * sync with the associated DAC
     */
    private boolean deSync;

    private final DacConfigDlg parent;

    /** Data access manager */
    private final DacDataManager dataManager;

    /** List of the port text fields */
    private final List<Text> dacPortTxtFldList = new ArrayList<>(4);

    private final List<Text> dacLevelTxtFldList = new ArrayList<>(4);

    /** Enumeration of dialog types. */
    public enum DialogType {
        CREATE, EDIT;
    }

    /** Type of dialog (Create or Edit). */
    private DialogType dialogType = DialogType.CREATE;

    /**
     * Keep track of the address of the DAC that is currently being edited (when
     * applicable).
     */
    private String currentConfigAddress;

    private DacConfigResponse latestConfigResponse;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dialogType
     *            Dialog type.
     */
    public CreateEditDacConfigDlg(Shell parentShell, DialogType dialogType,
            DacDataManager dataManager, DacConfigDlg parent) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);
        this.dialogType = dialogType;
        this.dataManager = dataManager;
        this.parent = parent;
    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);

        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        if (dialogType == DialogType.CREATE) {
            setText("Create DAC Configuration");
        } else if (dialogType == DialogType.EDIT) {
            setText("Edit DAC Configuration");
        }

        createControls();
        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);
        createBottomActionButtons();

        if (dialogType == DialogType.CREATE) {
            /*
             * Allow for easy, convenient: Enter -> Enter -> Enter
             * configuration.
             */
            this.createSaveBtn.forceFocus();
        }
    }

    @Override
    protected void opened() {
        if (dac == null) {
            populateNew();
        } else {
            populate();
            this.currentConfigAddress = dac.getAddress();
        }
    }

    /**
     * Create the input text controls and labels.
     */
    private void createControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        controlComp.setLayout(new GridLayout(2, false));
        controlComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        // Out of Sync Message.
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        this.dacSyncLabel = new Label(controlComp, SWT.NONE);
        this.dacSyncLabel.setLayoutData(gd);

        // DAC ID
        Label dacNameDescLbl = new Label(controlComp, SWT.NONE);
        dacNameDescLbl.setText("DAC Name:");

        dacNameTF = new Text(controlComp, SWT.BORDER);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        GC gc = new GC(dacNameTF);
        gd.minimumWidth = gc.getFontMetrics().getAverageCharWidth()
                * Dac.DAC_NAME_LENGTH;
        gc.dispose();
        dacNameTF.setTextLimit(Dac.DAC_NAME_LENGTH);
        dacNameTF.setLayoutData(gd);

        // DAC IP
        Label dacIPDescLbl = new Label(controlComp, SWT.NONE);
        dacIPDescLbl.setText("DAC IP Address:");

        dacIpTF = new Text(controlComp, SWT.BORDER);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gc = new GC(dacIpTF);
        final int minimumNetworkFieldWidth = gc.textExtent("000.000.000.000").x;
        gc.dispose();
        gd.minimumWidth = minimumNetworkFieldWidth;
        dacIpTF.setTextLimit(Dac.IP_LENGTH);
        dacIpTF.setLayoutData(gd);

        // Net Mask
        Label netMaskLbl = new Label(controlComp, SWT.NONE);
        netMaskLbl.setText(NET_MASK_LABEL);

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = minimumNetworkFieldWidth;
        dacNetMaskTF = new Text(controlComp, SWT.BORDER);
        dacNetMaskTF.setTextLimit(Dac.IP_LENGTH);
        dacNetMaskTF.setLayoutData(gd);

        // Gateway
        Label gatewayLbl = new Label(controlComp, SWT.NONE);
        gatewayLbl.setText(GATEWAY_LABEL);

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = minimumNetworkFieldWidth;
        dacGatewayTF = new Text(controlComp, SWT.BORDER);
        dacGatewayTF.setTextLimit(Dac.IP_LENGTH);
        dacGatewayTF.setLayoutData(gd);

        // Broadcast Buffer
        Label broadcastBufferLbl = new Label(controlComp, SWT.NONE);
        broadcastBufferLbl.setText(BROADCAST_BUFFER_LABEL);

        dacBroadcastBufferTF = new Text(controlComp, SWT.BORDER);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gc = new GC(dacBroadcastBufferTF);
        final int bufferTextWidth = gc.textExtent("9999").x;
        gc.dispose();
        gd.minimumWidth = bufferTextWidth;
        dacBroadcastBufferTF.setLayoutData(gd);

        // Receive Address
        Label receiveAddressDescLbl = new Label(controlComp, SWT.NONE);
        receiveAddressDescLbl.setText("Receive Address:");

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = minimumNetworkFieldWidth;
        receiveAddressTF = new Text(controlComp, SWT.BORDER);
        receiveAddressTF.setTextLimit(Dac.IP_LENGTH);
        receiveAddressTF.setLayoutData(gd);

        // Receive Port
        Label receivePortDescLbl = new Label(controlComp, SWT.NONE);
        receivePortDescLbl.setText("Receive Port:");

        /*
         * TODO: add more validation of the receive port on both the data access
         * layer and the database, itself. Currently, any numeric value is
         * allowed. Outside the scope of the lx branch.
         */
        receivePortTF = new Text(controlComp, SWT.BORDER);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gc = new GC(dacNameTF);
        final int channelPortConfigFieldWidth = gc.textExtent("99999").x;
        gc.dispose();
        gd.minimumWidth = channelPortConfigFieldWidth;
        receivePortTF.setLayoutData(gd);

        // Channels
        Composite channelComposite = new Composite(controlComp, SWT.NONE);
        GridLayout gl = new GridLayout(4, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        channelComposite.setLayout(gl);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        channelComposite.setLayoutData(gd);

        Label channel1DescLbl = new Label(channelComposite, SWT.NONE);
        channel1DescLbl.setText(String.format(CHANNEL_BASE_PORT, 1));

        /*
         * TODO: add more validation of the channel ports on both the data
         * access layer and the database, itself. Currently, any numeric value
         * is allowed. The DAC GUI, itself, only allows for five digit ports.
         * Additionally, it does not make sense to allow / attempt to use a port
         * below 1000. Outside the scope of the lx branch.
         */
        channel1TF = new Text(channelComposite, SWT.BORDER);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = channelPortConfigFieldWidth;
        channel1TF.setLayoutData(gd);

        Label channel1LvlLbl = new Label(channelComposite, SWT.NONE);
        channel1LvlLbl.setText(LEVEL_LABEL);
        channel1LvlTF = new Text(channelComposite, SWT.BORDER);
        gc = new GC(channel1LvlTF);
        final int channelLevelConfigFieldWidth = gc.textExtent("999.9").x;
        gc.dispose();
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = channelLevelConfigFieldWidth;
        channel1LvlTF.setLayoutData(gd);

        dacPortTxtFldList.add(channel1TF);
        dacLevelTxtFldList.add(channel1LvlTF);

        Label channel2DescLbl = new Label(channelComposite, SWT.NONE);
        channel2DescLbl.setText(String.format(CHANNEL_BASE_PORT, 2));

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = channelPortConfigFieldWidth;
        channel2TF = new Text(channelComposite, SWT.BORDER);
        channel2TF.setLayoutData(gd);

        Label channel2LvlLbl = new Label(channelComposite, SWT.NONE);
        channel2LvlLbl.setText(LEVEL_LABEL);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = channelLevelConfigFieldWidth;
        channel2LvlTF = new Text(channelComposite, SWT.BORDER);
        channel2LvlTF.setLayoutData(gd);

        dacPortTxtFldList.add(channel2TF);
        dacLevelTxtFldList.add(channel2LvlTF);

        Label channel3DescLbl = new Label(channelComposite, SWT.NONE);
        channel3DescLbl.setText(String.format(CHANNEL_BASE_PORT, 3));

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = channelPortConfigFieldWidth;
        channel3TF = new Text(channelComposite, SWT.BORDER);
        channel3TF.setLayoutData(gd);

        Label channel3LvlLbl = new Label(channelComposite, SWT.NONE);
        channel3LvlLbl.setText(LEVEL_LABEL);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = channelLevelConfigFieldWidth;
        channel3LvlTF = new Text(channelComposite, SWT.BORDER);
        channel3LvlTF.setLayoutData(gd);

        dacPortTxtFldList.add(channel3TF);
        dacLevelTxtFldList.add(channel3LvlTF);

        Label channel4DescLbl = new Label(channelComposite, SWT.NONE);
        channel4DescLbl.setText(String.format(CHANNEL_BASE_PORT, 4));

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = channelPortConfigFieldWidth;
        channel4TF = new Text(channelComposite, SWT.BORDER);
        channel4TF.setLayoutData(gd);

        Label channel4LvlLbl = new Label(channelComposite, SWT.NONE);
        channel4LvlLbl.setText(LEVEL_LABEL);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = channelLevelConfigFieldWidth;
        channel4LvlTF = new Text(channelComposite, SWT.BORDER);
        channel4LvlTF.setLayoutData(gd);

        dacPortTxtFldList.add(channel4TF);
        dacLevelTxtFldList.add(channel4LvlTF);

        // Reboot / Auto-Configure
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        this.rebootAutoConfigureChk = new Button(controlComp, SWT.CHECK);
        final String chkText = (dialogType == DialogType.CREATE) ? AUTO_CONFIGURE_TEXT
                : REBOOT_TEXT;
        this.rebootAutoConfigureChk.setText(chkText);
        this.rebootAutoConfigureChk
                .setSelection((dialogType == DialogType.CREATE));
        this.rebootAutoConfigureChk.setLayoutData(gd);
        rebootAutoConfigureChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dialogType == DialogType.EDIT) {
                    return;
                }

                if (rebootAutoConfigureChk.getSelection()) {
                    createSaveBtn.setText(CONFIGURE_TEXT);
                } else {
                    createSaveBtn.setText(SAVE_TEXT);
                }
            }
        });
    }

    /**
     * Create the save/create & cancel actions buttons.
     */
    private void createBottomActionButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, true));
        buttonComp.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false));

        final int buttonWidth = buttonComp.getDisplay().getDPI().x;
        createSaveBtn = new Button(buttonComp, SWT.PUSH);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = buttonWidth;
        createSaveBtn.setLayoutData(gd);
        if (dialogType == DialogType.CREATE) {
            createSaveBtn.setText(CONFIGURE_TEXT);
        } else if (dialogType == DialogType.EDIT) {
            createSaveBtn.setText(SAVE_TEXT);
        }

        createSaveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean success = true;
                if (dialogType == DialogType.CREATE) {
                    success = handleCreateAction();
                } else if (dialogType == DialogType.EDIT) {
                    success = handleSaveAction();
                }

                if (success) {
                    close();
                }
            }
        });

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumWidth = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Populate the dialog.
     */
    private void populate() {
        this.dacNameTF.setText(dac.getName());
        this.dacIpTF.setText(dac.getAddress());
        this.dacNetMaskTF.setText(dac.getNetMask());
        this.dacGatewayTF.setText(dac.getGateway());
        this.dacBroadcastBufferTF.setText(String.valueOf(dac
                .getBroadcastBuffer()));
        this.receiveAddressTF.setText(dac.getReceiveAddress());
        this.receivePortTF.setText(String.valueOf(dac.getReceivePort()));

        List<DacChannel> channels = dac.getChannels();
        if (channels.size() != dacPortTxtFldList.size()) {
            statusHandler.warn("The DAC's have 4 ports.  The database has "
                    + channels.size()
                    + " ports configured.  This should be investigated");
        }

        /*
         * Our implementation guarantees that four ports will always be
         * specified. However, there is nothing to prevent a user from manually
         * adding information to the BMH database.
         */
        final int channelCount = Math.min(channels.size(),
                dacPortTxtFldList.size());
        for (int i = 0; i < channelCount; i++) {
            DacChannel channel = channels.get(i);

            Text portTF = dacPortTxtFldList.get(i);
            portTF.setText(String.valueOf(channel.getPort()));

            Text levelTF = dacLevelTxtFldList.get(i);
            levelTF.setText(String.valueOf(channel.getLevel()));
        }

        if (this.deSync) {
            this.dacSyncLabel.setText(NO_SYNC_MESSAGE);
            this.dacSyncLabel.setBackground(getDisplay().getSystemColor(
                    SWT.COLOR_RED));
            /*
             * Only allow the user to complete a sync operation with the DAC in
             * this case.
             */
            this.createSaveBtn.setText(SYNC_TEXT);
            this.rebootAutoConfigureChk.setEnabled(false);
        } else {
            this.dacSyncLabel.setText(SYNC_MESSAGE);
            this.dacSyncLabel.setBackground(getDisplay().getSystemColor(
                    SWT.COLOR_GREEN));
        }
    }

    private void populateNew() {
        List<Dac> dacs = null;
        try {
            dacs = dataManager.getDacs();
        } catch (Exception e) {
            statusHandler.error(
                    "Error accessing database, cannot prepopulate dacs.", e);
            return;
        }
        Set<String> names = new HashSet<>(dacs.size(), 1.0f);
        Set<String> addresses = new HashSet<>(dacs.size(), 1.0f);
        Set<Integer> recievePorts = new HashSet<>(dacs.size(), 1.0f);
        Set<Integer> ports = new HashSet<>(4 * dacs.size(), 1.0f);
        for (Dac dac : dacs) {
            names.add(dac.getName());
            addresses.add(dac.getAddress());
            if (dac.getReceiveAddress().equals(Dac.DEFAULT_RECEIVE_ADDRESS)) {
                recievePorts.add(dac.getReceivePort());
            }

            ports.addAll(dac.getDataPorts());
        }
        InetAddress baseAddress = getBaseDacInetAddress();

        for (int i = 1; i < 100; i += 1) {
            String name = "dac" + i;

            byte[] bytes = baseAddress.getAddress();
            bytes[bytes.length - 1] += i;

            String address = null;
            try {
                address = InetAddress.getByAddress(bytes).getHostAddress();
            } catch (UnknownHostException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error determining new address.", e);
            }
            if (!names.contains(name) && !addresses.contains(address)) {
                this.dacNameTF.setText(name);
                this.dacIpTF.setText(address);
                this.dacNetMaskTF.setText(Dac.DEFAULT_NET_MASK);
                this.dacGatewayTF.setText(Dac.DEFAULT_GATEWAY);
                this.dacBroadcastBufferTF.setText(String
                        .valueOf(Dac.DEFAULT_BROADCAST_BUFFER));
                int basePort = 18000 + (10 * i);
                if (!recievePorts.contains(basePort)) {
                    this.receiveAddressTF.setText(Dac.DEFAULT_RECEIVE_ADDRESS);
                    this.receivePortTF.setText(String.valueOf(basePort));
                }
                int portIndex = 0;
                for (int p = 2; p <= 8; p += 2) {
                    int port = basePort + p;
                    if (!ports.contains(port)) {
                        Text portTF = dacPortTxtFldList.get(portIndex);
                        portTF.setText(String.valueOf(port));

                        Text levelTF = dacLevelTxtFldList.get(portIndex);
                        levelTF.setText(String
                                .valueOf(DacChannel.DEFAULT_LEVEL));
                    }
                    portIndex += 1;
                }
                break;
            }
        }

        this.dacSyncLabel.setText(NO_EXIST_MESSAGE);
        this.dacSyncLabel.setBackground(getDisplay().getSystemColor(
                SWT.COLOR_YELLOW));
    }

    private InetAddress getBaseDacInetAddress() {
        String baseDacAddress = System.getProperty("DacBaseAddress",
                "10.2.69.100");
        try {
            return InetAddress.getByName(baseDacAddress);
        } catch (UnknownHostException e) {
            statusHandler.handle(Priority.DEBUG,
                    "Error parsing System Property DacBaseAddress", e);
        }

        try {
            return InetAddress.getByName("10.2.69.100");
        } catch (UnknownHostException e) {
            statusHandler.handle(Priority.DEBUG,
                    "Error parsing Default DacBaseAddress", e);
        }
        return null;

    }

    /**
     * Create a new dac and save it.
     * 
     * @return true if successful
     */
    private boolean handleCreateAction() {
        dac = new Dac();
        return handleSaveAction();
    }

    /**
     * Save the dac.
     * 
     * @return true if successful
     */
    private boolean handleSaveAction() {
        /*
         * First, determine if this is a sync operation.
         */
        if (this.dialogType == DialogType.EDIT && this.deSync) {
            return this.syncWithDAC();
        }

        boolean isValid = true;
        StringBuilder errMsg = new StringBuilder("Invalid values entered \n\n");

        String name = dacNameTF.getText().trim();
        if (name.isEmpty()) {
            isValid = false;
            errMsg.append("DAC name is required\n");
        }

        String address = this.dacIpTF.getText().trim();
        if (address.isEmpty()) {
            isValid = false;
            errMsg.append("DAC address is required\n");
        }
        String netMask = this.dacNetMaskTF.getText().trim();
        if (netMask.isEmpty()) {
            isValid = false;
            errMsg.append("DAC net mask is required\n");
        }
        String gateway = this.dacGatewayTF.getText().trim();
        if (gateway.isEmpty()) {
            isValid = false;
            errMsg.append("DAC gateway is required\n");
        }
        String broadcastBuffer = this.dacBroadcastBufferTF.getText().trim();
        int broadcastBufferInt = 0;
        try {
            broadcastBufferInt = Integer.parseInt(broadcastBuffer);
            if ((broadcastBufferInt < Dac.BROADCAST_BUFFER_MIN)
                    || (broadcastBufferInt > Dac.BROADCAST_BUFFER_MAX)) {
                isValid = false;
                errMsg.append("The dac broadcast buffer size must be in the range: "
                        + Dac.BROADCAST_BUFFER_MIN
                        + " to "
                        + Dac.BROADCAST_BUFFER_MAX + "!");
            }
        } catch (NumberFormatException e) {
            isValid = false;
            errMsg.append("Broadcast buffer must be an integer value\n");
        }
        String receiveAddress = this.receiveAddressTF.getText().trim();
        if (receiveAddress.isEmpty()) {
            isValid = false;
            errMsg.append("DAC receive address is required\n");
        }
        String receivePort = this.receivePortTF.getText().trim();
        int receivePortInt = 0;
        try {
            receivePortInt = Integer.parseInt(receivePort);
        } catch (NumberFormatException e) {
            isValid = false;
            errMsg.append("Receive port must be an integer value\n");
        }

        List<DacChannel> channels = new LinkedList<>();
        int portNum = 1;
        try {
            for (int i = 0; i < dacPortTxtFldList.size(); i++) {
                String text = this.dacPortTxtFldList.get(i).getText().trim();
                /*
                 * Only populate {@link DacChannel}s if all of the data up to
                 * this point has been valid.
                 */
                if (isValid) {
                    channels.add(new DacChannel(Integer.valueOf(text)));
                }
                portNum++;
            }
        } catch (NumberFormatException e) {
            isValid = false;
            errMsg.append("The channel " + portNum
                    + " port must be an integer value.\n");
        }

        int levelNum = 1;
        try {
            for (int i = 0; i < dacLevelTxtFldList.size(); i++) {
                String text = this.dacLevelTxtFldList.get(i).getText().trim();
                double setLevel = Double.valueOf(text);
                if ((setLevel < DacChannel.LEVEL_MIN)
                        || (setLevel > DacChannel.LEVEL_MAX)) {
                    isValid = false;
                    errMsg.append("The channel " + levelNum
                            + " level must be in the range: "
                            + DacChannel.LEVEL_MIN + " to "
                            + DacChannel.LEVEL_MAX + ".\n");
                    continue;
                }
                /*
                 * Only populate {@link DacChannel}s if all of the data up to
                 * this point has been valid.
                 */
                if (isValid) {
                    channels.get(i).setLevel(setLevel);
                }
                levelNum++;
            }
        } catch (NumberFormatException e) {
            isValid = false;
            errMsg.append("The channel " + levelNum
                    + " level must be a decimal value.\n");
        }

        if (isValid) {
            Dac validateDac = new Dac();
            validateDac.setId(dac.getId());
            validateDac.setAddress(address);
            validateDac.setName(name);
            validateDac.setReceiveAddress(receiveAddress);
            validateDac.setReceivePort(receivePortInt);
            validateDac.setChannels(channels);

            List<Dac> conflictDacs = Collections.emptyList();
            try {
                conflictDacs = this.dataManager
                        .validateDacUniqueness(validateDac);
            } catch (Exception e) {
                errMsg.append("Unable to validate that the DAC is unique.\n");
                isValid = false;
            }
            if (CollectionUtils.isNotEmpty(conflictDacs)) {
                /*
                 * Determine how the dac being updated / saved is in conflict.
                 */
                for (Dac conflictDac : conflictDacs) {
                    if (validateDac.getName().equals(conflictDac.getName())) {
                        errMsg.append("DAC name must be unique. Use a different name\n");
                    }
                    if (validateDac.getAddress().equals(
                            conflictDac.getAddress())) {
                        errMsg.append("DAC address ").append(address)
                                .append(" is already in use by ")
                                .append(conflictDac.getName()).append(".\n");
                    }
                    if (validateDac.getReceiveAddress().equals(
                            conflictDac.getReceiveAddress())
                            && (validateDac.getReceivePort() == conflictDac
                                    .getReceivePort())) {
                        errMsg.append("DAC receive address/port ")
                                .append(receiveAddress).append(":")
                                .append(receivePort)
                                .append(" is already in use by ")
                                .append(conflictDac.getName()).append(".\n");
                    }

                    /*
                     * Determine if there are any port conflicts.
                     */
                    List<?> portConflicts = ListUtils.intersection(
                            validateDac.getDataPorts(),
                            conflictDac.getDataPorts());
                    if (CollectionUtils.isNotEmpty(portConflicts)) {
                        StringBuilder sb = new StringBuilder(
                                "Channel Base Port");
                        if (portConflicts.size() == 1) {
                            sb.append(": ")
                                    .append(portConflicts.get(0))
                                    .append(" has already been assigned to DAC: ");
                        } else {
                            sb.append("s: ")
                                    .append(portConflicts)
                                    .append(" have already been assigned to DAC: ");
                        }
                        sb.append(conflictDac.getName()).append(".\n");
                        errMsg.append(sb.toString());
                    }
                }

                isValid = false;
            }
        }

        if (isValid) {
            dac.setAddress(address);
            dac.setName(name);
            dac.setNetMask(netMask);
            dac.setGateway(gateway);
            dac.setBroadcastBuffer(broadcastBufferInt);
            dac.setReceiveAddress(receiveAddress);
            dac.setReceivePort(receivePortInt);
            dac.setChannels(channels);

            if ((this.dialogType == DialogType.CREATE && this.rebootAutoConfigureChk
                    .getSelection()) || this.dialogType == DialogType.EDIT) {
                return configureDac();
            } else {
                /*
                 * This path only exists to support adding a {@link Dac}
                 * associated with an already fully configured DAC to BMH.
                 */
                try {
                    dac = dataManager.saveDac(dac);
                    setReturnValue(dac);
                } catch (Exception e) {
                    statusHandler.error("Error saving DAC configuation.", e);
                }
            }
        } else {
            DialogUtility.showMessageBox(getShell(), SWT.ICON_ERROR,
                    "Invalid entries", errMsg.toString());
        }

        return isValid;
    }

    private boolean syncWithDAC() {
        try {
            latestConfigResponse = this.dataManager.syncWithDAC(this.dac);
        } catch (Exception e) {
            statusHandler.error("The DAC sync has failed.", e);
            return false;
        }

        if (latestConfigResponse == null) {
            return false;
        }
        if (latestConfigResponse.isSuccess()) {
            dac = latestConfigResponse.getDac();
            this.parent.updateDacSync(dac.getId());
            setReturnValue(dac);
        }

        return latestConfigResponse.isSuccess();
    }

    private boolean configureDac() {
        /*
         * Display any prompts that are necessary to retrieve additional
         * information and/or confirm the user's decision.
         */
        final String configAddress;
        if (this.dialogType == DialogType.CREATE) {
            /*
             * The DAC associated with the {@link Dac} has never been previously
             * configured. Request the address of the DAC to configure.
             */
            final InputTextDlg inputDlg = new InputTextDlg(shell,
                    "Configure DAC",
                    "Enter the IP Address of the DAC to configure:",
                    DacConfigDlg.MANUFACTURER_DAC_IP,
                    new ConfigureDacAddressValidator(), false, true);
            configAddress = (String) inputDlg.open();
            if (configAddress == null) {
                // Cancel
                return false;
            }
        } else {
            configAddress = this.currentConfigAddress;
        }

        final boolean reboot = rebootAutoConfigureChk.getSelection();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        try {
            dialog.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Configuring DAC: " + dac.getName()
                            + " ...", IProgressMonitor.UNKNOWN);
                    try {
                        latestConfigResponse = dataManager.configureSaveDac(
                                dac, reboot, configAddress);

                    } catch (Exception e) {
                        statusHandler.error(
                                "Failed to configure DAC: " + dac.getName()
                                        + ".", e);
                    }
                }
            });
        } catch (Exception e) {
            statusHandler.error("Failed to configure DAC: " + dac.getName()
                    + ".", e);
            return false;
        }

        if (latestConfigResponse == null) {
            return false;
        }
        if (latestConfigResponse.isSuccess()) {
            dac = latestConfigResponse.getDac();
            /*
             * Update the address that would be used to configure the DAC if any
             * additional changes are made before this dialog is closed.
             */
            this.currentConfigAddress = dac.getAddress();
            setReturnValue(dac);
        }

        DacConfigEventDlg dacEventDlg = new DacConfigEventDlg(shell,
                latestConfigResponse.getEvents(), null);
        dacEventDlg.open();

        return latestConfigResponse.isSuccess();
    }

    /**
     * @return the dac
     */
    public Dac getDac() {
        return dac;
    }

    /**
     * @param dac
     *            the dac to set
     */
    public void setDac(Dac dac, final boolean deSync) {
        this.dac = dac;
        this.deSync = deSync;
    }
}
