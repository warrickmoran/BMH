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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.schemas.ssml.Break;
import com.raytheon.uf.common.bmh.schemas.ssml.Phoneme;
import com.raytheon.uf.common.bmh.schemas.ssml.SSMLDocument;
import com.raytheon.uf.common.bmh.schemas.ssml.SayAs;
import com.raytheon.uf.common.bmh.schemas.ssml.Speak;
import com.raytheon.uf.common.bmh.schemas.ssml.jaxb.SSMLJaxbManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.BuilderComposite.BuilderType;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.LegacyPhonemeParser;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.ParsedPhoneme;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.ParsedPhoneme.ParseType;
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
     * JaxB manager for SSML
     */
    private SSMLJaxbManager jaxb;

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
     * SSML Snippet
     */
    private String ssmlSnippet;

    /**
     * List of add buttons
     */
    private final List<Button> addBtnList = new ArrayList<Button>();

    /**
     * Constructor.
     * 
     * @param parentShell
     *            parent shell
     * @param word
     *            Word
     */
    public PronunciationBuilderDlg(Shell parentShell, String word) {
        this(parentShell, word, null);
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
            String phoneme) {
        super(parentShell, SWT.DIALOG_TRIM, CAVE.PERSPECTIVE_INDEPENDENT);
        this.word = word;
        if (phoneme != null) {
            phoneme = phoneme.trim();
        }
        this.inputPhoneme = phoneme;

        setText("Pronunciation Builder");
        try {
            jaxb = SSMLJaxbManager.getInstance();
        } catch (JAXBException e) {
            statusHandler.error("Error getting SSMLJaxBManager", e);
        }
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
        cancelBtn.setText("Close");
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

        gd = new GridData(35, SWT.DEFAULT);
        Button addBtn = new Button(c, SWT.PUSH);
        addBtn.setText(" + ");
        addBtn.setLayoutData(gd);
        addBtn.setToolTipText("Add element to the end");
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addBuilder();
            }
        });

        for (Button b : addBtnList) {
            b.setVisible(false);
        }
        addBtnList.add(addBtn);

        BuilderComposite bc = new BuilderComposite(c);
        elementList.add(bc);
        this.builderComp.layout();
        sc.setMinSize(builderComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return bc;
    }

    /**
     * Populate when ssml snippet is provided
     */
    private void populateFromInput() {
        final String REPLACE = "REPLACEME";
        if (ssmlSnippet != null) {
            try {
                SSMLDocument ssmlDocument = new SSMLDocument();
                // Need to replace contents to get correct string representation
                ssmlDocument.getRootTag().getContent().add(REPLACE);
                String xml = jaxb.getJaxbManager().marshalToXml(
                        ssmlDocument.getRootTag());

                xml = xml.replace(REPLACE, ssmlSnippet);
                JAXBElement<Speak> o = (JAXBElement<Speak>) jaxb
                        .getJaxbManager().unmarshalFromXml(xml);

                Speak speak = o.getValue();
                for (Serializable s : speak.getContent()) {
                    if (s instanceof JAXBElement<?>) {
                        JAXBElement<?> element = (JAXBElement<?>) s;
                        if (element.getValue() instanceof Phoneme) {
                            Phoneme phoneme = (Phoneme) element.getValue();
                            BuilderComposite bc = addBuilder();
                            bc.setPhoneme(phoneme);
                        } else if (element.getValue() instanceof Break) {
                            Break br = (Break) element.getValue();
                            BuilderComposite bc = addBuilder();
                            bc.setBreak(br);
                        } else if (element.getValue() instanceof SayAs) {
                            SayAs sayas = (SayAs) element.getValue();
                            BuilderComposite bc = addBuilder();
                            bc.setSayAs(sayas);
                        }
                    } else if (s instanceof String) {
                        String str = (String) s;
                        if (str.trim().length() > 0) {
                            BuilderComposite bc = addBuilder();
                            bc.setText(str);
                        }
                    }
                }
            } catch (JAXBException e) {
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
        BmhUtils.playText(text);
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
