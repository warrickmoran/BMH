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
package com.raytheon.uf.viz.bmh.ui.dialogs.config.transmitter;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

/**
 * Slider supporting a scaled volume setting from -20 dB to 0 dB.
 * 
 * -20 is 0 and 0 is 200.
 * 
 * -20 + (x * 0.1) = dB (dB + 20) / 0.1 = Scaled Volume
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2015 4636       bkowal      Initial creation
 * Jul 17, 2015 4636       bkowal      Added {@link #enable()} and {@link #disable()}.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterVolumeComp {

    private static final DecimalFormat displayFormat = new DecimalFormat("#.#");

    private static final double MIN_VOLUME_DB = -20.0;

    private static final int MAX_SCALED = 200;

    private final Composite parent;

    private final String description;

    private IVolumeChangeListener listener;

    private Label descriptionLabel;

    private Label currentValueLabel;

    private Scale volumeScale;

    private Button incrementButton;

    private Button decrementButton;

    private int setVolume;

    /**
     * 
     */
    public TransmitterVolumeComp(Composite parent, String description) {
        this.parent = parent;
        this.description = description;
        this.render();
    }

    private void render() {
        GridLayout gl = new GridLayout(5, false);
        gl.marginHeight = 1;
        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);

        Composite volumeComp = new Composite(this.parent, SWT.NONE);
        volumeComp.setLayoutData(gd);
        volumeComp.setLayout(gl);

        this.descriptionLabel = new Label(volumeComp, SWT.NONE);
        this.descriptionLabel.setText(this.description);

        gd = new GridData(200, SWT.DEFAULT);
        this.volumeScale = new Scale(volumeComp, SWT.HORIZONTAL);
        this.volumeScale.setMinimum(0);
        this.volumeScale.setMaximum(MAX_SCALED);
        this.volumeScale.setIncrement(1);
        this.volumeScale.setPageIncrement(1);
        this.volumeScale.setLayoutData(gd);
        this.volumeScale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                syncWithVolumeLevel();
            }
        });

        gd = new GridData(45, SWT.DEFAULT);
        this.currentValueLabel = new Label(volumeComp, SWT.BORDER);
        this.currentValueLabel.setLayoutData(gd);

        gd = new GridData(25, SWT.DEFAULT);
        this.decrementButton = new Button(volumeComp, SWT.PUSH);
        this.decrementButton.setText("-");
        this.decrementButton.setLayoutData(gd);
        this.decrementButton.setToolTipText("Decrease Volume");
        this.decrementButton.setEnabled(false);
        this.decrementButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int currentVolumeLevel = volumeScale.getSelection();
                updateVolumeLevel(--currentVolumeLevel);
            }
        });

        gd = new GridData(25, SWT.DEFAULT);
        this.incrementButton = new Button(volumeComp, SWT.PUSH);
        this.incrementButton.setText("+");
        this.incrementButton.setLayoutData(gd);
        this.incrementButton.setToolTipText("Increase Volume");
        this.incrementButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int currentVolumeLevel = volumeScale.getSelection();
                updateVolumeLevel(++currentVolumeLevel);
            }
        });
    }

    public void disable() {
        this.setCurrentDecibelVolume(MIN_VOLUME_DB);
        this.volumeScale.setEnabled(false);
        this.incrementButton.setEnabled(false);
        this.decrementButton.setEnabled(false);
    }

    public void enable() {
        this.volumeScale.setEnabled(true);
        this.incrementButton.setEnabled(true);
        this.decrementButton.setEnabled(true);
    }

    private void updateVolumeLevel(int newLevel) {
        this.volumeScale.setSelection(newLevel);
        this.syncWithVolumeLevel();
    }

    private void syncWithVolumeLevel() {
        this.decrementButton.setEnabled(this.volumeScale.getSelection() > 0);
        this.incrementButton
                .setEnabled(this.volumeScale.getSelection() < MAX_SCALED);
        this.currentValueLabel.setText(this.getScaledDisplay());
        if (this.listener != null) {
            this.listener.volumeChanged();
        }
    }

    public void setCurrentDecibelVolume(double volumeDb) {
        this.setVolume = ScaledDecimalMapping.getInstance().getScaledMapping(
                volumeDb);
        this.updateVolumeLevel(setVolume);
    }

    public double getCurrentDecibelValue() {
        return ScaledDecimalMapping.getInstance().getDecibelMapping(
                this.volumeScale.getSelection());
    }

    private String getScaledDisplay() {
        double scaledCurrentValue = (((double) this.volumeScale.getSelection() / (double) MAX_SCALED) * 100);
        return displayFormat.format(scaledCurrentValue) + "%";
    }

    public boolean dataChanged() {
        return this.volumeScale.getSelection() != this.setVolume;
    }

    /**
     * @return the listener
     */
    public IVolumeChangeListener getListener() {
        return listener;
    }

    /**
     * @param listener
     *            the listener to set
     */
    public void setListener(IVolumeChangeListener listener) {
        this.listener = listener;
    }

    private static final class ScaledDecimalMapping {

        private static final ScaledDecimalMapping instance = new ScaledDecimalMapping();

        private static final double INTERVAL_DB = 0.1;

        private final Map<Double, Integer> decibelToScaledMapping = new HashMap<>(
                MAX_SCALED + 1, 1.0f);

        private final Map<Integer, Double> scaledToDecibelMapping = new HashMap<>(
                MAX_SCALED + 1, 1.0f);

        public static ScaledDecimalMapping getInstance() {
            return instance;
        }

        protected ScaledDecimalMapping() {
            for (int i = 0; i <= MAX_SCALED; i++) {
                double calculatedValue = Double.valueOf(displayFormat
                        .format(MIN_VOLUME_DB + ((double) i * INTERVAL_DB)));
                decibelToScaledMapping.put(calculatedValue, i);
                scaledToDecibelMapping.put(i, calculatedValue);
            }
        }

        public double getDecibelMapping(int scaledValue) {
            return scaledToDecibelMapping.get(scaledValue);
        }

        public int getScaledMapping(double decibelValue) {
            return decibelToScaledMapping.get(decibelValue);
        }
    }
}