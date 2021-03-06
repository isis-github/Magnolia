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
package info.magnolia.freemarker;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Dummy servlet used to satisfy all freemarker dependencies at runtime.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DoNothingServlet extends GenericServlet {
    private final ServletContext servletContext;

    DoNothingServlet(ServletContext sc) throws ServletException {
        this.servletContext = sc;
        this.init(new ServletConfig() {
            @Override
            public String getInitParameter(String name) {
                return null;
            }

            @Override
            public Enumeration getInitParameterNames() {
                return Collections.enumeration(Collections.EMPTY_LIST);
            }

            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }

            @Override
            public String getServletName() {
                return DoNothingServlet.class.getName();
            }
        });
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        // seriously, just don't do anything.
    }
}
