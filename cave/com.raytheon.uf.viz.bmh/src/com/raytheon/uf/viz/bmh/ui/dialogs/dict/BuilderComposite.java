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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.schemas.ssml.Break;
import com.raytheon.uf.common.bmh.schemas.ssml.Phoneme;
import com.raytheon.uf.common.bmh.schemas.ssml.SayAs;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.data.DictionaryManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.dict.convert.LegacyDictionaryConverter.WordType;
import com.raytheon.uf.viz.bmh.ui.dialogs.phonemes.PhonemesEditorDlg;

/**
 * Composite for building phonemes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 25, 2014    3355    mpduff      Initial creation
 * Sep 10, 2014    3355    mpduff      Make Combo boxes readonly
 * Jan 28, 2015    4045    bkowal      Use the new {@link DictionaryManager} constructor.
 * Mar 17, 2015    4281    rferrel     Additional setSayAs and setBreak methods.
 * May 20, 2015    4490    bkowal      Added {@link #language}.
 * Jun 11, 2015    4552    bkowal      Specify the {@link Language} when playing a phoneme.
 * Mar 30, 2016    5504    bkowal      Fix GUI sizing issues.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class BuilderComposite extends Composite {
    /**
     * Phonetic types
     */
    public enum BuilderType {
        Phoneme("Phoneme"), SayAs("Say As"), Text("Text"), Break("Break"), Dynamic(
                "Dynamic");

        private final String value;

        BuilderType(String value) {
            this.value = value;
        }

        public String getType() {
            return value;
        }

        public static String[] getTypes() {
            BuilderType[] types = values();
            String[] names = new String[types.length];

            for (int i = 0; i < types.length; i++) {
                names[i] = types[i].getType();
            }

            Arrays.sort(names);
            return names;
        }

        public static BuilderType getType(String value) {
            for (BuilderType type : values()) {
                if (type.getType().equals(value)) {
                    return type;
                }
            }

            throw new IllegalArgumentException(
                    "No enum constant for BuilderType: " + value);
        }
    }

    /**
     * Break strength values
     */
    private final String[] BREAK_VALUES = new String[] { "none", "x-weak",
            "weak", "medium", "strong", "x-strong" };

    /**
     * Say as interpret types supported
     */
    private final String[] INTERPRET_VALUES = new String[] { "characters",
            "telephone" };

    /**
     * Map of Type name -> type composite
     */
    private final Map<String, Composite> typeMap = new HashMap<String, Composite>();

    /**
     * Type selection combo
     */
    private Combo typeCombo;

    /**
     * Stack composite
     */
    private Composite stackComp;

    /**
     * Stack layout
     */
    private StackLayout stackLayout;

    /**
     * Phonetic text entry field
     */
    private Text textTextFld;

    /**
     * Say as interpret combo
     */
    private Combo interpretCombo;

    /**
     * Say as text entry field
     */
    private Text sayAsTextFld;

    /**
     * Break strength combo
     */
    private Combo breakCombo;

    /**
     * Play button
     */
    private Button playBtn;

    /**
     * Phoneme text entry field
     */
    private Text phonemeTextFld;

    /**
     * Dynamic text entry field
     */
    private Text dynTextFld;

    private final Language language;

    /**
     * Constructor.
     * 
     * @param parent
     *            parent composite
     */
    public BuilderComposite(Composite parent, Language language) {
        super(parent, SWT.BORDER);
        this.language = language;
        init();
    }

    /**
     * Initialize the composite
     */
    private void init() {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(2, true);
        this.setLayout(gl);
        this.setLayoutData(gd);

        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT);
        gd.horizontalAlignment = SWT.LEFT;
        typeCombo = new Combo(this, SWT.SINGLE | SWT.READ_ONLY);
        typeCombo.setLayoutData(gd);
        typeCombo.setItems(BuilderType.getTypes());
        typeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                changeStack();
                playBtn.setEnabled(!typeCombo.getText().equals(
                        BuilderType.Break.getType()));
            }
        });

        playBtn = new Button(this, SWT.PUSH);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = playBtn.getDisplay().getDPI().x;
        playBtn.setText("Play");
        playBtn.setLayoutData(gd);
        playBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                play();
            }
        });

        // Look at using DialogUtility for this
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        Label separator = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(gd);
        separator.setBackground(this.getDisplay().getSystemColor(
                SWT.COLOR_DARK_GRAY));

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 2;
        gl = new GridLayout(1, true);
        gl.horizontalSpacing = 0;
        gl.marginWidth = 0;
        Composite c = new Composite(this, SWT.NONE);
        c.setLayout(gl);
        c.setLayoutData(gd);

        stackLayout = new StackLayout();
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        stackComp = new Composite(c, SWT.NONE);
        stackComp.setLayout(stackLayout);
        stackComp.setLayoutData(gd);

        createStackComponents();

        stackLayout.topControl = typeMap.get(BuilderType.Text.getType());
        typeCombo.select(typeCombo.indexOf(BuilderType.Text.getType()));
        stackComp.layout();
    }

    /**
     * Create the different phonetic composites for the stack layout
     */
    private void createStackComponents() {
        // Text component
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        GridLayout gl = new GridLayout(1, false);
        Composite textComp = new Composite(stackComp, SWT.NONE);
        textComp.setLayout(gl);
        textComp.setLayoutData(gd);
        typeMap.put(BuilderType.Text.getType(), textComp);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Label l = new Label(textComp, SWT.NONE);
        l.setText("Enter Plain Text");
        l.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        textTextFld = new Text(textComp, SWT.BORDER);
        textTextFld.setLayoutData(gd);

        // Say As component
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(1, false);
        Composite sayAsComp = new Composite(stackComp, SWT.NONE);
        sayAsComp.setLayout(gl);
        sayAsComp.setLayoutData(gd);
        typeMap.put(BuilderType.SayAs.getType(), sayAsComp);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        interpretCombo = new Combo(sayAsComp, SWT.SINGLE);
        interpretCombo.setLayoutData(gd);
        interpretCombo.setItems(INTERPRET_VALUES);
        interpretCombo.select(0);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.horizontalSpan = 3;
        sayAsTextFld = new Text(sayAsComp, SWT.BORDER);
        sayAsTextFld.setLayoutData(gd);

        // Phoneme component
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(1, false);
        Composite phonemeComp = new Composite(stackComp, SWT.NONE);
        phonemeComp.setLayout(gl);
        phonemeComp.setLayoutData(gd);
        typeMap.put(BuilderType.Phoneme.getType(), phonemeComp);

        Button buildBtn = new Button(phonemeComp, SWT.PUSH);
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false);
        gd.minimumWidth = buildBtn.getDisplay().getDPI().x;
        buildBtn.setText("Build Phoneme...");
        buildBtn.setLayoutData(gd);
        buildBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PhonemesEditorDlg dlg = new PhonemesEditorDlg(getShell(), "",
                        "Pronunciation", phonemeTextFld.getText(), language);
                String phonemeText = (String) dlg.open();
                if (phonemeText != null) {
                    phonemeTextFld.setText(phonemeText);
                }
            }
        });

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        phonemeTextFld = new Text(phonemeComp, SWT.BORDER);
        phonemeTextFld.setLayoutData(gd);

        // Dynamic component
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(1, false);
        Composite dynComp = new Composite(stackComp, SWT.NONE);
        dynComp.setLayout(gl);
        dynComp.setLayoutData(gd);
        typeMap.put(BuilderType.Dynamic.getType(), dynComp);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        Label dynLbl = new Label(dynComp, SWT.NONE);
        dynLbl.setText("Replacement: ");
        dynLbl.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        dynTextFld = new Text(dynComp, SWT.BORDER);
        dynTextFld.setLayoutData(gd);

        // Break component
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gl = new GridLayout(2, false);
        Composite breakComp = new Composite(stackComp, SWT.NONE);
        breakComp.setLayout(gl);
        breakComp.setLayoutData(gd);
        typeMap.put(BuilderType.Break.getType(), breakComp);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        breakCombo = new Combo(breakComp, SWT.SINGLE);
        breakCombo.setLayoutData(gd);
        breakCombo.setItems(BREAK_VALUES);
        breakCombo.select(0);
    }

    /**
     * Change the stack based on the type combo selection
     */
    private void changeStack() {
        stackLayout.topControl = typeMap.get(typeCombo.getText());
        stackComp.layout();
    }

    /**
     * Play the sound
     */
    private void play() {
        String type = typeCombo.getText();
        BuilderType bt = BuilderType.getType(type);
        String text = null;
        if (bt == BuilderType.Text) {
            text = textTextFld.getText();
            BmhUtils.playText(text, this.language);
        } else if (bt == BuilderType.SayAs) {
            String toPlay = BmhUtils.getSayAsSnippet(getSayAsType(),
                    getSayAsText());
            BmhUtils.playText(toPlay, this.language);
        } else if (bt == BuilderType.Phoneme) {
            text = phonemeTextFld.getText();
            BmhUtils.playAsPhoneme(this.getShell(), text, this.language);
        } else if (bt == BuilderType.Dynamic) {
            text = dynTextFld.getText();
            BmhUtils.playText(text, this.language);
        }
    }

    /**
     * Set the phonetic text field
     * 
     * @param text
     *            The text to set
     */
    public void setText(String text) {
        this.textTextFld.setText(text);
        for (int i = 0; i < typeCombo.getItems().length; i++) {
            String type = typeCombo.getItem(i);
            if (type.equalsIgnoreCase(BuilderType.Text.getType())) {
                typeCombo.select(i);
                changeStack();
                return;
            }
        }
    }

    /**
     * Set a {@link Break}
     */
    public void setBreak() {
        for (int i = 0; i < typeCombo.getItems().length; i++) {
            String type = typeCombo.getItem(i);
            if (type.equalsIgnoreCase(BuilderType.Break.getType())) {
                typeCombo.select(i);
                changeStack();
                playBtn.setEnabled(false);
                breakCombo.select(3);
                return;
            }
        }
    }

    /**
     * Set a {@link Break} based on the provided Break
     * 
     * @param br
     */
    public void setBreak(Break br) {
        setBreak(br.getStrength().value());
    }

    /**
     * Set a {@link Break} base on strength.
     * 
     * @param strength
     */
    public void setBreak(String strength) {
        for (int i = 0; i < typeCombo.getItems().length; i++) {
            String type = typeCombo.getItem(i);
            if (type.equalsIgnoreCase(BuilderType.Break.getType())) {
                typeCombo.select(i);
                changeStack();
                playBtn.setEnabled(false);
                breakCombo.select(breakCombo.indexOf(strength));
                return;
            }
        }
    }

    /**
     * Set the phonetic phoneme text
     * 
     * @param text
     *            The text to set
     */
    public void setPhoneme(String text) {
        this.phonemeTextFld.setText(text);
        for (int i = 0; i < typeCombo.getItems().length; i++) {
            String type = typeCombo.getItem(i);
            if (type.equalsIgnoreCase(BuilderType.Phoneme.getType())) {
                typeCombo.select(i);
                changeStack();
                return;
            }
        }
    }

    /**
     * Set the phoneme text based on the provided {@link Phoneme}
     * 
     * @param ph
     *            The phoneme to set
     */
    public void setPhoneme(Phoneme ph) {
        this.phonemeTextFld.setText(ph.getPh());
        for (int i = 0; i < typeCombo.getItems().length; i++) {
            String type = typeCombo.getItem(i);
            if (type.equalsIgnoreCase(BuilderType.Phoneme.getType())) {
                typeCombo.select(i);
                changeStack();
                return;
            }
        }
    }

    /**
     * Set the phonetic {@link SayAs} text based on the provided {@link SayAs}
     * object.
     * 
     * @param sayas
     *            The SayAs to set
     */
    public void setSayAs(SayAs sayas) {
        setSayAs(sayas.getInterpretAs(), sayas.getContent());
    }

    /**
     * Set the phonetic {@link SayAs} text.
     * 
     * @param interpretAs
     * @param content
     */
    public void setSayAs(String interpretAs, String content) {
        this.sayAsTextFld.setText(content);
        for (int i = 0; i < interpretCombo.getItemCount(); ++i) {
            String interSel = interpretCombo.getItem(i);
            if (interSel.equalsIgnoreCase(interpretAs)) {
                interpretCombo.select(i);
                break;
            }
        }
        for (int i = 0; i < typeCombo.getItems().length; i++) {
            String type = typeCombo.getItem(i);
            if (type.equalsIgnoreCase(BuilderType.SayAs.getType())) {
                typeCombo.select(i);
                changeStack();
                return;
            }
        }
    }

    /**
     * Set the dynamic text
     * 
     * @param text
     *            The text to set
     */
    public void setDynamicText(String text) {
        dynTextFld.setText(text);
        for (int i = 0; i < typeCombo.getItems().length; i++) {
            String type = typeCombo.getItem(i);
            if (type.equalsIgnoreCase(WordType.DYNAMIC.getType())) {
                typeCombo.select(i);
                changeStack();
                return;
            }
        }
    }

    /**
     * Get the {@link BuilderType} of this composite
     * 
     * @return The BuilderType
     */
    public BuilderType getType() {
        String typeText = typeCombo.getText();
        return BuilderType.getType(typeText);
    }

    /**
     * Get the phoneme text.
     * 
     * @return the text
     */
    public String getPhonemeText() {
        return this.phonemeTextFld.getText();
    }

    /**
     * Get the say as text
     * 
     * @return the text
     */
    public String getSayAsText() {
        return this.sayAsTextFld.getText();
    }

    /**
     * Get the text type text
     * 
     * @return the text
     */
    public String getText() {
        return this.textTextFld.getText();
    }

    /**
     * Get the type of {@link SayAs}
     * 
     * @return The SayAs type
     */
    public String getSayAsType() {
        return interpretCombo.getText();
    }

    /**
     * Get the break strength
     * 
     * @return the break strength
     */
    public String getBreakStrength() {
        return breakCombo.getText();
    }

    /**
     * Get the dynamic text
     * 
     * @return the text
     */
    public String getDynamicText() {
        return dynTextFld.getText();
    }
}
