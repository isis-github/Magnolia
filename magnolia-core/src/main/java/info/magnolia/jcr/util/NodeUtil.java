/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.jcr.util;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.iterator.NodeIterableAdapter;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;
import info.magnolia.jcr.wrapper.JCRPropertiesFilteringNodeWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.FilteringNodeIterator;
import org.apache.jackrabbit.commons.predicate.NodeTypePredicate;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utility methods to collect data from JCR repository.
 */
public class NodeUtil {

    private static final Logger log = LoggerFactory.getLogger(NodeUtil.class);

    /**
     * Predicate hiding properties prefixed with jcr or mgnl.
     * @deprecated since 5.0 - obsolete as there's no nodetypes with namespace jcr. In addition you could use {@link info.magnolia.jcr.predicate.JCRMgnlPropertyHidingPredicate}
     */
    public static AbstractPredicate<Property> ALL_PROPERTIES_EXCEPT_JCR_AND_MGNL_FILTER = new AbstractPredicate<Property>() {

        @Override
        public boolean evaluateTyped(Property property) {
            try {
                String name = property.getName();
                return !name.startsWith(NodeTypes.JCR_PREFIX) && !name.startsWith(NodeTypes.MGNL_PREFIX);
            } catch (RepositoryException e) {
                String path;
                try {
                    path = property.getPath();
                } catch (RepositoryException e1) {
                    path = "<path not available>";
                }
                log.error("Unable to read name of property {}", path);
                // either invalid or not accessible to the current user
                return false;
            }
        }
    };

