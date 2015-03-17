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
package com.raytheon.uf.viz.bmh.ui.dialogs.phonemes;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.data.DictionaryManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.LegacyDictionaryConverter;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.LegacyDictionaryConverter.WordType;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Phonemes Editor
 * <p>
 * Dialog used to help operator set the appropriate phonetic representations for
 * dictionary words.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 10, 2014    3355    mpduff      Initial creation.
 * Jan 28, 2015    4045    bkowal      Provide the current {@link Shell} to the
 *                                     phoneme playback methods.
 * Feb 12, 2015    4109    bkowal      Place the phoneme at the position of the cursor. Switched
 *                                     to {@link StyledText} to that the cursor position could be
 *                                     updated after every phoneme insert.
 * Feb 19, 2015    4150    bkowal      Take the space into account when calculating the new
 *                                     phoneme cursor position.
 * Mar 17, 2015    4277    rferrel     Remove Auto Generate button and legacy label.
 * 
 * </pre>
 */
public class PhonemesEditorDlg extends CaveSWTDialog {
    // Constants
    private final String VOWEL = "vowel";

    private final String CONSONANT = "consonant";

    /** Width of the letter buttons */
    private final int letterBtnWidth = 45;

    /**
     * The DictionaryManager
     */
    private final DictionaryManager dictionaryManager;

    /**
     * The selected word
     */
    private final String selectedWord;

    /**
     * The phoneme text field
     */
    private StyledText phonemeTxt;

    /**
     * The play phoneme button
     */
    private Button phonemePlayBtn;

    /**
     * The word type combo
     */
    private Combo wordTypeCombo;

    /**
     * Right click popup menu for buttons
     */
    private Menu popupMenu;

    /**
     * The legacy phoneme
     */
    private final String legacyPhoneme;

    /**
     * The word type
     */
    private final WordType type;

    /**
     * The legacy dictionary converter
     */
    private final LegacyDictionaryConverter converter = new LegacyDictionaryConverter();

