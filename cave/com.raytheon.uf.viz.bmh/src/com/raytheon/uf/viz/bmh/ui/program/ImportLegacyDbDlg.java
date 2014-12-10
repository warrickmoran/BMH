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
package com.raytheon.uf.viz.bmh.ui.program;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.TtsVoice;
import com.raytheon.uf.common.bmh.legacy.ascii.AsciiFileTranslator;
import com.raytheon.uf.common.bmh.legacy.ascii.BmhData;
import com.raytheon.uf.common.bmh.request.ImportLegacyDbRequest;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.uf.viz.bmh.ui.dialogs.AbstractBMHDialog;
import com.raytheon.uf.viz.bmh.ui.dialogs.DlgInfo;
import com.raytheon.uf.viz.bmh.voice.VoiceDataManager;
import com.raytheon.uf.viz.core.VizApp;

/**
 * Dialog that manages importing a legacy database.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 05, 2014  #3824     rferrel     Initial creation
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class ImportLegacyDbDlg extends AbstractBMHDialog {

    /** Remember browser's last directory. */
    private static String browserDirPath = "/";

    private Text fileTxt;

    private String input;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell.
     * @param dlgMap
     *            Map to add this dialog to for closing purposes.
     */
    public ImportLegacyDbDlg(Shell parentShell,
            Map<AbstractBMHDialog, String> dlgMap) {
        super(dlgMap, DlgInfo.IMPORT_LEGACY_DB.getTitle(), parentShell,
                SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE, CAVE.DO_NOT_BLOCK
                        | CAVE.PERSPECTIVE_INDEPENDENT);
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
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        setText(DlgInfo.IMPORT_LEGACY_DB.getTitle());

        Composite progComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        progComp.setLayout(gl);
        progComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        GridData gd = null;

        Composite fileComp = new Composite(progComp, SWT.NONE);
        gl = new GridLayout(3, false);
        fileComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        fileComp.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        Label fileLbl = new Label(fileComp, SWT.NONE);
        fileLbl.setText("File: ");

        fileTxt = new Text(fileComp, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.minimumWidth = 300;
        fileTxt.setLayoutData(gd);

        Button browseBtn = new Button(fileComp, SWT.NONE);
        browseBtn.setText("Browse...");
        browseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fileBrowser();
            }
        });

        // addLabelSeparator(progComp);

        Composite buttonComp = new Composite(progComp, SWT.NONE);
        gl = new GridLayout(2, true);
        buttonComp.setLayout(gl);
        gd = new GridData(SWT.CENTER, SWT.NONE, true, false);
        buttonComp.setLayoutData(gd);

        Button importBtn = new Button(buttonComp, SWT.NONE);
        importBtn.setText("Import...");
        importBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getShell().setCursor(
                        getShell().getDisplay()
                                .getSystemCursor(SWT.CURSOR_WAIT));
                final String fileName = fileTxt.getText().trim();

                Job job = new Job("Import Legacy Database") {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        final AtomicBoolean closeDlg = new AtomicBoolean(false);
                        if (verifyFile(fileName)) {
                            closeDlg.set(saveImportDB(fileName));
                        }
                        VizApp.runAsync(new Runnable() {

                            @Override
                            public void run() {
                                if (closeDlg.get()) {
                                    close();
                                } else {
                                    getShell().setCursor(null);
                                }
                            }
                        });

                        return Status.OK_STATUS;
                    }
                };
                job.schedule();
            }
        });

        Button cancelBtn = new Button(buttonComp, SWT.NONE);
        cancelBtn.setText("Cancel");
        cancelBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Verify legacy data file. Assumed invoked from a non-UI thread.
     * 
     * @param fileName
     * @return true file ok to import.
     */
    private boolean verifyFile(String fileName) {
        if (fileName.length() == 0) {
            return false;
        }
        File file = new File(fileName);

        if (!file.canRead()) {
            showMessage(SWT.ICON_ERROR | SWT.OK, "Verify Legacy Database",
                    "Cannot read file:\n" + file.getAbsolutePath());
            return false;
        }

        byte[] fileData = new byte[(int) file.length()];
        try {
            new FileInputStream(file).read(fileData);
        } catch (IOException e) {
            showMessage(SWT.ICON_ERROR | SWT.OK, "Verify Legacy Database",
                    "Unable to ready file:\n" + file.getAbsolutePath());
            return false;
        }

        input = new String(fileData);
        BufferedReader reader = new BufferedReader(new StringReader(input));

        try {
            List<TtsVoice> voices = new VoiceDataManager().getAllVoices();
            if ((voices == null) || (voices.size() == 0)) {
                TtsVoice voice = new TtsVoice();
                voice.setVoiceNumber(101);
                voice.setVoiceName("Paul");
                voice.setLanguage(Language.ENGLISH);
                voice.setMale(true);
                if (voices == null) {
                    voices = new ArrayList<>();
                }
                voices.add(voice);
            }

            AsciiFileTranslator asciiFile = new AsciiFileTranslator(reader,
                    file.getAbsolutePath(), false, voices);

            if (!asciiFile.parsedData()) {
                showMessage(
                        SWT.ICON_ERROR,
                        "Verify Legacy Database",
                        "No data found in the the file:\n"
                                + file.getAbsolutePath());
                input = null;
                return false;
            }
            List<String> msgs = asciiFile.getValidationMessages();
            BmhData data = asciiFile.getTranslatedData();
            StringBuilder sb = new StringBuilder();
            sb.append(asciiFile.getVoiceMsg()).append("\n\nContains:\n");
            sb.append(data.getTransmitters().size()).append(" transmitters\n");
            sb.append(data.getTransmitterLanguages().size()).append(
                    " transmitter languages\n");
            sb.append(data.getAreas().size()).append(" areas\n");
            sb.append(data.getZones().size()).append(" zones\n");
            sb.append(data.getMsgTypes().size()).append(" messaage types\n");
            sb.append(data.getReplaceMap().size()).append(
                    " replacement message types\n");
            sb.append(data.getSuites().size()).append(" suites\n");
            sb.append(data.getPrograms().size()).append(" programs\n");

            if (msgs.size() > 0) {
                sb.append("\nThe following issues were found when verifying the legacy database:\n");
                for (String msg : msgs) {
                    sb.append(msg).append("\n");
                }
            } else {
                sb.append("\nVerification found no issues.\n");
            }

            sb.append("\nSelect OK to overwrite existing database with legacy database.");
            int result = showMessage(
                    SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL,
                    "Import Legacy Database", sb.toString());
            if (result != SWT.OK) {
                input = null;
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage(SWT.ICON_ERROR | SWT.OK, "Verify Legacy Database",
                    "Unable to access current database.");
            input = null;
            return false;
        }

        return true;
    }

    /**
     * Display message from non-UI thread.
     * 
     * @param style
     * @param title
     * @param message
     * @return result
     */
    private int showMessage(final int style, final String title,
            final String message) {
        final AtomicInteger value = new AtomicInteger();
        VizApp.runSync(new Runnable() {

            @Override
            public void run() {
                int result = DialogUtility.showMessageBox(shell, style, title,
                        message);
                value.set(result);
            }
        });
        return value.get();
    }

    /**
     * Save the legacy database. Assumed invoked from a non-UI thread.
     * 
     * @param fileName
     * @return true database save successful
     */
    private boolean saveImportDB(String fileName) {
        if (input == null) {
            return false;
        }

        ImportLegacyDbRequest request = new ImportLegacyDbRequest();
        request.setInput(input.getBytes());
        request.setSource(fileName);
        Object o;

        try {
            o = BmhUtils.sendRequest(request);
            if (o instanceof Boolean) {
                boolean result = ((Boolean) o).booleanValue();
                if (result) {
                    showMessage(SWT.ICON_INFORMATION, "Import Legacy Dialog",
                            "Successfully imported:\n" + fileName);
                }
                return result;
            }
        } catch (Exception e) {
            showMessage(SWT.ICON_ERROR | SWT.OK, "Database Access",
                    "Failed to save legacy database. Database may be corruptted.");
        }
        return false;
    }

    private void fileBrowser() {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setText(DlgInfo.IMPORT_LEGACY_DB + " Browser");
        String[] filterNames = new String[] { "Legacy Database",
                "All Files (*)" };
        String[] filterExtensions = new String[] { "*.[Aa][Ss][Cc]", "*" };
        String filterPath = browserDirPath;
        dialog.setFilterNames(filterNames);
        dialog.setFilterExtensions(filterExtensions);
        dialog.setFilterPath(filterPath);
        String file = dialog.open();
        if ((file != null) && (file.length() > 0)) {
            fileTxt.setText(file);
            browserDirPath = file.substring(0, file.lastIndexOf("/"));
        }
    }

    /**
     * Method to check if the dialog can close.
     * 
     * For example: if there are items that are unsaved then the user should be
     * prompted that the dialog has unsaved items and be given the opportunity
     * to prevent the dialog from closing.
     */
    @Override
    public boolean okToClose() {
        return true;
    }
}
