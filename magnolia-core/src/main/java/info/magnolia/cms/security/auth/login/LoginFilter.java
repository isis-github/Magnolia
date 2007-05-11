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
package info.magnolia.cms.security.auth.login;

import info.magnolia.cms.filters.AbstractMagnoliaFilter;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @author Sameer Charles
 * $Id$
 */
public class LoginFilter extends AbstractMagnoliaFilter {

    private Collection loginHandlers = new ArrayList();

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (Authenticator.isAuthenticated(request)) {
            // Switch anonymous context to WebContextImpl
            MgnlContext.initAsWebContext(request, response);
        } else {
            Iterator handlers = this.getLoginHandlers().iterator();
            int status = LoginHandler.STATUS_NOT_HANDLED;
            while (handlers.hasNext()) {
                LoginHandler handler = (LoginHandler) handlers.next();
                int retVal = handler.handle(request, response);
                if (retVal == LoginHandler.STATUS_IN_PROCESS) {
                    // special handling to support multi step login mechanisms like ntlm
                    // do not continue with the filter chain
                    return;
                } else if (retVal == LoginHandler.STATUS_SUCCEDED) {
                    status = LoginHandler.STATUS_SUCCEDED;
                }
            }
            // if any of the handlers succeed we have a session and can use WebContext
            if (status == LoginHandler.STATUS_SUCCEDED) {
                MgnlContext.initAsWebContext(request, response);
            }
        }
        // continue even if all login handlers failed
        chain.doFilter(request, response);
    }

    public Collection getLoginHandlers() {
        return loginHandlers;
    }

    public void setLoginHandlers(Collection loginHandlers) {
        this.loginHandlers = loginHandlers;
    }

    public void addLoginHandlers(LoginHandler handler) {
        this.loginHandlers.add(handler);
    }

}
