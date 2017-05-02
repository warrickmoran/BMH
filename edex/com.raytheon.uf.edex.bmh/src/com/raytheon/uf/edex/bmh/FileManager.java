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
package com.raytheon.uf.edex.bmh;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.FilePermissionUtils;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.file.IOPermissionsHelper;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * A common data manager that can be used to handle rejected bmh data. Designed
 * to be injected into any BMH component that may need to reject a file.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 19, 2015 4136       bkowal      Initial creation
 * Jun 17, 2015 4490       bkowal      Handle the case when a rejected data file may have the
 *                                     same name as a previously rejected data file.
 * Jul 29, 2015 4690       rjpeter     Updated to use Date/Hour directory structure.
 * Nov 16, 2015 5127       rjpeter     Renamed to FileManager, made applicable for archiving.
 * Mar 01, 2016 5382       bkowal      Cleanup.
 * Apr 06, 2016 5552       bkowal      Added {@link #toString()}.
 * May 02, 2017 6259       bkowal      Updated to use {@link com.raytheon.uf.common.util.file.Files}.
 * </pre>
 * 
 * @author bkowal
 */

public class FileManager {

    private final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(this.getClass());

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int MAX_UNIQUE_INDEX = 99;

    private final String processType;

    private final Path dataPath;

    private final SimpleDateFormat sdf;

    /**
     * Constructor.
     * 
     * @throws BMHConfigurationException
     */
    public FileManager(String processType, String pathProperty)
            throws BMHConfigurationException {
        this.processType = processType;
        String specifiedDirectory = System.getProperty(pathProperty);
        if (specifiedDirectory == null || specifiedDirectory.trim().isEmpty()) {
            /*
             * the data destination has not been specified. ensure that spring
             * initialization fails.
             */
            throw new BMHConfigurationException(String.format(
                    "Failed to retrieve the %s Data Destination from the configuration. Please specify the %s Data Destination using the %s property.",
                    processType, processType, pathProperty));
        }

        this.dataPath = Paths.get(specifiedDirectory);
        logger.info("Using {} data destination {} ...", this.processType,
                this.dataPath.toString());

        sdf = new SimpleDateFormat("yyyyMMdd/HH/");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        /*
         * verify that the directory exists.
         */
        if (Files.exists(dataPath)) {
            /*
             * the directory exists.
             */
            return;
        }

        /*
         * the directory does not exist. attempt to create it.
         */
        try {
            com.raytheon.uf.common.util.file.Files.createDirectories(dataPath,
                    FilePermissionUtils.DIRECTORY_PERMISSIONS_ATTR);
        } catch (IOException e) {
            /*
             * failed to create the data rejection destination. ensure that
             * spring initialization fails.
             */
            throw new BMHConfigurationException(
                    "Failed to create the Data Rejection Destination: "
                            + this.dataPath.toString() + ".",
                    e);
        }

        logger.info("Successfully created {} data destination: {}",
                this.processType, this.dataPath.toString());
    }

    /**
     * Processes the specified file by relocating it to the specified
     * destination.
     * 
     * @param filePath
     *            the specified file
     * @param category
     *            identifies why the file needs to be processed
     * @throws BMHFileProcessException
     */
    public void processFile(final Path filePath, final BMH_CATEGORY category,
            boolean keepOriginal) throws BMHFileProcessException {
        if (filePath == null) {
            throw new IllegalArgumentException(
                    "Required argument filePath can not be NULL.");
        }

        String curDate;
        synchronized (sdf) {
            curDate = sdf.format(new Date());
        }

        Path targetDirectoryPath = this.dataPath.resolve(curDate);
        Path targetFilePath = targetDirectoryPath
                .resolve(filePath.getFileName());

        if (!Files.exists(targetDirectoryPath)) {
            try {
                com.raytheon.uf.common.util.file.Files.createDirectories(
                        targetDirectoryPath,
                        FilePermissionUtils.DIRECTORY_PERMISSIONS_ATTR);
            } catch (IOException e) {
                /*
                 * failed to create the data destination. ensure that spring
                 * initialization fails.
                 */
                throw new BMHFileProcessException(targetFilePath, e);
            }
        }

        /*
         * Verify that a file with the same name does not already exist.
         */
        if (Files.exists(targetFilePath)) {
            /*
             * Attempt to find a unique name for the file.
             */
            int count = 1;
            targetFilePath = targetDirectoryPath
                    .resolve(filePath.getFileName() + "_" + count);
            while (Files.exists(targetFilePath)) {
                ++count;
                if (count > MAX_UNIQUE_INDEX) {
                    /*
                     * there are already 100 files with the same name. do not
                     * attempt to copy another to the destination.
                     */
                    statusHandler.warn(category,
                            "Unable to copy " + processType + " file: "
                                    + filePath.getFileName()
                                    + ". 100 files with the same name already exist; please review the contents of the BMH "
                                    + processType + " data directory: "
                                    + this.dataPath.toString() + ".");
                    return;
                }
                targetFilePath = targetDirectoryPath
                        .resolve(filePath.getFileName() + "_" + count);
            }
        }

        BMHFileProcessException ex = null;
        try {
            if (keepOriginal) {
                Files.copy(filePath, targetFilePath);
            } else {
                Files.move(filePath, targetFilePath);
            }
        } catch (IOException e) {
            ex = new BMHFileProcessException(filePath, e);
        }

        /*
         * Attempt to adjust the file permissions to fulfill the security
         * requirements. As of May 2017, all of the files that will be processed
         * are provided by an external source (ex: NWRWaves, manually copied,
         * etc.).
         */
        try {
            IOPermissionsHelper.applyFilePermissions(filePath,
                    FilePermissionUtils.FILE_PERMISSIONS_SET);
        } catch (Exception e) {
            /*
             * File has already been copied/moved and there is no reason to undo
             * that operation. Ideally, the externally provided files would
             * always arrive with the correct permissions.
             */
            logger.warn("Failed to complete permission update for "
                    + this.processType + " file: " + targetFilePath.toString()
                    + ".", e);
        }

        if (ex == null) {
            /*
             * the file has been successfully processed.
             */
            logger.info(this.processType + " complete for file "
                    + filePath.toString() + ".");
            return;
        }

        /*
         * the file has not been processed and still exists. Attempt to purge
         * the file if original was not to be kept.
         */
        if (!keepOriginal) {
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                statusHandler.error(category,
                        "Failed to purge unsuccessfully processed "
                                + this.processType + " file: "
                                + filePath.toString() + ".",
                        e);
            }
        }

        throw ex;
    }

    public int purge(int archiveDays, int retentionDays) {
        int filesDeleted = 0;
        logger.info("Purging " + this.processType + " files.");

        Pattern dayPattern = Pattern.compile("(\\d{8})(?:.zip)?");
        SimpleDateFormat dayParser = new SimpleDateFormat("yyyyMMdd");
        Calendar tmp = TimeUtil.newGmtCalendar();
        tmp.add(Calendar.DAY_OF_MONTH, -archiveDays);
        Date archiveThreshold = tmp.getTime();
        tmp = TimeUtil.newGmtCalendar();
        tmp.add(Calendar.DAY_OF_MONTH, -retentionDays);
        Date purgeThreshold = tmp.getTime();

        try (DirectoryStream<Path> stream = Files
                .newDirectoryStream(dataPath)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                Matcher dayMatcher = dayPattern.matcher(name);

                if (Files.isDirectory(entry)) {
                    if (dayMatcher.matches()) {
                        Date day = dayParser.parse(dayMatcher.group(1));

                        if (purgeThreshold.after(day)) {
                            /*
                             * Send Long.MAX_VALUE for purge threshold to purge
                             * all files
                             */
                            logger.info(
                                    "Purging directory " + entry.toString());
                            purgeDir(entry, Long.MAX_VALUE);
                        } else if (archiveThreshold.after(day)) {
                            logger.info("Compressing directory "
                                    + entry.toString());
                            compressDir(entry);
                        }
                    } else {
                        logger.info("Purging entries older than "
                                + purgeThreshold.getTime() + " from directory "
                                + entry.toString());
                        purgeDir(entry, purgeThreshold.getTime());
                    }
                } else {
                    boolean purgeFile = false;
                    if (dayMatcher.matches()) {
                        Date day = dayParser.parse(dayMatcher.group(1));
                        purgeFile = purgeThreshold.after(day);
                    } else {
                        purgeFile = Files.getLastModifiedTime(entry)
                                .toMillis() < purgeThreshold.getTime();
                    }

                    if (purgeFile) {
                        try {
                            logger.info("Purging " + entry.toString());
                            Files.deleteIfExists(entry);
                        } catch (IOException e) {
                            logger.error(
                                    "Failed to purge " + this.processType
                                            + " file: "
                                            + entry.toAbsolutePath().toString(),
                                    e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to purge " + this.processType + " files.", e);
        }

        return filesDeleted;
    }

    /**
     * Add files to archive and deletes original file.
     * 
     * @param zipName
     * @param files
     * @return
     */
    protected void compressDir(Path dir) {
        String zipName = dir.toString() + ".zip";

        File zipFile = new File(zipName);

        if (zipFile.exists()) {
            // zip file already exists, don't do anything
            return;
        }

        try (ZipOutputStream zos = new ZipOutputStream(
                IOPermissionsHelper.getOutputStream(zipFile.toPath(),
                        FilePermissionUtils.FILE_PERMISSIONS_SET))) {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) throws IOException {
                    if (Files.exists(file)) {
                        zos.putNextEntry(new ZipEntry(
                                dataPath.relativize(file).toString()));
                        Files.copy(file, zos);
                        zos.closeEntry();
                        Files.delete(file);
                    }
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                        IOException exc) throws IOException {
                    try (DirectoryStream<Path> stream = Files
                            .newDirectoryStream(dir)) {
                        if (!stream.iterator().hasNext()) {
                            Files.deleteIfExists(dir);
                        }
                    }

                    return super.postVisitDirectory(dir, exc);
                }
            });
        } catch (IOException e) {
            logger.error("Unexpected exception caught", e);
        }
    }

    protected void purgeDir(Path dir, final long purgeThreshold) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) throws IOException {
                    if (attrs.lastModifiedTime().toMillis() < purgeThreshold) {
                        Files.deleteIfExists(file);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file,
                        IOException exc) throws IOException {
                    logger.error("Unable to purge file: " + file, exc);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                        IOException exc) throws IOException {
                    try (DirectoryStream<Path> stream = Files
                            .newDirectoryStream(dir)) {
                        if (!stream.iterator().hasNext()) {
                            Files.deleteIfExists(dir);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("Unable to purge directory: " + dir, e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FileManager [processType=");
        sb.append(this.processType).append(", dataPath=")
                .append(this.dataPath.toString());
        sb.append("]");

        return sb.toString();
    }
}