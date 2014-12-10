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
package com.raytheon.uf.common.bmh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Request used to copy the entire operational database to a practice database.
 * This operation can be slow so be prepared to wait.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Dec 09, 2014  3824     rferrel     Initial creation.
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
@DynamicSerialize
public class ImportLegacyDbRequest extends AbstractBMHServerRequest {
    /** Source file name. */
    @DynamicSerializeElement
    private String source;

    /** Contents of the source file. */
    @DynamicSerializeElement
    private byte[] input;

    public byte[] getInput() {
        return input;
    }

    public void setInput(byte[] input) {
        this.input = input;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
