/**
 * This file Copyright (c) 2010 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.views.AbstractTreeTableView;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Window;


/**
 * Opens the selected page for page editing.
 */
public class OpenPageAction extends TreeAction {

    private static final long serialVersionUID = 751955514356448616L;

    @Override
    protected void handleAction(AbstractTreeTableView treeTable, Node node) throws RepositoryException {
        String uri = MgnlContext.getContextPath() + node.getPath() + ".html";
        Window window = treeTable.getApplication().getMainWindow();
        window.open(new ExternalResource(uri));
    }
}