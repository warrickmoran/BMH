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
package com.raytheon.uf.viz.bmh.ui.dialogs.dict;

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

import com.raytheon.uf.common.bmh.datamodel.language.Dictionary;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.data.DictionaryManager;
import com.raytheon.uf.viz.bmh.ui.common.utility.DialogUtility;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * New or Edit word dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2014    3407    mpduff      Initial creation
 * Aug 05, 2014 3414       rjpeter     Added BMH Thrift interface.
 * Aug 05, 2014 3175       rjpeter     Fixed saveWord validation.
 * Aug 23, 2014    3432    mpduff      Changed to Primary_modal
 * Aug 27, 2014    3432    mpduff      Set the phoneme
 * Nov 22, 2014    3740    mpduff      Clear phoneme var when phoneme text field is cleared
 * Jan 28, 2015    4045    bkowal      Provide the current {@link Shell} to the phoneme
 *                                     audio playback method.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class NewEditWordDlg extends CaveSWTDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NewEditWordDlg.class);

    /** Word text field */
    private Text wordTxt;

    /** Phoneme string */
    protected String phoneme;

    /** Phoneme text area */
    protected Text phonemeTxt;

    /** The {@link PronunciationBuilderDlg} */
    private PronunciationBuilderDlg pronunciationBuilderDlg;

    /** The Word */
    private Word word;

    /** The Dictionary the new word belongs in */
    private final Dictionary dictionary;

    /**
     * Edit word constructor.
     * 
     * @param parentShell
     *            The parent shell
     * @param word
     *            The word to edit, or null for a new word
     * @param dictionary
     *            The dictionary where this word will be added
     */
    public NewEditWordDlg(Shell parentShell, Word word, Dictionary dictionary) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL,
                CAVE.PERSPECTIVE_INDEPENDENT);

        if (word == null) {
            setText("New Word");
        } else {
            setText("Edit " + word.getWord());
        }

        this.word = word;
        this.dictionary = dictionary;
    }

    /**
     * New Word Constructor.
     * 
     * @param parentShell
     */
    public NewEditWordDlg(Shell parentShell, Dictionary dictionary) {
        this(parentShell, null, dictionary);
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite wordComp = new Composite(shell, SWT.NONE);
        wordComp.setLayout(gl);
        wordComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label wordLbl = new Label(wordComp, SWT.NONE);
        wordLbl.setText("Word/Phrase: ");
        wordLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        Composite c = new Composite(wordComp, SWT.NONE);
        c.setLayout(gl);
        c.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        wordTxt = new Text(c, SWT.BORDER);
        wordTxt.setLayoutData(gd);

        gd = new GridData(125, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        gd.verticalAlignment = SWT.CENTER;
        Button playBtn = new Button(c, SWT.PUSH);
        playBtn.setText("Pronounce Word");
        playBtn.setLayoutData(gd);
        playBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (wordTxt.getText().trim().length() > 0) {
                    BmhUtils.playText(wordTxt.getText().trim());
                }
            }
        });

        gd = new GridData(SWT.LEFT, SWT.FILL, false, true);
        gl = new GridLayout(1, false);
        Composite neoComp = new Composite(wordComp, SWT.NONE);
        neoComp.setLayout(gl);
        neoComp.setLayoutData(gd);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        Label neoLbl = new Label(neoComp, SWT.NONE);
        neoLbl.setText("Phoneme: ");
        neoLbl.setLayoutData(gd);

        gd = new GridData(105, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        Button phonemeBtn = new Button(neoComp, SWT.PUSH);
        phonemeBtn.setText("Play Phoneme");
        phonemeBtn.setLayoutData(gd);
        phonemeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (phoneme != null) {
                    BmhUtils.playBriefPhoneme(getShell(), phoneme);
                }
            }
        });

        gd = new GridData(105, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        Button clearPhonemeBtn = new Button(neoComp, SWT.PUSH);
        clearPhonemeBtn.setText("Clear Phoneme");
        clearPhonemeBtn.setLayoutData(gd);
        clearPhonemeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                phonemeTxt.setText("");
                phoneme = null;
            }
        });

        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.heightHint = 75;
        gd.widthHint = 375;
        gd.verticalSpan = 3;
        phonemeTxt = new Text(wordComp, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL
                | SWT.BORDER);
        phonemeTxt.setEditable(false);
        phonemeTxt.setLayoutData(gd);

        gl = new GridLayout(1, false);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        Composite phonemeBtnComp = new Composite(wordComp, SWT.NONE);
        phonemeBtnComp.setLayout(gl);
        phonemeBtnComp.setLayoutData(gd);

        gd = new GridData(175, SWT.DEFAULT);
        Button createPhonemeBtn = new Button(phonemeBtnComp, SWT.PUSH);
        createPhonemeBtn.setText("Create/Edit Phoneme...");
        createPhonemeBtn.setLayoutData(gd);
        createPhonemeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                launchPronunciationBuilder();
            }
        });

        DialogUtility.addSeparator(shell, SWT.HORIZONTAL);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        gl = new GridLayout(2, false);
        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(95, SWT.DEFAULT);
        Button saveWordBtn = new Button(btnComp, SWT.PUSH);
        saveWordBtn.setText("Save Word");
        saveWordBtn.setLayoutData(gd);
        saveWordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (saveWord()) {
                    close();
                }
            }
        });

        gd = new GridData(95, SWT.DEFAULT);
        Button cancelBtn = new Button(btnComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        if (word != null) {
            wordTxt.setText(word.getWord());
            phonemeTxt.setText(word.getSubstitute());
            this.phoneme = word.getSubstitute();
        }
    }

    protected boolean saveWord() {
        if (wordTxt.getText().isEmpty() || phonemeTxt.getText().isEmpty()) {
            DialogUtility
                    .showMessageBox(shell, SWT.ICON_WARNING, "Incomplete",
                            "Must have both a word and a Phoneme/Substitute to save word.");
            return false;
        }

        if (word == null) {
            word = new Word();
        }

        word.setSubstitute(phonemeTxt.getText().trim());
        word.setWord(wordTxt.getText());

        boolean isReplace = false;

        // check for existing word
        if (word.getId() > 0) {
            // check if a rename
            for (Word w : dictionary.getWords()) {
                if (w.getId() == word.getId()) {
                    if (!w.getWord().equalsIgnoreCase(word.getWord())) {
                        String msg = "Are you sure you want to rename "
                                + w.getWord() + " to " + word.getWord() + "?";
                        int answer = DialogUtility.showMessageBox(getShell(),
                                SWT.ICON_QUESTION | SWT.YES | SWT.NO,
                                "Replace Word?", msg);
                        if (answer == SWT.NO) {
                            return false;
                        }
                    }
                    break;
                }
            }

            // check for replace
            for (Word w : dictionary.getWords()) {
                if ((w.getId() != word.getId())
                        && w.getWord().equalsIgnoreCase(word.getWord())) {
                    String msg = "The word \"" + word.getWord()
                            + "\" already exists in the dictionary.\n"
                            + "Are you sure you want to replace it?";

                    int answer = DialogUtility.showMessageBox(getShell(),
                            SWT.ICON_QUESTION | SWT.YES | SWT.NO,
                            "Replace Word?", msg);
                    if (answer == SWT.NO) {
                        return false;
                    }

                    // need to update word and delete w
                    isReplace = true;
                }
            }
        } else {
            // new word
            for (Word w : dictionary.getWords()) {
                if (w.getWord().equalsIgnoreCase(word.getWord())) {
                    String msg = "The word \"" + word.getWord()
                            + "\" already exists in the dictionary.\n"
                            + "Are you sure you want to replace it?";

                    int answer = DialogUtility.showMessageBox(getShell(),
                            SWT.ICON_QUESTION | SWT.YES | SWT.NO,
                            "Replace Word?", msg);
                    if (answer == SWT.NO) {
                        return false;
                    }
                    word.setId(w.getId());
                }
            }
        }

        word.setDictionary(dictionary);

        try {
            DictionaryManager dictionaryManager = new DictionaryManager();
            if (isReplace) {
                word = dictionaryManager.replaceWord(word);
            } else {
                word = dictionaryManager.saveWord(word);
            }
        } catch (Exception e) {
            statusHandler.error("Error saving word: " + word.getWord(), e);
            return false;
        }

        setReturnValue(word);

        return true;
    }

    /**
     * Launch the {@link PronunciationBuilderDlg}
     */
    private void launchPronunciationBuilder() {
        if ((pronunciationBuilderDlg == null)
                || pronunciationBuilderDlg.isDisposed()) {
            if (phonemeTxt.getText().trim().length() > 0) {
                pronunciationBuilderDlg = new PronunciationBuilderDlg(
                        getShell(), wordTxt.getText().trim(), phonemeTxt
                                .getText().trim());
            } else {
                pronunciationBuilderDlg = new PronunciationBuilderDlg(
                        getShell(), wordTxt.getText().trim());
            }
            pronunciationBuilderDlg.setCloseCallback(new ICloseCallback() {
                @Override
                public void dialogClosed(Object returnValue) {
                    phoneme = (String) pronunciationBuilderDlg.getReturnValue();
                    if (phoneme != null) {
                        phonemeTxt.setText(phoneme);
                    }
                }
            });

            pronunciationBuilderDlg.open();
        } else {
            pronunciationBuilderDlg.bringToTop();
        }
    }
}
