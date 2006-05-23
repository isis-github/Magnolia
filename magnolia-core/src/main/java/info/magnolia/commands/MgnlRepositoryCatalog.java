package info.magnolia.commands;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.CatalogBase;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;

/**
 * Date: Mar 27, 2006 Time: 10:58:22 AM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlRepositoryCatalog extends CatalogBase {

    private static Logger log = LoggerFactory.getLogger(MgnlRepositoryCatalog.class); 
    
    static final String CLASS_NODE_DATA = "impl";

    
    /**
     * Initialize this catalog based on the configuration found in the repository
     */
    public MgnlRepositoryCatalog(Content content) {
        Iterator iter = content.getChildren(ItemType.CONTENTNODE).iterator();
        // loop over the command names one by one
        while (iter.hasNext()) {
            // get the action node and name
            Content actionNode = (Content) iter.next();
            String actionName = actionNode.getName();

            String className = StringUtils.EMPTY;

            NodeData impl = actionNode.getNodeData(CLASS_NODE_DATA);
            if (impl != null && impl.getString() != null && !(impl.getString().equals(""))) {

                log.debug("This is a simple action: {}", actionName);

                // this is a simple command
                className = impl.getString();

                Command command;
                try {
                    Class klass = Class.forName(className);
                    command = (Command) klass.newInstance();
                }
                catch (Exception e) {
                    log.error("Could not load action:" + actionName, e);
                    continue;
                }

                this.addCommand(actionName, command);

                // continue with next action
            }
            else {
                log.debug("This is a chain");

                Chain chain = new ChainBase();

                // consider any command as a chain, makes things easier
                // we iterate through each subnode and consider this as a command in the chain
                Collection childrens = actionNode.getChildren(ItemType.CONTENTNODE);

                log.debug("Found {}  children for action {}", Integer.toString(childrens.size()), actionName);
                Iterator iterNode = childrens.iterator();

                Exception e = null;
                while (iterNode.hasNext()) {
                    try {
                        Content commandNode = (Content) iterNode.next();
                        className = commandNode.getNodeData(CLASS_NODE_DATA).getString();

                        log.debug("Found class {} for action {}", className, actionName);
                        Class klass = Class.forName(className);
                        Command command = (Command)klass.newInstance();
                        chain.addCommand(command);
                    }
                    catch (Exception te) {
                        e = te;
                        break;
                    }
                }

                // add the command only if no error was reported
                if (e != null) {
                    log.error("Could not load commands for action:" + actionName, e);
                }
                else {
                    // add the chain to the catalog linked with the actionName
                    this.addCommand(actionName, chain);
                }
            }
        }

    }
}