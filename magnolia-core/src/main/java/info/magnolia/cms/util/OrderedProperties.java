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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

/**
 * Subclass of java.util.Properties which keeps the order in which properties were loaded,
 * by delegating to a LinkedHashMap.
 *
 * <strong>Warning:</strong> only the java.util.Map interface methods have been
 * overloaded, so be weary when using java.util.Properties specific methods. (load, save,
 * getProperty and setProperty are working.) (getProperty had to be explicitly overloaded
 * too)
 *
 * The equals() method respects the Map.equals() contract, since the entrySet() method is
 * delegating to the LinkedHashMap.
 *
 * @author philipp
 * @version $Id:  $
 */
public class OrderedProperties extends Properties {
    private final LinkedHashMap map = new LinkedHashMap();

    @Override
    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public String getProperty(String key) {
        return (String) get(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public Set entrySet() {
        return this.map.entrySet();
    }

    @Override
    public Set keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection values() {
        return this.map.values();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
}
