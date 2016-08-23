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
package com.raytheon.uf.common.bmh.datamodel.msg;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.datamodel.PositionUtil;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Broadcast Contents Record Object. Used to keep track of the message contents
 * and associated audio. A time-based attribute is used to keep track of when
 * the contents were generated to allow for multiple versions of broadcast
 * message contents which will be required to reuse {@link BroadcastMessage}s
 * for inline message edits with an associated content change.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 2, 2015  4293       bkowal      Initial creation
 * May 12, 2015 4248       rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * Jul 28, 2016 5722       rjpeter     Serialized id, handled null in compare.
 * </pre>
 * 
 * @author bkowal
 */
@Entity
@DynamicSerialize
@Table(name = "broadcast_msg_contents")
public class BroadcastContents implements Comparable<BroadcastContents> {

    @EmbeddedId
    @DynamicSerializeElement
    private BroadcastContentsPK id;

    @OneToMany(mappedBy = "contents", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<BroadcastFragment> fragments;

    @ManyToOne(optional = false)
    @MapsId("broadcastId")
    @JoinColumn(name = "broadcast_id")
    @ForeignKey(name = "fk_broadcast_msg_contents_to_broadcast_msg")
    private BroadcastMsg broadcastMsg;

    public long getTimestamp() {
        this.checkId();
        return this.id.getTimestamp();
    }

    /**
     * @return the id
     */
    public BroadcastContentsPK getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(BroadcastContentsPK id) {
        this.id = id;
    }

    private void checkId() {
        if (this.id == null) {
            this.id = new BroadcastContentsPK();
        }
    }

    /**
     * @return the fragments
     */
    public Set<BroadcastFragment> getFragments() {
        return fragments;
    }

    public List<BroadcastFragment> getOrderedFragments() {
        if (fragments == null) {
            return Collections.emptyList();
        }

        return PositionUtil.order(fragments);
    }

    /**
     * Sets the BroadcastFragments in the specified position order.
     * 
     * @param fragments
     */
    public void setOrderedFragments(List<BroadcastFragment> fragments) {
        if (fragments == null) {
            this.fragments = null;
            return;
        }

        PositionUtil.updatePositions(fragments);
        setFragments(new HashSet<>(fragments));
    }

    public void addFragment(BroadcastFragment fragment) {
        if (this.fragments == null) {
            this.fragments = new HashSet<>();
        }
        fragment.setContents(this);
        this.fragments.add(fragment);
    }

    /**
     * @param fragments
     *            the fragments to set
     */
    public void setFragments(Set<BroadcastFragment> fragments) {
        this.fragments = fragments;
        if (this.fragments != null && this.fragments.isEmpty() == false) {
            for (BroadcastFragment fragment : this.fragments) {
                fragment.setContents(this);
            }
        }
    }

    /**
     * @return the broadcastMsg
     */
    public BroadcastMsg getBroadcastMsg() {
        return broadcastMsg;
    }

    /**
     * @param broadcastMsg
     *            the broadcastMsg to set
     */
    public void setBroadcastMsg(BroadcastMsg broadcastMsg) {
        this.checkId();
        this.broadcastMsg = broadcastMsg;
        this.id.setBroadcastId(this.broadcastMsg.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(BroadcastContents o) {
        if (this.id == null) {
            if (o.id == null) {
                return 0;
            } else {
                return -1;
            }
        }
        return this.id.compareTo(o.getId());
    }
}
