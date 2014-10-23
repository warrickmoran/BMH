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
package com.raytheon.uf.common.bmh.diff;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Generic tools for logging.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2014            rferrel     Initial creation
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */

public class LoggerUtils {
    /**
     * Default priority to use for logging.
     */
    private final static Priority DEFAULT_PRIORITY = Priority.INFO;

    /**
     * Cache of fields to place in logging header for a given class.
     */
    private static final Map<Class<?>, DiffKeyCache> diffKeyCacheMap = new HashMap<>();

    /**
     * The field to display when the class is embedded in another class.
     */
    private static final Map<Class<?>, DiffKeyOverrideCache> diffKeyOverrideCacheMap = new HashMap<>();

    /**
     * The class' fields to check for differences when logging updates.
     */
    private static final Map<Class<?>, List<Field>> diffFieldsMap = new HashMap<>();

    /**
     * Log the new or update entry using the <code>DEFAULT_PRIORITY</code>
     * 
     * @param logger
     * @param user
     * @param oldObj
     *            - null perform new log instead of update
     * @param newObj
     *            - Should never be null.
     */
    public static <T> void logSave(IUFStatusHandler logger, String user,
            T oldObj, T newObj) {
        logSave(logger, user, oldObj, newObj, DEFAULT_PRIORITY);
    }

