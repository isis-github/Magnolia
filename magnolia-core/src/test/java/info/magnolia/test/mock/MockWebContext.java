/**
 * This file Copyright (c) 2007-2012 Magnolia International
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
package info.magnolia.test.mock;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.User;
import info.magnolia.context.UserContext;
import info.magnolia.context.WebContext;

/**
 * Implementation of mock context that also implements WebContext interface. Only methods needed for testing are implemented here.
 * @author had
 *
 */
public class MockWebContext extends MockContext implements WebContext, UserContext {

    private AggregationState aggregationState = new MockAggregationState();
    private Subject subject;
    private User user;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String contextPath;
    private Map<String, String> parameters;
    private ServletContext servletContext;
    private PageContext pageContext;
    private MultipartForm postedForm;

    public MockWebContext() {
    }

    public MockWebContext(String contextPath, String uri, Map<String, String> parameters) {
        this.contextPath = contextPath;
        this.parameters = parameters;
        this.getAggregationState().setCurrentURI(uri);
    }


    public Content getActivePage() {
        return (Content) (pageContext == null ? null : pageContext.getAttribute("actPage"));
    }

    @Override
    public AggregationState getAggregationState() {
        return aggregationState ;
    }

    @Override
    public String getParameter(String name) {
        return getParameters().get(name);
    }

    @Override
    public void include(String path, Writer out) throws ServletException, IOException {
        throw new IllegalStateException("not implemented !");
    }

    @Override
    public void init(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
    }

    @Override
    public void resetAggregationState() {
    }

    @Override
    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    /**
     * Set the AggregationState (required by some tests)
     * @param agState An AggregationState to use
     */
    public void setAggregationState(AggregationState agState) {
        this.aggregationState = agState;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public void login(Subject subject) {
        this.subject = subject;
    }

    @Override
    public void logout() {
        this.user = null;
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    @Override
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public MultipartForm getPostedForm() {
        return postedForm;
    }

    public void setPostedForm(MultipartForm postedForm) {
        this.postedForm = postedForm;
    }

    @Override
    public PageContext getPageContext() {
        return pageContext;
    }

    public void setCurrentURI(String uri) {
        getAggregationState().setCurrentURI(uri);
    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#pop()
     */
    @Override
    public void pop() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see info.magnolia.context.WebContext#push(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void push(HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] getParameterValues(String name) {
        // TODO Auto-generated method stub
        return null;
    }
}
