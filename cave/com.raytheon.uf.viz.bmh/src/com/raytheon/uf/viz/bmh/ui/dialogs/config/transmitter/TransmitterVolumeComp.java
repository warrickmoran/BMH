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
import java.util.NavigableMap;
import java.util.TreeMap;

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
 * Slider supporting a scaled volume setting from -20 dB to 0 dB. In amplitude
 * this is 3276 to 32767.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 16, 2015 4636       bkowal      Initial creation
 * Jul 17, 2015 4636       bkowal      Added {@link #enable()} and {@link #disable()}.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude, increase
 *                                     resolution from 200 to 1000.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class TransmitterVolumeComp {

    private static final DecimalFormat displayFormat = new DecimalFormat("#.#");

    /*
     * Roughly -20db
     */
    private static final short MIN_VOLUME_AMPLITUDE = 3276;

    private static final int MAX_SCALED = 1000;

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
        this.setCurrentAmplitudeVolume(MIN_VOLUME_AMPLITUDE);
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

    public void setCurrentAmplitudeVolume(short amplitude) {
        this.setVolume = ScaledAmplitudeMapping.getInstance().getScaledMapping(
                amplitude);
        this.updateVolumeLevel(setVolume);
    }

    public short getCurrentAmplitudeValue() {
        return ScaledAmplitudeMapping.getInstance().getAmplitudeMapping(
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

    private static final class ScaledAmplitudeMapping {

        private static final ScaledAmplitudeMapping instance = new ScaledAmplitudeMapping();

        private static final double INTERVAL_AMPLITUDE = (Short.MAX_VALUE - MIN_VOLUME_AMPLITUDE)
                / (double) MAX_SCALED;

        /*
         * Utilize a navigableMap so that if scaling amount changes we find the
         * closest value in the new paradigm.
         */
        private final NavigableMap<Short, Integer> amplitudeToScaledMapping = new TreeMap<>();

        private final Map<Integer, Short> scaledToAmplitudeMapping = new HashMap<>(
                MAX_SCALED + 1, 1.0f);

        public static ScaledAmplitudeMapping getInstance() {
            return instance;
        }

        protected ScaledAmplitudeMapping() {
            for (int i = 0; i <= MAX_SCALED; i++) {
                short calculatedValue = (short) (Short.MAX_VALUE - ((MAX_SCALED - i) * INTERVAL_AMPLITUDE));
                amplitudeToScaledMapping.put(calculatedValue, i);
                scaledToAmplitudeMapping.put(i, calculatedValue);
            }
        }

        public short getAmplitudeMapping(int scaledValue) {
            return scaledToAmplitudeMapping.get(scaledValue);
        }

        public int getScaledMapping(short amplitude) {
            Short floor = amplitudeToScaledMapping.floorKey(amplitude);
            Short closest = amplitudeToScaledMapping.ceilingKey(amplitude);
            if (floor != null && ((amplitude - floor) < (closest - amplitude))) {
                closest = floor;
            }

            return amplitudeToScaledMapping.get(closest);
        }
    }
}