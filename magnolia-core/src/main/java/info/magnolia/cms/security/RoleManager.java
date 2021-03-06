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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.ACL;

import java.util.Map;

/**
 * Manages roles.
 */
public interface RoleManager {

    /**
     * Create a role without any security restrictions.
     * @throws UnsupportedOperationException in case the role manager does not support this operation
     */
    public Role createRole(String name) throws UnsupportedOperationException, Exception;

    /**
     * Get the specific role without any security restrictions.
     * @throws UnsupportedOperationException in case the role manager does not support this operation
     */
    public Role getRole(String name) throws UnsupportedOperationException;

    /**
     * Obtain list of ACLs defined for specified role.
     * @throws UnsupportedOperationException in case the role manager does not support this operation
     */
    public Map<String, ACL> getACLs(String role) throws UnsupportedOperationException;

    /**
     * Add permission to the specified role, assuming current user has enough rights to perform such operation.
     */
    public void addPermission(Role role, String workspaceName, String path, long permission);


    /**
     * Remove permission from the specified role.
     */
    public void removePermission(Role role, String workspace, String path, long permission);

    /**
     * Retrieve role name by its identifier.
     */
    public String getRoleNameById(String string);

}
