/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.templating.paragraphs;

import freemarker.template.TemplateException;
import info.magnolia.cms.beans.config.ActionBasedParagraph;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.freemarker.FreemarkerContentRenderer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerParagraphRenderer implements ParagraphRenderer {
    private final FreemarkerContentRenderer fmRenderer;

    public FreemarkerParagraphRenderer(FreemarkerContentRenderer fmRenderer) {
        this.fmRenderer = fmRenderer;
    }

    public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
        final String templatePath = paragraph.getTemplatePath();

        if (templatePath == null) {
            throw new IllegalStateException("Unable to render paragraph " + paragraph.getName() + " in page " + content.getHandle() + ": templatePath not set.");
        }

        final ActionResult actionResult;
        if (paragraph instanceof ActionBasedParagraph) {
            ActionBasedParagraph abp = (ActionBasedParagraph) paragraph;
            final Class actionClass = abp.getActionClass();
            if (actionClass == null) {
                throw new IllegalStateException("Can't render paragraph " + paragraph.getName() + " in page " + content.getHandle() + ": actionClass not set.");
            }
            actionResult = execute(actionClass);
        } else {
            actionResult = null;
        }

        final String template = determineTemplatePath(templatePath, actionResult);

        final Map freemarkerCtx = new HashMap();
        freemarkerCtx.put("content", content);
        if (actionResult!=null){
            freemarkerCtx.put("result", actionResult.getResult());
            freemarkerCtx.put("action", actionResult.getActionBean());
        }
        try {
            fmRenderer.render(template, freemarkerCtx, out);
        } catch (TemplateException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    protected ActionResult execute(Class actionClass) {
        // see MVCServletHandlerImpl.init() if we need to populate the action bean
        try {
            final Object actionBean = actionClass.newInstance();
            final Method method = actionClass.getMethod("execute", null);
            final Object result = method.invoke(actionBean, null);
            return new ActionResult(result, actionBean);
        } catch (InstantiationException e) {
            throw new RuntimeException(e); // TODO
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e); // TODO
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    /**
     * Override this method if you need specific templates depending on the action result.
     */
    protected String determineTemplatePath(String originalTemplateName, ActionResult actionResult) {
        return originalTemplateName;
    }

    protected static final class ActionResult {
        private final Object result;
        private final Object actionBean;

        public ActionResult(Object result, Object actionBean) {
            this.result = result;
            this.actionBean = actionBean;
        }

        public Object getResult() {
            return result;
        }

        public Object getActionBean() {
            return actionBean;
        }
    }

}
