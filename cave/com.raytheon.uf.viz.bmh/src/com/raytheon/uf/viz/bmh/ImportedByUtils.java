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
package com.raytheon.uf.viz.bmh;

import java.nio.file.Path;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.VizApp;

/**
 * Utility to generate and check for the default "Imported By ..." message for
 * imported audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 16, 2015 4118       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ImportedByUtils {

    private static final String IMPORTED_BY_PREFIX = "#Imported by ";

    private static final String FROM_FILE = " from file ";

    private static final String MSG_ON = " on ";

    private static final String MSG_TERMINATOR = ".";

    /**
     * Constructor - protected to prevent instantiation of this class.
     */
    protected ImportedByUtils() {
    }

    /**
     * Builds the imported by message.
     * 
     * @param importedFilePath
     *            the {@link Path} of the file that was imported.
     * @return the imported by message that is constructed.
     */
    public static String getMessage(final Path importedFilePath) {
        StringBuilder sb = new StringBuilder(IMPORTED_BY_PREFIX);
        sb.append(VizApp.getWsId().getUserName());
        sb.append(FROM_FILE);
        sb.append(importedFilePath.toString());
        sb.append(MSG_ON);
        sb.append(TimeUtil.newCalendar().getTime().toString());
        sb.append(MSG_TERMINATOR);

        return sb.toString();
    }

    /**
     * Determines if the specified message is an imported by message.
     * 
     * @param msg
     *            the specified message
     * @return true, if the message is an imported by message; false, otherwise.
     */
    public static boolean isMessage(final String msg) {
        return msg.trim().startsWith(IMPORTED_BY_PREFIX);
    }
}