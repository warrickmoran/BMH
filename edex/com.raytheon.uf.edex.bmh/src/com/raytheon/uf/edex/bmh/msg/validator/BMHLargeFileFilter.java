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
package com.raytheon.uf.edex.bmh.msg.validator;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.edex.bmh.BMHFileProcessException;
import com.raytheon.uf.edex.bmh.FileManager;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * Used to prevent files above a certain size from being submitted for
 * processing by the system. This filter was created to ensure that malicious
 * users do not intentionally submit large files to trigger continuous out of
 * heap space crashes preventing new valid data from making it through the
 * system.
 * 
 * There is already a limit on the duration of audio that will be broadcast. So,
 * the file size limit should be set in such a way that it is fully
 * accommodative of the duration limit.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2015 4136       bkowal      Initial creation
 * Nov 16, 2015 5127       rjpeter     Renamed BMHRejectionDataManager to FileManager.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHLargeFileFilter<T> implements GenericFileFilter<T> {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(BMHLargeFileFilter.class);

    private static final String FILE_SIZE_LIMIT = "bmh.file.limit";

    private static final String KB_PREFIX = "KB";

    private static final String MB_PREFIX = "MB";

    private static final int DISABLED_SIZE = -1;

    private final boolean disabled;

    private final long sizeLimit;

    private final FileManager rejectionManager;

    /**
     * Constructor.
     */
    public BMHLargeFileFilter(final FileManager rejectionManager) {
        this.rejectionManager = rejectionManager;
        String maxSize = System.getProperty(FILE_SIZE_LIMIT, null);
        if (maxSize == null) {
            this.sizeLimit = DISABLED_SIZE;
        } else {
            this.sizeLimit = this.getSizeInBytes(maxSize);
        }

        this.disabled = (this.sizeLimit <= DISABLED_SIZE);

        if (this.disabled) {
            statusHandler
                    .info("The "
                            + BMHLargeFileFilter.class.getSimpleName()
                            + " has been disabled. All incoming files will be processed by the system.");
        } else {
            statusHandler
                    .info("File Size Limit: "
                            + this.sizeLimit
                            + " bytes. All incoming files with a size greater than the specified maximum will be discarded.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.camel.component.file.GenericFileFilter#accept(org.apache.camel
     * .component.file.GenericFile)
     */
    @Override
    public boolean accept(GenericFile<T> file) {
        if (disabled) {
            return true;
        }

        boolean discard = file.getFileLength() > this.sizeLimit;
        if (discard) {
            StringBuilder sb = new StringBuilder("Incoming file: ");
            sb.append(file.getAbsoluteFilePath()).append(" with size ");
            sb.append(file.getFileLength()).append(
                    " has been rejected and will not be processed.");
            statusHandler.warn(BMH_CATEGORY.EXCESSIVE_FILE_SIZE, sb.toString());

            Path discardedFilePath = Paths.get(file.getAbsoluteFilePath());
            try {
                this.rejectionManager.processFile(discardedFilePath,
                        BMH_CATEGORY.EXCESSIVE_FILE_SIZE, false);
            } catch (BMHFileProcessException e) {
                statusHandler.error(BMH_CATEGORY.EXCESSIVE_FILE_SIZE,
                        "Unsuccessfully rejected large file: "
                                + discardedFilePath.toString() + ".", e);
            }
        }
        return (discard == false);
    }

    /**
     * Converts the specified file size in bytes, kilobytes, or megabytes to
     * bytes
     * 
     * @param specifiedSize
     *            the specified file size
     * @return the specified file size in bytes
     */
    private long getSizeInBytes(String specifiedSize) {
        specifiedSize = specifiedSize.trim().toUpperCase();

        long conversionFactor = 1;
        if (specifiedSize.endsWith(MB_PREFIX)) {
            specifiedSize = specifiedSize.replace(MB_PREFIX, StringUtils.EMPTY);
            conversionFactor = FileUtils.ONE_MB;
        } else if (specifiedSize.endsWith(KB_PREFIX)) {
            specifiedSize = specifiedSize.replace(KB_PREFIX, StringUtils.EMPTY);
            conversionFactor = FileUtils.ONE_KB;
        }

        /*
         * allow for decimals just in case the user specified a size similar to
         * 0.1MB, etc.
         */
        double numericFileSize = DISABLED_SIZE;
        try {
            numericFileSize = Double.parseDouble(specifiedSize);
        } catch (NumberFormatException e) {
            StringBuilder sb = new StringBuilder(
                    "An invalid maximum file size: ");
            sb.append(specifiedSize).append(" has been specified. The ");
            sb.append(BMHLargeFileFilter.class.getSimpleName()).append(
                    " will be disabled.");

            statusHandler.error(BMH_CATEGORY.EXCESSIVE_FILE_SIZE,
                    sb.toString(), e);
            return DISABLED_SIZE;
        }

        return (long) (numericFileSize * conversionFactor);
    }
}