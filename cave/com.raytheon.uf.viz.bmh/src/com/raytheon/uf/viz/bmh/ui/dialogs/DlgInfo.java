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
package com.raytheon.uf.viz.bmh.ui.dialogs;

/**
 * 
 * Enum with roles and descriptions for the BMH dialogs.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2014 3413       rferrel     Initial creation
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public enum DlgInfo {
    BMH_MENU("bmh.dialog.menu", "BMH Menu"),

    TRANSMITTER_CONFIGURATION("bmh.dialog.transmitterConfiguration",
            "Transmitter Configuration"),

    TRANSMITTER_ALIGNMENT("bmh.dialog.transmitterAlignment",
            "Transmitter Alignment"),

    LISTENING_AREAS("bmh.dialog.listeningAreas", "Listening Areas"),

    LISTENING_ZONES("bmh.dialog.listeningZones", "Listening Zones"),

    DISABLES_SILENCE_ALARM("bmh.dialog.disableSilenceAlarm", "Silence Alarm"),

    BROADCAST_CYCLE("bmh.dialog.broadcastCycle", "Broadcast Cycle"),

    BROADCAST_PROGRAMS("bmh.dialog.broadcastPrograms",
            "Broadcast Program Configuration"),

    SUITE_MANAGER("bmh.dialog.suiteManager", "Suite Manager"),

    MESSAGE_TYPE("bmh.dialog.messageType", "Message Type Manager"),

    MESSAGE_TYPE_ASSOCIATION("bmh.dialog.messageTypeAssociation",
            "Message Type Association"),

    WEATHER_MESSAGES("bmh.dialog.weatherMessages", "Weather Messages"),

    EMERGENCY_OVERRIDE("bmh.dialog.emergencyOverride", "Emergency Override"),

    SYSTEM_STATUS("bmh.dialog.systemStatus", "System Status"),

    ALERT_MONITOR("bmh.dialog.alertMonitor", "Alert Monitor"),

    COPY_OPERATIONAL_DB("bmh.dialog.CopyOperationalDB",
            "Copy Operational Database"),

    LDAD_CONFIGURATION("bmh.dialog.ldadConfiguration", "LDAD Configuration"),

    MANAGE_DICTIONARIES("bmh.dialog.manageDictionaries", "Dictionary Manager"),

    CONVERT_LEGACY_DICTIONARY("bmh.dialog.convertLegacyDictionary",
            "Convert Legacy Dictionary"),

    DAC_CONFIGURATION("bmh.dialog.dacConfiguration", "DAC Configuration");

    private final String roleId;

    private final String title;

    private DlgInfo(String roleId, String title) {
        this.roleId = roleId;
        this.title = title;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getTitle() {
        return title;
    }
}