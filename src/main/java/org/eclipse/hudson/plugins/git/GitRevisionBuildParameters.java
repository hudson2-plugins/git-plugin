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

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import org.eclipse.hudson.plugins.git.util.BuildData;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Build parameter in the parameterized build trigger to pass the Git commit to the downstream build
 * (to do something else on the same commit.)
 *
 * @author Kohsuke Kawaguchi
 */
public class GitRevisionBuildParameters extends AbstractBuildParameters {
    @DataBoundConstructor
    public GitRevisionBuildParameters() {
    }

    @Override
    public Action getAction(AbstractBuild<?, ?> build, TaskListener listener) {
        BuildData data = build.getAction(BuildData.class);
        if (data == null) {
            listener.getLogger().println("This project doesn't use Git as SCM. Can't pass the revision to downstream");
            return null;
        }

        return new RevisionParameterAction(data.getLastBuiltRevision().getSha1String());
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<AbstractBuildParameters> {
        @Override
        public String getDisplayName() {
            return "Pass-through Git Commit that was built";
        }
    }
}

