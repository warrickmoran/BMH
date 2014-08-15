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
package com.raytheon.uf.edex.bmh.test.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses a prior set of data to simulate current data. Updates effective YYYYMMDD
 * to be for the current day. Drops files in to the specified directory based on
 * the mod time of the data in the given day. Has option to drop in all files
 * that would currently be effective to allow for initial start up of a clean
 * system.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 12, 2014 3515       rjpeter     Initial creation.
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class DataSimulator {
    private static final long MILLIS_PER_SECOND = 1000;

    private static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;

    private static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;

    private static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    private static final SimpleDateFormat logFormatter = new SimpleDateFormat(
            "yyyyMMdd HH:mm:ss.SSS");

    static {
        logFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final boolean DO_INITIAL_LOAD = Boolean.getBoolean("doInitialLoad");

    private final SimpleDateFormat dateParser = new SimpleDateFormat(
            "yyMMddHHmm");

    // Pattern to match input header, times are in epoch
    private final Pattern HEADER_PATTERN = Pattern
            .compile("^(\\ea[A-Z]_[A-Z]{3}[A-Z]{9})(\\d{10})(\\d{10})(.*)(\\d{10})$");

    private final Path INPUT_PATH;

    private final Path OUTPUT_PATH;

    // Path to its associated createTime in the current day
    private final SortedMap<Long, List<Path>> pathTimes = new TreeMap<>();

    /**
     * @throws IOException
     * 
     */
    public DataSimulator() throws IOException {
        dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));

        // validate input and output directories
        FileSystem fs = FileSystems.getDefault();
        String dir = System.getProperty("inputDir",
                "/awips2/bmh/data/nwr/simulate");
        INPUT_PATH = fs.getPath(dir);

        if (Files.notExists(INPUT_PATH)) {
            try {
                Files.createDirectories(INPUT_PATH);
            } catch (Exception e) {
                throw new IOException("Cannot create input directory: " + dir,
                        e);
            }
        } else if (!Files.isDirectory(INPUT_PATH)) {
            throw new IOException("Input directory is not a directory: " + dir);
        } else if (!Files.isReadable(INPUT_PATH)) {
            throw new IOException("Cannot read from input directory: " + dir);
        }

        dir = System.getProperty("outputDir", "/awips2/bmh/data/nwr/ready");
        OUTPUT_PATH = fs.getPath(dir);

        if (Files.notExists(OUTPUT_PATH)) {
            try {
                Files.createDirectories(OUTPUT_PATH);
            } catch (Exception e) {
                throw new IOException("Cannot create output directory: " + dir,
                        e);
            }
        } else if (!Files.isDirectory(OUTPUT_PATH)) {
            throw new IOException("Output directory is not a directory: " + dir);
        } else if (!Files.isReadable(OUTPUT_PATH)) {
            throw new IOException("Cannot read from output directory: " + dir);
        }
    }

    /**
     * Launches thread to watch input directory for any changes.
     * 
     * @throws IOException
     */
    private void startDirectoryWatcher() throws IOException {
        final WatchService watchService = FileSystems.getDefault()
                .newWatchService();
        INPUT_PATH.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
        Thread t = new Thread("Directory Watcher") {
            @Override
            public void run() {
                while (true) {

                    try {
                        WatchKey key = watchService.poll(60, TimeUnit.SECONDS);
                        if (key != null) {
                            Path parent = ((Path) key.watchable());

                            List<WatchEvent<?>> events = key.pollEvents();
                            if (events != null) {
                                for (WatchEvent<?> event : events) {
                                    @SuppressWarnings("unchecked")
                                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                                    Path path = parent.resolve(pathEvent
                                            .context());
                                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                        addPath(path);
                                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                        removePath(path);
                                        addPath(path);
                                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                        removePath(path);
                                    }
                                }
                            }

                            key.reset();
                        }
                    } catch (Throwable e) {
                        logError("Error occurred watching directory: "
                                + INPUT_PATH, e);
                    }
                }
            }
        };

        t.start();
    }

    /**
     * Does initial scan of the input directory.
     * 
     * @throws IOException
     */
    private void doInitialScan() throws IOException {
        // walk directory
        Files.walkFileTree(INPUT_PATH, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) throws IOException {
                try {
                    addPath(file);
                } catch (ParseException e) {
                    logError("Error processing file: "
                            + file.toFile().getAbsolutePath(), e);
                }

                return FileVisitResult.CONTINUE;
            }
        });

    }

    /**
     * Main method that will copy files from input directory to output directory
     * based on the original file creation time.
     */
    private void processFiles() {
        while (true) {
            try {
                SortedMap<Long, List<Path>> pathsToProcess = null;
                synchronized (pathTimes) {
                    Long keyTime = System.currentTimeMillis();
                    SortedMap<Long, List<Path>> headMap = pathTimes
                            .headMap(keyTime);

                    if ((headMap == null) || headMap.isEmpty()) {
                        if (pathTimes.isEmpty()) {
                            pathTimes.wait(MILLIS_PER_HOUR);
                        } else {
                            Long nextTime = pathTimes.firstKey();
                            if (nextTime != null) {
                                pathTimes.wait((nextTime - keyTime)
                                        + MILLIS_PER_SECOND);
                            }
                        }
                    } else {
                        pathsToProcess = new TreeMap<>(headMap);
                        // clear entries that are about to be processed
                        headMap.clear();

                        // re-add entries 24 hours in future
                        for (Map.Entry<Long, List<Path>> entry : pathsToProcess
                                .entrySet()) {
                            Long newKey = entry.getKey() + MILLIS_PER_DAY;
                            List<Path> paths = pathTimes.get(newKey);
                            if (paths != null) {
                                paths.addAll(entry.getValue());
                            } else {
                                pathTimes.put(newKey, entry.getValue());
                            }
                        }

                    }
                }
                if (pathsToProcess != null) {
                    for (Map.Entry<Long, List<Path>> entry : pathsToProcess
                            .entrySet()) {
                        long timeBasis = entry.getKey();
                        for (Path path : entry.getValue()) {
                            copyPath(path, timeBasis);
                        }
                    }
                }
            } catch (Throwable e) {
                logError("Error occurred processing files", e);
            }
        }
    }

    /**
     * Add Path to internal structures for copying.
     * 
     * @param path
     * @throws IOException
     * @throws ParseException
     */
    private void addPath(Path path) throws IOException, ParseException {
        long runTime = getInitialRunTime(path);
        schedulePath(path, runTime);
    }

    /**
     * Add the Path for the scheduled runTime.
     * 
     * @param path
     * @param runTime
     */
    private void schedulePath(Path path, long runTime) {
        synchronized (pathTimes) {
            List<Path> paths = pathTimes.get(runTime);
            if (paths == null) {
                paths = new ArrayList<>(1);
                pathTimes.put(runTime, paths);
            }
            paths.add(path);
            pathTimes.notifyAll();
        }
    }

    /**
     * Removes the path from tracking.
     * 
     * @param path
     */
    private void removePath(Path path) {
        synchronized (pathTimes) {
            Iterator<List<Path>> iter = pathTimes.values().iterator();
            while (iter.hasNext()) {
                List<Path> paths = iter.next();
                if (paths.remove(path)) {
                    if (paths.isEmpty()) {
                        iter.remove();
                    }

                    break;
                }
            }

        }
    }

    /**
     * Returns header object from file if found, null otherwise.
     * 
     * @param path
     * @return
     * @throws IOException
     */
    private Matcher getHeader(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path,
                Charset.defaultCharset())) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = HEADER_PATTERN.matcher(line);
                if (matcher.matches()) {
                    return matcher;
                }
            }
        }

        return null;
    }

    /**
     * Copy path from input directory to output directory using timeBasis to
     * change times within file.
     * 
     * @param path
     * @param timeBasis
     * @throws IOException
     */
    private void copyPath(Path path, long timeBasis) throws IOException {

        Path relative = INPUT_PATH.relativize(path);
        Path output = OUTPUT_PATH.resolve(relative);
        logInfo("Inserting file [" + output.toFile().getAbsolutePath() + "]");

        try (BufferedWriter writer = Files.newBufferedWriter(output,
                Charset.defaultCharset(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                BufferedReader reader = Files.newBufferedReader(path,
                        Charset.defaultCharset())) {

            String line = null;
            boolean foundHeader = false;
            while ((line = reader.readLine()) != null) {
                if (!foundHeader) {
                    Matcher matcher = HEADER_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        foundHeader = true;
                        StringBuilder updatedLine = new StringBuilder(
                                line.length());
                        List<String> newTimes = adjustTimes(timeBasis,
                                matcher.group(2), matcher.group(3),
                                matcher.group(5));

                        // copy in groups replacing dates for current day
                        updatedLine.append(matcher.group(1));

                        // create time
                        updatedLine.append(newTimes.get(0));

                        // effective time
                        updatedLine.append(newTimes.get(1));
                        updatedLine.append(matcher.group(4));

                        // expiration time
                        updatedLine.append(newTimes.get(2));
                        line = updatedLine.toString();
                    }
                }

                writer.write(line);
                // TODO: Issue with possibly changing line terminator?
                writer.write('\n');
            }
        }
    }

    /**
     * Returns time from header. Times are creation time, effective time, and
     * expiration time.
     * 
     * @param matcher
     * @return
     */
    private List<String> getTimes(Matcher matcher) {
        List<String> rval = new ArrayList<>(3);
        rval.add(matcher.group(2));
        rval.add(matcher.group(3));
        rval.add(matcher.group(5));
        return rval;
    }

    /**
     * Adjusts the passed times so that createTime happens on the same day as
     * timeBasis. Effective time and expiration time are then offset from new
     * creation time.
     * 
     * @param timeBasis
     * @param createTime
     * @param effectiveTime
     * @param expirationTime
     * @return
     */
    private List<String> adjustTimes(long timeBasis, String createTime,
            String effectiveTime, String expirationTime) {
        List<String> rval = new ArrayList<>(3);
        String newCreateTime = adjustDate(timeBasis, createTime);
        rval.add(newCreateTime);
        rval.add(adjustDate(newCreateTime, createTime, effectiveTime));
        rval.add(adjustDate(newCreateTime, createTime, expirationTime));
        return rval;
    }

    /**
     * Adjusts passed date String from the passed date time to current date plus
     * the offset from the passed reference times.
     * 
     * @param date
     * @return
     */
    private String adjustDate(long timeBasis, String createDate) {
        String curDay = dateParser.format(new Date(timeBasis));
        if (createDate.length() == 10) {
            return curDay.substring(0, 6) + createDate.substring(6);
        }

        return curDay;
    }

    /**
     * Adjusts passed date String to current year month day and returns.
     * Expected format is yyMMddHHmm.
     * 
     * @param date
     * @return
     */
    private String adjustDate(String curCreateTime, String refCreateTime,
            String refOffsetTime) {
        try {
            long timeOffset = dateParser.parse(refOffsetTime).getTime()
                    - dateParser.parse(refCreateTime).getTime();
            long newTime = dateParser.parse(curCreateTime).getTime()
                    + timeOffset;
            return dateParser.format(new Date(newTime));
        } catch (ParseException e) {
            logError("Error ocurred adjust dates, using current time", e);
            return curCreateTime;
        }
    }

    /**
     * Determines the initial run time for the passed path. If
     * {@link DO_INITIAL_LOAD} is true then it will check if product could
     * possibly be effective right now if program had been running early enough
     * and will mark file to copy in at that time, otherwise time basis will be
     * set such the file would appear to be copied in later in the same day or
     * the next day.
     * 
     * @param path
     * @return
     * @throws IOException
     * @throws ParseException
     */
    private long getInitialRunTime(Path path) throws IOException,
            ParseException {
        Matcher header = getHeader(path);
        long runTime = 0;

        if (header == null) {
            // can't find NWR Waves Header, use file mod time
            runTime = getInitialRunTime(Files.getLastModifiedTime(path)
                    .toMillis(), System.currentTimeMillis(), MILLIS_PER_MINUTE);
        } else {
            // get creation/effective/expiration times
            List<String> times = getTimes(header);
            String createTimeString = times.get(0);
            long createTime = dateParser.parse(createTimeString).getTime();

            if (DO_INITIAL_LOAD) {
                String effectiveTimeString = times.get(1);
                String expirationTimeString = times.get(2);

                // check for yesterday
                long checkTime = System.currentTimeMillis() - MILLIS_PER_DAY;
                long checkRunTime = getInitialRunTime(createTime, checkTime,
                        MILLIS_PER_MINUTE);
                if (checkRunTime(checkRunTime, createTimeString,
                        effectiveTimeString, expirationTimeString)) {
                    runTime = checkRunTime;
                } else {
                    // wasn't active for yesterday, check today
                    checkRunTime += MILLIS_PER_DAY;
                    if (checkRunTime(checkRunTime, createTimeString,
                            effectiveTimeString, expirationTimeString)) {
                        runTime = checkRunTime;
                    }
                }
            }

            // if runTime hasn't been set apply default logic
            if (runTime == 0) {
                // if creation time offset within last minute run, otherwise
                // offset in to future
                runTime = getInitialRunTime(createTime,
                        System.currentTimeMillis(), MILLIS_PER_MINUTE);
            }
        }

        return runTime;
    }

    /**
     * Checks if checkTimeMillis is a valid day day for timeToOffset or if it
     * will need to be 24 hours in the future.
     * 
     * @param timeToOffset
     * @param checkTimeMillis
     * @param toleranceInMillis
     * @return
     * @throws IOException
     */
    private long getInitialRunTime(long timeToOffset, long checkTimeMillis,
            long toleranceInMillis) throws IOException {
        long offsetMillisInDay = timeToOffset % MILLIS_PER_DAY;
        long checkMillisInDay = checkTimeMillis % MILLIS_PER_DAY;

        long newTime = (checkTimeMillis - checkMillisInDay) + offsetMillisInDay;

        // use newTime if after checkTime, otherwise offset 24 hours in future
        if ((newTime > checkTimeMillis)
                || ((checkTimeMillis - newTime) < toleranceInMillis)) {
            return newTime;
        } else {
            // return 24 froms new time
            return newTime + MILLIS_PER_DAY;
        }
    }

    /**
     * Adjusts the passed times based on the time basis.
     * 
     * @param timeBasis
     * @param createTime
     * @param effectiveTime
     * @param expirationTime
     * @return
     * @throws ParseException
     */
    private List<Long> adjustDatesAsMillis(long timeBasis, String createTime,
            String effectiveTime, String expirationTime) throws ParseException {
        List<Long> rval = new ArrayList<>(3);
        long createTimeMillis = dateParser.parse(createTime).getTime();
        long effectiveTimeMillis = dateParser.parse(effectiveTime).getTime();
        long expirationTimeMillis = dateParser.parse(expirationTime).getTime();

        long timeBasisMillisInDay = timeBasis % MILLIS_PER_DAY;
        long createMillisInDay = createTimeMillis % MILLIS_PER_DAY;
        long newTime = (timeBasis - timeBasisMillisInDay) + createMillisInDay;
        rval.add(newTime);
        rval.add(newTime + (effectiveTimeMillis - createTimeMillis));
        rval.add(newTime + (expirationTimeMillis - createTimeMillis));

        return rval;
    }

    /**
     * Returns true if the times adjusted to the same day as checkTime would
     * cause checkTime to be bounded by the new effectiveTime and
     * expirationTime.
     * 
     * @param checkTime
     * @param createTime
     * @param effectiveTime
     * @param expirationTime
     * @return
     * @throws ParseException
     */
    private boolean checkRunTime(long checkTime, String createTime,
            String effectiveTime, String expirationTime) throws ParseException {
        List<Long> newTimes = adjustDatesAsMillis(checkTime, createTime,
                effectiveTime, expirationTime);
        long curTime = System.currentTimeMillis();
        return (curTime > newTimes.get(1)) && (curTime < newTimes.get(2));
    }

    private static void logInfo(String msg) {
        System.out.println("INFO " + logFormatter.format(new Date()) + ": "
                + msg);
    }

    private static void logError(String msg, Throwable t) {
        System.err.println("ERROR " + logFormatter.format(new Date()) + ": "
                + msg);
        t.printStackTrace();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            DataSimulator simulator = new DataSimulator();
            simulator.startDirectoryWatcher();
            simulator.doInitialScan();
            simulator.processFiles();
        } catch (Throwable e) {
            System.err.println("Error occurred running data simulator");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
