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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.core.Content;

import java.io.IOException;
import java.io.Writer;

/**
 * An interface to renderer paragraphs of content.
 * 
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ParagraphRenderer {

    /**
     * @param content the content to render (usually passed to the appropriate templating engine)
     * @param paragraph information about the rendering (template to use etc)
     * @param out where the renderering happens
     */
    void render(Content content, Paragraph paragraph, Writer out) throws IOException;
}
