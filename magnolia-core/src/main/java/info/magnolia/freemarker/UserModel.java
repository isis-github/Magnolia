/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.freemarker;

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.cms.security.User;

/**
 * Exposes User instances to freemarker in such a way that getter methods are tried first,
 * then getProperty() is used. ie ${user.name} internally calls the getName() method,
 * and ${user.fooBar} will eventually return the value of user.getProperty("fooBar").
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class UserModel extends BeanModel {
    private final User user;

    UserModel(User user, MagnoliaObjectWrapper wrapper) {
        super(user, wrapper);
        this.user = user;
    }

    public TemplateModel get(String key) throws TemplateModelException {
        final TemplateModel result = super.get(key);
        if (result != null) {
            return result;
        }
        return new SimpleScalar(user.getProperty(key));
    }

    public User asUser() {
        return this.user;
    }
}
