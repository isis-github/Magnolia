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
package info.magnolia.module.owfe.tree;

import freemarker.template.SimpleDate;
import info.magnolia.cms.beans.commands.CommandsMap;
import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.beans.runtime.WebContextImpl;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.owfe.MgnlConstants;
import info.magnolia.module.owfe.commands.ParametersSetterHelper;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * This is a subclass of the regular MVCHandler to plug in flow events. <p/> In this case, only the activate method is
 * part of a flow. We should find a way to plug in flow on the different methods.
 * @author jackie
 * @author Niko
 */

public abstract class CommandBasedTreeHandler extends AdminTreeMVCHandler {

    private static Logger log = Logger.getLogger(info.magnolia.module.owfe.tree.CommandBasedTreeHandler.class);

    public CommandBasedTreeHandler(String name, HttpServletRequest vrequest, HttpServletResponse vresponse) {
        super(name, vrequest, vresponse);
    }

    /**
     * execute dynamic command
     */
    public String execute(String command) {
        // get command from command map in JCR repository
        MgnlCommand tc = (MgnlCommand) CommandsMap.getCommand(MgnlConstants.WEBSITE_REPOSITORY, command);
        if (tc == null) { // not found, do in the old ways
            if (log.isDebugEnabled())
                log.debug("can not find command named " + command + " in tree command map");
            return super.execute(command);
        }
        if (log.isDebugEnabled())
            log.debug("find command for " + command + ": " + tc);
        
        // set parameters
        HashMap params = new HashMap();
        
        // set some general parameters
        params.put(MgnlConstants.P_REQUEST, request);
        params.put(MgnlConstants.P_TREE, tree);
        params.put(MgnlConstants.P_PATH, pathSelected);

        populateParams(command, params);

        Context context = (MgnlContext.hasInstance()) ? MgnlContext.getInstance() : new WebContextImpl();
        context.put(MgnlConstants.P_REQUEST, request);
        context.put(MgnlConstants.INTREE_PARAM, params);

        try {
            // translate parameter
            new ParametersSetterHelper().translateParam(tc, context);
            // execute
            tc.execute(context);
        }
        catch (Exception e) {
            // TODO: check that this is processed somewhere else
            log.error("Error while executing the command:" + command, e);
        }

        return VIEW_TREE;
    }

    /**
     * This method populates the params passed to the command
     * @param params
     */
    protected void populateParams(String command, HashMap params) {
        // add start date and end date
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.WEBSITE);
        Content ct = null;
        try {
            ct = hm.getContent(pathSelected);
            Calendar cd = ct.getMetaData().getStartTime();
            String date;// new Timestamp(cd.getTimeInMillis()).toString();
            //Timestamp tm = new Timestamp(cd.getTimeInMillis());
            //date = new SimpleDate(new Date(cd.getTimeInMillis()) ).toString();
            date = ""+cd.get(Calendar.YEAR)+"-"+(cd.get(Calendar.MONTH)+1)+"-"+cd.get(Calendar.DAY_OF_MONTH)
            +" "+cd.get(Calendar.HOUR_OF_DAY)+":"+cd.get(Calendar.MINUTE)+":"+cd.get(Calendar.SECOND)+"+0000";
            log.info("start date = " + date);
            //date = "2006-04-14 10:23:15+0800";
            params.put("startDate", date);

            cd = ct.getMetaData().getEndTime();          
            date = ""+cd.get(Calendar.YEAR)+"-"+(cd.get(Calendar.MONTH)+1)+"-"+cd.get(Calendar.DAY_OF_MONTH)
            +" "+cd.get(Calendar.HOUR_OF_DAY)+":"+cd.get(Calendar.MINUTE)+":"+cd.get(Calendar.SECOND)+"+0000";
          
            log.info("end date = " + date);
            params.put("endDate", date);
        }
        catch (Exception e) {
            log.warn("can not get start/end date for path "
                + pathSelected
                + ", please use sevlet FlowDef to set start/end date for node.");
        }

        String recursive = "false";
        if (request.getParameter("recursive") != null)
            recursive = "true";
        params.put(MgnlConstants.P_RECURSIVE, recursive);
    }

}
