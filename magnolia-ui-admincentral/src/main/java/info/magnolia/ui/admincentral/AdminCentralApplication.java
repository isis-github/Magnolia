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
package info.magnolia.ui.admincentral;

import java.lang.reflect.Type;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.AbstractAdapter;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.pico.PicoComponentProvider;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.dialog.builder.VaadinDialogBuilder;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.admincentral.main.activity.MainActivityMapper;
import info.magnolia.ui.admincentral.navigation.NavigationView;
import info.magnolia.ui.admincentral.navigation.NavigationViewImpl;
import info.magnolia.ui.admincentral.navigation.action.NavigationActionFactory;
import info.magnolia.ui.admincentral.navigation.activity.NavigationActivity;
import info.magnolia.ui.admincentral.navigation.activity.NavigationActivityMapper;
import info.magnolia.ui.admincentral.tree.builder.TreeBuilder;
import info.magnolia.ui.admincentral.tree.builder.TreeBuilderProvider;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.model.menu.definition.MenuItemDefinitionImpl;
import info.magnolia.ui.model.navigation.registry.NavigationPermissionSchema;
import info.magnolia.ui.model.navigation.registry.NavigationPermissionSchemaImpl;
import info.magnolia.ui.vaadin.integration.shell.ShellImpl;

/**
 * Application class for AdminCentral. Provides a scoped IoC container and performs initialization of the UI.
 */
public class AdminCentralApplication extends Application implements HttpServletRequestListener {

    private static final long serialVersionUID = 5773744599513735815L;

    private PicoComponentProvider componentProvider;

    @Override
    public void init() {

        // Initialize the view first since ShellImpl depends on it being set up when it's constructor is called
        componentProvider.getComponent(AdminCentralView.class).init();

        // Now initialize the presenter to start up MVP
        componentProvider.getComponent(AdminCentralPresenter.class).init();
    }

    private void createComponentProvider() {

        PicoComponentProvider provider = (PicoComponentProvider) Components.getComponentProvider();
        PicoBuilder builder = new PicoBuilder(provider.getContainer()).withConstructorInjection().withCaching();

        MutablePicoContainer container = builder.build();

        componentProvider = new PicoComponentProvider(container, provider);
        Properties properties = new Properties();
        properties.put(DialogBuilder.class.getName(), VaadinDialogBuilder.class.getName());
        properties.put(MenuItemDefinition.class.getName(), MenuItemDefinitionImpl.class.getName());
        properties.put(TreeBuilderProvider.class.getName(), "/modules/admin-central/components/treeBuilderProvider");
        componentProvider.parseConfiguration(properties);

        // We use a pico adapter here to delay creation of the TreeBuilder instance until it is needed (compare to before when we created an instance here and set it in the container).
        container.addAdapter(new AbstractAdapter<TreeBuilder>(TreeBuilder.class, TreeBuilder.class) {

            public TreeBuilder getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {

                TreeBuilderProvider treeBuilderProvider = componentProvider.getComponent(TreeBuilderProvider.class);

                // TODO: getBuilder should take params user, device...
                return treeBuilderProvider.getBuilder();
            }

            public void verify(PicoContainer container) throws PicoCompositionException {
            }

            public String getDescriptor() {
                return "Adapter creating TreeBuilder instance(s) by calling TreeBuilderProvider";
            }
        });

        container.addComponent(ComponentProvider.class, componentProvider);

        container.addComponent(Application.class, this);
        container.addComponent(AdminCentralView.class, AdminCentralViewImpl.class);
        container.addComponent(AdminCentralPresenter.class, AdminCentralPresenter.class);
        container.addComponent(MainActivityMapper.class, MainActivityMapper.class);


        container.addComponent(DialogPresenter.class, DialogPresenter.class);

        container.addComponent(NavigationView.class, NavigationViewImpl.class);
        container.addComponent(NavigationPermissionSchema.class, NavigationPermissionSchemaImpl.class);
        container.addComponent(NavigationActivityMapper.class, NavigationActivityMapper.class);
        container.addComponent(NavigationActivity.class, NavigationActivity.class);

        container.addComponent(EventBus.class, SimpleEventBus.class);
        container.addComponent(Shell.class, ShellImpl.class);
        container.addComponent(PlaceController.class, PlaceController.class);
        container.addComponent(NavigationActionFactory.class, NavigationActionFactory.class);

        // TODO how do we find and register classes from other modules that will be used by AdminCentral
        // TODO maybe configured in the module descriptors with scopes specified
    }

    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        if (componentProvider == null)
            createComponentProvider();

        // TODO keeping scopes in ThreadLocal is not necessary if we allow components to have their ComponentProvider injected, expect that since Content2Bean is a static service it needs to get it this way which is a shame..

        Components.pushScope(componentProvider);
    }

    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        Components.popScope(componentProvider);
    }
}
