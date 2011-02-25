/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admincentral.editor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.module.admincentral.RuntimeRepositoryException;
import info.magnolia.module.admincentral.control.DateControl;
import info.magnolia.module.admincentral.control.EditControl;
import info.magnolia.module.admincentral.control.RichTextControl;
import info.magnolia.module.admincentral.dialog.DialogControl;
import info.magnolia.module.admincentral.dialog.DialogDefinition;
import info.magnolia.module.admincentral.dialog.DialogTab;

/**
 * Automates editing of entities defined by the content model.
 *
 * @author tmattsson
 */
public class ContentDriver extends AbstractDriver<Node> {

    /**
     * Describes a connection between an editor and a property on the entity.
     *
     * @author tmattsson
     */
    private static class EditorMapping {

        private String name;
        private Editor editor;
        private Class<?> type;

        public EditorMapping(String name, Editor editor, Class<?> type) {
            this.name = name;
            this.editor = editor;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Editor getEditor() {
            return editor;
        }

        public Class<?> getType() {
            return type;
        }
    }

    private List<EditorMapping> editorMappings = new ArrayList<EditorMapping>();

    public void initialize(DialogBuilder builder, DialogDefinition dialogDefinition) {

        for (DialogTab dialogTab : dialogDefinition.getTabs()) {

            builder.addTab(dialogTab.getLabel(), dialogTab.getLabel());

            for (DialogControl dialogControl : dialogTab.getFields()) {

                // TODO passing the type to the builder is not enough, it also needs to be give more explicit instructions like 'richText' and things like options.

                Class<?> type = getTypeFromDialogControl(dialogControl);

                // TODO for now we just skip things that we dont know about, later on we should fail instead
                if (type == null)
                    continue;

                Editor editor = builder.addField(
                        dialogTab.getLabel(),
                        dialogControl.getName(),
                        dialogControl.getLabel(),
                        dialogControl.getDescription(),
                        type);

                editorMappings.add(new EditorMapping(dialogControl.getName(), editor, type));
            }
        }
    }

    private Class<?> getTypeFromDialogControl(DialogControl dialogControl) {
        if (dialogControl instanceof EditControl)
            return String.class;
        if (dialogControl instanceof DateControl)
            return Calendar.class;
        if (dialogControl instanceof RichTextControl)
            return String.class;
        return null;
//        throw new IllegalArgumentException("Unsupported type " + dialogControl.getClass());
    }

    public void edit(Node node) {

        // TODO default values should also be set here

        try {
            for (EditorMapping mapping : editorMappings) {
                String name = mapping.getName();
                Editor editor = mapping.getEditor();
                Class<?> type = mapping.getType();
                if (type.equals(String.class)) {
                    if (node.hasProperty(name))
                        editor.setValue(node.getProperty(name).getString());
                } else if (type.equals(Calendar.class)) {
                    if (node.hasProperty(name))
                        editor.setValue(node.getProperty(name).getDate());
                }
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void flush(Node node) {
        try {
            for (EditorMapping mapping : editorMappings) {
                String name = mapping.getName();
                Editor editor = mapping.getEditor();
                Class<?> type = mapping.getType();
                if (type.equals(String.class)) {
                    node.setProperty(name, (String) editor.getValue());
                } else if (type.equals(Calendar.class)) {
                    node.setProperty(name, (Calendar) editor.getValue());
                }
            }
            node.getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public boolean hasErrors() {
        return false;
    }

    public List<EditorError> getErrors() {
        return null;
    }
}