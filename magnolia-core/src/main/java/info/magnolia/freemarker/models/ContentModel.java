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
package info.magnolia.freemarker.models;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Transformer;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNodeModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * A wrapper for Content nodes. Exposes properties using an HashModel (.property, ?size, ...)
 * a hierarchy (TemplateNodeModel: ?children, ?parent, ...)
 * and as a scalar (returns the node name)
 *
 * @author Chris Miner
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ContentModel implements TemplateHashModelEx, TemplateNodeModel, TemplateScalarModel, AdapterTemplateModel {
    static final MagnoliaModelFactory FACTORY = new MagnoliaModelFactory() {
        @Override
        public Class factoryFor() {
            return Content.class;
        }

        @Override
        public AdapterTemplateModel create(Object object, ObjectWrapper wrapper) {
            return new ContentModel((Content) object, (MagnoliaObjectWrapper) wrapper);
        }
    };

    private final Content content;
    private final MagnoliaObjectWrapper wrapper;

    ContentModel(Content content, MagnoliaObjectWrapper wrapper) {
        this.content = content;
        this.wrapper = wrapper;
    }

    @Override
    public String getAsString() {
        return content.getName();
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        final Object result;

        try {
            if (key.equals("@handle")) {
                result = content.getHandle();
            } else if (key.equals("@uuid")) {
                result = content.getUUID();
            } else if (key.equals("@name")) {
                result = content.getName();
            } else if (key.equals("@level") || key.equals("@depth")) {
                result = content.getLevel();
            } else if (key.equalsIgnoreCase("metaData")) {
                result = content.getMetaData();
            } else {
                // try for node data or child node
                if (content.hasNodeData(key)) {
                    result = content.getNodeData(key);
                } else {
                    if (content.hasContent(key)) {
                        result = content.getContent(key);
                    } else {
                        result = null;
                    }
                }
            }
        } catch (RepositoryException e) {
            throw new TemplateModelException(e);
        }
        return wrapper.wrap(result);
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return (size() == 0);
    }

    @Override
    public int size() throws TemplateModelException {
        return content.getNodeDataCollection().size();
    }

    @Override
    public TemplateCollectionModel keys() throws TemplateModelException {
        final Iterator it = IteratorUtils.transformedIterator(content.getNodeDataCollection().iterator(), new Transformer() {
            @Override
            public Object transform(Object input) {
                return ((NodeData) input).getName();
            }
        });
        return new SimpleCollection(it);
    }

    @Override
    public TemplateCollectionModel values() throws TemplateModelException {
        return (TemplateCollectionModel) wrapper.wrap(content.getNodeDataCollection().iterator());
    }

    @Override
    public TemplateNodeModel getParentNode() throws TemplateModelException {
        try {
            // todo : check if this is the root?
            // content.getLevel() == 0;
            final Content parent = content.getParent();
            return (TemplateNodeModel) wrapper.wrap(parent);
        } catch (RepositoryException e) {
            throw new TemplateModelException("Can't get parent of " + content + ":" + e.getMessage(), e);
        }
    }

    /**
     * This returns all children, except nodes or jcr: types and mgnl:metaData.
     */
    @Override
    public TemplateSequenceModel getChildNodes() throws TemplateModelException {
        final Collection<Content> children = ContentUtil.getAllChildren(content);
        return (TemplateSequenceModel) wrapper.wrap(children);
    }

    @Override
    public String getNodeName() throws TemplateModelException {
        return content.getName();
    }

    @Override
    public String getNodeType() throws TemplateModelException {
        try {
            return content.getNodeTypeName();
        } catch (RepositoryException e) {
            throw new TemplateModelException("Can't get node type of " + content + ":" + e.getMessage(), e);
        }
    }

    @Override
    public String getNodeNamespace() throws TemplateModelException {
        return null; // non XML implementation
    }

    public Content asContent() {
        return this.content;
    }

    @Override
    public Object getAdaptedObject(Class hint) {
        return asContent();
    }
}
