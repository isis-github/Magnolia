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
package info.magnolia.module.admincentral.dialog.editor;

import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AdminCentral Page/Section/View (main container content) for testing dialog stuff, temporary stuff.
 */
public class DraftDialogEditorPage extends FormLayout {

    private static final Logger log = LoggerFactory.getLogger(DraftDialogEditorPage.class);

    public DraftDialogEditorPage() {

        setMargin(true);
        addComponent(new Label("Please sit down before pressing New Dialog button."));
        addComponent(new Label("  "));
        final TextField dialogName = new TextField();
        dialogName.setRequired(true);
        dialogName.setRequiredError("please specify dialog name");
        dialogName.setMaxLength(500);
        dialogName.setInputPrompt("Dialog Name");
        dialogName.setRows(1);
        dialogName.setValue("foo");
        addComponent(dialogName);

        // TODO: combo of all existing modules
        final TextField parentModule = new TextField();
        parentModule.setRequired(true);
        parentModule.setRequiredError("please specify module under which the dialog should be defined");
        parentModule.setMaxLength(500);
        parentModule.setInputPrompt("Parent Module name:");
        parentModule.setRows(1);
        parentModule.setValue("samples");
        addComponent(parentModule);



        addComponent(new Button("New dialog", new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                getApplication().getMainWindow().addWindow(new EditDialogWindow((String) dialogName.getValue(), (String) parentModule.getValue()));
            }

        }));

        /*
        addComponent(new Button("Select paragraph", new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                getApplication().getMainWindow().addWindow(new SelectParagraphWindow((String) paragraphs.getValue(),
                        (String) repository.getValue(), (String) path.getValue(), (String) nodeCollectionName.getValue(), (String) nodeName.getValue()));
            }
        }));
        */
    }
}
