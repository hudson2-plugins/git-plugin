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

import hudson.model.InvisibleAction;
import java.io.Serializable;
import org.eclipse.jgit.lib.ObjectId;

/**
 * Used as a build parameter to specify the revision to be built.
 *
 * @author Kohsuke Kawaguchi
 */
public class RevisionParameterAction extends InvisibleAction implements Serializable {
    /**
     * SHA1, ref name, etc. that can be "git rev-parse"d into a specific commit.
     */
    public final String commit;

    public RevisionParameterAction(String commit) {
        this.commit = commit;
    }

    public Revision toRevision(IGitAPI git) {
        ObjectId sha1 = git.revParse(commit);
        Revision revision = new Revision(sha1);
        revision.getBranches().add(new Branch("detached", sha1));
        return revision;
    }

    @Override
    public String toString() {
        return super.toString() + "[commit=" + commit + "]";
    }

    private static final long serialVersionUID = 1L;
}

