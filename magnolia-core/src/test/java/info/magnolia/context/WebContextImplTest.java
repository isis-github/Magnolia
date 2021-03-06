/**
 * This file Copyright (c) 2007-2012 Magnolia International
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

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.repository.DefaultRepositoryManager;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class WebContextImplTest
/* TODO - implementing Serializable for the purpose of this test - see MAGNOLIA-3523 */
implements Serializable {

    private static final String SESSION_SUBJECT = Subject.class.getName();

    @Before
    public void setUp() {
        ComponentsTestUtil.setInstance(RepositoryManager.class, new DefaultRepositoryManager());
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }


    @Test
    public void testLoginLogout() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpSession session = createMock(HttpSession.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        ServletContext servletContext = createMock(ServletContext.class);
        User user = createMock(User.class);
        SecuritySupport securitySupport = createMock(SecuritySupport.class);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);
        UserManager userManager = createMock(UserManager.class);
        RoleManager roleManager = createMock(RoleManager.class);
        Subject subject = MockUtil.createSubject(user);
        User anonymousUser = createMock(User.class);

        // login
        expect(user.getLanguage()).andReturn("en");
        expect(request.getSession(false)).andReturn(session).anyTimes();
        expect(user.getName()).andReturn("toto");
        session.setAttribute(SESSION_SUBJECT, subject);

        // logout
        session.removeAttribute(SESSION_SUBJECT);
        session.invalidate();

        // getUser() after logout
        expect(session.getAttribute(SESSION_SUBJECT)).andReturn(null);

        expect(securitySupport.getUserManager(Realm.REALM_SYSTEM.getName())).andReturn(userManager);
        expect(userManager.getAnonymousUser()).andReturn(anonymousUser);
        expect(securitySupport.getRoleManager()).andReturn(roleManager);
        expect(anonymousUser.getAllRoles()).andReturn(new ArrayList<String>());
        expect(anonymousUser.getLanguage()).andReturn("en");
        expect(anonymousUser.getName()).andReturn("anonymous");

        replay(request, response, servletContext, user, session, securitySupport, userManager, anonymousUser);

        WebContextImpl context = (WebContextImpl) newWebContextImpl(request, response, servletContext);
        context.login(subject);
        assertEquals(Locale.ENGLISH, context.getLocale());
        assertSame(user, context.getUser());
        assertSame(subject, context.getSubject());
        context.logout();
        assertSame(anonymousUser, context.getUser());
        verify(request, response, servletContext, user, session, securitySupport, userManager, anonymousUser);
    }

    // TODO commented out until MAGNOLIA-3523 is fixed
    //    public void testSerializable() throws Exception {
    //        WebContext context = newWebContextImpl(null, null, null);
    //        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //        ObjectOutputStream oos = new ObjectOutputStream(baos);
    //        try {
    //            oos.writeObject(context);
    //        } catch (NotSerializableException e) {
    //            fail("WebContextImpl should be serializable, failed with: " + e);
    //        }
    //    }

    private WebContext newWebContextImpl(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        // the factory calls the init() method.
        return new WebContextFactoryImpl().createWebContext(request,response, servletContext);
    }
}
