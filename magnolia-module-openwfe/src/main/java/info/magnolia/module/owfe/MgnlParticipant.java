package info.magnolia.module.owfe;

import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.commands.ParametersSetterHelper;
import info.magnolia.module.owfe.jcr.JCRWorkItemAPI;
import openwfe.org.embed.impl.engine.AbstractEmbeddedParticipant;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.WorkItem;
import org.apache.log4j.Logger;

import java.util.HashMap;

public class MgnlParticipant extends AbstractEmbeddedParticipant {
    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(AbstractEmbeddedParticipant.class);
    JCRWorkItemAPI storage = null;

    public MgnlParticipant() throws Exception {
        super();
        storage = new JCRWorkItemAPI();
        if (log.isDebugEnabled())
            log.debug("storage = " + storage);
    }

    public MgnlParticipant(String arg0) throws Exception {
        super(arg0);
        storage = new JCRWorkItemAPI();
        if (log.isDebugEnabled())
            log.debug("storage = " + storage);
    }

    public void consume(WorkItem wi) throws Exception {

        // get participant name
        if (log.isDebugEnabled())
            log.debug("enter consume()..");
        if (wi == null) {
            log.error("work item is null");
            return;
        }
        String parName = ((InFlowWorkItem) (wi)).getParticipantName();
        if (log.isDebugEnabled())
            log.debug("participant name = " + parName);
        if (parName.startsWith(MgnlCommand.PREFIX_COMMAND)) // handle commands
        {
            log.info("consume command " + parName + "...");
            String cmd = parName.substring(MgnlCommand.PREFIX_COMAMND_LEN, parName.length());
            if (log.isDebugEnabled())
                log.debug("command name is " + cmd);


            try {
                // for testing now
//                MgnlCommand test = CommandsMap.getCommandFromFullName(parName);
//                if (test == null) { // not found, do in the old ways
//                    if (log.isDebugEnabled())
//                        log.debug("can not find command named " + cmd + " from command map");
//                } else {
//                    if (log.isDebugEnabled())
//                        log.debug("found command for " + cmd + "in command map");
//                }

                MgnlCommand c = CommandsMap.getCommandFromFullName(cmd);
                if (c != null) {
                    if (log.isDebugEnabled())
                        log.debug("Command has been found through the magnolia catalog:" + c.getClass().getName());

                  //  String[] expected = c.getExpectedParameters();

                    // set parameters in the context
                    HashMap params = new HashMap();
                    params.put(MgnlCommand.INFLOW_PARAM, wi);

                    Context context = MgnlContext.getInstance();
                    context.put(MgnlCommand.PARAMS, params);

                    
                    // translate parameter
                    new ParametersSetterHelper().translateParam(c, context);
                    
                    // execute
                    c.execute(context);

                } else // not found, do in the old ways
                    if (log.isDebugEnabled())
                        log.debug("No command has been found through the magnolia catalog for name:" + cmd);

                log.info("consume command " + parName + "end.");
            }
            catch (Exception e) {
                // does not really matter here
            }
        } else {
            if (log.isDebugEnabled())
                log.debug("storage = " + storage);
            storage.storeWorkItem("", (InFlowWorkItem) wi);
        }

        if (log.isDebugEnabled())
            log.debug("leave consume()..");

    }


}
