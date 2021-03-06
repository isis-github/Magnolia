/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.NonExistingNodeData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.jackrabbit.util.ChildrenCollectorFilter;


/**
 * Mock implementation of Content.
 */
public class MockContent extends DefaultContent {

    /**
     * Filters a name of a NodeData or Content instance according to the same rules applied by Jackrabbit
     * in the Property and Node interfaces.
     */
    private static class NamePatternFilter implements Predicate {
        private final String namePattern;

        public NamePatternFilter(String namePattern) {
            this.namePattern = namePattern;
        }

        @Override
        public boolean evaluate(Object object) {
            return matchesNamePattern(object, namePattern);
        }
    }


    public MockContent(String name) {
        this(new MockNode(name));
    }

    public MockContent(String name, ItemType type) {
        this(new MockNode(name, type.getSystemName()));
    }

    public MockContent(MockNode node) {
        super(node);
    }

    public MockContent(MockNode rootNode, String path) throws PathNotFoundException, RepositoryException, AccessDeniedException{
        super(rootNode, path);
    }

    public MockContent(MockNode rootNode, String path, String contentType) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        super(rootNode, path, contentType);
    }

    public void setUUID(String identifier) {
        ((MockNode) getJCRNode()).setIdentifier(identifier);
    }

    @Override
    public String getHandle() {
        try {
            return getJCRNode().getPath();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public MetaData getMetaData() {
        return new MetaData(getJCRNode());
    }

    @Override
    public Collection<NodeData> getNodeDataCollection(String namePattern) {
        // FIXME try to find a better solution than filtering now
        // problem is that getNodeData(name, type) will have to add the node data
        // as setValue() might be called later on an the node data starts to exist
        ArrayList<NodeData> onlyExistingNodeDatas = new ArrayList<NodeData>();
        final String pattern = namePattern == null ? "*" : namePattern;
        try {
            PropertyIterator iterator = getJCRNode().getProperties(pattern);
            while(iterator.hasNext()) {
                Property property = iterator.nextProperty();
                MockNodeData nodeData = new MockNodeData(this, property.getName(), property.getType());
                onlyExistingNodeDatas.add(nodeData);
            }
            // now checking for binaryNodes
            NodeIterator nodeIterator = getJCRNode().getNodes(pattern);
            Node currentNode;
            while(nodeIterator.hasNext()) {
                currentNode = nodeIterator.nextNode();
                if (NodeTypes.Resource.NAME.equals(currentNode.getPrimaryNodeType().getName())) {
                    onlyExistingNodeDatas.add(addBinaryNodeData(currentNode.getName()));
                }
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return onlyExistingNodeDatas;
    }


    @Override
    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Node parentNode = getJCRNode().getParent();
        return parentNode == null ? null: new MockContent((MockNode) parentNode);
    }

    @Override
    public NodeData newNodeDataInstance(String name, int type, boolean createIfNotExisting) throws AccessDeniedException, RepositoryException {
        if(hasNodeData(name)){
            Property property;
            try {
                property = getJCRNode().getProperty(name);
                Value value = property.getValue();
                return new MockNodeData(this, name, value.getType());
            } catch (PathNotFoundException e) {
                // exception although hasNodeData returned true -> then it's a binary!
            }
            return addBinaryNodeData(name);
        }
        else if(!createIfNotExisting){
            //&& type != PropertyType.BINARY){
            // binaries might have been created via property format or import, so we currently only have them as MockContent instances in the system
            // todo - better fix and/or remove them from child nodes ?
            Content parent;
            try {
                parent = getParent();
            } catch (ItemNotFoundException e) {
                // that's ok - we use null then
                parent = null;
            }
            return new NonExistingNodeData(parent, name);
        }
        else{
            NodeData nd;
            if(type == PropertyType.BINARY){
                nd = addBinaryNodeData(name);
            }
            else{
                nd = addNodeData(name, type);
            }
            return nd;
        }
    }

    public MockNodeData addNodeData(String name, Object value) {
        return new MockNodeData(this, name, value);
    }

    public void addContent(MockContent content) {
        ((MockNode) getJCRNode()).addNode((MockNode) content.getJCRNode());
    }

    public void setName(String name) {
        ((MockNode)getJCRNode()).setName(name);
    }

    public MetaData createMetaData() {
        addContent(new MockContent("MetaData"));
        return getMetaData();
    }

    @Override
    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        MockContent c = new MockContent(name, new ItemType(contentType));
        addContent(c);

        if (c.isNodeType(ItemType.NT_RESOURCE)) {
            // TODO dlipp - to be verified
            addBinaryNodeData(name);
        }
        return c;
    }

    @Override
    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new MockContent((MockNode) node, name));
    }

    @Override
    public Collection<Content> getChildren(final ContentFilter filter, final String namePattern, Comparator<Content> orderCriteria) {
        // copy
        final Collection<MockNode> children = ((MockNode)node).getChildren().values();
        final Collection<Content> contentChildren = new ArrayList<Content>();
        for(MockNode current: children) {
            contentChildren.add(wrapAsContent(current));
        }

        final Predicate filterPredicate = new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return filter.accept((Content) object);
            }
        };

        CollectionUtils.filter(contentChildren, filterPredicate);

        if (namePattern != null) {
            CollectionUtils.filter(contentChildren, new NamePatternFilter(namePattern));
        }


        return contentChildren;
    }


    private static boolean matchesNamePattern(Object object, String namePattern) {
        final String name;
        if (object instanceof NodeData) {
            name = ((NodeData) object).getName();
        } else if (object instanceof Content) {
            name = ((Content) object).getName();
        } else {
            throw new IllegalStateException("Unsupported object type: " + object.getClass());
        }
        return ChildrenCollectorFilter.matches(name, namePattern);
    }

    @Override
    public void delete() throws RepositoryException {
        final MockNode parent = (MockNode) getParent().getJCRNode();
        final boolean removedFromParent = parent.removeFromChildren(getJCRNode());

        if (!removedFromParent) {
            throw new RepositoryException("MockContent could not delete itself");
        }
    }

    @Override
    protected Content wrapAsContent(Node node) {
        return new MockContent((MockNode)node);
    }

    @Override
    protected Content wrapAsContent(Node node, String name) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        return new MockContent((MockNode) node, name);
    }

    @Override
    protected Content wrapAsContent(Node node, String name, String contentType) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        return new MockContent((MockNode) node, name, contentType);
    }

    @Override
    protected HierarchyManager createHierarchyManager(Session session) {
        return new MockHierarchyManager(session);
    }

    @Override
    public Workspace getWorkspace() throws RepositoryException {
        return node.getSession() != null ? node.getSession().getWorkspace() : null;
    }

}
