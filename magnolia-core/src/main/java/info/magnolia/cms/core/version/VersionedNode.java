/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.core.version;

import java.util.Calendar;

import info.magnolia.cms.core.util.DelegateNodeWrapper;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

/**
 * Wrapper for version of the node exposing frozen node content as its own as used to happen in old Content API.
 * @author had
 * @version $Id: $
 */
public class VersionedNode extends DelegateNodeWrapper implements Node, Version {


    private final Node frozenNode;
    private final Version version;

    public VersionedNode(Version versionedNode) throws PathNotFoundException, RepositoryException {
        this.version = versionedNode;
        this.frozenNode = versionedNode.getNode("jcr:frozenNode");
    }

    @Override
    public Node getWrappedNode() {
        return this.frozenNode;
    }

    public VersionHistory getContainingHistory() throws RepositoryException {
        return version.getContainingHistory();
    }

    public Calendar getCreated() throws RepositoryException {
        return version.getCreated();
    }

    public Node getFrozenNode() throws RepositoryException {
        return frozenNode;
    }

    public Version getLinearPredecessor() throws RepositoryException {
        return version.getLinearPredecessor();
    }

    public Version getLinearSuccessor() throws RepositoryException {
        return version.getLinearSuccessor();
    }

    public Version[] getPredecessors() throws RepositoryException {
        return null;
    }

    public Version[] getSuccessors() throws RepositoryException {
        return version.getSuccessors();
    }

}