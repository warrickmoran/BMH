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
package com.raytheon.uf.viz.bmh.ui.dialogs.voice;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * Component that allows for selecting and clearing an already selected
 * {@link Dictionary}. A {@link SelectDictionaryDlg} is used to display the
 * dictionaries that are available for selection.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 03, 2015  4424       bkowal      Initial creation
 * Mar 30, 2016  5504       bkowal      Fix GUI sizing issues.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DictionaryAssignmentComp {

    private enum ASSIGN_MODE {
        ADD("Add..."), CLEAR("Clear");

        private final String text;

        private ASSIGN_MODE(String text) {
            this.text = text;
        }

        protected String getText() {
            return this.text;
        }
    }

    private ASSIGN_MODE mode;

    private Dictionary selectedDictionary;

    private Language selectedLanguage;

    private final Composite parent;

    private final Shell shell;

    private final IDictionarySelectionListener listener;

    private Label selectedDictionaryLabel;

    private Button addClearBtn;

    public DictionaryAssignmentComp(final Composite parent, final Shell shell,
            final IDictionarySelectionListener listener) {
        this.parent = parent;
        this.shell = shell;
        this.listener = listener;

        this.initialize();
    }

    private void initialize() {
        GridLayout gl = new GridLayout(3, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;

        Composite composite = new Composite(this.parent, SWT.NONE);
        composite.setLayout(gl);
        composite.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        Label dictionaryLabel = new Label(composite, SWT.RIGHT);
        dictionaryLabel.setText("Dictionary: ");
        dictionaryLabel.setLayoutData(gd);

        this.selectedDictionaryLabel = new Label(composite, SWT.BORDER);
        GC gc = new GC(this.selectedDictionaryLabel);
        int textWidth = gc.getFontMetrics().getAverageCharWidth() * 34;
        gc.dispose();
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.minimumWidth = textWidth;
        this.selectedDictionaryLabel.setLayoutData(gd);

        this.addClearBtn = new Button(composite, SWT.PUSH);
        gd = new GridData(SWT.DEFAULT, SWT.CENTER, true, false);
        gd.minimumWidth = this.addClearBtn.getDisplay().getDPI().x;
        this.addClearBtn.setLayoutData(gd);
        this.addClearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAddClearAction();
            }
        });

        this.syncDictionaryMode();
    }

    public void disable() {
        this.addClearBtn.setEnabled(false);
    }

    public void enable() {
        this.addClearBtn.setEnabled(true);
    }

    public boolean dictionaryChanged(final Dictionary toCompare) {
        if (toCompare == null && this.selectedDictionary == null) {
            return false;
        }

        return (toCompare == null) ? true : (toCompare
                .equals(this.selectedDictionary) == false);
    }

    private void syncDictionaryMode() {
        this.mode = (this.selectedDictionary == null) ? ASSIGN_MODE.ADD
                : ASSIGN_MODE.CLEAR;
        this.addClearBtn.setText(this.mode.getText());
    }

    private void handleAddClearAction() {
        if (this.mode == ASSIGN_MODE.CLEAR) {
            this.setSelectedDictionary(null);
            this.selectedDictionaryLabel.setText(StringUtils.EMPTY);
            this.syncDictionaryMode();
            return;
        }

        SelectDictionaryDlg selectDictDlg = new SelectDictionaryDlg(this.shell,
                this.selectedLanguage);
        selectDictDlg.setFilterDictionary(this.selectedDictionary);
        selectDictDlg.setCloseCallback(new ICloseCallback() {
            @Override
            public void dialogClosed(Object returnValue) {
                if (returnValue instanceof Dictionary) {
                    setSelectedDictionary((Dictionary) returnValue);
                } else {
                    setSelectedDictionary(null);
                }
            }
        });
        selectDictDlg.open();
    }

    /**
     * @return the selectedDictionary
     */
    public Dictionary getSelectedDictionary() {
        return selectedDictionary;
    }

    /**
     * @param selectedDictionary
     *            the selectedDictionary to set
     */
    public void setSelectedDictionary(Dictionary selectedDictionary) {
        this.selectedDictionary = selectedDictionary;
        this.selectedDictionaryLabel
                .setText(this.selectedDictionary == null ? StringUtils.EMPTY
                        : this.selectedDictionary.getName());
        this.syncDictionaryMode();
        this.listener.dictionarySelected(this.selectedDictionary);
    }

    /**
     * @param selectedLanguage
     *            the selectedLanguage to set
     */
    public void setSelectedLanguage(Language selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }
}