/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.jcr.util.JCRVersionUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.predicate.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link ContentFilter} using a {@link Rule}.
 * @version $Revision: 41135 $ ($Author: gjoseph $)
 */
public class RuleBasedNodePredicate implements Predicate {

    private static Logger log = LoggerFactory.getLogger(RuleBasedNodePredicate.class);

    /**
     * Rule on which this filter works.
     */
    private final Rule rule;

    public RuleBasedNodePredicate(Rule rule) {
        this.rule = rule;
    }

    @Override
    public boolean evaluate(Object object) {
        if (!(object instanceof Node)) {
            return false;
        }
        Node content = (Node) object;
        String nodeType = "";
        try {
            nodeType = JCRVersionUtil.getNodeTypeName(content);
        }
        catch (RepositoryException re) {
            if (log.isDebugEnabled()) {
                log.debug("failed to retrieve node type : " + re.getMessage(), re);
            }
        }
        return this.rule.isAllowed(nodeType);
    }

}
