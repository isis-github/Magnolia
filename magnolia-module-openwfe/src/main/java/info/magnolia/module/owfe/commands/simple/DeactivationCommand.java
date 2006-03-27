package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.module.owfe.commands.MgnlCommand;
import openwfe.org.engine.workitem.InFlowWorkItem;
import org.apache.commons.chain.Context;

import java.util.HashMap;

public class DeactivationCommand extends SimpleCommand {

    public boolean exec(HashMap params, Context Ctx) {
		String path;
//		 boolean recursive; 
	   path = (String) params.get(P_PATH);
//       recursive = ((Boolean) params.get(P_RECURSIVE)).booleanValue();
	  try {
		  doDeactivate(path);
      } catch (Exception e) {
          log.error("cannot do activate", e);
          return false;
      }
      return true;
}
//    public boolean execute(Context context) {
//        HashMap params = (HashMap) context.get(PARAMS);
//        String path;
//        InFlowWorkItem if_wi = (InFlowWorkItem) params.get(MgnlCommand.P_WORKITEM);
//        if (if_wi != null) { // if call from flow
//            path = (if_wi.getAttribute(P_PATH)).toString();
//        } else {
//            path = (String) params.get(P_PATH);
//        }
//        try {
//            doDeactivate(path);
//        } catch (Exception e) {
//            log.error("cannot do deactivate", e);
//            return false;
//        }
//        return true;
//    }

    private void doDeactivate(String path) throws Exception {
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_FILE);
        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), REPOSITORY, ContentRepository.getDefaultWorkspace(REPOSITORY), rule);
        syndicator.deActivate(path);
    }

}
