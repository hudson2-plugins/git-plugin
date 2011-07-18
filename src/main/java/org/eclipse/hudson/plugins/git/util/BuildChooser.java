/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Andrew Bayer, Anton Kozak, Nikita Levyankov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.eclipse.hudson.plugins.git.util;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import org.eclipse.hudson.plugins.git.GitException;
import org.eclipse.hudson.plugins.git.GitSCM;
import org.eclipse.hudson.plugins.git.IGitAPI;
import org.eclipse.hudson.plugins.git.Revision;

/**
 * Interface defining an API to choose which revisions ought to be
 * considered for building.
 * <p/>
 * <p/>
 * This object is persisted as a part of the project configuration.
 *
 * @author magnayn
 * @author Kohsuke Kawaguchi
 */
public abstract class BuildChooser implements ExtensionPoint, Describable<BuildChooser>, Serializable {
    /**
     * Refers back to the {@link GitSCM} that owns this build chooser.
     * Do not modify from outside {@link GitSCM}.
     */
    public transient GitSCM gitSCM;

    /**
     * Short-hand to get to the display name.
     */
    public final String getDisplayName() {
        return getDescriptor().getDisplayName();
    }

    /**
     * Get a list of revisions that are candidates to be built.
     * May be an empty set.
     *
     * @param isPollCall true if this method is called from pollChanges.
     * @param singleBranch contains the name of a single branch to be built
     * this will be non-null only in the simple case, in advanced
     * cases with multiple repositories and/or branches specified
     * then this value will be null.
     * @return the candidate revision.
     * @throws IOException
     * @throws GitException
     */
    public abstract Collection<Revision> getCandidateRevisions(boolean isPollCall, String singleBranch,
                                                               IGitAPI git, TaskListener listener, BuildData buildData)
        throws GitException, IOException;

    /**
     * What was the last SHA1 that a named branch was built with?
     * @param branch
     * @return ObjectId, or NULL
     */
    //Build getLastBuiltRevisionOfBranch(String branch);

    /**
     * What was the last revision to be built?
     *
     * @return
     */
    //public Revision getLastBuiltRevision();
    public BuildChooserDescriptor getDescriptor() {
        return (BuildChooserDescriptor) Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    /**
     * All the registered build choosers.
     */
    public static DescriptorExtensionList<BuildChooser, BuildChooserDescriptor> all() {
        return Hudson.getInstance()
            .<BuildChooser, BuildChooserDescriptor>getDescriptorList(BuildChooser.class);
    }

    public Build prevBuildForChangelog(String singleBranch, BuildData data, IGitAPI git) {
        return data == null ? null : data.getLastBuildOfBranch(singleBranch);
    }

    private static final long serialVersionUID = 1L;
}