    /**
     * Log the new or update entry at the given priority.
     * 
     * @param logger
     * @param user
     * @param oldObj
     *            - null perform new log instead of update
     * @param newObj
     *            - Should never be null.
     * @param priority
     */
    public static <T> void logSave(IUFStatusHandler logger, String user,
            T oldObj, T newObj, Priority priority) {
        if (!logger.isPriorityEnabled(priority)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (oldObj == null) {
            createHeader(sb, user, "New", newObj, logger).append(newObj);
        } else {
            // Difference Log.
            List<Field> diffFields = getDiffFields(newObj.getClass());
            if (diffFields.size() == 0) {
                return;
            }

            createHeader(sb, user, "Update", newObj, logger).append(" [");

            boolean logChanges = false;

            for (Field field : diffFields) {
                try {
                    Object oldValue = PropertyUtils.getProperty(oldObj,
                            field.getName());
                    Object newValue = PropertyUtils.getProperty(newObj,
                            field.getName());

                    // one or both may be null.
                    if ((oldValue instanceof Collection<?>)
                            || (newValue instanceof Collection<?>)) {
                        Collection<?> oldCol = (Collection<?>) oldValue;
                        Collection<?> newCol = (Collection<?>) newValue;
                        if (collectionDiffer(oldCol, newCol)) {
                            logCollection(sb, field.getName(), oldCol, newCol);
                            logChanges = true;
                        }
                    } else {
                        if (oldValue == null) {
                            oldValue = "None";
                        }
                        if (newValue == null) {
                            newValue = "None";
                        }
                        if (!oldValue.equals(newValue)) {
                            String oldStr = objectDisplayString(oldValue);
                            String newStr = objectDisplayString(newValue);
                            logFieldChange(sb, field.getName(), oldStr, newStr);
                            logChanges = true;
                        }
                    }

                } catch (IllegalAccessException | InvocationTargetException
                        | NoSuchMethodException e) {
                    logger.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
                    logFieldChange(sb, field.getName(), "<error>", "<error>");
                    logChanges = true;
                }
            }

            if (!logChanges) {
                return;
            }
            sb.setLength(sb.length() - 2);
            sb.append("]");
        }

        logger.handle(priority, sb.toString());
    }

    /**
     * Standard header information for a log entry.
     * 
     * @param sb
     * @param user
     * @param action
     * @param newObj
     * @param logger
     * @return sb
     */
    private static StringBuilder createHeader(StringBuilder sb, String user,
            String action, Object newObj, IUFStatusHandler logger) {

        sb.append("User ").append(user).append(" ").append(action).append(" ");
        Class<?> clazz = newObj.getClass();
        DiffKeyCache diffKeyCache = getDiffKeyCache(clazz);
        List<Field> titleFields = diffKeyCache.getDiffKeyFields();

        int start = sb.length();
        sb.append(clazz.getName());
        int end = sb.lastIndexOf(".") + 1;
        sb.replace(start, end, "");

        if (titleFields.size() > 0) {
            sb.append(" ");
            for (Field field : titleFields) {
                sb.append(diffKeyCache.getTitle(field)).append("/");
            }
            sb.setLength(sb.length() - 1);
            sb.append(": ");
            for (Field field : titleFields) {
                try {
                    Object o = PropertyUtils.getProperty(newObj,
                            field.getName());
                    Field f = getDiffKeyOverrideField(o.getClass());
                    if (f != null) {
                        o = PropertyUtils.getProperty(o, f.getName());
                    }

                    if (o == null) {
                        o = "<None>";
                    }
                    sb.append(o).append("/");
                } catch (SecurityException | IllegalAccessException
                        | InvocationTargetException | NoSuchMethodException e) {
                    logger.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
                    sb.append("<error>/");
                }
            }
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }

    /**
     * Get log entry showing just the difference between the old and new
     * collection.
     * 
     * @param sb
     * @param title
     * @param oldCol
     * @param newCol
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private static void logCollection(StringBuilder sb, String title,
            Collection<?> oldCol, Collection<?> newCol)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        StringBuilder colSb = new StringBuilder();
        colSb.append("-[");
        int startIndex = colSb.length();
        if ((oldCol != null) && (oldCol.size() > 0)) {
            if ((newCol == null) || (newCol.size() == 0)) {
                for (Object o : oldCol) {
                    colSb.append(objectDisplayString(o)).append(", ");
                }
            } else if (newCol != null) {
                for (Object o : oldCol) {
                    if (!newCol.contains(o)) {
                        colSb.append(objectDisplayString(o)).append(", ");
                    }
                }
            }
        }
        if (colSb.length() > startIndex) {
            colSb.setLength(colSb.length() - 2);
        }
        colSb.append("]");
        String oldValue = colSb.toString();

        colSb.setLength(0);
        colSb.append("+[");
        startIndex = colSb.length();
        if ((newCol != null) && (newCol.size() > 0)) {
            if ((oldCol == null) || (oldCol.size() == 0)) {
                for (Object o : oldCol) {
                    colSb.append(objectDisplayString(o)).append(", ");
                }
            } else if (oldCol != null) {
                for (Object o : oldCol) {
                    if (!oldCol.contains(o)) {
                        colSb.append(objectDisplayString(o)).append(", ");
                    }
                }
            }
        }
        if (colSb.length() > startIndex) {
            colSb.setLength(colSb.length() - 2);
        }
        colSb.append("]");
        String newValue = colSb.toString();
        logFieldChange(sb, title, oldValue, newValue);
    }

    /**
     * Get the objects display string based on following order
     * 
     * <pre>
     * {@link DiffKeyOverride}
     * {@link DiffKey} - field with lowest position
     * object's toString()
     * </pre>
     * 
     * @param obj
     * @return displayString
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private static String objectDisplayString(Object obj)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        if (obj == null) {
            return "None";
        }
        Class<?> clazz = obj.getClass();
        Field field = getDiffKeyOverrideField(clazz);
        if (field == null) {
            List<Field> fields = getDiffKeyCache(clazz).getDiffKeyFields();
            if ((fields != null) && (fields.size() > 0)) {
                field = fields.get(0);
            }
        }

        if (field == null) {
            return obj.toString();
        }
        Object value = PropertyUtils.getProperty(obj, field.getName());
        return value.toString();
    }

    /**
     * Standard format for logging a field's old and new value.
     * 
     * @param sb
     * @param title
     *            - Field name
     * @param oldValue
     *            - Field's old value
     * @param newValue
     *            - Field's new value
     */
    private static void logFieldChange(StringBuilder sb, String title,
            Object oldValue, Object newValue) {
        String oldStr = oldValue.toString();
        String newStr = newValue.toString();
        sb.append(title);
        if (oldStr.matches("^[+-]{0,1}\\[.*$")) {
            sb.append(": ").append(oldStr).append(" | ").append(newStr)
                    .append(", ");
        } else {
            sb.append(": \"").append(oldStr).append("\" | \"").append(newStr)
                    .append("\", ");
        }
    }

    /**
     * Determine if two collections have the exact same entries. A zero size and
     * null collections are considered the same.
     * 
     * @param col1
     * @param col2
     * @return true if collections are different
     */
    private static boolean collectionDiffer(Collection<?> col1,
            Collection<?> col2) {

        if (col1 == null) {
            return ((col2 != null) && (col2.size() > 0));
        }

        if (col2 == null) {
            return (col1.size() > 0);
        }

        if (col1.size() != col2.size()) {
            return true;
        }

        // containsAll doesn't always return true for 2 empty sets.
        if (col1.size() > 0) {
            return !col1.containsAll(col2);
        }

        return false;
    }

    /**
     * Make a delete log entry with <code>DEFAULT_PRIORITY</code>.
     * 
     * @param logger
     * @param user
     * @param delObj
     */
    public static <T> void logDelete(IUFStatusHandler logger, String user,
            T delObj) {
        logDelete(logger, user, delObj, DEFAULT_PRIORITY);
    }

    /**
     * Make a delete log entry with desired priority.
     * 
     * @param logger
     * @param user
     * @param delObj
     * @param priority
     */
    public static <T> void logDelete(IUFStatusHandler logger, String user,
            T delObj, Priority priority) {
        if (!logger.isPriorityEnabled(priority)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("rlf-- ");
        createHeader(sb, user, "Deleted", delObj, logger).append(delObj);
        logger.handle(priority, sb.toString());
    }

    /**
     * Get a list of fields to check for changes for the given class.
     * 
     * @param clazz
     * @return diffFields
     */
    private static List<Field> getDiffFields(Class<?> clazz) {
        synchronized (diffFieldsMap) {
            List<Field> fields = diffFieldsMap.get(clazz);
            if (fields == null) {
                List<Field> list = new ArrayList<>();
                Class<?> currentClass = clazz;
                while (currentClass != null) {
                    for (Field field : currentClass.getDeclaredFields()) {
                        if ((field.getAnnotation(DiffIgnore.class) == null)
                                && !Modifier.isStatic(field.getModifiers())) {
                            list.add(field);
                        }
                    }

                    currentClass = currentClass.getSuperclass();
                }

                fields = new ArrayList<>(list);
                diffFieldsMap.put(clazz, fields);
            }
            return fields;
        }
    }

    /**
     * Get list of fields to display in the log's header see {@link DiffKey}
     * 
     * @param clazz
     * @return keyFields - null indicate no entries for the class
     */
    private static Field getDiffKeyOverrideField(Class<?> clazz) {
        synchronized (diffKeyOverrideCacheMap) {
            DiffKeyOverrideCache cache = diffKeyOverrideCacheMap.get(clazz);
            if (cache == null) {
                Class<?> currentClass = clazz;
                mainLoop: while (currentClass != null) {
                    for (Field field : currentClass.getDeclaredFields()) {
                        if (field.getAnnotation(DiffKeyOverride.class) != null) {
                            cache = new DiffKeyOverrideCache(field);
                            break mainLoop;
                        }
                    }
                    currentClass = currentClass.getSuperclass();
                }
                if (cache == null) {
                    cache = new DiffKeyOverrideCache(null);
                }
                diffKeyOverrideCacheMap.put(clazz, cache);
            }
            return cache.getField();
        }
    }

    /**
     * 
     * @param clazz
     * @return diffKeyCache
     */
    private static DiffKeyCache getDiffKeyCache(Class<?> clazz) {
        synchronized (diffKeyCacheMap) {
            DiffKeyCache diffKeyCache = diffKeyCacheMap.get(clazz);
            if (diffKeyCache == null) {
                diffKeyCache = new DiffKeyCache(clazz);
                diffKeyCacheMap.put(clazz, diffKeyCache);
            }
            return diffKeyCache;
        }
    }

    /**
     * Wrapper class for caching a class' {@link DiffKeyOverride}
     */
    private static class DiffKeyOverrideCache {
        private final Field field;

        public DiffKeyOverrideCache(Field field) {
            this.field = field;
        }

        public Field getField() {
            return field;
        }
    }

    /**
     * Wrapper class for caching a class's {@link DiffKey}s.
     */
    private static class DiffKeyCache {
        /**
         * Order list by position.
         */
        private static final Comparator<Field> diffKeyComparator = new Comparator<Field>() {

            @Override
            public int compare(Field f1, Field f2) {
                int i1 = f1.getAnnotation(DiffKey.class).position();
                int i2 = f2.getAnnotation(DiffKey.class).position();
                return i1 - i2;
            }
        };

        private final List<Field> diffKeyFields;

        private static List<Field> getOrderedDiffKeyFields(Class<?> clazz) {
            List<Field> list = new ArrayList<>();
            Class<?> currentClass = clazz;
            while (currentClass != null) {
                for (Field field : currentClass.getDeclaredFields()) {
                    if (field.getAnnotation(DiffKey.class) != null) {
                        list.add(field);
                    }
                }
                currentClass = currentClass.getSuperclass();
            }

            List<Field> fields = new ArrayList<>(list);
            Collections.sort(fields, diffKeyComparator);
            return fields;
        }

        public DiffKeyCache(Class<?> clazz) {
            diffKeyFields = getOrderedDiffKeyFields(clazz);
        }

        public List<Field> getDiffKeyFields() {
            return diffKeyFields;
        }

        public String getTitle(Field field) {
            String title = field.getAnnotation(DiffKey.class).title();
            if (title.trim().length() == 0) {
                title = field.getName();
            }
            return title;
        }
    }
}
