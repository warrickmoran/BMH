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
package com.raytheon.uf.common.bmh;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import com.raytheon.uf.common.util.file.IOPermissionsHelper;

/**
 * BMH File I/O utility class to ensure that all files written and updated by
 * BMH have access permissions set to 664. Note: this class will only work
 * correctly on Linux and as a result this class is not OS independent.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 1, 2017  6259       bkowal      Initial creation
 *
 * </pre>
 *
 * @author bkowal
 */

public final class FilePermissionUtils {

    public static final Set<PosixFilePermission> FILE_PERMISSIONS_SET = IOPermissionsHelper
            .getPermissionsAsSet(
                    new PosixFilePermission[] { PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.GROUP_READ,
                            PosixFilePermission.GROUP_WRITE,
                            PosixFilePermission.OTHERS_READ });

    public static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS_SET = IOPermissionsHelper
            .getPermissionsAsSet(
                    new PosixFilePermission[] { PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.GROUP_READ,
                            PosixFilePermission.GROUP_WRITE,
                            PosixFilePermission.GROUP_EXECUTE,
                            PosixFilePermission.OTHERS_READ,
                            PosixFilePermission.OTHERS_EXECUTE });

    public static final FileAttribute<Set<PosixFilePermission>> DIRECTORY_PERMISSIONS_ATTR = PosixFilePermissions
            .asFileAttribute(DIRECTORY_PERMISSIONS_SET);

    private FilePermissionUtils() {
    }

    /**
     * Writes the specified contents to the specified file {@link Path} with the
     * 664 permissions applied.
     * 
     * @param filePath
     *            the specified file {@link Path}.
     * @param contents
     *            the contents to write
     * @throws FilePermissionException
     * @throws IOException
     */
    public static void writeFileContents(final Path filePath,
            final byte[] contents) throws IOException {
        try (OutputStream os = IOPermissionsHelper.getOutputStream(filePath,
                FILE_PERMISSIONS_SET)) {
            os.write(contents);
        }
    }
}