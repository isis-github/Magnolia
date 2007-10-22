/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddMainMenuItemTask extends AbstractTask {
    private final String menuName;
    private final String label;
    private final String i18nBasename;
    private final String onClick;
    private final String icon;
    private final String orderBefore;

    /**
     * @param orderBefore the menu name before which this new menu should be positioned. ignored if null.
     * @param i18nBasename ignored if null.
     */
    public AddMainMenuItemTask(String menuName, String label, String i18nBasename, String onClick, String icon, String orderBefore) {
        super("Menu", "Adds or updates an item in the admin interface menu for " + menuName);
        this.menuName = menuName;
        this.label = label;
        this.i18nBasename = i18nBasename;
        this.onClick = onClick;
        this.icon = icon;
        this.orderBefore = orderBefore;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        try {
            final Content parent = getParentNode(ctx);
            final Content menu = ContentUtil.getOrCreateContent(parent, menuName, ItemType.CONTENTNODE);
            NodeDataUtil.getOrCreateAndSet(menu, "icon", icon);
            NodeDataUtil.getOrCreateAndSet(menu, "onclick", onClick);
            NodeDataUtil.getOrCreateAndSet(menu, "label", label);
            if (i18nBasename != null) {
                NodeDataUtil.getOrCreateAndSet(menu, "i18nBasename", i18nBasename);
            }

            if (orderBefore != null){
                parent.orderBefore(menuName, orderBefore);
            }
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Could not create or place " + menuName + " menu item.", e);
        }
    }

    protected Content getParentNode(InstallContext ctx) throws RepositoryException {
        return ctx.getConfigHierarchyManager().getContent("/modules/adminInterface/config/menu");
    }
}
