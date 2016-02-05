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
package com.raytheon.uf.viz.bmh.ui.common.table;

import org.eclipse.swt.graphics.Color;

import com.raytheon.uf.viz.bmh.ui.common.table.TableData.SortDirection;

/**
 * Table Cell Object,
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 05/27/2014      3289    mpduff      Initial Version.
 * Jan 27, 2016 5160       rjpeter     Removed cellAlignment.
 * </pre>
 */

public class TableCellData {

    /**
     * Type of data indicator
     */
    public static enum DataType {
        STRING, NUMBER, BOOLEAN;
    };

    /**
     * Cell value if data is String type
     */
    private String stringValue = null;

    /**
     * Cell value if data is numeric
     */
    private Double numericValue = Double.NaN;

    /**
     * Cell value if data is boolean
     */
    private Boolean booleanValue = null;

    /**
     * Display the numeric data as an int flag
     */
    private boolean displayAsInt = false;

    /**
     * The table cell display string
     */
    private String displayStr = null;

    /**
     * Modification state of this cell
     */
    private boolean modified = false;

    /**
     * Default number format string
     */
    private String numberFormatString = "%1.2f";

    /**
     * Data type of this cell
     */
    private DataType dataType = DataType.STRING;

    /**
     * Foreground color of the cell
     */
    private Color foregroundColor = null;

    /**
     * Background color of the cell
     */
    private Color backgroundColor = null;

    /**
     * Constructor for displaying text data.
     * 
     * @param cellText
     *            The text to display
     */
    public TableCellData(String cellText) {
        this.stringValue = cellText;
        dataType = DataType.STRING;
    }

    /**
     * Constructor for displaying numeric data.
     * 
     * @param value
     *            The numeric value
     * @param numberFormatString
     *            The format string
     */
    public TableCellData(Double value, String numberFormatString) {
        this.numericValue = value;
        dataType = DataType.NUMBER;

        if ((numberFormatString != null) && (numberFormatString.length() > 0)) {
            this.numberFormatString = numberFormatString;
        }
    }

    /**
     * Constructor for displaying Integer data
     * 
     * @param value
     *            The integer value
     */
    public TableCellData(Integer value, String numberFormatString) {
        this.numericValue = value.doubleValue();
        this.displayAsInt = true;
        dataType = DataType.NUMBER;
        if ((numberFormatString != null) && (numberFormatString.length() > 0)) {
            this.numberFormatString = numberFormatString;
        }
    }

    /**
     * Constructor for displaying boolean data
     * 
     * @param value
     */
    public TableCellData(boolean value) {
        this.booleanValue = value;
        this.dataType = DataType.BOOLEAN;
    }

    /**
     * Get the cell text
     * 
     * @return the cellText
     */
    public String getCellText() {
        return stringValue;
    }

    /**
     * Get the numeric value
     * 
     * @return The numeric value
     */
    public Double getValue() {
        return numericValue;
    }

    /**
     * Set the data value from a string
     * 
     * @param strValue
     */
    public void setDataFromString(String strValue) {
        if (dataType == DataType.STRING) {
            this.stringValue = strValue;
        } else if (dataType == DataType.BOOLEAN) {
            this.booleanValue = Boolean.valueOf(strValue);
        } else {
            try {
                numericValue = Double.valueOf(strValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Expecting a numeric String value [" + strValue + "]");
            }
        }

        this.modified = true;
        this.displayStr = null;

        modified = true;
    }

    /**
     * Set the int value.
     * 
     * @param intValue
     */
    public void setData(Integer intValue) {
        this.numericValue = intValue.doubleValue();
    }

    /**
     * Set the double value.
     * 
     * @param doubleValue
     */
    public void setData(Double doubleValue) {
        this.numericValue = doubleValue;
    }

    /**
     * Mark as saved
     */
    public void markDataAsSaved() {
        this.modified = false;
    }

    /**
     * Get the DataType
     * 
     * @return The DataType
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Set the cell's text
     * 
     * @param cellText
     *            The text for the cell
     */
    public void setCellText(String cellText) {
        this.stringValue = cellText;
        this.modified = true;
    }

    /**
     * Get the number format string
     * 
     * @return
     */
    public String getNumberFormatString() {
        return numberFormatString;
    }

    /**
     * Get the value as an Integer.
     * 
     * @param round
     *            true to round the value, false to trunc the value
     * @return The Integer value
     */
    public Integer getValueAsInt(boolean round) {
        if (!numericValue.isNaN()) {
            if (round) {
                return (int) Math.round(numericValue);
            } else {
                return numericValue.intValue();
            }
        }

        return 0;
    }

    /**
     * Get the display string
     * 
     * @return The cell's display string
     */
    public String getDisplayString() {
        /*
         * Format the data for the display.
         */
        switch (dataType) {
        case BOOLEAN:
            if (booleanValue) {
                displayStr = "true";
            } else {
                displayStr = "false";
            }
            break;
        case NUMBER:
            if (!numericValue.isNaN()) {
                if (displayAsInt) {
                    displayStr = String.valueOf(Math.round(numericValue));
                } else {
                    displayStr = String
                            .format(numberFormatString, numericValue);
                }
            } else {
                displayStr = "Unk";
            }
            break;
        case STRING:
            displayStr = stringValue;
            break;
        default:
            displayStr = "Unknown";
            break;
        }

        return displayStr;
    }

    /**
     * @return the foregroundColor
     */
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /**
     * @param foregroundColor
     *            the foregroundColor to set
     */
    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    /**
     * @return the backgroundColor
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * @param backgroundColor
     *            the backgroundColor to set
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @return the modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * @return the booleanValue
     */
    public boolean getBooleanValue() {
        return booleanValue;
    }

    /**
     * @param booleanValue
     *            the booleanValue to set
     */
    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    /**
     * Sort method.
     * 
     * @param direction
     *            The sort direction
     * @return Sort direction value
     */
    public Object sortByObject(SortDirection direction) {
        if (stringValue != null) {
            return String.format("%-30S", stringValue);
        } else if (!numericValue.isNaN()) {
            if (displayAsInt) {
                return new Float(Math.round(numericValue));
            }

            return new Float(numericValue);
        } else if (numericValue.isNaN()) {
            if (direction == SortDirection.DESCENDING) {
                return Float.MAX_VALUE * -1.0f;
            }
            return Float.MAX_VALUE;
        }

        return "Unknown";
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}