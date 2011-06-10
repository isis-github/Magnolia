/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.renderer.registry;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObservedManager for {@link Renderer} configured in repository.
 */
public class ConfiguredRendererManager extends ObservedManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<String> registeredIds = new HashSet<String>();
    private RendererRegistry registry;

    public ConfiguredRendererManager(RendererRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void onRegister(Content node) {
        // TODO use the jcr api

        for (Content rendererNode : node.getChildren(ItemType.CONTENTNODE)) {

            final String id = rendererNode.getName();

            synchronized (registeredIds) {
                try {
                    ConfiguredRendererProvider provider = new ConfiguredRendererProvider(rendererNode);
                    registry.registerRenderer(id, provider);
                    this.registeredIds.add(id);
                } catch (IllegalStateException e) {
                    log.error("Unable to register renderer [" + id + "]", e);
                }
            }
        }
    }

    @Override
    protected void onClear() {
        synchronized (registeredIds) {
            for (String id : registeredIds) {
                registry.unregister(id);
            }
            this.registeredIds.clear();
        }
    }
}