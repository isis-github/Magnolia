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
package info.magnolia.module.templating.paragraphs;

import info.magnolia.module.templating.RenderableDefinition;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.NodeMapWrapper;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.templating.RenderException;

import java.io.Writer;
import java.util.Map;

/**
 * A simple paragraph renderer which delegates to a jsp.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JspParagraphRenderer extends AbstractParagraphRenderer {

    @Override
    protected void onRender(Content content, RenderableDefinition definition, Writer out, Map ctx, String templatePath) throws RenderException {
        try {
            ((WebContext) ctx).include(templatePath, out);
        } catch (Exception e) {
            throw new RenderException("Can't render paragraph template " + templatePath, e);
        }

    }

    @Override
    protected Map newContext() {
        return MgnlContext.getWebContext("JspParagraphRenderer can only be used with a WebContext");
    }

    /**
     * We expose nodes as Map instances in JSPs.
     */
    @Override
    protected Content wrapNodeForTemplate(Content currentContent, Content mainContent) {
        final Content wrapped = super.wrapNodeForTemplate(currentContent, mainContent);
        return new NodeMapWrapper(wrapped, mainContent.getHandle());
    }

    @Override
    protected String getPageAttributeName() {
        return "actpage";
    }

}