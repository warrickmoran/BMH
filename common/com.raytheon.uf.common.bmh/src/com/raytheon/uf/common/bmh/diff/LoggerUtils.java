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

import javax.persistence.Transient;

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
    public final static Priority DEFAULT_PRIORITY = Priority.INFO;

    /**
     * Cache of fields to place in logging header for a given class.
     */
    private static final Map<Class<?>, DiffTitleCache> diffTitleCacheMap = new HashMap<>();

    /**
     * The field to display when the class is embedded in another class.
     */
    private static final Map<Class<?>, DiffStringCache> diffStringCacheMap = new HashMap<>();

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

            createHeader(sb, user, "Update", newObj, logger).append(" ");

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
        DiffTitleCache diffTitleCache = getDiffTitleCache(clazz);
        List<Field> titleFields = diffTitleCache.getDiffKeyFields();

        int start = sb.length();
        sb.append(clazz.getName());
        int end = sb.lastIndexOf(".") + 1;
        sb.replace(start, end, "");

        if (titleFields.size() > 0) {
            sb.append(" ");

            for (Field field : titleFields) {
                String title = getDiffStringTitle(field);
                boolean userOverrideValue = true;
                if (title == null) {
                    title = diffTitleCache.getTitle(field);
                    userOverrideValue = false;
                }
                sb.append(title).append(" ");
                try {
                    Object value = PropertyUtils.getProperty(newObj,
                            field.getName());
                    if (userOverrideValue) {
                        sb.append(getDiffStringDisplayValue(field.getType(),
                                value));
                    } else {
                        sb.append(displayValue(value));
                    }
                } catch (SecurityException | IllegalAccessException
                        | InvocationTargetException | NoSuchMethodException e) {
                    logger.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
                    sb.append("<error>");
                }
                sb.append(" ");
            }
            sb.setLength(sb.length() - 1);
            sb.append(": ");
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

        sb.append(title).append(":");

        StringBuilder colSb = new StringBuilder();
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

        if (colSb.length() > 0) {
            colSb.setLength(colSb.length() - 2);
            sb.append(" removed [").append(colSb).append("]");
            colSb.setLength(0);
        }

        if ((newCol != null) && (newCol.size() > 0)) {
            if ((oldCol == null) || (oldCol.size() == 0)) {
                for (Object o : newCol) {
                    colSb.append(objectDisplayString(o)).append(", ");
                }
            } else if (oldCol != null) {
                for (Object o : newCol) {
                    if (!oldCol.contains(o)) {
                        colSb.append(objectDisplayString(o)).append(", ");
                    }
                }
            }
        }

        if (colSb.length() > 0) {
            colSb.setLength(colSb.length() - 2);
            sb.append(" added [").append(colSb).append("]");
        }

        sb.append(", ");
    }

    /**
     * Get the objects display string based on following order
     * 
     * <pre>
     * {@link DiffString}
     * {@link DiffTitle} - field with lowest position
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
        Field field = getDiffStringField(clazz);
        if (field == null) {
            List<Field> fields = getDiffTitleCache(clazz).getDiffKeyFields();
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

    private static String getDiffStringDisplayValue(Class<?> clazz, Object obj)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        Field field = getDiffStringField(clazz);
        Object value = obj;

        while ((field != null) && (value != null)) {
            value = PropertyUtils.getProperty(value, field.getName());
            field = getDiffStringField(field.getType());
        }
        return displayValue(value);
    }

    private static String displayValue(Object value) {
        if (value == null) {
            return "None";
        }
        return "[" + value.toString() + "]";
    }

    private static String getDiffStringTitle(Field field) {
        StringBuilder sb = new StringBuilder();

        while (field != null) {
            sb.append(field.getName()).append(".");
            field = getDiffStringField(field.getType());
        }
        if (sb.length() == 0) {
            return null;
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
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
        sb.append(title).append(": [").append(oldStr).append("] -> [")
                .append(newStr).append("], ");
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
                                && (field.getAnnotation(Transient.class) == null)
                                && !Modifier.isStatic(field.getModifiers())
                                && !Modifier.isTransient(field.getModifiers())) {
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
     * Get list of fields to display in the log's header see {@link DiffTitle}
     * 
     * @param clazz
     * @return keyFields - null indicate no entries for the class
     */
    private static Field getDiffStringField(Class<?> clazz) {
        synchronized (diffStringCacheMap) {
            DiffStringCache cache = diffStringCacheMap.get(clazz);
            if (cache == null) {
                Class<?> currentClass = clazz;
                mainLoop: while (currentClass != null) {
                    for (Field field : currentClass.getDeclaredFields()) {
                        if (field.getAnnotation(DiffString.class) != null) {
                            cache = new DiffStringCache(field);
                            break mainLoop;
                        }
                    }
                    currentClass = currentClass.getSuperclass();
                }
                if (cache == null) {
                    cache = new DiffStringCache(null);
                }
                diffStringCacheMap.put(clazz, cache);
            }
            return cache.getField();
        }
    }

    /**
     * 
     * @param clazz
     * @return diffKeyCache
     */
    private static DiffTitleCache getDiffTitleCache(Class<?> clazz) {
        synchronized (diffTitleCacheMap) {
            DiffTitleCache diffKeyCache = diffTitleCacheMap.get(clazz);
            if (diffKeyCache == null) {
                diffKeyCache = new DiffTitleCache(clazz);
                diffTitleCacheMap.put(clazz, diffKeyCache);
            }
            return diffKeyCache;
        }
    }

    /**
     * Wrapper class for caching a class' {@link DiffString}
     */
    private static class DiffStringCache {
        private final Field field;

        public DiffStringCache(Field field) {
            this.field = field;
        }

        public Field getField() {
            return field;
        }
    }

    /**
     * Wrapper class for caching a class's {@link DiffTitle}s.
     */
    private static class DiffTitleCache {
        /**
         * Order list by position.
         */
        private static final Comparator<Field> diffKeyComparator = new Comparator<Field>() {

            @Override
            public int compare(Field f1, Field f2) {
                int i1 = f1.getAnnotation(DiffTitle.class).position();
                int i2 = f2.getAnnotation(DiffTitle.class).position();
                return i1 - i2;
            }
        };

        private final List<Field> diffKeyFields;

        private static List<Field> getOrderedDiffKeyFields(Class<?> clazz) {
            List<Field> list = new ArrayList<>();
            Class<?> currentClass = clazz;
            while (currentClass != null) {
                for (Field field : currentClass.getDeclaredFields()) {
                    if (field.getAnnotation(DiffTitle.class) != null) {
                        list.add(field);
                    }
                }
                currentClass = currentClass.getSuperclass();
            }

            List<Field> fields = new ArrayList<>(list);
            Collections.sort(fields, diffKeyComparator);
            return fields;
        }

        public DiffTitleCache(Class<?> clazz) {
            diffKeyFields = getOrderedDiffKeyFields(clazz);
        }

        public List<Field> getDiffKeyFields() {
            return diffKeyFields;
        }

        public String getTitle(Field field) {
            String title = field.getAnnotation(DiffTitle.class).title();
            if (title.trim().length() == 0) {
                title = field.getName();
            }
            return title;
        }
    }
}
