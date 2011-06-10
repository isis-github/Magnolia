/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.module.templating.renderers;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.TemplateRenderer;
import info.magnolia.test.ComponentsTestUtil;
import junit.framework.TestCase;

import java.io.StringWriter;

import static org.easymock.EasyMock.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ServletTemplateRendererTest extends TestCase {
    @Override
    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        super.tearDown();
    }

    public void testServletTemplateRendererOnlyWorksInWebContextAndIsPoliteEnoughToGiveAnExplicitExceptionMessageAboutIt() throws Exception {
        final Context ctx = createStrictMock(Context.class);
        replay(ctx);
        MgnlContext.setInstance(ctx);
        final TemplateRenderer renderer = new ServletTemplateRenderer();
        try {
            renderer.renderTemplate(null, null, new StringWriter());
            fail("should have failed");
        } catch (Throwable t) {
            assertTrue(t.getMessage().startsWith("ServletTemplateRenderer can only be used with a WebContext"));
        } finally {
            verify(ctx);
        }
    }
}