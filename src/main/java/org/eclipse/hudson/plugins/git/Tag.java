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

import org.eclipse.jgit.lib.ObjectId;

public class Tag extends GitObject {
    private static final long serialVersionUID = 1L;
    public String commitSHA1;
    public String commitMessage;

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public Tag(String name, ObjectId sha1) {
        super(name, sha1);
    }

    /**
     * Get the sha1 of the commit associated with this tag
     */
    public String getCommitSHA1() {
        return commitSHA1;
    }

    public void setCommitSHA1(String commitSHA1) {
        this.commitSHA1 = commitSHA1;
    }
}
