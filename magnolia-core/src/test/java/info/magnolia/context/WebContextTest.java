/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 * 
 * Any modifications to this file must keep this entire header
 * intact.
 * 
 */
package info.magnolia.context;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import info.magnolia.cms.security.User;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

/**
 *
 * @author ashapochka
 * @version $Revision: $ ($Author: $)
 */
public class WebContextTest extends TestCase {
    private static final String SESSION_USER = WebContextImpl.class.getName() + ".user";

    public void testLoginLogout() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpSession session = createMock(HttpSession.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        ServletContext servletContext = createMock(ServletContext.class);
        User user = createMock(User.class);
        //User anonymousUser = Authenticator.getAnonymousUser();
        //assertNotNull("anonymous user must not be null", anonymousUser);
        expect(user.getLanguage()).andReturn("en");
        expect(request.getSession(false)).andReturn(session).anyTimes();
        session.setAttribute(SESSION_USER, user);
        expect(session.getAttribute(SESSION_USER)).andReturn(user);		
        //session.invalidate();
        //session.setAttribute(SESSION_USER, anonymousUser);
        //expect(session.getAttribute(SESSION_USER)).andReturn(anonymousUser);
        replay(request, response, servletContext, user, session);
        WebContextImpl context = new WebContextImpl();
        context.init(request, response, servletContext);
        context.login(user);
        assertEquals(Locale.ENGLISH, context.getLocale());
        assertEquals(user, context.getUser());
        //context.logout();
        //assertEquals("logout must set user to anonymous", anonymousUser, context.getUser());
        verify(request, response, servletContext, user, session);
    }
}
