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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.datamodel.language.Word;
import com.raytheon.uf.common.bmh.schemas.ssml.Break;
import com.raytheon.uf.common.bmh.schemas.ssml.Phoneme;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.SayAs;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.BuilderComposite.BuilderType;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.LegacyPhonemeParser;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.ParsedPhoneme;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.ParsedPhoneme.ParseType;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.SSMLPhonemeParser;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to build phonemes/pronunciations for words/phrases
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 25, 2014    3355    mpduff      Initial creation
 * Jul 21, 2014    3407    mpduff      Removed unused variable
 * Feb 24, 2015    4157    bkowal      Specify a {@link Language} for the {@link SSMLDocument}.
 * Mar 10, 2015    4253    rferrel     Change label on cancel button.
 * Mar 16, 2015    4283    bkowal      Use {@link SSMLPhonemeParser}.
 * Mar 17, 2015    4281    rferrel     Add delete/insert of a builder composite.
 * May 20, 2015    4490    bkowal      Added {@link #language}.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class PronunciationBuilderDlg extends CaveSWTDialog {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PronunciationBuilderDlg.class);

    /**
     * Label showing results
     */
    private Label resultLbl;

    /**
     * Sound group
     */
    private Group soundGrp;

    /**
     * Scrolled composite holding the individual parts
     */
    private ScrolledComposite sc;

    /**
     * Composite inside the scrolled composite
     */
    private Composite builderComp;

    /**
     * List of {@link BuilderComposite} elements
     */
    private final List<BuilderComposite> elementList = new ArrayList<BuilderComposite>();

    /**
     * The word
     */
    private final String word;

    /**
     * Input Phoneme
     */
    private final String inputPhoneme;

    /**
     * The {@link Language} that the phoneme is being built for.
     */
    private final Language language;

    /**
     * SSML Snippet
     */
    private String ssmlSnippet;

    /**
     * Listener to insert element at the top of the list.
     */
    private final SelectionListener topBcListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            insertElement(0);
        }
    };

    /**
     * Listener to insert a new BuilderComposite after the one associated with
     * the button.
     */
    private final SelectionListener addBCListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            insertElement((BuilderComposite) e.widget.getData());
        }
    };

    /**
     * Delete the BuilderComposite associated with the button.
     */
    private final SelectionListener deleteBCListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            deleteElement(e.widget.getData());
        }
    };

    /**
     * Constructor.
     * 
     * @param parentShell
     *            parent shell
     * @param word
     *            Word
     */
    public PronunciationBuilderDlg(Shell parentShell, String word,
            Language language) {
        this(parentShell, word, null, language);
    }

    /**
     * Constructor.
     * 
     * @param parentShell
     *            Parent shell
     * @param word
     *            word
     * @param phoneme
     *            phoneme string
     * @param wordType
     *            WordType
     */
    public PronunciationBuilderDlg(Shell parentShell, String word,
            String phoneme, Language language) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT);
        this.word = word;
        if (phoneme != null) {
            phoneme = phoneme.trim();
        }
        this.inputPhoneme = phoneme;
        this.language = language;

        setText("Pronunciation Builder");
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumWidth = 450;
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        resultLbl = new Label(shell, SWT.NONE);
        resultLbl.setLayoutData(gd);
        resultLbl.setText("Generating Pronunciation:  " + word);

        final Composite c = new Composite(shell, SWT.NONE);
        c.setLayout(new GridLayout(1, false));
        c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout gl = new GridLayout(1, false);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        soundGrp = new Group(c, SWT.BORDER);
        soundGrp.setText(" Sound Elements ");
        soundGrp.setLayout(gl);
        soundGrp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 500;
        gd.heightHint = 500;
        gl = new GridLayout(1, false);
        sc = new ScrolledComposite(soundGrp, SWT.BORDER | SWT.V_SCROLL);
        sc.setLayout(gl);
        sc.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl = new GridLayout(1, false);
        builderComp = new Composite(sc, SWT.NONE);
        builderComp.setLayout(gl);
        builderComp.setLayoutData(gd);

        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        sc.setContent(builderComp);
        sc.layout();
        sc.setMinSize(builderComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        if (inputPhoneme != null || ssmlSnippet != null) {
            populateFromInput();
        } else {
            addBuilder();
        }

        createBottomButtons(shell);

        gd = new GridData(85, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.CENTER;
        Button playAllBtn = new Button(soundGrp, SWT.PUSH);
        playAllBtn.setText("Play All");
        playAllBtn.setLayoutData(gd);
        playAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                playPhrase(getAllText());
            }
        });
    }

    private void createBottomButtons(Composite comp) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite btnComp = new Composite(comp, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button okBtn = new Button(btnComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(getAllText());
                close();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button cancelBtn = new Button(btnComp, SWT.PUSH);
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
     * Add a new {@link BuilderComposite} element
     * 
     * @return the new BuilderComposite
     */
    private BuilderComposite addBuilder() {
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        gd.widthHint = 490;
        GridLayout gl = new GridLayout(2, false);
        Composite c = new Composite(builderComp, SWT.NONE);
        c.setLayout(gl);
        c.setLayoutData(gd);

        Composite btnC = new Composite(c, SWT.NONE);
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gl = new GridLayout(1, false);
        btnC.setLayout(gl);
        btnC.setLayoutData(gd);

        BuilderComposite bc = new BuilderComposite(c, this.language);

        Button btn = createButton(btnC, " ^ ", "Add element at start", bc);
        btn.addSelectionListener(topBcListener);
        btn.setVisible(elementList.isEmpty());
        bc.setData(btn);

        btn = createButton(btnC, " - ", "Delete element", bc);
        btn.addSelectionListener(deleteBCListener);

        btn = createButton(btnC, " + ", "Add element after this element", bc);
        btn.addSelectionListener(addBCListener);

        elementList.add(bc);
        this.builderComp.layout();
        sc.setMinSize(builderComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return bc;
    }

    /**
     * Created consistent buttons associated with a given builder composite.
     * 
     * @param parent
     * @param text
     * @param help
     * @param bc
     * @return btn
     */
    private Button createButton(Composite parent, String text, String help,
            BuilderComposite bc) {
        GridData gd = new GridData(35, SWT.DEFAULT);
        Button btn = new Button(parent, SWT.DEFAULT);
        btn.setText(text);
        btn.setLayoutData(gd);
        btn.setToolTipText(help);
        btn.setData(bc);
        return btn;
    }

    /**
     * Only display insert at the top button on the first sound element.
     */
    private void refreshTopBtnDisplay() {
        boolean visible = true;
        for (BuilderComposite bc : elementList) {
            ((Button) bc.getData()).setVisible(visible);
            visible = false;
        }
    }

    /**
     * Remove the builder composite from the play list.
     * 
     * 
     * @param o
     */
    private void deleteElement(Object o) {
        BuilderComposite bc = (BuilderComposite) o;
        int index = elementList.indexOf(bc);

        bc.getParent().dispose();
        elementList.remove(bc);

        if (elementList.isEmpty()) {
            addBuilder();
            index = 0;
        } else if (index >= elementList.size()) {
            index--;
        }

        builderComp.pack();
        builderComp.layout();
        sc.showControl(elementList.get(index));
        refreshTopBtnDisplay();
    }

    /**
     * Insert element after the {@link BuilderComposite}.
     * 
     * @param bc
     */
    private void insertElement(BuilderComposite bc) {
        int index = elementList.indexOf(bc) + 1;
        insertElement(index);
    }

    /**
     * Insert element given index of elementList.
     * 
     * @param index
     */
    private void insertElement(int index) {
        /*
         * Get list of elements that come after the new element. They will need
         * to be disposed and appended back in.
         */
        int size = elementList.size();
        List<BuilderComposite> reinsertBC = new ArrayList<>(size);
        for (int i = index; i < size; ++i) {
            reinsertBC.add(elementList.get(i));
        }

        addBuilder();

        for (BuilderComposite oldBc : reinsertBC) {
            BuilderComposite bc = addBuilder();

            switch (oldBc.getType()) {
            case Text:
                bc.setText(oldBc.getText());
                break;
            case Break:
                String brString = oldBc.getBreakStrength();
                bc.setBreak(brString);
                break;
            case Phoneme:
                bc.setPhoneme(oldBc.getPhonemeText());
                break;
            case SayAs:
                bc.setSayAs(oldBc.getSayAsType(), oldBc.getSayAsText());
                break;
            case Dynamic:
                bc.setDynamicText(oldBc.getDynamicText());
                break;
            }
            oldBc.getParent().dispose();
            elementList.remove(oldBc);
        }
        builderComp.pack();
        builderComp.layout();
        sc.showControl(elementList.get(index));
        refreshTopBtnDisplay();
    }

    /**
     * Populate when ssml snippet is provided. When working from ssmlSnippet
     * insert a new element at when newElementIndex is a valid index for the
     * play list.
     */
    private void populateFromInput() {
        if (ssmlSnippet != null) {
            try {

                List<Serializable> ssmlContents = SSMLPhonemeParser
                        .parse(ssmlSnippet);

                for (Serializable s : ssmlContents) {
                    if (s instanceof Phoneme) {
                        BuilderComposite bc = addBuilder();
                        bc.setPhoneme((Phoneme) s);
                    } else if (s instanceof Break) {
                        BuilderComposite bc = addBuilder();
                        bc.setBreak((Break) s);
                    } else if (s instanceof SayAs) {
                        BuilderComposite bc = addBuilder();
                        bc.setSayAs((SayAs) s);
                    } else if (s instanceof String) {
                        String str = (String) s;
                        if (str.trim().isEmpty() == false) {
                            BuilderComposite bc = addBuilder();
                            if (str.contains(Word.DYNAMIC_NUMERIC_CHAR)) {
                                bc.setDynamicText(str);
                            } else {
                                bc.setText(str.trim());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                statusHandler.error("Error parsing data.", e);
            }
        } else {
            List<ParsedPhoneme> phonemes = new LegacyPhonemeParser()
                    .parse(inputPhoneme);

            for (ParsedPhoneme phoneme : phonemes) {
                BuilderComposite bc;
                bc = addBuilder();

                if (phoneme.getType() == ParseType.Text) {
                    bc.setText(phoneme.getParsedValue());
                } else if (phoneme.getType() == ParseType.Phoneme) {
                    bc.setPhoneme(phoneme.getParsedValue());
                } else if (phoneme.getType() == ParseType.Dynamic) {
                    bc.setDynamicText(phoneme.getParsedValue());
                } else {
                    bc.setBreak();
                }
            }
        }
    }

    /**
     * Build the final product and return it.
     * 
     * @return The full text string
     */
    private String getAllText() {
        StringBuilder sb = new StringBuilder();

        for (BuilderComposite bc : elementList) {
            if (bc.getType() == BuilderType.Phoneme) {
                sb.append(BmhUtils.PHONEME_OPEN).append(bc.getPhonemeText())
                        .append(BmhUtils.PHONEME_CLOSE);
            } else if (bc.getType() == BuilderType.SayAs) {
                sb.append(BmhUtils.getSayAsSnippet(bc.getSayAsType(),
                        bc.getSayAsText()));
            } else if (bc.getType() == BuilderType.Break) {
                sb.append("<break strength=\"").append(bc.getBreakStrength())
                        .append("\"/>");
            } else if (bc.getType() == BuilderType.Dynamic) {
                sb.append(bc.getDynamicText());
            } else {
                // plain text
                sb.append(bc.getText());
            }
        }

        return sb.toString();
    }

    /**
     * Play the phrase
     * 
     * @param text
     *            The text to play
     */
    private void playPhrase(String text) {
        BmhUtils.playText(text, this.language);
    }

    /**
     * Set the SSML Snippet
     * 
     * @param ssmlSnippet
     *            The snippet
     */
    public void setSsmlSnippet(String ssmlSnippet) {
        this.ssmlSnippet = ssmlSnippet;
    }
}
