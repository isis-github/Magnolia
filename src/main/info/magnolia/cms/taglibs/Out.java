/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.misc.FileProperties;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version $Revision: $ ($Author: $)
 */
public class Out extends TagSupport
{

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static Logger log = Logger.getLogger(Out.class);

    private final static String DEFAULT_LINEBREAK = NodeData.HTML_LINEBREAK;

    private final static String DEFAULT_DATEPATTERN = "yyyy-MM-dd";

    private String nodeDataName = null;

    private String contentNodeName = null;

    private String contentNodeCollectionName = null;

    private Content contentNode = null;

    private NodeData nodeData = null;

    private String fileProperty = "";

    private String datePattern = DEFAULT_DATEPATTERN; // according to ISO 8601

    private String dateLanguage = null;

    private String lineBreak = DEFAULT_LINEBREAK;

    private boolean isNullOrEmtpy(String s)
    {
        // returns true is s is null or ""
        if (s == null)
            return true;
        else if (s.equals(""))
            return true;
        else
            return false;
    }

    /**
     * <p>
     * starts out tag
     * </p>
     * @return int
     */
    public int doStartTag()
    {
        // System.out.println("");
        // System.out.println("name: "+this.getNodeDataName());

        ContentNode local = Resource.getLocalContentNode((HttpServletRequest) pageContext.getRequest());
        Content actpage = Resource.getCurrentActivePage((HttpServletRequest) pageContext.getRequest());

        String contentNodeName = this.getContentNodeName();
        String contentNodeCollectionName = this.getContentNodeCollectionName();

        if (contentNodeName != null && !contentNodeName.equals(""))
        {
            // contentNodeName is defined
            try
            {
                if (this.isNullOrEmtpy(contentNodeCollectionName))
                {
                    // e.g. <cms:out nodeDataName="title" contentNodeName="footer"/>
                    this.setContentNode(actpage.getContentNode(contentNodeName));
                }
                else
                {
                    // e.g. <cms:out nodeDataName="title" contentNodeName="01" contentNodeCollectionName="mainPars"/>
                    // e.g. <cms:out nodeDataName="title" contentNodeName="footer" contentNodeCollectionName=""/>
                    this.setContentNode(actpage.getContentNode(contentNodeCollectionName).getContentNode(
                        contentNodeName));
                }
            }
            catch (RepositoryException re)
            {
                log.debug(re.getMessage());
            }
        }
        else
        {
            if (local == null)
            {
                // outside collection iterator
                if (contentNodeCollectionName != null && !contentNodeCollectionName.equals(""))
                {
                    // ERROR: no content node assignable because contentNodeName is empty
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName="mainPars"/>
                    return SKIP_BODY;
                }
                else
                {
                    // e.g. <cms:out nodeDataName="title"/>
                    // e.g. <cms:out nodeDataName="title" contentNodeName=""/>
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                    this.setContentNode(actpage);
                }
            }
            else
            {
                // inside collection iterator
                if (contentNodeName == null && contentNodeCollectionName == null)
                {
                    // e.g. <cms:out nodeDataName="title"/>
                    this.setContentNode(local);
                }
                else if ((contentNodeName != null && contentNodeName.equals(""))
                    || (contentNodeCollectionName != null && contentNodeCollectionName.equals("")))
                {
                    // empty collection name -> use actpage
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                    this.setContentNode(actpage);
                }
                else
                {
                    // ERROR: no content node assignable because contentNodeName is empty
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName="mainPars"/>
                    return SKIP_BODY;
                }
            }

        }
        return SKIP_BODY;
    }

    /**
     * <p>
     * continue evaluating jsp
     * </p>
     * @return int
     */
    public int doEndTag()
    {
        this.display();
        this.setContentNodeCollectionName(null);
        this.setContentNodeName(null);
        this.setContentNode(null);
        this.setNodeDataName(null);
        this.setNodeData(null);
        this.setDateLanguage(null);
        this.setDatePattern(DEFAULT_DATEPATTERN);
        this.setLineBreak(DEFAULT_LINEBREAK);
        return EVAL_PAGE;
    }

