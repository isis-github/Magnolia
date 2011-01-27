/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.tree;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Field;


/**
 * Base class for tree columns.
 * 
 * @param <E> type of the hosted values of this column.
 */
public abstract class TreeColumn<E> {

    private String label;

    private int width = 1;

    /**
     * @return Field used when editing this column. Defaults to null.
     */
    public Field getEditField(Node node) {
        // TODO: has to operate on vaadin's Item rather than on JCR-Node. Table could also contain
        // e.g. NodePropertyItems...
        return null;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Type of the column: Subclasses have to make sure the getValue methods return instances of
     * this type!
     */
    public abstract Class<E> getType();

    /**
     * @return value to be displayed in the corresponding column (from the provided Node)
     */
    public abstract Object getValue(Node node) throws RepositoryException;

    public int getWidth() {
        return width;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Set value of Property for the provided node to the new value.
     * 
     * @param node node to set property for
     * @param newValue value to set
     */
    public abstract void setValue(Node node, Object newValue) throws RepositoryException;

    public void setWidth(int width) {
        this.width = width;
    }

}
