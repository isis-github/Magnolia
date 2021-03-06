/**
 * This file Copyright (c) 2003-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.importexport.filters;

import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * SAX filter, strips version information from a JCR XML (system view).
 *
 * @version $Id$
 */
public class VersionFilter extends XMLFilterImpl {

    /**
     * if != 0 we are in the middle of a filtered element.
     */
    private int inVersionElement;

    /**
     * Instantiates a new version filter.
     * @param parent wrapped XMLReader
     */
    public VersionFilter(XMLReader parent) {
        super(parent);
    }

    public static String[] FILTERED_PROPERTIES = new String[]{//
        "jcr:predecessors", // version
        "jcr:baseVersion", // version
        "jcr:versionHistory", // version
        "jcr:isCheckedOut", // useless
        "jcr:created", // useless
        "mgnl:sequenceposition" // old
    };

    public static String[] FILTERED_NODES = new String[]{//
        "jcr:system", // jcr system
    };

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(String, String, String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (inVersionElement > 0) {
            inVersionElement--;
            return;
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // filter content
        if (inVersionElement == 0) {
            super.characters(ch, start, length);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        if (inVersionElement > 0) {
            inVersionElement++;
            return;
        }
        if ("sv:node".equals(qName)) {
            String attName = atts.getValue("sv:name");
            if (attName != null && ArrayUtils.contains(FILTERED_NODES, attName)) {
                inVersionElement++;
                return;
            }
        } else if ("sv:property".equals(qName)) {
            String attName = atts.getValue("sv:name");
            if (attName != null && ArrayUtils.contains(FILTERED_PROPERTIES, attName)) {
                inVersionElement++;
                return;
            }
        }

        super.startElement(uri, localName, qName, atts);
    }

}
