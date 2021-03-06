/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.logging;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.login.LoginResult;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.model.Pair;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Revision: $ ($Author: $)
 */
public class AuditLoggingUtilTest {

    private RecordingAuditLoggingManager audit;

    @Before
    public void setUp() throws Exception {
        audit = new RecordingAuditLoggingManager();
        ComponentsTestUtil.setInstance(AuditLoggingManager.class, audit);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        SystemProperty.getProperties().clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testLogsLoginSuccesses() {
        final User user = createStrictMock(User.class);
        final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        final LoginResult loginResult = new LoginResult(LoginResult.STATUS_SUCCEEDED, MockUtil.createSubject(user));
        expect(request.getParameter("mgnlUserId")).andReturn("greg");
        expect(request.getRemoteAddr()).andReturn("127.0.0.1");

        replay(user, request);

        AuditLoggingUtil.log(loginResult, request);

        assertEquals(1, audit.records.size());
        assertEquals("login", audit.records.get(0).getLeft());
        assertEquals(3, audit.records.get(0).getRight().length);
        assertEquals("greg", audit.records.get(0).getRight()[0]);
        assertEquals("127.0.0.1", audit.records.get(0).getRight()[1]);
        assertEquals("Success", audit.records.get(0).getRight()[2]);
        verify(user,request);
    }

    @Test
    public void testLogsLoginFailures() {
        final User user = createStrictMock(User.class);
        final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        final LoginResult loginResult = new LoginResult(LoginResult.STATUS_FAILED, new LoginException("Fail!"));
        expect(request.getParameter("mgnlUserId")).andReturn("greg");
        expect(request.getRemoteAddr()).andReturn("127.0.0.1");

        replay(user, request);

        AuditLoggingUtil.log(loginResult, request);

        assertEquals(1, audit.records.size());
        assertEquals("login", audit.records.get(0).getLeft());
        assertEquals(3, audit.records.get(0).getRight().length);
        assertEquals("greg", audit.records.get(0).getRight()[0]);
        assertEquals("127.0.0.1", audit.records.get(0).getRight()[1]);
        assertEquals("Failure Fail!", audit.records.get(0).getRight()[2]);
        verify(user,request);
    }

    private static class RecordingAuditLoggingManager extends AuditLoggingManager {
        private final List<Pair<String,String[]>> records = new ArrayList<Pair<String, String[]>>();
        @Override
        public void log(String action, String[] data) {
            records.add(new Pair<String, String[]>(action, data));
        }
    }
}
