/**
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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.Resource;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;


/**
 * @author Marcel Salathe
 * @version $Revision: $ ($Author: $)
 */
public class IfEmpty extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static Logger log = Logger.getLogger(IfEmpty.class);

    private String nodeDataName = "";

    private String contentNodeName = "";

    private String contentNodeCollectionName = "";

    private transient ContentNode contentNodeCollection;

    private transient Content contentNode;

    private transient NodeData nodeData;

    private boolean actpage;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        // in the case where a contentNodeCollectionName is provided
        if (!this.contentNodeCollectionName.equals("")) {
            try {
                this.contentNodeCollection = Resource.getCurrentActivePage(req).getContentNode(
                    this.contentNodeCollectionName);
            }
            catch (RepositoryException e) {
                log.info("Exception caught: " + e.getMessage(), e);
            }
            if (this.contentNodeCollection == null) {
                return EVAL_BODY_INCLUDE;
            }
            if (!this.contentNodeCollection.hasChildren()) {
                return EVAL_BODY_INCLUDE;
            }
            return SKIP_BODY;
        }
        // if only contentNodeName is provided, it checks if this contentNode exists
        if (!this.contentNodeName.equals("") && this.nodeDataName.equals("")) {
            try {
                this.contentNode = Resource.getCurrentActivePage(req).getContentNode(this.contentNodeName);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage());
            }
            if (this.contentNode == null) {
                // contentNode doesn't exist, evaluate body
                return EVAL_BODY_INCLUDE;
            }
        }
        // if both contentNodeName and nodeDataName are set, it checks if that nodeData of that contentNode exitsts
        // and is not empty
        else if (!this.contentNodeName.equals("") && !this.nodeDataName.equals("")) {
            try {
                this.contentNode = Resource.getCurrentActivePage(req).getContentNode(this.contentNodeName);
            }
            catch (RepositoryException re) {
                log.debug("Repository exception while reading " + this.contentNodeName + ": " + re.getMessage());
            }
            if (this.contentNode == null) {
                return EVAL_BODY_INCLUDE;
            }
            if (this.contentNode != null) {

                this.nodeData = this.contentNode.getNodeData(this.nodeDataName);

                if ((this.nodeData == null) || !this.nodeData.isExist() || this.nodeData.getString().equals("")) {
                    return EVAL_BODY_INCLUDE;
                }
            }
        }
        // if only nodeDataName is provided, it checks if that nodeData of the current contentNode exists and is not
        // empty
        else if (this.contentNodeName.equals("") && !this.nodeDataName.equals("")) {
            if (this.actpage) {
                this.contentNode = Resource.getCurrentActivePage((HttpServletRequest) pageContext.getRequest());
            }
            else {
                this.contentNode = Resource.getLocalContentNode((HttpServletRequest) pageContext.getRequest());
                if (this.contentNode == null) {
                    this.contentNode = Resource.getGlobalContentNode((HttpServletRequest) pageContext.getRequest());
                }
            }
            if (this.contentNode == null) {
                return EVAL_BODY_INCLUDE;
            }
            if (this.contentNode != null) {

                this.nodeData = this.contentNode.getNodeData(this.nodeDataName);

                if ((this.nodeData == null) || !this.nodeData.isExist() || this.nodeData.getString().equals("")) {
                    return EVAL_BODY_INCLUDE;
                }
            }
        }
        // if both contentNodeName and nodeDataName are not provided, it checks if the current contentNode exists
        else {
            this.contentNode = Resource.getLocalContentNode((HttpServletRequest) pageContext.getRequest());
            if (this.contentNode == null) {
                this.contentNode = Resource.getGlobalContentNode((HttpServletRequest) pageContext.getRequest());
            }
            if (this.contentNode == null) {
                return EVAL_BODY_INCLUDE;
            }
        }
        return SKIP_BODY;
    }

    /**
     * @deprecated
     */
    public void setAtomName(String name) {
        this.setNodeDataName(name);
    }

    /**
     * @param name , antom name to evaluate
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * @deprecated
     */
    public void setContainerName(String name) {
        this.setContentNodeName(name);
    }

    /**
     * @param contentNodeName , contentNodeName to check
     */
    public void setContentNodeName(String contentNodeName) {
        this.contentNodeName = contentNodeName;
    }

    /**
     * @param name , contentNode collection name
     * @deprecated
     */
    public void setContainerListName(String name) {
        this.setContentNodeCollectionName(name);
    }

    /**
     * @param name contentNodeCollectionName to check
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * <p>
     * set the actpage
     * </p>
     * @param set
     */
    public void setActpage(boolean set) {
        this.actpage = set;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        this.nodeDataName = "";
        this.contentNodeName = "";
        this.contentNodeCollectionName = "";
        this.contentNodeCollection = null;
        this.contentNode = null;
        this.nodeData = null;
        this.actpage = false;
    }
}
