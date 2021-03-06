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
package com.raytheon.uf.viz.bmh.ui.recordplayback;

import java.nio.ByteBuffer;

import org.apache.commons.lang.StringUtils;
import org.csstudio.swt.widgets.figures.MeterFigure;
import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Composite used to render and maintain/update the decibel meter and decibel
 * line graph plots.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 24, 2014 3863       bkowal      Initial creation
 * Dec 1, 2014  3863       bkowal      Average audio over one second.
 * Aug 12, 2015 4424       bkowal      Increase range of decibel level meter.
 * Aug 17, 2015 4424       bkowal      Calculate audio decibel range every 0.25 seconds.
 * Aug 20, 2015 4768       bkowal      Adjusted meter range.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class DecibelPlotsComp extends Composite implements
        IAudioRecorderListener {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(DecibelPlotsComp.class);

    private final double DECIBEL_MIN_VALUE = -70.0;

    private final double DECIBEL_MAX_VALUE = -20.0;

    /* All must be specified for the level color bar to show up correctly. */
    private final double METER_LOLO_LEVEL = -60.0;

    private final double METER_LO_LEVEL = -55.0;

    private final double METER_HI_LEVEL = -25.0;

    private final double METER_HIHI_LEVEL = -20.0;

    private final String TREND_X_TITLE = "Segment";

    private final String TREND_Y_TITLE = "Decibels";

    private final int INITIAL_TREND_X_MIN = 0;

    private final int INITIAL_TREND_X_MAX = 50;

    private int trendXMin = INITIAL_TREND_X_MIN;

    private int trendXMax = INITIAL_TREND_X_MAX;

    /*
     * The minimum number of x points that will not be plotted at any point in
     * time.
     */
    private int trendEmptyBuffer = 10;

    private MeterFigure decibelMeter;

    private XYGraph decibelTrend;

    private CircularBufferDataProvider trendData;

    private double segmentCount;

    /*
     * want to collect 0.25 seconds of audio before plotting it.
     */
    private final int REQUIRED_SAMPLE_SIZE = 2000;

    private final ByteBuffer audioBuffer = ByteBuffer
            .allocate(REQUIRED_SAMPLE_SIZE);

    /**
     * Constructor
     * 
     * @param parent
     *            the parent of the composite
     */
    public DecibelPlotsComp(Composite parent) {
        super(parent, SWT.NONE);
        this.init();
    }

    /**
     * Initialization Method - define the layout. Initialize and create the
     * graph objects.
     */
    private void init() {
        GridLayout gl = new GridLayout(2, false);
        this.setLayout(gl);

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        this.setLayoutData(gd);

        this.constructDecibelMeter();
        this.constructDecibelTrend();
    }

    /**
     * Constructs the Audio Decibel Meter
     */
    private void constructDecibelMeter() {
        // create the containing canvas.
        final Canvas canvas = new Canvas(this, SWT.DOUBLE_BUFFERED);
        canvas.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        final GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 100;
        gd.widthHint = 40;
        canvas.setLayoutData(gd);

        // initialize the draw2d lightweight system.
        final LightweightSystem lws = new LightweightSystem(canvas);

        // create the chart object
        this.decibelMeter = new MeterFigure();
        this.decibelMeter.setRange(DECIBEL_MIN_VALUE, DECIBEL_MAX_VALUE);
        this.decibelMeter.setLoloLevel(METER_LOLO_LEVEL);
        this.decibelMeter.setLoLevel(METER_LO_LEVEL);
        this.decibelMeter.setHiLevel(METER_HI_LEVEL);
        this.decibelMeter.setHihiLevel(METER_HIHI_LEVEL);
        this.decibelMeter.setValue(0.0);
        this.decibelMeter.setNeedleColor(Display.getCurrent().getSystemColor(
                SWT.COLOR_BLACK));

        lws.setContents(this.decibelMeter);
    }

    /**
     * Constructs the Audio Decibel X-Y Graph
     */
    private void constructDecibelTrend() {
        // create the containing canvas.
        final Canvas canvas = new Canvas(this, SWT.DOUBLE_BUFFERED);
        canvas.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WHITE));
        final GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.heightHint = 100;
        gd.widthHint = 60;
        canvas.setLayoutData(gd);

        // initialize the draw2d lightweight system.
        final LightweightSystem lws = new LightweightSystem(canvas);

        // create the chart object
        this.decibelTrend = new XYGraph();
        this.decibelTrend.setShowLegend(false);
        this.decibelTrend.setShowTitle(false);
        this.decibelTrend.primaryXAxis.setTitle(TREND_X_TITLE);
        this.decibelTrend.primaryYAxis.setTitle(TREND_Y_TITLE);
        this.decibelTrend.primaryXAxis.setRange(this.trendXMin, this.trendXMax);
        this.decibelTrend.primaryYAxis.setRange(-90, DECIBEL_MAX_VALUE);

        // prepare a data structure to add data to the chart
        this.trendData = new CircularBufferDataProvider(false);
        // Display 50 samples on the graph at a time.
        this.trendData.setBufferSize(60);
        final Trace trace = new Trace(StringUtils.EMPTY,
                this.decibelTrend.primaryXAxis, this.decibelTrend.primaryYAxis,
                this.trendData);
        trace.setPointStyle(PointStyle.XCROSS);
        this.decibelTrend.addTrace(trace);

        lws.setContents(this.decibelTrend);
    }

    /**
     * Reset the audio decibel plots to their initial state.
     */
    public void resetPlots() {
        this.decibelMeter.setValue(0.0);
        this.trendData.clearTrace();
        this.segmentCount = 0;
        this.trendXMin = INITIAL_TREND_X_MIN;
        this.trendXMax = INITIAL_TREND_X_MAX;
        this.decibelTrend.primaryXAxis.setRange(trendXMin, trendXMax);
    }

    /**
     * Update the audio decibel plots using the specified decibel level.
     * 
     * @param sampleDecibelLevel
     *            the specified decibel level.
     */
    private synchronized void updateDecibelPlots(final double sampleDecibelLevel) {
        if (Double.isNaN(sampleDecibelLevel)
                || Double.isInfinite(sampleDecibelLevel)) {
            return;
        }

        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                decibelMeter.setValue(sampleDecibelLevel);
                ++segmentCount;
                // determine if the min x, max x need to be adjusted
                if (segmentCount >= trendXMax - trendEmptyBuffer) {
                    trendXMin += trendEmptyBuffer;
                    trendXMax += trendEmptyBuffer;
                    decibelTrend.primaryXAxis.setRange(trendXMin, trendXMax);
                }
                trendData
                        .addSample(new Sample(segmentCount, sampleDecibelLevel));
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.bmh.ui.recordplayback.IAudioRecorderListener#audioReady
     * (byte[])
     */
    @Override
    public void audioReady(byte[] audioData) {
        /*
         * Need to ensure we do not overrun the buffer for 0.25 seconds of
         * audio: 2000 mod 160 != 0.
         */
        int offsetToCopy = 0;
        if (audioData.length > this.audioBuffer.remaining()) {
            offsetToCopy = this.audioBuffer.remaining();
            this.audioBuffer.put(audioData, 0, this.audioBuffer.remaining());
        } else {
            this.audioBuffer.put(audioData);
        }
        if (this.audioBuffer.hasRemaining()) {
            return;
        }
        double sampleDecibelLevel = 0.;
        try {
            sampleDecibelLevel = DecibelCalculator
                    .calculateAudioDecibels(this.audioBuffer.array());
        } catch (Exception e) {
            // unlikely scenario
            statusHandler
                    .warn("Failed to determine the decibel level of the current audio sample!");
        }
        updateDecibelPlots(sampleDecibelLevel);
        this.audioBuffer.clear();
        if (offsetToCopy > 0) {
            final int copyLength = audioData.length - offsetToCopy;
            this.audioBuffer.put(audioData, offsetToCopy, copyLength);
        }
    }
}