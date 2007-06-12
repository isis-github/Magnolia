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
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.SaveHandler;
import info.magnolia.api.HierarchyManager;

import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class UserEditDialog extends ConfiguredDialog {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(UserEditDialog.class);

    protected static final String NODE_ACLUSERS = "acl_users"; //$NON-NLS-1$

    protected static final String NODE_ACLROLES = "acl_userroles"; //$NON-NLS-1$

    protected static final String NODE_ACLCONFIG = "acl_config"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#getRepository()
     */
    public String getRepository() {
        String repository = super.getRepository();
        if (repository == null) {
            repository = ContentRepository.USERS;
        }
        return repository;
    }

    /**
     * @param name
     * @param request
     * @param response
     * @param configNode
     */
    public UserEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
    }

    /**
     * @see info.magnolia.module.admininterface.DialogMVCHandler#configureSaveHandler(info.magnolia.module.admininterface.SaveHandler)
     */
    protected void configureSaveHandler(SaveHandler save) {
        super.configureSaveHandler(save);
        save.setPath(path);
    }

    /**
     * Is called during showDialog(). Here can you create/ add controls for the dialog.
     * @param configNode
     * @param storageNode
     * @throws javax.jcr.RepositoryException
     */
    protected Dialog createDialog(Content configNode, Content storageNode) throws RepositoryException {
        Dialog dialog = super.createDialog(configNode, storageNode);
        // dont do anythig if command is "save"
        if (this.getCommand().equalsIgnoreCase(COMMAND_SAVE)) {
            return dialog;
        }

        // replace UUID with Path for groups and roles
        DialogControlImpl control = dialog.getSub("groups");
        HierarchyManager groupsHM = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USER_GROUPS);
        List values = control.getValues();
        for (int index = 0; index < values.size(); index++) {
            // replace uuid with path
            String uuid = (String) values.get(index);
            if (StringUtils.isEmpty(uuid)) {
                continue;
            }
            try {
                values.set(index, groupsHM.getContentByUUID(uuid).getHandle());
            }
            catch (ItemNotFoundException e) {
                // remove invalid ID
                values.remove(index);
            }
        }

        control = dialog.getSub("roles");
        HierarchyManager rolesHM = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USER_ROLES);
        values = control.getValues();
        for (int index = 0; index < values.size(); index++) {
            // replace uuid with path
            String uuid = (String) values.get(index);
            if (StringUtils.isEmpty(uuid)) {
                continue;
            }
            try {
                values.set(index, rolesHM.getContentByUUID(uuid).getHandle());
            }
            catch (ItemNotFoundException e) {
                // remove invalid ID
                values.remove(index);
            }
        }
        return dialog;
    }

    /**
     * Write ACL entries under the given user node
     * @param node under which ACL for all workspaces needs to be created
     */
    protected void writeACL(Content node) throws RepositoryException {
        // remove existing
        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repository = (String) repositoryNames.next();
            try {
                node.delete("acl_" + repository); //$NON-NLS-1$
            }
            catch (RepositoryException re) {
                // new user
            }
        }

        // rewrite
        Content aclUsers;

        aclUsers = node.createContent(NODE_ACLUSERS, ItemType.CONTENTNODE);

        node.createContent(NODE_ACLROLES, ItemType.CONTENTNODE);
        node.createContent(NODE_ACLCONFIG, ItemType.CONTENTNODE);

        // give user permission to read and edit himself
        Content u3 = aclUsers.createContent("0", ItemType.CONTENTNODE); //$NON-NLS-1$
        u3.createNodeData("path").setValue(node.getHandle() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
        u3.createNodeData("permissions").setValue(Permission.ALL); //$NON-NLS-1$
    }

    protected boolean onPostSave(SaveHandler saveControl) {

        Content node = this.getStorageNode();

        HierarchyManager groupsHM = MgnlContext.getSystemContext().getHierarchyManager(
            ContentRepository.USER_GROUPS);
        HierarchyManager rolesHM = MgnlContext.getSystemContext().getHierarchyManager(
            ContentRepository.USER_ROLES);

        try {
            this.writeRolesOrGroups(groupsHM, node, "groups");
            this.writeRolesOrGroups(rolesHM, node, "roles");
            this.writeACL(node);
            node.save();
            return true;
        } catch (RepositoryException re) {
            log.error("Failed to update user, reverting all transient modifications made for this node", re);
            try {
                node.refresh(false);
            } catch (RepositoryException e) {
                log.error("Failed to revert transient modifications", re);
            }
        }
        return false;
    }

    private void writeRolesOrGroups(HierarchyManager hm, Content parentNode, String nodeName)
            throws RepositoryException {
        try {
            Content groupOrRoleNode = parentNode.getContent(nodeName);
            // remove existing roles, leave the node as is
            Iterator existingNodes = groupOrRoleNode.getNodeDataCollection().iterator();
            while (existingNodes.hasNext()) {
                ((NodeData) existingNodes.next()).delete();
            }
            List values = getDialog().getSub(nodeName).getValues();
            for (int index = 0; index < values.size(); index++) {
                String path = (String) values.get(index);
                if (StringUtils.isNotEmpty(path)) {
                    groupOrRoleNode.createNodeData(Integer.toString(index)).setValue(hm.getContent(path).getUUID());
                }
            }
        }
        catch (PathNotFoundException e) {
            // this might happen if all groups are deleted via dialog
        }
    }


}