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
package info.magnolia.ui.admincentral.tree.view;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.ui.admincentral.tree.builder.TreeBuilder;
import info.magnolia.ui.admincentral.tree.container.ContainerItemId;
import info.magnolia.ui.model.UIModel;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import javax.jcr.RepositoryException;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

/**
 * Vaadin UI component that displays a tree.
 *
 * @author tmattsson
 */
// TODO don't extend CustomComponent, make it composite
public class TreeViewImpl extends CustomComponent implements TreeView, IsVaadinComponent {

    private JcrBrowser jcrBrowser;

    public TreeViewImpl(String treeName, final Presenter presenter, UIModel uiModel, TreeBuilder builder) throws RepositoryException {
        jcrBrowser = new JcrBrowser(treeName, uiModel, builder);
        setCompositionRoot(jcrBrowser);
        setSizeFull();
        jcrBrowser.addListener(new ItemClickEvent.ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {
                try {
                    presenter.onItemSelection(jcrBrowser.getContainer().getJcrItem((ContainerItemId) event.getItemId()));
                } catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
            }
        });
    }

    /**
     *
     * @param path relative to the tree root, must start with /
     */
    public void select(String path){
        jcrBrowser.select(path);
    }

    public void refresh() {
        jcrBrowser.getContainer().fireItemSetChange();
    }

    public Component asVaadinComponent() {
        return this;
    }

}