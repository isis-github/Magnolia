package info.magnolia.module.owfe.commands.simple.flow;

import info.magnolia.module.owfe.OWFEEngine;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.commands.simple.SimpleCommand;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;

import java.util.HashMap;

import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;

import org.apache.commons.chain.Context;

/**
 * Created by IntelliJ IDEA.
 * User: niko
 * Date: Mar 22, 2006
 * Time: 1:11:29 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractFlowCommand extends SimpleCommand {
	
	String flowName = "";
	
//	public AbstractFlowCommand(String flowName){
//		this.flowName = flowName;
//	}
	
    public boolean exec(HashMap params, Context ctx) {
	
        log.info("- Flow command -" + this.getClass().toString() + "- Start");
        try {
            // Get the references
            LaunchItem li = new LaunchItem();
            li.addAttribute(P_ACTION, new StringAttribute(this.getClass().getName()));
            li.setWorkflowDefinitionUrl(MgnlCommand.P_WORKFLOW_DEFINITION_URL);
            
            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition().getflowDefAsString(getFlowName());
            li.getAttributes().puts(MgnlCommand.P_DEFINITION, flowDef);
            JCRPersistedEngine engine = OWFEEngine.getEngine();

            // start activation
            preLaunchFlow(ctx, params, engine, li);

            // Launch the item
            engine.launch(li, true);

        } catch (Exception e) {
            log.error("Launching failed", e);
        }

        // End execution
        log.info("- Flow command -" + this.getClass().toString() + "- End");
        return true;
	}

//	public boolean execute(Context context) {
//        HashMap params = (HashMap) context.get(P_WORKITEM);
//        log.info("- Flow command -" + this.getClass().toString() + "- Start");
//        try {
//            // Get the references
//            LaunchItem li = new LaunchItem();
//            li.addAttribute(P_ACTION, new StringAttribute(this.getClass().getName()));
//            JCRPersistedEngine engine = OWFEEngine.getEngine();
//
//            // start activation
//            onExecute(context, params, engine, li);
//
//            // Launch the item
//            engine.launch(li, true);
//
//        } catch (Exception e) {
//            log.error("Launching failed", e);
//        }
//
//        // End execution
//        log.info("- Flow command -" + this.getClass().toString() + "- End");
//        return true;
//    }

    public abstract void preLaunchFlow(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem launchItem);
    public abstract String getFlowName();
}
