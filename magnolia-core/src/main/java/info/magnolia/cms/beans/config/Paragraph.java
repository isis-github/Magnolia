/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;

import java.util.Hashtable;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 */
public final class Paragraph {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Paragraph.class);

    private static final String DIALOGS_DIR = "/dialogs/"; //$NON-NLS-1$

    private static Map cachedContent = new Hashtable();

    private String name;

    private String title;

    private String templatePath;

    private String dialogPath;

    private String templateType;

    private String description;

    // private Content dialogContent;

    /**
     * constructor
     */
    private Paragraph() {
    }

    /**
     * Returns the cached content of the requested template. TemplateInfo properties :
     * <ol>
     * <li>title - title describing template</li>
     * <li>type - jsp / servlet</li>
     * <li>path - jsp / servlet path</li>
     * <li>description - description of a template</li>
     * </ol>
     * @return TemplateInfo
     */
    public static Paragraph getInfo(String key) {
        return (Paragraph) Paragraph.cachedContent.get(key);
    }

    /**
     * Adds paragraph definition to ParagraphInfo cache.
     */
    public static Paragraph addParagraphToCache(Content c, String startPage) {

        Paragraph pi = new Paragraph();
        pi.name = c.getNodeData("name").getString(); //$NON-NLS-1$
        pi.templatePath = c.getNodeData("templatePath").getString(); //$NON-NLS-1$
        pi.dialogPath = c.getNodeData("dialogPath").getString(); //$NON-NLS-1$
        pi.templateType = c.getNodeData("type").getString(); //$NON-NLS-1$
        pi.title = c.getNodeData("title").getString(); //$NON-NLS-1$
        pi.description = c.getNodeData("description").getString(); //$NON-NLS-1$
        if (log.isDebugEnabled()) {
            log.debug("Registering paragraph [" + pi.name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        cachedContent.put(pi.name, pi);
        return pi;
    }

    /**
     * @return String, name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return String, title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return String, templatePath
     */
    public String getTemplatePath() {
        return this.templatePath;
    }

    /**
     * @return String, dialogPath
     */
    public String getDialogPath() {
        return this.dialogPath;
    }

    /**
     * @return String, template type (jsp / servlet)
     */
    public String getTemplateType() {
        return this.templateType;
    }

    /**
     * @return String, description
     */
    public String getDescription() {
        return this.description;
    }

    // /**
    // * @return Content, Content holding information for the paragraph dialog
    // */
    // public Content getDialogContent() {
    // return this.dialogContent;
    // }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this)
        //
            .append("name", this.name) //$NON-NLS-1$
            .append("templateType", this.templateType) //$NON-NLS-1$
            .append("description", this.description) //$NON-NLS-1$
            .append("dialogPath", this.dialogPath) //$NON-NLS-1$
            .append("title", this.title) //$NON-NLS-1$
            .append("templatePath", this.templatePath) //$NON-NLS-1$
            .toString();
    }
}