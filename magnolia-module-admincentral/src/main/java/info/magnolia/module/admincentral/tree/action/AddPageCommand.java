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
package info.magnolia.module.admincentral.tree.action;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.jcr.HackContent;
import info.magnolia.module.admincentral.jcr.JCRMetadataUtil;
import info.magnolia.module.admincentral.jcr.JCRUtil;
import info.magnolia.module.admincentral.tree.JcrBrowser;
import info.magnolia.module.admincentral.tree.container.ContainerItemId;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;


/**
 * Tree action for adding a page to the website repository.
 */
public class AddPageCommand extends Command {

    private static final long serialVersionUID = 7745378363506148188L;

    @Override
    public boolean isAvailable(Item item) {
        return item instanceof Node;
    }

    @Override
    public void execute(JcrBrowser jcrBrowser, Item item) throws RepositoryException {

        if (item instanceof Node) {
            Node node = (Node) item;

            String name = JCRUtil.getUniqueLabel(node, "untitled");
            Node newChild = node.addNode(name, ItemType.CONTENT.getSystemName());

            MetaData metaData = JCRMetadataUtil.getMetaData(newChild);
            metaData.setAuthorId(MgnlContext.getUser().getName());
            metaData.setCreationDate();
            metaData.setModificationDate();
            Template newTemplate = TemplateManager.getInstance().getDefaultTemplate(new HackContent(newChild));
            if (newTemplate != null) {
                metaData.setTemplate(newTemplate.getName());
            }

            node.getSession().save();

            if (jcrBrowser != null)
            jcrBrowser.addItem(new ContainerItemId(newChild));
            if (jcrBrowser != null)
            jcrBrowser.setCollapsed(new ContainerItemId(item), false);
        }
    }
}