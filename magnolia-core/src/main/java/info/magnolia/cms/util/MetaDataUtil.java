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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.jcr.util.NodeTypes;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to work with {@link info.magnolia.cms.core.MetaData}.
 *
 * @deprecated since 5.0 - use {@link info.magnolia.jcr.util.NodeUtil} instead.
 */
public class MetaDataUtil {
    private static final Logger log = LoggerFactory.getLogger(MetaDataUtil.class);

    public static String getPropertyValueString(Content content, String propertyName) {
        return getPropertyValueString(content, propertyName, null);
    }

    /**
     * Returns the representation of the value as a String.
     * @return String
     */
    public static String getPropertyValueString(Content content, String propertyName, String dateFormat) {
        try {
            if (propertyName.equals(MetaData.CREATION_DATE) || propertyName.equals(MetaData.LAST_MODIFIED) || propertyName.equals(MetaData.LAST_ACTION)) {
                final Calendar date;
                if(propertyName.equals(MetaData.CREATION_DATE)){
                    date = content.getMetaData().getCreationDate();
                }
                else if(propertyName.equals(MetaData.LAST_MODIFIED)){
                    date = content.getMetaData().getModificationDate();
                }
                else if(propertyName.equals(MetaData.LAST_ACTION)){
                    date = content.getMetaData().getLastActionDate();
                }
                else{
                    date = content.getMetaData().getDateProperty(propertyName);
                }

                if(date != null){
                    return DateUtil.format(date.getTime(), dateFormat);
                }
            }
            else if (propertyName.equals(MetaData.ACTIVATED)) {
                return Boolean.toString(content.getMetaData().getBooleanProperty(propertyName));
            }
            else {
                return content.getMetaData().getStringProperty(propertyName);
            }
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * @deprecated since 4.5 - use {@link #getActivationStatusIcon(Node)} instead.
     */
    public static String getActivationStatusIcon(Content content) {
        return getActivationStatusIcon(content.getJCRNode());
    }

    /**
     * Return iconFileName for a node.
     *
     * @param node
     *            node to read status from
     * @return file name for an icon
     *
     *         TODO dlipp: move to ui-layer as soon as there's a ui-project on which magnolia-gui,
     *         magnolia-module-workflow and admincentral are based (all these currently use this method).
     */
    public static String getActivationStatusIcon(Node node) {

        int activationStatus;
        try {
            activationStatus = NodeTypes.ActivatableMixin.getActivationStatus(node);
        } catch (RepositoryException e) {
            activationStatus = NodeTypes.ActivatableMixin.ACTIVATION_STATUS_NOT_ACTIVATED;
        }

        switch (activationStatus) {
            case NodeTypes.ActivatableMixin.ACTIVATION_STATUS_MODIFIED:
                return "indicator_yellow.gif";
            case NodeTypes.ActivatableMixin.ACTIVATION_STATUS_ACTIVATED:
                return "indicator_green.gif";
            default:
                return "indicator_red.gif";
        }
    }
}