    /**
     * Constructor
     * 
     * @param parentShell
     *            The parent shell
     * @param word
     *            The word
     * @param type
     *            The type
     * @param legacyPhoneme
     *            The legacy phoneme
     * @param dictionaryManager
     *            DictionaryManager
     */
    public PhonemesEditorDlg(Shell parentShell, String word, String type,
            String legacyPhoneme, DictionaryManager dictionaryManager) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT);
        this.selectedWord = word;
        this.dictionaryManager = dictionaryManager;
        this.legacyPhoneme = legacyPhoneme;
        this.type = WordType.getWordType(type);

        this.setText("Phonemes Editor/Generator");
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
        createWordDetailsGroup(shell);
        createVowelsGroup(shell);
        createConsonantsGroup(shell);
        createBottomButtons(shell);

        String[] items = wordTypeCombo.getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(type.getType())) {
                wordTypeCombo.select(i);
                break;
            }
        }
    }

    private void createWordDetailsGroup(Composite parent) {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(2, false);
        Group wordGrp = new Group(parent, SWT.NONE);
        wordGrp.setText(" Word:  " + this.selectedWord + " ");
        wordGrp.setLayout(gl);
        wordGrp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        Composite leftComp = new Composite(wordGrp, SWT.NONE);
        leftComp.setLayout(gl);
        leftComp.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        Label wordTypeLbl = new Label(leftComp, SWT.NONE);
        wordTypeLbl.setText("Word Type: ");
        wordTypeLbl.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gd.widthHint = 150;
        wordTypeCombo = new Combo(leftComp, SWT.NONE);
        wordTypeCombo.setLayoutData(gd);
        wordTypeCombo.setItems(WordType.getTypes());

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(1, false);
        Composite rightComp = new Composite(wordGrp, SWT.NONE);
        rightComp.setLayout(gl);
        rightComp.setLayoutData(gd);

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        Label phonemeLbl = new Label(wordGrp, SWT.NONE);
        phonemeLbl.setText("New Phonemes/Substitute:");
        phonemeLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 50;
        gd.horizontalSpan = 2;
        phonemeTxt = new StyledText(wordGrp, SWT.MULTI | SWT.BORDER | SWT.WRAP
                | SWT.V_SCROLL);
        this.phonemeTxt.setWordWrap(true);
        phonemeTxt.setLayoutData(gd);
        phonemeTxt.setText(autoGenPhoneme());

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        gl = new GridLayout(2, false);
        Composite btnComp = new Composite(wordGrp, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.LEFT;
        phonemePlayBtn = new Button(btnComp, SWT.PUSH);
        phonemePlayBtn.setLayoutData(gd);
        phonemePlayBtn.setText("Play");
        phonemePlayBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BmhUtils.playAsPhoneme(getShell(), phonemeTxt.getText());
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.LEFT;
        Button clearBtn = new Button(btnComp, SWT.PUSH);
        clearBtn.setText("Clear");
        clearBtn.setLayoutData(gd);
        clearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                phonemeTxt.setText("");
            }
        });
    }

    private void createVowelsGroup(Composite parent) {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(1, false);
        Group group = new Group(parent, SWT.NONE);
        group.setText(" Vowels ");
        group.setLayout(gl);
        group.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Label vowelLbl = new Label(group, SWT.NONE);
        vowelLbl.setLayoutData(gd);
        vowelLbl.setText("Use SHIFT+vowel or CTRL+vowel for accents");

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gl = new GridLayout(8, true);
        Composite vowelButtonsComposite = new Composite(group, SWT.NONE);
        vowelButtonsComposite.setLayoutData(gd);
        vowelButtonsComposite.setLayout(gl);

        List<String> vowels = dictionaryManager.getPhonemeMapping()
                .getVowelPhonemes();
        Collections.sort(vowels);
        for (String vowel : vowels) {
            gd = new GridData(letterBtnWidth, SWT.DEFAULT);
            Button b = new Button(vowelButtonsComposite, SWT.PUSH);
            b.setText(vowel);
            b.setLayoutData(gd);
            b.setData(VOWEL);
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    handleMouseAction(e);
                }
            });
        }
    }

    private void createConsonantsGroup(Composite parent) {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Group group = new Group(parent, SWT.NONE);
        group.setText(" Consonants ");
        group.setLayout(gl);
        group.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gl = new GridLayout(8, true);
        Composite consonantsBtnComp = new Composite(group, SWT.NONE);
        consonantsBtnComp.setLayoutData(gd);
        consonantsBtnComp.setLayout(gl);

        List<String> consonants = dictionaryManager.getPhonemeMapping()
                .getConsonantPhonemes();
        Collections.sort(consonants);
        for (String consonant : consonants) {
            gd = new GridData(letterBtnWidth, SWT.DEFAULT);
            Button b = new Button(consonantsBtnComp, SWT.PUSH);
            b.setLayoutData(gd);
            b.setText(consonant);
            b.setData(CONSONANT);
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    handleMouseAction(e);
                }
            });
        }
    }

    private void createBottomButtons(Composite parent) {
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(2, false);
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button okBtn = new Button(comp, SWT.PUSH);
        okBtn.setLayoutData(gd);
        okBtn.setText("OK");
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(phonemeTxt.getText());
                close();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button cancelBtn = new Button(comp, SWT.PUSH);
        cancelBtn.setLayoutData(gd);
        cancelBtn.setText("Cancel");
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Mouse action handler.
     * 
     * @param e
     *            The MouseEvent
     */
    private void handleMouseAction(final MouseEvent e) {
        final Button srcBtn = (Button) e.getSource();
        popupMenu = new Menu(((Button) e.getSource()).getShell());
        boolean left = (e.stateMask == SWT.BUTTON1);
        String text = "";

        String textToUpdate = this.phonemeTxt.getText().trim() + " %s";
        int cursorPosition = this.phonemeTxt.getCaretOffset();
        if (cursorPosition > 0 && cursorPosition < textToUpdate.length()) {
            String currentText = this.phonemeTxt.getText();

            String txtBeforeCursor = currentText.substring(0, cursorPosition);
            String txtAfterCursor = currentText.substring(cursorPosition);
            textToUpdate = txtBeforeCursor.trim() + " %s "
                    + txtAfterCursor.trim();
        } else if (cursorPosition == 0) {
            textToUpdate = "%s " + this.phonemeTxt.getText().trim();
        }
        ++cursorPosition;

        if (srcBtn.getData().equals(VOWEL)) {
            boolean shift = false;
            boolean ctrl = false;
            shift = (e.stateMask == SWT.BUTTON1 + SWT.SHIFT);
            ctrl = (e.stateMask == SWT.BUTTON1 + SWT.CTRL);
            if (shift || ctrl) {
                left = true;
            }

            if (left) {
                if (shift) {
                    text = srcBtn.getText();
                    text += "1";
                } else if (ctrl) {
                    text = srcBtn.getText();
                    text += "2";
                } else {
                    text = srcBtn.getText();
                    text += "0";
                }
                cursorPosition += text.length();
                phonemeTxt.setText(String.format(textToUpdate, text));
                this.phonemeTxt.setCaretOffset(cursorPosition);

                BmhUtils.playAsPhoneme(this.shell, text);
            } else if (e.stateMask == SWT.BUTTON3) {
                MenuItem speakPhoneme = new MenuItem(popupMenu, SWT.NONE);
                speakPhoneme.setText("Say Phoneme without stressed accent");
                speakPhoneme.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent se) {
                        String text = srcBtn.getText();
                        BmhUtils.playAsPhoneme(getShell(), text + "0");
                    }
                });

                MenuItem speakPhoneme1 = new MenuItem(popupMenu, SWT.NONE);
                speakPhoneme1.setText("Say Phoneme with stressed level 1");
                speakPhoneme1.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent se) {
                        String text = srcBtn.getText();
                        BmhUtils.playAsPhoneme(getShell(), text + "1");
                    }
                });

                MenuItem speakPhoneme2 = new MenuItem(popupMenu, SWT.NONE);
                speakPhoneme2.setText("Say Phoneme with stressed level 2");
                speakPhoneme2.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent se) {
                        String text = srcBtn.getText();
                        BmhUtils.playAsPhoneme(getShell(), text + "2");
                    }
                });
            }
        } else {
            if (e.stateMask == SWT.BUTTON1) {
                text = srcBtn.getText();
                cursorPosition += text.length();
                phonemeTxt.setText(String.format(textToUpdate, text));
                this.phonemeTxt.setCaretOffset(cursorPosition);
                BmhUtils.playAsPhoneme(this.shell, text);
            } else if (e.stateMask == SWT.BUTTON3) {
                MenuItem speakPhoneme = new MenuItem(popupMenu, SWT.NONE);
                speakPhoneme.setText("Say Phoneme");
                speakPhoneme.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent se) {
                        String text = srcBtn.getText();
                        BmhUtils.playAsPhoneme(getShell(), text);
                    }
                });
            }
        }
        popupMenu.setVisible(true);
    }

    /**
     * Auto generate the phoneme
     * 
     * @return The generated phoneme
     */
    private String autoGenPhoneme() {
        String result = converter.convertWordOrPhoneme(this.selectedWord,
                this.type.getType(), legacyPhoneme);
        return result;
    }
}