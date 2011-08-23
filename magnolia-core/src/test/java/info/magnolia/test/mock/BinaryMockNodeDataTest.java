/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.test.mock;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;
import junit.framework.TestCase;


public class BinaryMockNodeDataTest extends TestCase {

    /**
     * MAGNOLIA-3777: mock content: write binary attributes to the underlying binary node if existing
     */
    public void testThatAttributesAreSetOnThePassedNode() throws AccessDeniedException, UnsupportedOperationException, RepositoryException{
        // GIVEN a binary node data which wraps a resource node
        MockContent resourceNode = new MockContent("file", new ItemType(ItemType.NT_FILE));
        BinaryMockNodeData binaryNodeData = new BinaryMockNodeData("file", resourceNode);

        // WHEN setting an attribute
        binaryNodeData.setAttribute("attribute", "value");

        // THEN the value should be stored in the node
        assertEquals(resourceNode.getNodeData("attribute").getString(), "value");

    }

}