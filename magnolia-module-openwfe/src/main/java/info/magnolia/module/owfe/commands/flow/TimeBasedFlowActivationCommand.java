/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.owfe.commands.flow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.commands.ContextAttributes;
import info.magnolia.context.Context;
import openwfe.org.engine.workitem.LaunchItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The activation command which will launch a flow to do scheduled activation by "sleep" functionality of owfe
 * @author jackie
 */
public class TimeBasedFlowActivationCommand extends FlowCommand {

    private static final String WEB_SCHEDULED_ACTIVATION = "webScheduledActivation";

    private static Logger log = LoggerFactory.getLogger(TimeBasedFlowActivationCommand.class);

    public TimeBasedFlowActivationCommand() {
        // set default value
        setWorkflowName(WEB_SCHEDULED_ACTIVATION);
    }

    /**
     * Set the start and end date for this page
     */
    public void prepareLaunchItem(Context context, LaunchItem launchItem) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        
        // add start date and end date
        String repository = (String) context.get(ContextAttributes.P_REPOSITORY);
        String path = (String) context.get(ContextAttributes.P_PATH);

        HierarchyManager hm = ContentRepository.getHierarchyManager(repository);

        Content node = null;
        try {
            node = hm.getContent(path);
        }
        catch (RepositoryException e) {
            log.error("can't find node for path [" + path + "]", e);
            return;
        }

        Calendar cd = null;
        String date;

        // get start time
        try {
            cd = node.getMetaData().getStartTime();
        }
        catch (Exception e) {
            log.warn("cannot get start time for node " + path, e);
        }
        
        if (cd != null) {
            date = sdf.format(new Date(cd.getTimeInMillis()));
            log.debug("start date = " + date);
            launchItem.getAttributes().puts(ContextAttributes.P_START_DATE, date);
        }

        // get end time
        try {
            cd = node.getMetaData().getEndTime();
        }
        catch (Exception e) {
            log.warn("cannot get end time for node " + path, e);
        }

        if (cd != null) {
            date = sdf.format(new Date(cd.getTimeInMillis()));
            log.debug("end date = " + date);
            launchItem.getAttributes().puts(ContextAttributes.P_END_DATE, date);
        }
    }

}
