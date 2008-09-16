/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.config.ParagraphManager;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Resource;
import info.magnolia.test.MgnlTagTestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EditBarTest extends MgnlTagTestCase {
    private Content currentPage;

    protected HierarchyManager initWebsiteData() throws IOException, RepositoryException {
        return null;
    }

    protected void setUp() throws Exception {
        super.setUp();

        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setAdmin(true);
        FactoryUtil.setInstance(ServerConfiguration.class, serverConfiguration);

        currentPage = createMock(Content.class);

        final AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(this.currentPage);

        webContext.setAggregationState(aggregationState);
        webContext.setParameters(Collections.singletonMap(Resource.MGNL_PREVIEW_ATTRIBUTE, "false"));
    }

    public void testDisplaysParagraphNameAsLabel() throws Exception {
        final String paraTitle = "testParaTitleKey";
        final Paragraph paraInfo = new Paragraph();
        paraInfo.setI18nBasename("test.messages");
        paraInfo.setTitle(paraTitle);

        ParagraphManager.getInstance().getParagraphs().put("test-para", paraInfo);
        expect(currentPage.isGranted(Permission.SET)).andReturn(true).anyTimes();
        expect(currentPage.getHandle()).andReturn("/foo/bar").anyTimes();
        replay(currentPage);

        final EditBar tag = new EditBar();
        tag.setShowParagraphName(true);

        tag.setParagraph("test-para");

        tag.setPageContext(pageContext);
        tag.doEndTag();
        // assertJspContent("Should display paragraph name", "--");
        final String output = getJspOutput();
        assertTrue("Output should contain the paragraph's title", output.contains(paraTitle));

        assertMatchesDotAll("Output should contain the paragraph's title in a <td>",
                output, ".*<td class=\"smothParagraphTitle\"( style=\"[a-z;: -]+)?\">" + paraTitle + "</td>.*");

        verify(currentPage);
    }

    // TODO : failing :
//    public void testShouldDisplayOnPublicIfAdminOnlySetToFalse() {
//          serverCfg.setAdmin(false)
//        final EditBar tag = new EditBar();
//        tag.setAdminOnly(false);
//        assert(test will fail because BarEdit does its own check for admin)
//    }

}
