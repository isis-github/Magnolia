/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.objectfactory.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Configuration used to create component providers.
 *
 * @version $Id$
 */
public class ComponentProviderConfiguration implements Cloneable {

    private Map<Class<?>, Class<?>> typeMapping = new HashMap<Class<?>, Class<?>>();
    private Map<Class, ComponentConfiguration> components = new HashMap<Class, ComponentConfiguration>();
    private List<ComponentConfigurer> configurers = new ArrayList<ComponentConfigurer>();

    public void addComponent(ComponentConfiguration componentConfiguration) {
        this.components.put(componentConfiguration.getType(), componentConfiguration);
    }

    public void addTypeMapping(Class<?> from, Class<?> to) {
        this.typeMapping.put(from, to);
    }

    public Map<Class<?>, Class<?>> getTypeMapping() {
        return typeMapping;
    }

    public Map<Class, ComponentConfiguration> getComponents() {
        return components;
    }

    public List<ComponentConfigurer> getConfigurers() {
        return configurers;
    }

    public boolean addConfigurer(ComponentConfigurer configurer) {
        return configurers.add(configurer);
    }

    public <T> void registerImplementation(Class<T> type, Class<? extends T> implementation) {
        addComponent(ImplementationConfiguration.valueOf(type, implementation));
    }

    public <T> void registerImplementation(Class<T> type) {
        registerImplementation(type, type);
    }

    public <T> void registerInstance(Class<T> type, Object instance) {
        addComponent(InstanceConfiguration.valueOf(type, instance));
    }

    public void combine(ComponentProviderConfiguration components) {
        this.typeMapping.putAll(components.typeMapping);
        this.components.putAll(components.clone().components);
        this.configurers.addAll(components.configurers);
    }

    @Override
    public ComponentProviderConfiguration clone() {
        try {
            ComponentProviderConfiguration clone = (ComponentProviderConfiguration) super.clone();
            clone.components = new HashMap<Class, ComponentConfiguration>();
            for (Map.Entry<Class, ComponentConfiguration> entry : components.entrySet()) {
                clone.components.put(entry.getKey(), entry.getValue().clone());
            }
            clone.typeMapping = new HashMap<Class<?>, Class<?>>(typeMapping);
            clone.configurers = new ArrayList<ComponentConfigurer>(configurers);
            return clone;
        } catch (CloneNotSupportedException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }
}
