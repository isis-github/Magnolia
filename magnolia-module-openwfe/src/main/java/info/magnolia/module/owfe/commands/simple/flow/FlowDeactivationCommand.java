package info.magnolia.module.owfe.commands.simple.flow;

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import org.apache.commons.chain.Context;

import java.util.HashMap;

/**
 * Starts the deactivation flow
 *
 * @author jackie
 * @author nicolas
 */
public class FlowDeactivationCommand extends AbstractFlowCommand {

    private final static String FLOWNAME = "webDetactivation";

    private static final String WEB_SCHEDULED_ACTIVATION = "webScheduledActivation";

    static final String[] parameters = {P_PATH};

    /**
     * List of the parameters that this command needs to run
     *
     * @return a list of string describing the parameters needed. The parameters should have a  mapping in this class.
     */
    public String[] getExpectedParameters() {
        return parameters;
    }


    public String getFlowName() {
        return FLOWNAME;
    }

    public void preLaunchFlow(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem li) {
        try {
            // Retrieve parameters
            String pathSelected = (String) params.get(MgnlCommand.P_PATH);

            // Parameters for the flow item
            li.addAttribute(MgnlCommand.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute(MgnlCommand.P_OK, MgnlCommand.ATT_FALSE);

        } catch (Exception e) {
            MgnlCommand.log.error("can't launch deactivate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }

    }
}
