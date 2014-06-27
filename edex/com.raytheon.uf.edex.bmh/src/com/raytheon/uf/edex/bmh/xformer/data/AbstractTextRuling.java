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
package com.raytheon.uf.edex.bmh.xformer.data;

import java.util.UUID;

/**
 * Abstract representation of text that is a transformation candidate.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 26, 2014 3302       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractTextRuling implements ITextRuling {

    private String id;

    private String text;

    /**
     * Constructor
     * 
     * @param text
     *            candidate text
     */
    public AbstractTextRuling(String text) {
        this.id = UUID.randomUUID().toString().toUpperCase();
        this.text = text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.xformer.data.ITextRuling#getID()
     */
    @Override
    public String getID() {
        return this.id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.bmh.xformer.data.ITextRuling#getText()
     */
    @Override
    public String getText() {
        return this.text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractTextRuling other = (AbstractTextRuling) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}