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
package info.magnolia.cms.filters;

import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This class initializes the current context.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class MgnlContextFilter extends AbstractMagnoliaFilter {

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{
        if (!MgnlContext.hasInstance()) {
            MgnlContext.initAsWebContext((HttpServletRequest) request, (HttpServletResponse) response);
        }

        chain.doFilter(request, response);

        MgnlContext.setInstance(null);
    }
}
