/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.test.mock.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jcr.PathNotFoundException;

import org.junit.Test;

/**
 * @version $Id$
 */
public class MockNodeTest {

    @Test
    public void testAddNodeWithParamFakeJCRNode() throws Exception {
        final MockNode parent = new MockNode("parent");
        final MockNode child = new MockNode("child");
        parent.addNode(child);

        assertEquals(parent, child.getParent());
        assertEquals(parent.getChildren().get("child"), child);
    }

    @Test
    public void testAddNodeWithParamString() throws Exception {
        final MockNode parent = new MockNode("parent");
        final MockNode child = (MockNode) parent.addNode("child");

        assertEquals(parent, child.getParent());
        assertEquals(parent.getChildren().get("child"), child);
    }

    @Test
    public void testAddNodeWithParamStringString() throws Exception {
        final MockNode parent = new MockNode("parent");
        final MockNode child = (MockNode) parent.addNode("child", "primaryNodeTypeName");

        assertEquals(parent, child.getParent());
        assertEquals(parent.getChildren().get("child"), child);
        assertEquals("primaryNodeTypeName", child.getPrimaryNodeType().getName());
    }

    @Test
    public void testGetNodeWithExistingPath() throws Exception {
        final MockNode parent = new MockNode("parent");
        final MockNode child = (MockNode) parent.addNode("child");
        final MockItem childOfChild = (MockItem) child.addNode("childOfChild");

        assertEquals(child, parent.getNode("child"));
        assertEquals(childOfChild, parent.getNode("child/childOfChild"));
    }

    @Test
    public void testGetNodeWithFalsePath() throws Exception {
        final MockNode parent = new MockNode("parent");

        try {
            parent.getNode("does/not/exist");
            fail("Expected excption instead");
        } catch (PathNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testHasNode() throws Exception {
        final MockNode parent = new MockNode("parent");
        final MockNode child = (MockNode) parent.addNode("child");
        child.addNode("childOfChild");

        assertTrue(parent.hasNode("child/childOfChild"));
        assertTrue(!parent.hasNode("childOfChild"));
        assertTrue(!parent.hasNode("does/not/exist"));
    }

    @Test
    public void testHasNodes() throws Exception {
        final MockNode parent = new MockNode("parent");
        final MockNode child = new MockNode("child");
        parent.addNode(child);

        assertTrue(parent.hasNodes());
        assertTrue(!child.hasNodes());
    }

    @Test
    public void testHasProperties() throws Exception {
        final MockNode parent = new MockNode("parent");

        assertTrue(!parent.hasProperties());

        parent.setProperty("property", "string");
        assertTrue(parent.hasProperties());
    }

    @Test
    public void testSetPropertyWithStringAndBoolean() throws Exception {
        final MockNode parent = new MockNode("parent");
        parent.setProperty("boolean", false);

        assertEquals(false, parent.getProperty("boolean").getValue().getBoolean());
    }

    @Test
    public void testSetPropertyWithStringAndValue() throws Exception {
        final MockNode parent = new MockNode("parent");
        final MockValue value = new MockValue("stringValue");
        parent.setProperty("string", value);

        assertEquals(value, parent.getProperty("string").getValue());
    }

    @Test
    public void testChildNodesAndPropertiesGetProperSession() throws Exception {
        MockSession session = new MockSession("test");

        MockNode child = (MockNode) session.getRootNode().addNode("child");
        MockProperty property = (MockProperty) child.setProperty("property", "propertyValue");

        assertEquals(session, child.getSession());
        assertEquals(session, property.getSession());
    }
}
