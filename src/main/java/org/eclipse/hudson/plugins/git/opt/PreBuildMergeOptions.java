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
package org.eclipse.hudson.plugins.git.opt;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.transport.RemoteConfig;

/**
 * Git SCM can optionally perform a merge with another branch (possibly another repository.)
 * <p/>
 * This object specifies that configuration.
 */
public class PreBuildMergeOptions implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Remote repository that contains the {@linkplain #mergeTarget ref}.
     */
    public RemoteConfig mergeRemote = null;

    /**
     * Remote ref to merge.
     */
    public String mergeTarget = null;

    public RemoteConfig getMergeRemote() {
        return mergeRemote;
    }

    public void setMergeRemote(RemoteConfig mergeRemote) {
        this.mergeRemote = mergeRemote;
    }

    public String getMergeTarget() {
        return mergeTarget;
    }

    public void setMergeTarget(String mergeTarget) {
        this.mergeTarget = mergeTarget;
    }

    public String getRemoteBranchName() {
        return mergeRemote.getName() + "/" + mergeTarget;
    }

    public boolean doMerge() {
        return mergeTarget != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PreBuildMergeOptions that = (PreBuildMergeOptions) o;

        return new EqualsBuilder()
            .append(mergeRemote, that.mergeRemote)
            .append(mergeTarget, that.mergeTarget)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(mergeRemote)
            .append(mergeTarget)
            .toHashCode();
    }
}
