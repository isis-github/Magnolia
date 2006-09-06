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
package info.magnolia.cms.core;

import org.apache.commons.lang.StringUtils;

import java.util.Hashtable;
import java.util.Map;


/**
 * @author Sameer charles
 * @version 2.0 $Id$
 */
public final class SystemProperty {

    public static final String MAGNOLIA_REPOSITORIES_CONFIG = "magnolia.repositories.config"; //$NON-NLS-1$

    public static final String MAGNOLIA_EXCHANGE_HISTORY = "magnolia.exchange.history"; //$NON-NLS-1$

    public static final String MAGNOLIA_UPLOAD_TMPDIR = "magnolia.upload.tmpdir"; //$NON-NLS-1$

    public static final String MAGNOLIA_CACHE_STARTDIR = "magnolia.cache.startdir"; //$NON-NLS-1$

    public static final String MAGNOLIA_APP_ROOTDIR = "magnolia.app.rootdir"; //$NON-NLS-1$

    public static final String MAGNOLIA_BOOTSTRAP_ROOTDIR = "magnolia.bootstrap.dir"; //$NON-NLS-1$

    public static final String MAGNOLIA_WEBAPP = "magnolia.webapp";

    public static final String MAGNOLIA_SERVERNAME = "magnolia.servername";
    
    /**
     * If this repository/workspace is empty bootstrap this repository. This will be the config repository in the most
     * cases.
     */
    public static final String BOOTSTRAP_IF_EMPTY = "magnolia.bootstrap.ifEmpty";

    private static Map properties = new Hashtable();

    /**
     * Web app root key parameter at the servlet context level (i.e. a context-param in web.xml): "webAppRootKey".
     */
    public static final String MAGNOLIA_ROOT_SYSPROPERTY = "magnolia.root.sysproperty"; //$NON-NLS-1$

    /**
     * Utility class, don't instantiate.
     */
    private SystemProperty() {
        // unused
    }

    /**
     * @param name
     * @param value
     */
    public static void setProperty(String name, String value) {
        SystemProperty.properties.put(name, value);
    }

    /**
     * @param name
     */
    public static String getProperty(String name) {
        return (String) SystemProperty.properties.get(name);
    }

    /**
     * @param name
     * @param defaultValue
     */
    public static String getProperty(String name, String defaultValue) {
        String value = getProperty(name);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    /**
     *
     */
    public static Map getPropertyList() {
        return SystemProperty.properties;
    }
}
