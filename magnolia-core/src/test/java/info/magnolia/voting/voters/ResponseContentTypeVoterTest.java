/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.voting.voters;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @version $Id$
 */
public class ResponseContentTypeVoterTest {
    private ResponseContentTypeVoter voter;
    private HttpServletResponse response;
    private WebContext ctx;

    @Before
    public void setUp() throws Exception {
        voter = new ResponseContentTypeVoter();
        ctx = createStrictMock(WebContext.class);
        response = createStrictMock(HttpServletResponse.class);
        expect(ctx.getResponse()).andReturn(response);
        MgnlContext.setInstance(ctx);

        // shunt log4j
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testVotesTrueIfNoAllowedNorRejectedIsConfigured() {
        expect(response.getContentType()).andReturn("whatever");
        doTest(true);
    }

    @Test
    public void testVotesTrueIfContentTypeIsAllowed() {
        voter.addAllowed("text/html");
        voter.addAllowed("application/x-javascript");
        voter.addAllowed("text/plain");
        expect(response.getContentType()).andReturn("application/x-javascript");
        doTest(true);
    }

    @Test
    public void testVotesFalseIfContentTypeIsNotInAllowedList() {
        voter.addAllowed("text/html");
        voter.addAllowed("application/x-javascript");
        voter.addAllowed("text/plain");
        expect(response.getContentType()).andReturn("whatever");
        doTest(false);
    }

    @Test
    public void testVotesFalseIfContentTypeIsExplicitelyRejected() {
        voter.addRejected("image/gif");
        voter.addRejected("image/jpeg");
        voter.addRejected("application/octet-stream");
        expect(response.getContentType()).andReturn("image/gif");
        doTest(false);
    }

    @Test
    public void testVotesTrueIfContentTypeIsNotRejected() {
        voter.addRejected("image/gif");
        voter.addRejected("image/jpeg");
        voter.addRejected("application/octet-stream");
        expect(response.getContentType()).andReturn("text/plain");
        doTest(true);
    }

    @Test
    public void testVotesTrueIfContentTypeIsAllowedAndNotRejected() {
        voter.addAllowed("text/html");
        voter.addAllowed("application/x-javascript");
        voter.addAllowed("text/plain");
        voter.addRejected("image/gif");
        voter.addRejected("image/jpeg");
        voter.addRejected("application/octet-stream");
        expect(response.getContentType()).andReturn("text/plain");
        doTest(true);
    }

    @Test
    public void testVotesFalseIfContentTypeIsNotExplicitelyAllowedAndExplicitelyRejected() {
        voter.addAllowed("text/html");
        voter.addAllowed("application/x-javascript");
        voter.addAllowed("text/plain");
        voter.addRejected("image/gif");
        voter.addRejected("image/jpeg");
        voter.addRejected("application/octet-stream");
        expect(response.getContentType()).andReturn("image/gif");
        doTest(false);
    }

    @Test
    public void testVotesFalseIfContentTypeIsNotExplicitelyAllowedAndNotExplicitelyRejectedEither() {
        voter.addAllowed("text/html");
        voter.addAllowed("application/x-javascript");
        voter.addAllowed("text/plain");
        voter.addRejected("image/gif");
        voter.addRejected("image/jpeg");
        voter.addRejected("application/octet-stream");
        expect(response.getContentType()).andReturn("whatever");
        doTest(false);
    }

    @Test
    public void testVotesFalseIfResponseDoesNotHaveAContentTypeSetYet() {
        voter.addAllowed("text/html");
        voter.addAllowed("application/x-javascript");
        voter.addAllowed("text/plain");
        voter.addRejected("image/gif");
        voter.addRejected("image/jpeg");
        voter.addRejected("application/octet-stream");
        expect(response.getContentType()).andReturn(null);
        doTest(false);
    }

    @Test
    public void testVotesFalseIfResponseDoesNotHaveAContentTypeSetYetEvenIfNoRejectedAreConfigured() {
        voter.addAllowed("text/html");
        voter.addAllowed("application/x-javascript");
        voter.addAllowed("text/plain");
        expect(response.getContentType()).andReturn(null);
        doTest(false);
    }

    @Test
    public void testVotesFalseIfResponseDoesNotHaveAContentTypeSetYetEvenIfNoAllowedAreConfigured() {
        voter.addRejected("text/html");
        voter.addRejected("application/x-javascript");
        voter.addRejected("text/plain");
        expect(response.getContentType()).andReturn(null);
        doTest(false);
    }

    @Test
    public void testIgnoresCharsetInContentType() {
        voter.addAllowed("text/html");
        voter.addAllowed("application/x-javascript");
        voter.addAllowed("text/plain");
        voter.addRejected("image/gif");
        voter.addRejected("image/jpeg");
        voter.addRejected("application/octet-stream");
        expect(response.getContentType()).andReturn("text/html;charset=UTF-8");
        doTest(true);
    }

    @Test
    public void testIgnoresCharsetInContentType2() {
        voter.addAllowed("text/html");
        voter.addAllowed("application/x-javascript");
        voter.addAllowed("text/plain");
        expect(response.getContentType()).andReturn("image/jpeg;charset=UTF-8");
        doTest(false);
    }

    private void doTest(boolean expectedBoolResult) {
        replay(response, ctx);
        assertEquals(expectedBoolResult, voter.boolVote(null));
        verify(response, ctx);
    }

}
