/*
 * Created on Mar 30, 2005
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
package info.magnolia.cms.i18n;

import javax.servlet.http.HttpServletRequest;

import info.magnolia.cms.gui.dialog.DialogSuper;

/**
 * @author philipp
 *
 * This class helps to get the messages. First it make a lookup in messages_templating_custom and then in messages_templating..
 */
public class TemplateMessages {
    public static String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages_templating";
    public static String CUSTOM_BASENAME = "info.magnolia.module.admininterface.messages_templating_custom";
    
    public static String get(DialogSuper dialog, String key) {
        return get(dialog.getRequest(),key);
    }

    public static String get(DialogSuper dialog, String key, Object[] args) {
        return get(dialog.getRequest(),key, args);
    }
    
    public static String get(HttpServletRequest request, String key) {
        Messages msgs = ContextMessages.getInstanceSafely(request);
        String msg = msgs.getWithDefault(key, DEFAULT_BASENAME, key);
        if(!msg.equals(key)){
            return msg;
        }
        return msgs.getWithDefault(key, CUSTOM_BASENAME, key);
        
    }
    
    public static String get(HttpServletRequest request, String key, Object[] args) {
        Messages msgs = ContextMessages.getInstanceSafely(request);
        String msg = msgs.getWithDefault(key, DEFAULT_BASENAME, args, key);
        if(!msg.equals(key)){
            return msg;
        }
        return msgs.getWithDefault(key, CUSTOM_BASENAME, args, key);
    }

}
