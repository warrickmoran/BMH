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
package com.raytheon.uf.viz.bmh.standalone;

import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.raytheon.uf.common.bmh.comms.LineTapRequest;
import com.raytheon.uf.common.bmh.request.GetBmhServersRequest;
import com.raytheon.uf.common.localization.msgs.GetServersResponse;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.viz.bmh.BMHServers;
import com.raytheon.uf.viz.core.VizServers;
import com.raytheon.uf.viz.core.comm.ConnectivityManager;
import com.raytheon.uf.viz.core.comm.ConnectivityManager.ConnectivityResult;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.ConnectivityPreferenceDialog;
import com.raytheon.uf.viz.core.localization.TextOrCombo;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.dialogs.ModeListener;

/**
 * Dialog giving the user options for how to start BMH in standalone mode. By
 * default connects to the standard localization server, if this is successful
 * no dialog is displayed. When this fails the user can either enter the
 * location of the bmh server, or in emergencies can bypass even the bmh server
 * and activate only broadcast live.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#   Engineer    Description
 * ------------- --------- ----------- --------------------------
 * Feb 05, 2015  3743      bsteffen    Initial creation
 * Feb 16, 2015  4119      bsteffen    Do not validate when BMH is not available.
 * Feb 16, 2015  4168      bsteffen    Do not allow broadcast live in practice mode.
 * Aug 20, 2015  4768      bkowal      Ensure information entered into the dialog is included
 *                                     with the available servers.
 * Aug 20, 2015  4768      bkowal      Line Tap Server is now required when connecting directly
 *                                     to BMH.                                    
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class BmhConnectivityPreferenceDialog extends
        ConnectivityPreferenceDialog {

    private Button bmhServerCheck;

    private boolean useBmhServer = false;

    private Button onlyBroadcastLiveCheck;

    private boolean onlyBroadcastLive = false;

    private TextOrCombo bmhServerEntry;

    private String bmhServer;

    private TextOrCombo broadcastServerEntry;

    private String lineTapServer;

    private TextOrCombo lineTapServerEntry;

    private String broadcastServer;

    private GetServersResponse validatedServers;

    public BmhConnectivityPreferenceDialog() {
        super(false, "BMH Connectivity Preferences");
        bmhServer = BMHServers.getSavedBmhServer();
        broadcastServer = BMHServers.getSavedBroadcastServer();
        lineTapServer = BMHServers.getSavedLineTapServer();
    }

    @Override
    protected void createTextBoxes(Composite textBoxComp) {
        /*
         * This is not the most obvious spot to put in the mode listener because
         * it affects the entire dialog and not just the text boxes but this is
         * the first time that this class gains access to the shell so it is the
         * best spot to do this.
         */
        new ModeListener(textBoxComp.getShell());
        super.createTextBoxes(textBoxComp);

        Label label = new Label(textBoxComp, SWT.RIGHT);
        label.setText("Connect to BMH directly:");
        GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, true);
        gd.horizontalIndent = 20;
        label.setLayoutData(gd);

        Composite edexComp = new Composite(textBoxComp, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        edexComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        edexComp.setLayoutData(gd);

        bmhServerCheck = new Button(edexComp, SWT.CHECK | SWT.LEFT);
        bmhServerCheck.setSelection(useBmhServer);
        bmhServerCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                useBmhServer = bmhServerCheck.getSelection();
                onlyBroadcastLive &= !useBmhServer;
                onlyBroadcastLiveCheck.setSelection(onlyBroadcastLive);
                updateCheckboxes();
            }
        });

        bmhServerEntry = new TextOrCombo(edexComp, SWT.BORDER,
                BMHServers.getSavedBmhServerOptions());
        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        bmhServerEntry.widget.setLayoutData(gd);
        bmhServerEntry.setText(bmhServer);
        bmhServerEntry.addSelectionListener(new EntrySelectionListener());
        bmhServerEntry.widget.setEnabled(useBmhServer);

        label = new Label(textBoxComp, SWT.RIGHT);
        label.setText("Line Tap Server:");
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, true);
        gd.horizontalIndent = 20;
        // gd.horizontalSpan = 2;
        label.setLayoutData(gd);

        lineTapServerEntry = new TextOrCombo(textBoxComp, SWT.BORDER,
                BMHServers.getSavedBmhLineTapServerOptions());
        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        lineTapServerEntry.widget.setLayoutData(gd);
        lineTapServerEntry.setText(lineTapServer);
        lineTapServerEntry.addSelectionListener(new EntrySelectionListener());
        lineTapServerEntry.widget.setEnabled(useBmhServer);

        label = new Label(textBoxComp, SWT.RIGHT);
        label.setText("Only Broadcast Live:");
        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, true);
        gd.horizontalIndent = 20;
        label.setLayoutData(gd);

        Composite emergencyComp = new Composite(textBoxComp, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        emergencyComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        emergencyComp.setLayoutData(gd);
        onlyBroadcastLiveCheck = new Button(emergencyComp, SWT.CHECK | SWT.LEFT);
        onlyBroadcastLiveCheck.setSelection(onlyBroadcastLive);
        onlyBroadcastLiveCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onlyBroadcastLive = onlyBroadcastLiveCheck.getSelection();
                useBmhServer &= !onlyBroadcastLive;
                bmhServerCheck.setSelection(useBmhServer);
                updateCheckboxes();
            }
        });

        broadcastServerEntry = new TextOrCombo(emergencyComp, SWT.BORDER,
                BMHServers.getSavedBroadcastServerOptions());
        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        broadcastServerEntry.widget.setLayoutData(gd);
        broadcastServerEntry.setText(broadcastServer);
        broadcastServerEntry.addSelectionListener(new EntrySelectionListener());
        broadcastServerEntry.widget
                .setEnabled(useBmhServer | onlyBroadcastLive);
        if (CAVEMode.getMode() != CAVEMode.OPERATIONAL) {
            /*
             * Since broadcast live is using a server that does not behave
             * differently in practice mode we have no way to ensure they are
             * not affecting operations so this option is disabled.
             */
            label.setVisible(false);
            ((GridData) label.getLayoutData()).exclude = true;
            emergencyComp.setVisible(false);
            ((GridData) emergencyComp.getLayoutData()).exclude = true;
        }
    }

    @Override
    public boolean validate() {
        if (useBmhServer) {
            status = null;
            details = null;
            bmhServer = bmhServerEntry.getText();
            lineTapServer = lineTapServerEntry.getText();
            validatedServers = null;

            if (siteText != null && !siteText.isDisposed()) {
                String site = siteText.getText().trim();
                this.setSite(site);
                validateSite();
                siteText.setBackground(getTextColor(isSiteGood()));
            } else {
                validateSite();
            }

            ConnectivityResult results = validateBmhServer(bmhServer);
            appendDetails(buildDetails(results));
            if (!results.hasConnectivity && status == null) {
                status = buildErrorMessage(results);
            }
            if (bmhServerEntry != null && !bmhServerEntry.widget.isDisposed()) {
                bmhServerEntry.widget
                        .setBackground(getTextColor(results.hasConnectivity));
            }
            boolean everythingGood = results.hasConnectivity && isSiteGood();
            updateStatus(everythingGood, status, details);

            if (everythingGood == false) {
                return false;
            }

            results = validateLineTapServer(lineTapServer);
            appendDetails(buildDetails(results));
            if (!results.hasConnectivity && status == null) {
                status = buildErrorMessage(results);
            }
            if (lineTapServerEntry != null
                    && !lineTapServerEntry.widget.isDisposed()) {
                lineTapServerEntry.widget
                        .setBackground(getTextColor(results.hasConnectivity));
            }
            everythingGood = results.hasConnectivity;
            updateStatus(everythingGood, status, details);

            return everythingGood;
        } else if (onlyBroadcastLive) {
            status = null;
            details = null;
            broadcastServer = broadcastServerEntry.getText();
            validatedServers = null;
            ConnectivityResult results = new ConnectivityResult(false,
                    bmhServer);
            try {
                Map<String, String> serverLocations = new HashMap<>();
                serverLocations.put(BMHServers.BROADCAST_SERVER,
                        broadcastServer);
                VizServers.getInstance().setServerLocations(serverLocations);
                StandaloneBroadcastLiveDlg.getGroups();
                validatedServers = new GetServersResponse();

                validatedServers.setServerLocations(serverLocations);
                results.hasConnectivity = true;
            } catch (Exception e) {
                results.exception = e;
            }

            appendDetails(buildDetails(results));
            if (!results.hasConnectivity && status == null) {
                status = buildErrorMessage(results);
            }
            if (broadcastServerEntry != null
                    && !broadcastServerEntry.widget.isDisposed()) {
                broadcastServerEntry.widget
                        .setBackground(getTextColor(results.hasConnectivity));
            }

            boolean everythingGood = results.hasConnectivity && isSiteGood();
            updateStatus(everythingGood, status, details);

            return everythingGood;
        } else if (super.validate()) {
            ConnectivityResult results = null;
            try {
                GetServersResponse servers = ConnectivityManager
                        .checkLocalizationServer(getLocalization(), false);
                results = validateBmhServer(servers.getServerLocations().get(
                        BMHServers.BMH_SERVER));
            } catch (VizException e) {
                results = new ConnectivityResult(false, bmhServer);
                results.exception = e;
            }
            appendDetails(buildDetails(results));
            if (!results.hasConnectivity) {
                status = buildErrorMessage(results);
            }
            if (localizationSrv != null && !localizationSrv.widget.isDisposed()) {
                localizationSrv.widget
                        .setBackground(getTextColor(results.hasConnectivity));
            }
            updateStatus(results.hasConnectivity, status, details);
            return results.hasConnectivity;

        } else {
            return false;
        }
    }

    private ConnectivityResult validateBmhServer(String bmhServer) {
        ConnectivityResult results = new ConnectivityResult(false, bmhServer);
        if (bmhServer == null) {
            results.exception = new NullPointerException(
                    "No BMH Server Available");
            return results;
        }
        try {
            GetBmhServersRequest test = new GetBmhServersRequest();
            validatedServers = (GetServersResponse) ThriftClient.sendRequest(
                    test, bmhServer);
            results.hasConnectivity = true;
        } catch (Exception e) {
            results.exception = e;
        }
        return results;
    }

    private ConnectivityResult validateLineTapServer(String lineTapServer) {
        ConnectivityResult results = new ConnectivityResult(false,
                lineTapServer);
        if (lineTapServer == null) {
            results.exception = new NullPointerException(
                    "No Line Tap Server Avaialble");
            return results;
        }

        URI commsURI = null;
        try {
            commsURI = new URI(lineTapServer);
        } catch (URISyntaxException e) {
            results.exception = e;
            return results;
        }

        try (Socket socket = new Socket(commsURI.getHost(), commsURI.getPort())) {
            LineTapRequest test = new LineTapRequest();
            socket.setTcpNoDelay(true);
            OutputStream outputStream = socket.getOutputStream();
            SerializationUtil.transformToThriftUsingStream(test, outputStream);
            results.hasConnectivity = true;
        } catch (Exception e) {
            results.exception = e;
            return results;
        }

        return results;
    }

    protected void updateCheckboxes() {
        bmhServerEntry.widget.setEnabled(useBmhServer);
        lineTapServerEntry.widget.setEnabled(useBmhServer);
        broadcastServerEntry.widget.setEnabled(onlyBroadcastLive);
        super.setLocalizationEnabled(!(useBmhServer || onlyBroadcastLive));
        super.siteText.setEnabled(!onlyBroadcastLive);
        validate();
    }

    public boolean isUseBmhServer() {
        return useBmhServer;
    }

    public boolean isOnlyBroadcastLive() {
        return onlyBroadcastLive;
    }

    public GetServersResponse getServers() {
        if (this.useBmhServer) {
            /*
             * Verify that the entered bmh server is included with the validated
             * servers.
             */
            Map<String, String> serverLocations = validatedServers
                    .getServerLocations();
            if (serverLocations == null) {
                serverLocations = new HashMap<>(1, 1.0f);
            }
            serverLocations.put(BMHServers.BMH_SERVER, this.bmhServer);
            serverLocations.put(BMHServers.getLineTapServerKey(),
                    this.lineTapServer);
            this.validatedServers.setServerLocations(serverLocations);
        }

        return validatedServers;
    }

    private final class EntrySelectionListener implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            validate();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            performOk();
        }
    }

}
