/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.jcr.wrapper;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.decoration.NodePredicateContentDecorator;
import info.magnolia.jcr.predicate.AbstractPredicate;

/**
 * ContentDecorator that hides content based on a multi value property called channels.
 *
 * @version $Id$
 */
public class ChannelVisibilityContentDecorator extends NodePredicateContentDecorator {

    private static class ChannelVisibilityPredicate extends AbstractPredicate<Node> {

        public static final String CHANNEL_PROPERTY_NAME = "channels";

        private final String currentChannel;

        private ChannelVisibilityPredicate(String currentChannel) {
            this.currentChannel = currentChannel;
        }

        @Override
        public boolean evaluateTyped(Node node) {
            try {
                if (currentChannel == null || currentChannel.equalsIgnoreCase("all")) {
                    return true;
                }
                if (node.hasProperty(CHANNEL_PROPERTY_NAME)) {
                    Property channel = node.getProperty(CHANNEL_PROPERTY_NAME);
                    if (channel.isMultiple()) {
                        Value[] values = channel.getValues();
                        if (values.length == 0) {
                            return true;
                        }
                        for (Value value : values) {
                            if (value.getString().equals(currentChannel)) {
                                return true;
                            }
                        }
                        return false;
                    }
                }
                return true;

            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
    }

    public ChannelVisibilityContentDecorator(String currentChannel) {
        super(new ChannelVisibilityPredicate(currentChannel));
    }
}