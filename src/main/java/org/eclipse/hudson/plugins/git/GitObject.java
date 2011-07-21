/*******************************************************************************
 *
 * Copyright (c) 2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * Andrew Bayer, Anton Kozak, Nikita Levyankov
 *
 *******************************************************************************/
package org.eclipse.hudson.plugins.git;

import java.io.Serializable;
import org.eclipse.jgit.lib.ObjectId;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 999)
public class GitObject implements Serializable {

    private static final long serialVersionUID = 1L;

    ObjectId sha1;
    String name;

    public GitObject(String name, ObjectId sha1) {
        this.name = name;
        this.sha1 = sha1;
    }

    public ObjectId getSHA1() {
        return sha1;
    }

    @Exported
    public String getName() {
        return name;
    }

    @Exported(name = "SHA1")
    public String getSHA1String() {
        return sha1.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GitObject gitObject = (GitObject) o;

        if (name != null ? !name.equals(gitObject.name) : gitObject.name != null) {
            return false;
        }
        if (sha1 != null ? !sha1.equals(gitObject.sha1) : gitObject.sha1 != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = sha1 != null ? sha1.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
