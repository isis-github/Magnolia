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
package info.magnolia.setup.initial;

import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;


/**
 * Updates virtualURIMappings to the format suitable for c2b.
 *
 * @version $Id$
 */
public class UpdateURIMappings extends AllModulesNodeOperation {

    public UpdateURIMappings() {
        super("Update virtualURIMapping nodes", "Adds the class property to each virtualURIMapping node as these are dynamic now.");
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.delta.AllModulesNodeOperation#operateOnModuleNode(info.magnolia.cms.core.Content,
     * info.magnolia.cms.core.HierarchyManager, info.magnolia.module.InstallContext)
     */
    @Override
    protected void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx)
        throws RepositoryException, TaskExecutionException {

        try {
            if (node.hasContent("virtualURIMapping")) {
                ContentUtil.visit(node.getContent("virtualURIMapping"), new ContentUtil.Visitor() {
                    @Override
                    public void visit(Content node) throws Exception {
                        if (node.hasNodeData("fromURI") && node.hasNodeData("toURI")) {
                            NodeData classNodeData = NodeDataUtil.getOrCreate(node, "class");
                            classNodeData.setValue(DefaultVirtualURIMapping.class.getName());
                        }
                    }
                });
            }
        }
        catch (RepositoryException e) {
            throw e;
        }
        catch (Exception e) {
            throw new TaskExecutionException("can't reconfigure virtualURIMapping", e);
        }
    }

}
