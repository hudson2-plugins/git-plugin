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

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import java.util.Collections;
import java.util.List;

/**
 * List of changeset that went into a particular build.
 *
 * @author Nigel Magnay
 * @author Nikita Levyankov
 */
public class GitChangeSetList extends ChangeLogSet<GitChangeSet> {
    private final List<GitChangeSet> changeSets;

    /*package*/ GitChangeSetList(AbstractBuild build, List<GitChangeSet> logs) {
        super(build);
        Collections.reverse(logs);  // put new things first
        this.changeSets = Collections.unmodifiableList(logs);
        for (GitChangeSet log : logs) {
            log.setParent(this);
        }
    }

    public List<GitChangeSet> getLogs() {
        return changeSets;
    }
}