    /**
     * <p>
     * set the requested node data
     * </p>
     * @param node
     */
    public void setNodeData(NodeData node)
    {
        this.nodeData = node;
    }

    public NodeData getNodeData()
    {
        return this.nodeData;
    }

    /**
     * <p>
     * set the node data name, e.g. "mainText"
     * </p>
     * @param name
     */
    public void setNodeDataName(String name)
    {
        this.nodeDataName = name;
    }

    public String getNodeDataName()
    {
        return this.nodeDataName;
    }

    /**
     * <p>
     * set the content node name name, e.g. "01"
     * </p>
     * @param name
     */
    public void setContentNodeName(String name)
    {
        this.contentNodeName = name;
    }

    /**
     * <p>
     * set the content node collection name name, e.g. "mainColumnParagraphs"
     * </p>
     * @param name
     */
    public void setContentNodeCollectionName(String name)
    {
        this.contentNodeCollectionName = name;
    }

    public String getContentNodeCollectionName()
    {
        return this.contentNodeCollectionName;
    }

    public String getContentNodeName()
    {
        return this.contentNodeName;
    }

    /**
     * <p>
     * set the content node name
     * </p>
     * @param c
     */
    public void setContentNode(Content c)
    {
        this.contentNode = c;
    }

    public Content getContentNode()
    {
        return this.contentNode;
    }

    /**
     * @deprecated
     * <p>
     * set the actpage
     * </p>
     * @param set (true/false; false is default)
     */
    public void setActpage(String set)
    {
    }

    /**
     * <p>
     * set which information of a file to retrieve
     * </p>
     * <p>
     * does only apply for nodeDatas of type=Binary
     * </p>
     * <p>
     * supported values (sample value): <br>
     * <ul>
     * <li><b>path (default): </b> path inlcuding the filename (/dev/mainColumnParagraphs/0/image/Alien.png)
     * <li><b>name </b>: name and extension (Alien.png)
     * <li><b>extension: </b> extension as is (Png)
     * <li><b>extensionLowerCase: </b> extension lower case (png)
     * <li><b>extensionUpperCase: </b> extension upper case (PNG)
     * <li><b>nameWithoutExtension: (Alien)
     * <li><b>handle: </b> /dev/mainColumnParagraphs/0/image
     * <li><b>pathWithoutName: </b> (/dev/mainColumnParagraphs/0/image.png)
     * <li><b>size: </b> size in bytes (2827)
     * <li><b>sizeString: </b> size in bytes, KB or MB - max. 3 digits before comma - with unit (2.7 KB)
     * <li><b>contentType: </b> (image/png)
     * </ul>
     * </p>
     * @param property
     */
    public void setFileProperty(String property)
    {
        this.fileProperty = property;
    }

    public String getFileProperty()
    {
        return this.fileProperty;
    }

    /**
     * <p>
     * set which date format shall be delivered
     * </p>
     * <p>
     * does only apply for nodeDatas of type=Date
     * </p>
     * <p>
     * language according to java.text.SimpleDateFormat: <br>
     * <ul>
     * <li><b>G </b> Era designator Text AD
     * <li><b>y </b> Year Year 1996; 96
     * <li><b>M </b> Month in year Month July; Jul; 07
     * <li><b>w </b> Week in year Number 27
     * <li><b>W </b> Week in month Number 2
     * <li><b>D </b> Day in year Number 189
     * <li><b>d </b> Day in month Number 10
     * <li><b>F </b> Day of week in month Number 2
     * <li><b>E </b> Day in week Text Tuesday; Tue
     * <li><b>a </b> Am/pm marker Text PM
     * <li><b>H </b> Hour in day (0-23) Number 0
     * <li><b>k </b> Hour in day (1-24) Number 24
     * <li><b>K </b> Hour in am/pm (0-11) Number 0
     * <li><b>h </b> Hour in am/pm (1-12) Number 12
     * <li><b>m </b> Minute in hour Number 30
     * <li><b>s </b> Second in minute Number 55
     * <li><b>S </b> Millisecond Number 978
     * <li><b>z </b> Time zone General time zone Pacific Standard Time; PST; GMT-08:00
     * <li><b>Z </b> Time zone RFC 822 time zone -0800
     * </ul>
     * </p>
     * @param pattern , default is yyyy-MM-dd
     */
    public void setDatePattern(String pattern)
    {
        this.datePattern = pattern;
    }

