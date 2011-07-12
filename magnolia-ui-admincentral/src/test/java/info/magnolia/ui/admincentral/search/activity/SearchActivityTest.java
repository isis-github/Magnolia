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
package info.magnolia.ui.admincentral.search.activity;

import static org.mockito.Mockito.mock;
import info.magnolia.ui.admincentral.search.place.SearchPlace;
import info.magnolia.ui.admincentral.search.view.SearchParameters;
import info.magnolia.ui.admincentral.toolbar.view.FunctionToolbarView;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @version $Id$
 *
 */
public class SearchActivityTest{

    private SearchActivity searchActivity;
    private PlaceController placeController;
    private FunctionToolbarView view;
    private Shell shell;
    private SearchParameters params;

    @Before
    public void setUp() throws Exception {
        shell = mock(Shell.class);
        view = mock(FunctionToolbarView.class);
        EventBus eventBus = mock(EventBus.class);
        placeController = new PlaceController(eventBus, shell);
        params = new SearchParameters("website", "foo bar");
    }

    @Test
    public void testAfterOnStartSearchIsInvokedCurrentPlaceIsSearchPlace() throws Exception {
        //GIVEN
        searchActivity = new SearchActivity(view, placeController, shell);
        //WHEN
        searchActivity.onStartSearch(params);
        //THEN
        Assert.assertTrue(placeController.getWhere() instanceof SearchPlace);
    }
}