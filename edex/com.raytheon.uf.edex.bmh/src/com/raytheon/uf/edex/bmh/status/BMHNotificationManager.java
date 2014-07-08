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
package com.raytheon.uf.edex.bmh.status;

import java.io.File;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.status.BMH_ACTION;

/**
 * Reads the notification configuration at startup. Used to lookup what action
 * should be taken for a particular category and priority combination, if any.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 16, 2014 3291       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class BMHNotificationManager {
    /*
     * IUFStatusHandler will be used in this class because it is utilized before
     * the BMH Notification Manager has been fully initialized (it is used
     * during the initialization of the BMH Notification Manager).
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BMHNotificationManager.class);

    /*
     * This should be relocated to BMHConstants if other components use the
     * configuration directory.
     */
    private static final String CONFIGURATION_DIRECTORY = "configuration";

    private static final String NOTIFICATION_PROPERTIES_FILE = "notification.properties";

    private static final String NOTIFY_PROPERTY_PREFIX = "bmh.notify.";

    private static final String NOTIFY_PROPERTY_PATTERN_REGEX = "^"
            + NOTIFY_PROPERTY_PREFIX
            + "([0-9]+)\\.(FATAL|ERROR|WARN|INFO|DEBUG)$";

    private static final Pattern notificationPropertyPattern = Pattern
            .compile(NOTIFY_PROPERTY_PATTERN_REGEX);

    private static final String NOTIFY_AUDIO_PROPERTY_SUFFIX = ".audio";

    private static final String NOTIFICATION_IDENTIFIER_DELIMITER = "\\+";

    private static final Map<String, Priority> priorityMap;

    private static BMHNotificationManager instance;

    private Table<BMH_CATEGORY, Priority, BMHNotificationAction> actionTable = HashBasedTable
            .create();

    static {
        /* Initialize the priority lookup map. */
        priorityMap = new HashMap<String, Priority>();

        priorityMap.put("FATAL", Priority.FATAL);
        priorityMap.put("ERROR", Priority.ERROR);
        priorityMap.put("WARN", Priority.WARN);
        priorityMap.put("INFO", Priority.INFO);
        priorityMap.put("DEBUG", Priority.DEBUG);
    }

    public static synchronized BMHNotificationManager getInstance() {
        if (instance == null) {
            instance = new BMHNotificationManager();
        }
        return instance;
    }

    /**
     * 
     */
    protected BMHNotificationManager() {
        /* Determine if custom configuration has been provided. */

        /*
         * Determine where the custom configuration should be loaded based on
         * BMH_DATA.
         */
        String notificationProperties = BMHConstants.getBmhDataDirectory()
                + File.separatorChar + CONFIGURATION_DIRECTORY
                + File.separatorChar + NOTIFICATION_PROPERTIES_FILE;
        /*
         * There is a possibility that BMH_DATA may not be set. However, the
         * Notification manager can still function without the configuration and
         * the default actions will be triggered based on priority.
         */
        File notificationPropertiesFile = new File(notificationProperties);
        if (notificationPropertiesFile.exists() == false) {
            /*
             * The notification properties file could not be found, the default
             * actions will be used.
             */
            statusHandler
                    .info("The custom notification management configuration file does not exist: "
                            + notificationProperties
                            + ". Using default notification actions.");
            return;
        }

        this.configure(notificationPropertiesFile);
    }

    private void configure(File notificationPropertiesFile) {
        statusHandler
                .info("Attempting to configure the BMH Notification Manager using the following configuration: "
                        + notificationPropertiesFile.getAbsolutePath() + " ...");
        // Configuration will be provided in the following format:
        // bmh.notify.{category_code}.{priority}=ACTION
        // bmh.notify.{category_code}.{priority}.audio=FILE
        //
        // category_code is the numeric code associated with the category
        // as it has been defined in: @{link BMHCategory}
        //
        // priority is the String value associated with one of the recognized
        // UFStatus priorities: { DEBUG, INFO, WARN, ERROR, FATAL }
        //
        // ACTION is the identifier(s) associated with one or multiple
        // enumerations as they have been defined in: {@link BMHAction}.
        // Multiple action identifiers can be specified using '+' as a
        // delimiter.
        // NOTE: IF BOTH ALERTVIZ ACTIONS ARE SPECIFIED, TWO MESSAGES WILL BE
        // SENT TO ALERTVIZ!
        //
        // FILE is the name of the audio file that should be played when the
        // AlertViz Audio action is specified. If the AlertViz Audio action is
        // specified and the audio property has not been set for the category,
        // an error will be logged and the custom AlertViz audio action will be
        // ignored.
        //
        // (similar to wrapper.conf property setting)
        Configuration configuration = null;
        try {
            configuration = new PropertiesConfiguration(
                    notificationPropertiesFile);
        } catch (ConfigurationException e) {
            /*
             * The BMH Notification Configuration is not critical to the
             * operation of BMH. However, the expectation was that it would be
             * loaded at this point; so, this failure will be logged as an
             * error.
             */
            statusHandler.error(
                    "Failed to load the BMH Notification Configuration: "
                            + notificationPropertiesFile.getAbsolutePath(), e);
            return;
        }

        Iterator<?> notificationPropertiesIterator = configuration.getKeys();
        while (notificationPropertiesIterator.hasNext()) {
            final String propertyName = notificationPropertiesIterator.next()
                    .toString();
            /*
             * is this a notification property or an audio notification
             * property?
             */
            Matcher matcher = notificationPropertyPattern.matcher(propertyName);
            if (matcher.matches() == false) {
                /*
                 * an audio notification property or an invalid property.
                 */
                continue;
            }

            /*
             * extract the category code from the property.
             * 
             * The regex pattern will ensure that the retrieved portion of the
             * property is numeric.
             */
            int categoryCode = Integer.parseInt(matcher.group(1));
            /*
             * Retrieve and verify that the specified category code is valid.
             */
            BMH_CATEGORY category = BMH_CATEGORY.lookup(categoryCode);
            if (category == null) {
                statusHandler.warn("Invalid category specified: "
                        + categoryCode + "! Skipping ...");
                continue;
            }

            /*
             * extract the priority from the property.
             * 
             * The regex pattern will ensure that only a valid priority has been
             * specified.
             */
            String priorityValue = matcher.group(2);
            Priority priority = priorityMap.get(priorityValue);

            /*
             * Retrieve the notification type identifiers associated with the
             * property.
             */
            String notificationIdentifiersValue = configuration.getString(
                    propertyName, null);
            /*
             * Verify that the property has actually been set.
             */
            if (notificationIdentifiersValue == null
                    || notificationIdentifiersValue.trim().isEmpty()) {
                statusHandler
                        .warn("Property "
                                + propertyName
                                + " was listed in the configuration; but, it has not been set to a value!");
                continue;
            }

            /*
             * Prepare to read the notification identifiers.
             */
            String[] notificationIdentifiers = notificationIdentifiersValue
                    .trim().split(NOTIFICATION_IDENTIFIER_DELIMITER);

            /* Create a list to store the actions as they are evaluated. */
            List<BMH_ACTION> actions = new LinkedList<BMH_ACTION>();
            String audioPropertyValue = null;

            /*
             * Iterate over and validate the specified actions.
             */
            for (String identifier : notificationIdentifiers) {
                BMH_ACTION action = BMH_ACTION.lookup(identifier);
                if (action == BMH_ACTION.ACTION_DEFAULT) {
                    /* invalid action specifier */
                    statusHandler.warn("An invalid action identifier: "
                            + identifier
                            + " has been encountered for property: "
                            + propertyName + "!");
                    continue;
                }

                if (action == BMH_ACTION.ACTION_ALERTVIZ_AUDIO) {
                    /*
                     * there should be an associated audio notification
                     * property.
                     */
                    String expectedAudioPropertyName = propertyName
                            + NOTIFY_AUDIO_PROPERTY_SUFFIX;
                    audioPropertyValue = configuration.getString(
                            expectedAudioPropertyName, null);
                    if (audioPropertyValue == null
                            || audioPropertyValue.trim().isEmpty()) {
                        /* The expected audio property has not been set. */
                        statusHandler
                                .error("The "
                                        + BMH_ACTION.ACTION_ALERTVIZ_AUDIO
                                                .toString()
                                        + " was specified for category code "
                                        + categoryCode
                                        + " ; however, the expected and associated audio property has not been set: "
                                        + expectedAudioPropertyName
                                        + "! AlertViz audio will not be played for this category code ...");
                    } else {
                        /* valid audio property. */
                        actions.add(action);
                        /*
                         * no further validation will occur. If the property has
                         * not been set to a valid file, AlertViz will be
                         * responsible for handling any issues that will occur.
                         */
                    }
                } else {
                    /* valid action; no other dependencies to verify. */
                    actions.add(action);
                }
            } // end for (String identifier : notificationIdentifiers)
            if (actions.isEmpty()) {
                /* no valid actions were read. */
                continue;
            }

            /* Create the notification action. */
            BMHNotificationAction notificationAction = new BMHNotificationAction(
                    audioPropertyValue, actions.toArray(new BMH_ACTION[actions
                            .size()]));
            this.actionTable.put(category, priority, notificationAction);
            statusHandler.info("Successfully read configuration for category: "
                    + category.getCode() + " AND priority: " + priorityValue
                    + ".");
        }

        statusHandler.info("BMH Notification Manager Configuration complete!");
    }

    public BMHNotificationAction getAction(BMH_CATEGORY category,
            Priority priority) {
        return this.actionTable.get(category, priority);
    }
}