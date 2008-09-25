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
package info.magnolia.module.templating.paragraphs;

import freemarker.template.TemplateException;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Renders a paragraph using freemarker. Optionally supports the execution of an action
 * class whose constructor can either be empty or take exactly one Content parameter and
 * one ActionBasedParagraph parameter.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerParagraphRenderer extends ActionBasedParagraphRenderer implements ParagraphRenderer {

    private final FreemarkerHelper fmHelper;

    /**
     * Constructs a FreemarkerParagraphRenderer that uses the default (singleton)
     * instance of FreemarkerHelper.
     */
    public FreemarkerParagraphRenderer() {
        this(FreemarkerHelper.getInstance());
    }

    FreemarkerParagraphRenderer(FreemarkerHelper fmRenderer) {
        this.fmHelper = fmRenderer;
    }

    protected void render(final String templatePath, Content content, Paragraph paragraph, final ActionResult actionResult, Writer out) throws IOException {
        final Map freemarkerCtx = new HashMap();
        freemarkerCtx.put("content", content);
        freemarkerCtx.put("actpage", MgnlContext.getAggregationState().getMainContent());
        freemarkerCtx.put("paragraphConfig", paragraph);
        if (actionResult != null) {
            freemarkerCtx.put("result", actionResult.getResult());
            freemarkerCtx.put("action", actionResult.getActionBean());
        }

        final Locale locale = MgnlContext.getAggregationState().getLocale();

        try {
            fmHelper.render(templatePath, locale, paragraph.getI18nBasename(), freemarkerCtx, out);
        } catch (TemplateException e) {
            throw new RuntimeException(e); // TODO
        }
    }

}