    public String getDatePattern()
    {
        return this.datePattern;
    }

    /**
     * <p>
     * set which date format shall be delivered
     * </p>
     * <p>
     * does only apply for nodeDatas of type=Date
     * </p>
     * <p>
     * language according to java.util.Locale
     * </p>
     * @param language
     */
    public void setDateLanguage(String language)
    {
        this.dateLanguage = language;
    }

    public String getDateLanguage()
    {
        return this.dateLanguage;
    }

    /**
     * <p>
     * set the lineBreak String
     * </p>
     * @param lineBreak
     */
    public void setLineBreak(String lineBreak)
    {
        this.lineBreak = lineBreak;
    }

    public String getLineBreak()
    {
        return this.lineBreak;
    }

    /**
     *
     */
    private void display()
    {
        try
        {
            /*
             * //@todo //check if mutliple values (checkboxes) -> not nodeData but contentNode try { int i=0; Iterator
             * it=this.contentNode.getContentNode(this.nodeDataName).getChildren(ChildrenCollector.PROPERTY).iterator();
             * while (it.hasNext()) { System.out.println(i++); NodeData data=(NodeData) it.next();
             * System.out.println("GN:"+data.getName()); //System.out.println("TYPE:"+data.getType()); } } catch
             * (ElementNotFoundException e) {
             */
            // System.out.println("1 "+this.getNodeDataName());
            // System.out.println("2 "+this.getContentNode());
            // if (this.property!=null) this.nodeData =
            // this.contentNode.getContentNode(this.nodeDataName+"_properties").getNodeData(this.property);
            // else this.nodeData = this.getContentNode().getNodeData(this.nodeDataName);
            // System.out.println("3 "+this.getContentNode().getNodeData(this.nodeDataName).getString());
            NodeData nodeData = this.getContentNode().getNodeData(this.getNodeDataName());
            String value = "";
            int type = nodeData.getType();

            if (type == PropertyType.DATE)
            {
                value = this.getDateFormatted(nodeData.getDate().getTime());
            }
            else if (type == PropertyType.BINARY)
            {
                value = this.getFilePropertyValue();
            }
            else
            {
                if (this.getLineBreak().equals(""))
                    value = nodeData.getString();
                else
                    value = nodeData.getString(this.getLineBreak());
            }

            JspWriter out = pageContext.getOut();
            try
            {
                out.print(value);
            }
            catch (IOException e)
            {
            }

        }
        catch (Exception e)
        {
        }

    }

    // @todo: place in another package to make it availbable globaly -> NodeData?
    public String getDateFormatted(Date date)
    {
        String value = "";
        if (date == null || date.equals(""))
            return "";
        SimpleDateFormat formatter;
        String lang = this.getDateLanguage();
        if (lang == null)
            formatter = new SimpleDateFormat(this.getDatePattern());
        else
            formatter = new SimpleDateFormat(this.getDatePattern(), new Locale(lang));
        value = formatter.format(date);
        return value;
    }

    public String getFilePropertyValue()
    {
        FileProperties props = new FileProperties(this.getContentNode(), this.nodeDataName);
        String value = props.getProperty(this.getFileProperty());
        return value;
    }

}