    /**
     * Node filter accepting everything except nodes with namespace jcr (version and system store).
     * @deprecated since 5.0 - obsolete as there's no nodetypes with namespace jcr
     */
    public static Predicate ALL_NODES_EXCEPT_JCR_FILTER = new AbstractPredicate<Node>() {
        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return !node.getName().startsWith(NodeTypes.JCR_PREFIX);
            } catch (RepositoryException e) {
                log.error("Unable to read name for node {}", getNodePathIfPossible(node));
                return false;
            }
        }
    };

    /**
     * Node filter accepting everything except meta data and jcr types.
     * @deprecated since 5.0 - obsolete as there's no nodetypes with namespace jcr and because of MAGNOLIA-4640
     */
    public static AbstractPredicate<Node> EXCLUDE_META_DATA_FILTER = new AbstractPredicate<Node>() {

        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return !node.getName().startsWith(NodeTypes.JCR_PREFIX)
                && !NodeUtil.isNodeType(node, NodeTypes.MetaData.NAME);
            } catch (RepositoryException e) {
                log.error("Unable to read name or nodeType for node {}", getNodePathIfPossible(node));
                return false;
            }
        }
    };

    /**
     * Node filter accepting all nodes of a type with namespace mgnl.
     */
    public static AbstractPredicate<Node> MAGNOLIA_FILTER = new AbstractPredicate<Node>() {

        @Override
        public boolean evaluateTyped(Node node) {

            try {
                String nodeTypeName = node.getPrimaryNodeType().getName();
                // accept only "magnolia" nodes
                return nodeTypeName.startsWith(NodeTypes.MGNL_PREFIX);
            } catch (RepositoryException e) {
                log.error("Unable to read nodeType for node {}", getNodePathIfPossible(node));
            }
            return false;
        }
    };

    /**
     * Get a Node by identifier.
     */
    public static Node getNodeByIdentifier(String workspace, String identifier) throws RepositoryException {
        Node target = null;
        Session jcrSession;
        if (workspace == null || identifier == null) {
            return target;
        }

        jcrSession = MgnlContext.getJCRSession(workspace);
        if (jcrSession != null) {
            target = jcrSession.getNodeByIdentifier(identifier);
        }
        return target;
    }

    /**
     * from default content.
     */
    public static boolean hasMixin(Node node, String mixinName) throws RepositoryException {
        if (StringUtils.isBlank(mixinName)) {
            throw new IllegalArgumentException("Mixin name can't be empty.");
        }
        for (NodeType type : node.getMixinNodeTypes()) {
            if (mixinName.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO dlipp: better name? Clear javadoc! Do not assign method-param!
     * TODO cringele : shouldn't @param nodeType be aligned to JCR API? There it is nodeTypeName, nodeType is used for NodeType object
     */
    public static boolean isNodeType(Node node, String type) throws RepositoryException {
        node = NodeUtil.deepUnwrap(node, JCRPropertiesFilteringNodeWrapper.class);
        final String actualType = node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString();
        // if the node is frozen, and we're not looking specifically for frozen nodes, then we compare with the original
        // node type
        if (JcrConstants.NT_FROZENNODE.equals(actualType) && !(JcrConstants.NT_FROZENNODE.equals(type))) {
            final Property p = node.getProperty(JcrConstants.JCR_FROZENPRIMARYTYPE);
            final String s = p.getString();
            NodeTypeManager ntManager = node.getSession().getWorkspace().getNodeTypeManager();
            NodeType primaryNodeType = ntManager.getNodeType(s);
            return primaryNodeType.isNodeType(type);
        }
        return node.isNodeType(type);
    }

    public static Node unwrap(Node node) throws RepositoryException {
        Node unwrappedNode = node;
        while (unwrappedNode instanceof DelegateNodeWrapper) {
            unwrappedNode = ((DelegateNodeWrapper) unwrappedNode).getWrappedNode();
        }
        return unwrappedNode;
    }

    /**
     * Removes a wrapper by type. The wrapper can be deep in a chain of wrappers in which case wrappers before it will
     * be cloned creating a new chain that leads to the same real node.
     */
    public static Node deepUnwrap(Node node, Class<? extends DelegateNodeWrapper> wrapper) {
        if (node instanceof DelegateNodeWrapper) {
            return ((DelegateNodeWrapper) node).deepUnwrap(wrapper);
        }
        return node;
    }

    /**
     * Removes all wrappers of a given type. Other wrappers are cloned creating a new chain that leads to the same real
     * node.
     */
    public static Node deepUnwrapAll(Node node, Class<? extends DelegateNodeWrapper> wrapperClass) {
        while (node instanceof DelegateNodeWrapper) {
            Node unwrapped = ((DelegateNodeWrapper) node).deepUnwrap(wrapperClass);
            // If the unwrapping had no effect we're done
            if (unwrapped == node) {
                break;
            }
            node = unwrapped;
        }
        return node;
    }

    public static boolean isWrappedWith(Node node, Class<? extends DelegateNodeWrapper> wrapper) {
        if (wrapper.isInstance(node)){
            return true;
        }

        if (node instanceof DelegateNodeWrapper) {
            return isWrappedWith(((DelegateNodeWrapper)node).getWrappedNode(), wrapper);
        }
        return false;
    }


    /**
     * Convenience - delegate to {@link Node#orderBefore(String, String)}.
     */
    public static void orderBefore(Node node, String siblingName) throws RepositoryException {
        node.getParent().orderBefore(node.getName(), siblingName);
    }

    /**
     * Orders the node directly after a given sibling. If no sibling is specified the node is placed first.
     */
    public static void orderAfter(Node node, String siblingName) throws RepositoryException {

        if (siblingName == null) {
            orderFirst(node);
            return;
        }

        Node parent = node.getParent();
        Node sibling = parent.getNode(siblingName);
        Node siblingAfter = getSiblingAfter(sibling);

        if (siblingAfter == null) {
            orderLast(node);
            return;
        }

        // Move the node before the sibling directly after the target sibling
        parent.orderBefore(node.getName(), siblingAfter.getName());
    }

    /**
     * Orders the node first among its siblings.
     */
    public static void orderFirst(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator siblings = parent.getNodes();
        Node firstSibling = siblings.nextNode();
        if (!firstSibling.isSame(node)) {
            parent.orderBefore(node.getName(), firstSibling.getName());
        }
    }

    /**
     * Orders the node last among its siblings.
     */
    public static void orderLast(Node node) throws RepositoryException {
        node.getParent().orderBefore(node.getName(), null);
    }

    /**
     * Orders the node up one step among its siblings. If the node is the only sibling or the first sibling this method
     * has no effect.
     */
    public static void orderNodeUp(Node node) throws RepositoryException {
        Node siblingBefore = getSiblingBefore(node);
        if (siblingBefore != null) {
            node.getParent().orderBefore(node.getName(), siblingBefore.getName());
        }
    }

    /**
     * Orders the node down one step among its siblings. If the node is the only sibling or the last sibling this method
     * has no effect.
     */
    public static void orderNodeDown(Node node) throws RepositoryException {
        Node siblingAfter = getSiblingAfter(node);
        if (siblingAfter != null) {
            node.getParent().orderBefore(siblingAfter.getName(), node.getName());
        }
    }

    public static Node getSiblingBefore(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator siblings = parent.getNodes();
        Node previousSibling = null;
        while (siblings.hasNext()) {
            Node sibling = siblings.nextNode();
            if (isSame(node, sibling)) {
                return previousSibling;
            }
            previousSibling = sibling;
        }
        return null;
    }

    public static Node getSiblingAfter(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator siblings = parent.getNodes();
        while (siblings.hasNext()) {
            Node sibling = siblings.nextNode();
            if (isSame(node, sibling)) {
                break;
            }
        }
        return siblings.hasNext() ? siblings.nextNode() : null;
    }

    /**
     * Gets the siblings of this node.
     * @param node node from which will be siblings retrieved
     * @param nodeTypeName requested type of siblings nodes
     * @return list of siblings of the given Node (only the given node is excluded)
     */
    public static Iterable<Node> getSiblings(Node node) throws RepositoryException {
        Node parent = node.getParent();
        Iterable<Node> allSiblings = NodeUtil.getNodes(parent);
        List<Node> siblings = new ArrayList<Node>();

        for(Node sibling: allSiblings) {
            if (!NodeUtil.isSame(node, sibling)) {
                siblings.add(sibling);
            }
        }
        return siblings;
    }

    /**
     * Gets the siblings of this node with certain type.
     * @param node node from which will be siblings retrieved
     * @param nodeTypeName requested type of siblings nodes
     * @return list of siblings of the given Node (the given node is excluded)
     */
    public static Iterable<Node> getSiblings(Node node, String nodeTypeName) throws RepositoryException {
        Node parent = node.getParent();
        Iterable<Node> allSiblings = NodeUtil.getNodes(parent, nodeTypeName);
        List<Node> sameTypeSiblings = new ArrayList<Node>();

        for(Node sibling: allSiblings) {
            if (!NodeUtil.isSame(node, sibling)) {
                sameTypeSiblings.add(sibling);
            }
        }
        return sameTypeSiblings;
    }

    /**
     * Gets the siblings of this node according to predicate.
     * @param node node from which will be siblings retrieved
     * @param predicate predicate
     * @return list of siblings of the given Node (the given node is excluded)
     */
    public static Iterable<Node> getSiblings(Node node, Predicate predicate) throws RepositoryException {
        Node parent = node.getParent();
        Iterable<Node> allSiblings = NodeUtil.getNodes(parent, predicate);
        List<Node> sameTypeSiblings = new ArrayList<Node>();

        for(Node sibling: allSiblings) {
            if (!NodeUtil.isSame(node, sibling)) {
                sameTypeSiblings.add(sibling);
            }
        }
        return sameTypeSiblings;
    }

    /**
     * Gets the siblings before this node.
     * @param node node from which will be siblings retrieved
     * @return list of siblings before the given Node (the given node is excluded)
     */
    public static Iterable<Node> getSiblingsBefore(Node node) throws RepositoryException {
        int toIndex = 0;
        Node parent = node.getParent();
        List<Node> allSiblings = NodeUtil.asList(NodeUtil.getNodes(parent));

        for(Node sibling: allSiblings) {
            if (NodeUtil.isSame(node, sibling)) {
                break;
            }
            toIndex++;
        }
        return allSiblings.subList(0, toIndex);
    }

    /**
     * Gets the siblings after this node.
     * @param node node from which will be siblings retrieved
     * @return list of siblings after the given Node (the given node is excluded)
     */
    public static Iterable<Node> getSiblingsAfter(Node node) throws RepositoryException {
        int fromIndex = 0;
        Node parent = node.getParent();
        List<Node> allSiblings = NodeUtil.asList(NodeUtil.getNodes(parent));

        for(Node sibling: allSiblings) {
            if (NodeUtil.isSame(node, sibling)) {
                fromIndex++;
                break;
            }
            fromIndex++;
        }
        return allSiblings.subList(fromIndex, allSiblings.size());
    }

    /**
     * Gets the siblings before this node with certain type.
     * @param node node from which will be siblings retrieved
     * @param nodeTypeName requested type of siblings nodes
     * @return list of siblings before the given Node (the given node is excluded)
     */
    public static Iterable<Node> getSiblingsBefore(Node node, String nodeTypeName) throws RepositoryException {
        Node parent = node.getParent();
        Iterable<Node> allSiblings = NodeUtil.getNodes(parent);
        List<Node> sameTypeSiblings = new ArrayList<Node>();

        for(Node sibling: allSiblings) {
            if (NodeUtil.isSame(node, sibling)) {
                break;
            }
            if (isNodeType(sibling, nodeTypeName)) {
                sameTypeSiblings.add(sibling);
            }
        }
        return sameTypeSiblings;
    }

    /**
     * Gets the siblings after this node with certain type.
     * @param node node from which will be siblings retrieved
     * @param nodeTypeName requested type of siblings nodes
     * @return list of siblings after the given Node (the given node is excluded)
     */
    public static Iterable<Node> getSiblingsAfter(Node node, String nodeTypeName) throws RepositoryException {
        Node parent = node.getParent();
        List<Node> allSiblings = NodeUtil.asList(NodeUtil.getNodes(parent));
        int fromIndex = 0;

        for(Node sibling: allSiblings) {
            fromIndex++;
            if (NodeUtil.isSame(node, sibling)) {
                break;
            }
        }

        List<Node> sameTypeSiblings = new ArrayList<Node>();
        for(Node sibling: allSiblings.subList(fromIndex, allSiblings.size())) {
            if (isNodeType(sibling, nodeTypeName)) {
                sameTypeSiblings.add(sibling);
            }
        }
        return sameTypeSiblings;
    }

    public static void moveNode(Node nodeToMove, Node newParent) throws RepositoryException {
        if (!isSame(newParent, nodeToMove.getParent())) {
            String newPath = combinePathAndName(newParent.getPath(), nodeToMove.getName());
            nodeToMove.getSession().move(nodeToMove.getPath(), newPath);
        }
    }

    public static void moveNodeBefore(Node nodeToMove, Node target) throws RepositoryException {
        Node targetParent = target.getParent();
        moveNode(nodeToMove, targetParent);
        targetParent.orderBefore(nodeToMove.getName(), target.getName());
    }

    public static void moveNodeAfter(Node nodeToMove, Node target) throws RepositoryException {
        Node targetParent = target.getParent();
        moveNode(nodeToMove, targetParent);
        orderAfter(nodeToMove, target.getName());
    }

    public static boolean isFirstSibling(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator nodes = parent.getNodes();
        return isSame(nodes.nextNode(), node);
    }

    /**
     * Check if node1 and node2 are siblings.
     */
    public static boolean isSameNameSiblings(Node node1, Node node2) throws RepositoryException {
        Node parent1 = node1.getParent();
        Node parent2 = node2.getParent();
        return isSame(parent1, parent2) && node1.getName().equals(node2.getName());
    }

    public static boolean isLastSibling(Node node) throws RepositoryException {
        Node parent = node.getParent();
        NodeIterator nodes = parent.getNodes();
        Node last = null;
        while (nodes.hasNext()) {
            last = nodes.nextNode();
        }
        return isSame(last, node);
    }

    public static void renameNode(Node node, String newName) throws RepositoryException {
        Node parent = node.getParent();
        String newPath = combinePathAndName(parent.getPath(), newName);
        node.getSession().move(node.getPath(), newPath);
    }


    /**
     * @return Whether the provided node as the provided permission or not.
     * @throws RuntimeRepositoryException in case of RepositoryException.
     */
    public static boolean isGranted(Node node, long permissions) {
        try {
            return PermissionUtil.isGranted(node, permissions);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * Returns true if both arguments represents the same node. In case the nodes are wrapped the comparison is done one
     * the actual nodes behind the wrappers.
     */
    public static boolean isSame(Node lhs, Node rhs) throws RepositoryException {
        return unwrap(lhs).isSame(unwrap(rhs));
    }

    /**
     * @return a valid jcr path combined from the provided path and name.
     */
    public static String combinePathAndName(String path, String name) {
        if ("/".equals(path)) {
            return "/" + name;
        }
        return path + "/" + name;
    }

    /**
     * Creates a node under the specified parent and relative path, then returns it. Should the node already exist, the
     * method will simply return it.
     */
    public static Node createPath(Node parent, String relPath, String primaryNodeTypeName) throws RepositoryException, PathNotFoundException, AccessDeniedException {
        return createPath(parent, relPath, primaryNodeTypeName, false);
    }

    /**
     * Creates a node under the specified parent and relative path, then returns it. Should the node already exist, the
     * method will simply return it.
     */
    public static Node createPath(Node parent, String relPath, String primaryNodeTypeName, boolean save) throws RepositoryException, PathNotFoundException, AccessDeniedException {
        // remove leading /
        String currentPath = StringUtils.removeStart(relPath, "/");

        if (StringUtils.isEmpty(currentPath)) {
            return parent;
        }

        Node root = parent;
        String[] names = currentPath.split("/");

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (root.hasNode(name)) {
                root = root.getNode(name);
            } else {
                final Node newNode = root.addNode(name, primaryNodeTypeName);
                if (save) {
                    root.getSession().save();
                }
                root = newNode;
            }
        }
        return root;
    }

    /**
     * Visits the given node and then all of nodes beneath it except for metadata nodes and nodes of jcr type.
     */
    public static void visit(Node node, NodeVisitor visitor) throws RepositoryException {
        visit(node, visitor, EXCLUDE_META_DATA_FILTER);
    }

    public static void visit(Node node, NodeVisitor visitor, Predicate predicate) throws RepositoryException {
        // TODO should it really visit the start node even if it doesn't match the filter?
        visitor.visit(node);
        for (Node child : getNodes(node, predicate)) {
            visit(child, visitor, predicate);
        }
        if (visitor instanceof PostNodeVisitor) {
            ((PostNodeVisitor) visitor).postVisit(node);
        }
    }

    public static Iterable<Node> getNodes(Node parent, Predicate predicate) throws RepositoryException {
        return asIterable(new FilteringNodeIterator(parent.getNodes(), predicate));
    }

    public static Iterable<Node> getNodes(Node parent) throws RepositoryException {
        return getNodes(parent, EXCLUDE_META_DATA_FILTER);
    }

    public static Iterable<Node> getNodes(Node parent, String nodeTypeName) throws RepositoryException {
        return getNodes(parent, new NodeTypePredicate(nodeTypeName, false));
    }

    public static Iterable<Node> asIterable(NodeIterator iterator) {
        return new NodeIterableAdapter(iterator);
    }

    public static List<Node> asList(Iterable<Node> nodes) {
        List<Node> nodesList = new ArrayList<Node>();
        for (Node node : nodes) {
            nodesList.add(node);
        }
        return nodesList;
    }

    /**
     * This method return the node's name on success, otherwise it handles the {@link RepositoryException} by throwing a
     * {@link RuntimeRepositoryException}.
     */
    public static String getName(Node content) {
        try {
            return content.getName();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * Get all children (by recursion) using MAGNOLIA_FILTER (filter accepting all nodes of a type with namespace mgnl).
     */
    public static Iterable<Node> collectAllChildren(Node node) throws RepositoryException {
        List<Node> nodes = new ArrayList<Node>();
        return collectAllChildren(nodes, node, MAGNOLIA_FILTER);
    }

    /**
     * Get all children (by recursion) using a Predicate.
     */
    public static Iterable<Node> collectAllChildren(Node node, Predicate predicate) throws RepositoryException {
        List<Node> nodes = new ArrayList<Node>();
        return collectAllChildren(nodes, node, predicate);
    }

    /**
     * Get all children (by recursion) using a Predicate.
     * // TODO this method should really be private or renamed
     */
    public static Iterable<Node> collectAllChildren(List<Node> nodes, Node parent, Predicate predicate) throws RepositoryException {
        // get filtered sub nodes first
        nodes.addAll(asList(getNodes(parent, predicate)));

        // get all children to find recursively
        Iterator<Node> allChildren = getNodes(parent, EXCLUDE_META_DATA_FILTER).iterator();

        // recursion
        while (allChildren.hasNext()) {
            collectAllChildren(nodes, allChildren.next(), predicate);
        }

        return nodes;
    }

    /**
     * Get all Ancestors until level 1.
     */
    public static Collection<Node> getAncestors(Node node) throws RepositoryException {
        List<Node> allAncestors = new ArrayList<Node>();
        int level = node.getDepth();
        while (level != 0) {
            try {
                allAncestors.add((Node) node.getAncestor(--level));
            } catch (AccessDeniedException e) {
                log.debug("Node " + node.getIdentifier() + " didn't allow access to Ancestor's ");
            }
        }
        return allAncestors;
    }

    /**
     * Used for building exception messages where we want to avoid handling another exception inside a throws clause.
     */
    public static String getNodeIdentifierIfPossible(Node content) {
        try {
            return content.getIdentifier();
        } catch (RepositoryException e) {
            return "<not available>";
        }
    }

    public static String getNodePathIfPossible(Node node) {
        try {
            return node.getPath();
        } catch (RepositoryException e) {
            return "<not available>";
        }
    }

    /**
     * Return the Path of the node.
     *
     * @return the path for the node or an empty String in case of exception
     */
    public static String getPathIfPossible(Node node) {
        try {
            return node.getPath();
        } catch (RepositoryException e) {
            log.error("Failed to get handle: " + e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    public static NodeIterator filterNodeType(NodeIterator iterator, String nodeType){
        return new FilteringNodeIterator(iterator, new info.magnolia.jcr.predicate.NodeTypePredicate(nodeType));
    }

    public static NodeIterator filterDuplicates(NodeIterator iterator){
        return new FilteringNodeIterator(iterator, new info.magnolia.jcr.predicate.DuplicateNodePredicate());
    }

    public static NodeIterator filterParentNodeType(NodeIterator iterator, final String nodeType) throws RepositoryException{
        return new FilteringNodeIterator(iterator, new info.magnolia.jcr.predicate.NodeTypeParentPredicate(nodeType)) {
            @Override
            public Node nextNode(){
                Node node = super.nextNode();
                try {
                    while(node.getDepth() != 0 && !node.isNodeType(nodeType)){
                        if(node.getDepth() != 0){
                            node = node.getParent();
                        }
                    }
                } catch (RepositoryException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                return node;
            }
        };
    }

    public static Collection<Node> getCollectionFromNodeIterator(NodeIterator iterator){
        Collection<Node> nodeCollection = new HashSet<Node>(150);
        while(iterator.hasNext()){
            nodeCollection.add(iterator.nextNode());
        }
        return nodeCollection;
    }

    //for n2b
    public static Collection<Node> getSortedCollectionFromNodeIterator(NodeIterator iterator){
        Collection<Node> nodeCollection = new LinkedList<Node>();
        while(iterator.hasNext()){
            nodeCollection.add(iterator.nextNode());
        }
        return nodeCollection;
    }


    /**
     * Write the properties of the node to the log.
     */
    public static void traceNodeProperties(Node nodeOp) throws RepositoryException {
        // debug by logging properties.
        PropertyIterator propIter;
        propIter = nodeOp.getProperties();
        log.info("Trace Node Properties:");
        while (propIter.hasNext()) {
            Property prop = propIter.nextProperty();
            if (prop.isMultiple()){
                Value[] values = prop.getValues();
                for (int i=0; i<values.length; i++){
                    log.info(prop.toString() + "[" + i + "] = " + upToNCharacters(values[i].getString(), 30));
                }
            }else{
                log.info(prop.toString() + " = " + upToNCharacters(prop.getString(), 30));
            }

        }
    }

    /**
     * Write the children of the node to the log.
     */
    public static void traceNodeChildren(Node nodeOp) throws RepositoryException {
        // debug by logging properties.
        NodeIterator nodeIter;
        nodeIter = nodeOp.getNodes();
        log.info("Trace Node Children:");
        while (nodeIter.hasNext()) {
            Node n = nodeIter.nextNode();
            log.info(n.toString());
        }
    }

    private static String upToNCharacters(String s, int n) {
        return s.substring(0, Math.min(s.length(), n));
    }


}
