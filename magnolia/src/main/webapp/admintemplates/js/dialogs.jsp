<%@ page contentType="text/javascript; charset=utf-8" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="org.apache.commons.io.IOUtils" %>

var contextPath = '<%= request.getContextPath() %>';
<%
    String[] includes = {
        "dialogs.js",
        "acl.js",
        "calendar.js",
        "controls.js",
        "tree.js",
        "i18n.js",
        "contextmenu.js",
        "inline.js"
    };

    for(int i=0; i<includes.length; i++){
        InputStream in = getClass().getResourceAsStream("/mgnl-resources/admin-js/dialogs/" + includes[i]);
        IOUtils.copy(in, out);
    }
%>
