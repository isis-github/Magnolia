package info.magnolia.module.owfe;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ValueFactory;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

//import com.ns.log.Log;

public class JCRFlowDefinition {
	  /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(FlowDefServlet.class);

	public Content findFlowDef(String name){
		if (name == null)
			return null;
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("flowdef");
		try {
			Content root = hm.getRoot();
			Collection c = root.getChildren(ItemType.CONTENT);
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Content ct = (Content) it.next();
//				String title = ct.getTitle();
//				log.info("title="+title);
				String sname = ct.getName();
//				log.info("name="+sname);
				if (name!= null && name.equals(name)) {					
					return ct;
				}
			}
		} catch (Exception e) {
//			Log.error("owfe", e);
			//log.error("exception:" + e);
		}
		return null;
	}
	
	public List getFlows(HttpServletRequest request){
		String url_base = "http://"+ request.getServerName()+":" + request.getServerPort() + request.getRequestURI();
		ArrayList list = new ArrayList();
		log.info(url_base);
		//log.info(request.getRealPath());
//		log.info(request.getContextPath());
//		log.info(request.getServletPath());
//		log.info(request.getPathInfo());
//		log.info(request.getPathTranslated());
		
		
		
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("flowdef");
		try {
			Content root = hm.getRoot();
			Collection c = root.getChildren(ItemType.CONTENT);
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Content ct = (Content) it.next();
				String name = ct.getName();
	
				if (name!= null) {					
					list.add(url_base+"?name="+name);
				}
			}
		} catch (Exception e) {
			log.error("error", e);
			//log.error("exception:" + e);
		}
		return list;
	}

	public List addFlow(String flowDef) throws Exception{
		if (flowDef == null)
			return null;
		String name = "";
		// jdom
		final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
		Document doc = builder.build(new StringReader(flowDef));
		Element process_definition = doc.getRootElement();
		name = process_definition.getAttribute("name").getValue();	
		
		// jaxp
		
//			 DocumentBuilderFactory factory = null;
//			 DocumentBuilder builder = null;
//			//get a DocumentBuilderFactory from the underlying implementation
//			factory = DocumentBuilderFactory.newInstance();
//
//			//factory.setValidating(true);
//
//			//get a DocumentBuilder from the factory
//			builder = factory.newDocumentBuilder();
//
//			Document doc = builder.parse(new StringBufferInputStream(flowDef));
//		Element process_definition = doc.getDocumentElement();
//			
//		name = process_definition.getAttribute("name");	
		
//		String flowDef = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//			+ "<process-definition "
//			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
//			+ "xsi:noNamespaceSchemaLocation=\"http://www.openwfe.org/flowdef_r1.5.0.xsd\" "
//			+ "name=\"docflow\" "
//			+ "revision=\"1.0\">"
//			+ "<description language=\"default\"> "
//			+ "This just the complete flow definition of docflow process. "
//			+ "</description>" + "<sequence>" +
//			"<participant ref=\""+ name + "\"/>" + "</sequence>"
//			+ "</process-definition>";

		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("flowdef");
		try {
			Content root = hm.getRoot();
			Content c = root.createContent(name, ItemType.CONTENT);
			ValueFactory vf = c.getJCRNode().getSession().getValueFactory();
			c.createNodeData("value", vf.createValue(flowDef));
			hm.save();
			log.info("add ok");
		} catch (Exception e) {
			log.error("add flow failed", e);
//			Log.error("owfe", e);
			//log.error("exception:" + e);
		}
		return null;
	}

}
