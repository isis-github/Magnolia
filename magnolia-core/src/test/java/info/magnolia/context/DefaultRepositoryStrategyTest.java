/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.context;


import javax.jcr.Session;

import org.junit.Test;

import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.RepositoryTestCase;
import static org.easymock.classextension.EasyMock.createMock;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class DefaultRepositoryStrategyTest extends RepositoryTestCase {

    @Test
    public void testRepositorySessions() throws Exception {
        UserContext context = createMock(UserContext.class);
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(repositoryManager, context);
        Session session = strategy.getSession("website");
        assertNotNull(session);
        strategy.release();
    }
/*
    @Test
    public void testHierarchyManagers() throws Exception {
        UserContext context = createMock(UserContext.class);
        User user = createMock(User.class);
        RepositoryManager registry = createMock(RepositoryManager.class);

        Set principalSet = new HashSet();
        PrincipalCollection principals = createMock(PrincipalCollection.class);
        principalSet.add(principals);
        ACL acl = createMock(ACL.class);
        Subject subject = new Subject(false, principalSet, new HashSet(), new HashSet());
        expect(context.getUser()).andReturn(user).anyTimes();
        expect(user.getName()).andReturn("admin").anyTimes();
        expect(principals.get("magnolia_website")).andReturn(acl).anyTimes();
        expect(acl.getList()).andReturn(new ArrayList()).anyTimes();
        expect(registry.getSession("website", any(Credentials.class))).andReturn(sessionProvider);
        replay(context, user, registry, principals, acl);

        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(registry, context);
        HierarchyManager hierarchyManager = HierarchyManagerUtil.asHierarchyManager(strategy.getSession("website"));
        assertNotNull(hierarchyManager);
        verify(context, user, registry, principals, acl);
    }
*/
}
