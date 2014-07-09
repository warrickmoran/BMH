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
package com.raytheon.uf.edex.bmh.test;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.raytheon.uf.common.status.IUFStatusHandler;

/**
 * Abstract representation of a testing capability that processes an input file
 * and returns a result.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 8, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractBMHTester {

    protected final IUFStatusHandler statusHandler;

    protected final String name;

    /**
     * 
     */
    protected AbstractBMHTester(IUFStatusHandler statusHandler,
            final String name) {
        this.statusHandler = statusHandler;
        this.name = name;
    }

    public void initialize() {
        statusHandler.info("Initializing the " + this.name + " Tester ...");
    }

    protected boolean validateTestInputs(final String inputDirectoryProperty) {
        String inputDirectory = System
                .getProperty(inputDirectoryProperty, null);

        if (inputDirectory == null) {
            /*
             * If the input directory has not been set, do not continue. This
             * will eventually cause problems when the Spring Container attempts
             * to use the configuration property directly.
             */
            statusHandler
                    .error("Failed to retrieve the Test Input Directory from the configuration. Spring Container Crash Expected!");
            return false;
        }

        File inputDirectoryFile = new File(inputDirectory);
        /* Attempt to create the specified input directory if it does not exist. */
        if (inputDirectoryFile.exists() == false) {
            statusHandler.info("Attempting to create Test Input Directory: "
                    + inputDirectory + " ...");

            /*
             * In this case, we do not care if the creation was successful or
             * not because the directory will eventually need to exist in order
             * to complete testing.
             */
            inputDirectoryFile.mkdirs();
        }

        statusHandler.info("Test Input Directory = " + inputDirectory);

        return true;
    }

    public Object process(File testFile) throws Exception {
        /*
         * Data integrity checks.
         */

        /* Verify a file has been provided and that the file exists. */
        if (testFile == null) {
            throw new Exception("File cannot be NULL!");
        }

        if (testFile.exists() == false) {
            throw new Exception("The specified file: "
                    + testFile.getAbsolutePath() + " does not exist!");
        }

        statusHandler.info("Processing input file: "
                + testFile.getAbsolutePath() + " ...");

        /* Read the file. */
        Configuration configuration = null;
        try {
            configuration = new PropertiesConfiguration(testFile);
        } catch (ConfigurationException e) {
            testFile.delete();

            throw new Exception("Failed to load the specified file: "
                    + testFile.getAbsolutePath() + "!", e);
        }

        Object output = null;
        try {
            output = this.processInput(configuration,
                    testFile.getAbsolutePath());
        } catch (TestProcessingFailedException e) {
            /*
             * Input file processing failed, manually remove the file.
             */
            testFile.delete();

            throw new Exception("Failed to process input file: "
                    + testFile.getAbsolutePath() + "!", e);
        }

        statusHandler.info("Successfully processed input file: "
                + testFile.getAbsolutePath() + ".");
        return output;
    }

    protected abstract Object processInput(Configuration configuration,
            final String inputFileName) throws TestProcessingFailedException;

    protected String getStringProperty(Configuration configuration,
            String propertyName, final String inputFileName)
            throws TestInputPropertyInvalidException,
            TestInputPropertyNotSetException {
        String propertyValue = null;
        try {
            propertyValue = configuration.getString(propertyName, null);
            if (propertyValue == null) {
                throw new TestInputPropertyNotSetException(inputFileName,
                        propertyName);
            }
        } catch (ConversionException e) {
            throw new TestInputPropertyInvalidException(inputFileName,
                    propertyName, e);
        }

        return propertyValue;
    }

    protected Double getDoubleProperty(Configuration configuration,
            String propertyName, final String inputFileName)
            throws TestInputPropertyInvalidException,
            TestInputPropertyNotSetException {
        Double propertyValue = null;
        try {
            propertyValue = configuration.getDouble(propertyName, null);
            if (propertyValue == null) {
                throw new TestInputPropertyNotSetException(inputFileName,
                        propertyName);
            }
        } catch (ConversionException e) {
            throw new TestInputPropertyInvalidException(inputFileName,
                    propertyName, e);
        }

        return propertyValue;
    }
}