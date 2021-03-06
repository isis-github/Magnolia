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
package info.magnolia.link;

import static org.mockito.Mockito.*;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.text.MessageFormat;

import javax.jcr.Node;

import org.junit.Before;

/**
 * @version $Id$
 */
public abstract class BaseLinkTest extends MgnlTestCase {

    protected static final String SOME_CONTEXT = "/some-context";
    protected static final String HANDLE_PARENT_SUB = "/parent/sub";
    protected static final String UUID_PATTERN_OLD_FORMAT = "$'{'link:'{'uuid:'{'{0}'}',repository:'{'{1}'}',workspace:'{'default'}',path:'{'{2}'}}}'";
    protected static final String UUID_PATTERN_NEW_FORMAT = "$'{'link:'{'uuid:'{'{0}'}',repository:'{'{1}'}',path:'{'{2}'}',nodeData:'{'{3}'}',extension:'{'{4}'}}}'";
    protected static final String UUID_PATTERN_SIMPLE = MessageFormat.format(UUID_PATTERN_NEW_FORMAT, new String[]{"2", RepositoryConstants.WEBSITE, HANDLE_PARENT_SUB, "", "html"});
    protected static final String UUID_PATTERN_SIMPLE_OLD_FORMAT = MessageFormat.format(UUID_PATTERN_OLD_FORMAT, new String[]{"2", RepositoryConstants.WEBSITE, HANDLE_PARENT_SUB});

    protected static final String HREF_ABSOLUTE_LINK = HANDLE_PARENT_SUB + ".html";

    protected WebContext webContext;

    protected MockSession session;

    protected String website =
        "/parent.uuid=1\n" +
        "/parent/sub.uuid=2\n" +
        "/parent/sub2.uuid=3";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = SessionTestUtil.createSession(RepositoryConstants.WEBSITE, website);
        webContext = mock(WebContext.class);
        when(webContext.getContextPath()).thenReturn(SOME_CONTEXT);
        when(webContext.getJCRSession("website")).thenReturn(session);

        MockNode page = (MockNode) session.getNode(HANDLE_PARENT_SUB);

        Node bmnd = page.addNode("file");
        bmnd.setProperty(FileProperties.PROPERTY_FILENAME, "test");
        bmnd.setProperty(FileProperties.PROPERTY_EXTENSION, "jpg");
        bmnd.setProperty("jcr:mimeType", "image/jpeg");

        MgnlContext.setInstance(webContext);

        // not configured in the repository
        ComponentsTestUtil.setImplementation(URI2RepositoryManager.class, URI2RepositoryManager.class);

        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());

        ComponentsTestUtil.setInstance(LinkTransformerManager.class, new LinkTransformerManager());

        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDefaultBaseUrl("http://myTests:1234/yay");
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);
    }

}