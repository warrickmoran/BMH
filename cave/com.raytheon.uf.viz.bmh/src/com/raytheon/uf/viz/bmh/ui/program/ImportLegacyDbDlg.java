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
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import com.raytheon.uf.viz.bmh.ui.dialogs.MessageTextDlg;
import com.raytheon.uf.viz.bmh.voice.VoiceDataManager;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

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
 * Mar 03, 2015  #4175     bkowal      Always expect at least one voice.
 * Mar 27, 2015  #4315     rferrel     Check to allow Spanish and disable when no voices.
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

    private List<TtsVoice> voices;

    private Boolean haveSpanish;

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
        super.initializeComponents(shell);
        setText(DlgInfo.IMPORT_LEGACY_DB.getTitle());

        Composite progComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        progComp.setLayout(gl);
        progComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        GridData gd = null;

        boolean haveVoices = getAllVoices(progComp);

        if (haveVoices) {
            haveSpanish = spanishAvailable(progComp);
        }

        Composite fileComp = new Composite(progComp, SWT.NONE);
        gl = new GridLayout(3, false);
        fileComp.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
        fileComp.setLayoutData(gd);
        gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        Label fileLbl = new Label(fileComp, SWT.NONE);
        fileLbl.setText("File: ");

        fileTxt = new Text(fileComp, SWT.BORDER);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.minimumWidth = 600;
        fileTxt.setLayoutData(gd);
        fileTxt.setEnabled(haveVoices);

        Button browseBtn = new Button(fileComp, SWT.NONE);
        browseBtn.setText("Browse...");
        browseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fileBrowser();
            }
        });
        browseBtn.setEnabled(haveVoices);

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
                verifyFile(fileTxt.getText().trim());
            }
        });
        importBtn.setEnabled(haveVoices);

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
     * Determine if allowed to do Spanish.
     * 
     * @param parent
     * @return available
     */
    private boolean spanishAvailable(Composite parent) {
        try {
            return new VoiceDataManager().languageIsAvaiable(Language.SPANISH);
        } catch (Exception e) {
            String msg = "Unable to obtain language data.\n\n"
                    + BmhUtils.getRootCauseMessage(e);
            Label lbl = new Label(parent, SWT.NONE);
            lbl.setText(msg);
        }
        return false;
    }

    /**
     * Query for voices.
     * 
     * @param parent
     * @return true when at least one voice available
     */
    private boolean getAllVoices(Composite parent) {
        try {
            voices = new VoiceDataManager().getAllVoices();
        } catch (Exception e) {
            String msg = "Unable to obtain voices.\n\n"
                    + BmhUtils.getRootCauseMessage(e);
            Label lbl = new Label(parent, SWT.NONE);
            lbl.setText(msg);
            return false;
        }
        if ((voices == null) || (voices.isEmpty())) {
            Label lbl = new Label(parent, SWT.NONE);
            lbl.setText("No voices currently exist in the BMH system!");
            return false;
        }
        return true;
    }

    /**
     * Verify legacy data file. Assumed invoked from a non-UI thread.
     * 
     * @param fileName
     * @return true file ok to import.
     */
    private void verifyFile(final String fileName) {
        if (fileName.length() == 0) {
            return;
        }
        File file = new File(fileName);

        if (!file.canRead()) {
            DialogUtility.showMessageBox(shell, SWT.ICON_ERROR | SWT.OK,
                    "Verify Legacy Database",
                    "Cannot read file:\n" + file.getAbsolutePath());
            return;
        }

        byte[] fileData = new byte[(int) file.length()];
        try {
            new FileInputStream(file).read(fileData);
        } catch (IOException e) {
            DialogUtility.showMessageBox(shell, SWT.ICON_ERROR | SWT.OK,
                    "Verify Legacy Database",
                    "Unable to ready file:\n" + file.getAbsolutePath());
            input = null;
            return;
        }

        input = new String(fileData);
        BufferedReader reader = new BufferedReader(new StringReader(input));

        try {
            final AsciiFileTranslator asciiFile = new AsciiFileTranslator(
                    reader, file.getAbsolutePath(), false, voices);

            if (!asciiFile.parsedData()) {
                DialogUtility.showMessageBox(
                        shell,
                        SWT.ICON_ERROR | SWT.OK,
                        "Verify Legacy Database",
                        "No data found in the the file:\n"
                                + file.getAbsolutePath());
                input = null;
                return;
            }

            confirmMessage(fileName, asciiFile);

        } catch (Exception ex) {
            DialogUtility.showMessageBox(shell, SWT.ICON_ERROR | SWT.OK,
                    "Verify Legacy Database",
                    "Unable to access current database.");
        }
    }

    /**
     * Display message from non-UI thread.
     * 
     * @param style
     * @param title
     * @param message
     */
    private void showMessage(final int style, final String title,
            final String message) {
        VizApp.runSync(new Runnable() {

            @Override
            public void run() {
                DialogUtility.showMessageBox(shell, style | SWT.OK, title,
                        message);
            }
        });
    }

    private int determineSize(List<?> list) {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    private int determineSize(Map<?, ?> map) {
        if (map == null) {
            return 0;
        }
        return map.size();
    }

    private void confirmMessage(final String fileName,
            final AsciiFileTranslator asciiFile) {
        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        String voiceMsg = haveSpanish ? "Including Spanish information."
                : "Excluding Spanish information.";

        final String question = voiceMsg
                + "\n\nSelect OK to import legacy database.";
        List<String> msgs = asciiFile.getValidationMessages();
        final BmhData data = asciiFile.getTranslatedData();
        final StringBuilder sb = new StringBuilder();
        sb.append("Contains:\n");
        sb.append(determineSize(data.getTransmitters())).append(
                " transmitters\n");
        sb.append(determineSize(data.getTransmitterLanguages())).append(
                " transmitter languages\n");
        sb.append(determineSize(data.getAreas())).append(" areas\n");
        sb.append(determineSize(data.getZones())).append(" zones\n");
        sb.append(determineSize(data.getMsgTypes()))
                .append(" messaage types\n");
        sb.append(determineSize(data.getReplaceMap())).append(
                " replacement message types\n");
        sb.append(determineSize(data.getSuites())).append(" suites\n");

        if (data.getPrograms() != null) {
            sb.append(data.getPrograms().size()).append(" programs\n");
        }
        if (msgs.size() > 0) {
            sb.append("\nThe following issues were found when verifying the legacy database:\n");
            for (String msg : msgs) {
                sb.append(msg).append("\n");
            }
        } else {
            sb.append("\nVerification found no issues.\n");
        }
        MessageTextDlg mtd = new MessageTextDlg(shell,
                "Import Legacy DataBase", question, sb.toString(),
                SWT.ICON_QUESTION);
        mtd.setCloseCallback(new ICloseCallback() {

            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue instanceof Integer) {
                    int result = ((Integer) returnValue).intValue();
                    if (result == SWT.OK) {
                        saveImportDB(fileName);
                    } else {
                        shell.setCursor(null);
                        input = null;
                    }
                }
            }
        });
        mtd.open();
    }

    /**
     * Save the legacy database. Assumed invoked from a non-UI thread.
     * 
     * @param fileName
     * @return true database save successful
     */
    private void saveImportDB(final String fileName) {
        if (input == null) {
            return;
        }

        final AtomicBoolean saved = new AtomicBoolean(false);
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        try {
            dialog.run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    ImportLegacyDbRequest request = new ImportLegacyDbRequest();
                    request.setInput(input.getBytes());
                    request.setSource(fileName);
                    Object o;

                    monitor.beginTask(
                            "Importing Legacy database file: "
                                    + fileName.substring(fileName
                                            .lastIndexOf('/') + 1),
                            IProgressMonitor.UNKNOWN);
                    try {
                        o = BmhUtils.sendRequest(request);
                        if (o instanceof Boolean) {
                            boolean result = ((Boolean) o).booleanValue();
                            if (result) {
                                saved.set(true);
                            }
                        }
                    } catch (Exception e) {
                        showMessage(SWT.ICON_ERROR, "Database Access",
                                "Failed to save legacy database.\nDatabase may be corruptted.");
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e1) {
            DialogUtility
                    .showMessageBox(shell, SWT.ICON_ERROR | SWT.OK,
                            "Interrupted",
                            "Failed to finish save legacy database.\nDatabase may be corruptted.");
        } finally {
            if (saved.get()) {
                close();
            } else {
                shell.setCursor(null);
            }
        }

    }

    private void fileBrowser() {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setText(DlgInfo.IMPORT_LEGACY_DB.getTitle() + " Browser");
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
