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
package info.magnolia.templating.editor.client;


import static info.magnolia.templating.editor.client.PageEditor.getDictionary;
import info.magnolia.templating.editor.client.dom.CMSComment;
import info.magnolia.templating.editor.client.jsni.LegacyJavascript;
import info.magnolia.templating.editor.client.model.ModelStorage;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * Page bar. The HTML output by this widget contains an empty <code>span</code> element with an id called <code>mgnlMainbarPlaceholder</code> as a convenience which can be used by other modules to inject
 * their own DOM elements into the main bar, <strong>once the page editor is loaded</strong>.
 * <p>I.e., assuming usage of jQuery, a module's own javascript could do something like this
 * <p>
 * {@code
 *  jQuery('#mgnlMainbarPlaceholder').append('<p>Blah</p>')
 * }
 * <p>The placeholder is styled to be automatically centered in the main bar. See this module's styles.css file (id selector #mgnlMainbarPlaceholder).
 */
public class PageBarWidget extends AbstractBarWidget {

    private PageEditor pageEditor;

    private String workspace;
    private String path;
    private String dialog;
    private boolean previewMode = false;

    public PageBarWidget(final PageEditor pageEditor, final CMSComment comment) {
        super(null, null);
        this.pageEditor = pageEditor;

        String content = comment.getAttribute("content");
        int i = content.indexOf(':');
        this.workspace = content.substring(0, i);
        this.path = content.substring(i + 1);
        this.dialog = comment.getAttribute("dialog");

        if(LegacyJavascript.isPreviewMode()){
            createPreviewModeBar();
        } else {
            createAuthoringModeBar();
        }

        addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                ModelStorage.getInstance().getFocusModel().reset();
            }
        }, MouseUpEvent.getType());
    }

    private void createAuthoringModeBar() {
        //the placeholder span must be added to the DOM bar BEFORE the other elements so that the style (named after its id) applied to it centers it correctly.
        InlineLabel mainbarPlaceholder = new InlineLabel();
        mainbarPlaceholder.getElement().setId("mgnlMainbarPlaceholder");
        mainbarPlaceholder.setStylePrimaryName("mgnlMainbarPlaceholder");
        add(mainbarPlaceholder);

        Button properties = new Button(getDictionary().get("buttons.properties.js"));
        properties.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.openDialog(dialog, workspace, path, null, null);
            }
        });
        addButton(properties, Float.RIGHT);

        Button preview = new Button(getDictionary().get("buttons.preview.js"));
        preview.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.preview(true);
            }
        });
        addButton(preview, Float.LEFT);

        Button adminCentral = new Button(getDictionary().get("buttons.admincentral.js"));
        adminCentral.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.showTree(workspace, path);
            }
        });
        addButton(adminCentral, Float.LEFT);

        setClassName("mgnlMainbar mgnlControlBar");

        previewMode = false;
    }

    private void createPreviewModeBar() {
        Button preview = new Button(getDictionary().get("buttons.preview.hidden.js"));
        preview.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.preview(false);
            }
        });
        preview.getElement().getStyle().setTop(4.0, Unit.PX);
        preview.getElement().getStyle().setLeft(4.0, Unit.PX);
        preview.getElement().getStyle().setBackgroundColor("#9DB517");
        addButton(preview, Float.LEFT);
        //bar has to show up on the left hand side
        getStyle().setTop(0.0, Unit.PX);
        getStyle().setLeft(0.0, Unit.PX);
        setClassName("mgnlMainbarPreview");

        previewMode = true;
    }

    public final boolean isPreviewMode() {
        return previewMode;
    }
}
