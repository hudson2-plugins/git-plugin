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
package org.eclipse.hudson.plugins.git.util;

import hudson.Extension;
import hudson.model.TaskListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.hudson.plugins.git.Branch;
import org.eclipse.hudson.plugins.git.BranchSpec;
import org.eclipse.hudson.plugins.git.GitException;
import org.eclipse.hudson.plugins.git.GitSCM;
import org.eclipse.hudson.plugins.git.IGitAPI;
import org.eclipse.hudson.plugins.git.Revision;
import org.eclipse.jgit.lib.ObjectId;
import org.kohsuke.stapler.DataBoundConstructor;

import static java.util.Collections.emptyList;

public class DefaultBuildChooser extends BuildChooser {
    @DataBoundConstructor
    public DefaultBuildChooser() {
    }

    /**
     * Determines which Revisions to build.
     * <p/>
     * If only one branch is chosen and only one repository is listed, then
     * just attempt to find the latest revision number for the chosen branch.
     * <p/>
     * If multiple branches are selected or the branches include wildcards, then
     * use the advanced usecase as defined in the getAdvancedCandidateRevisons
     * method.
     *
     * @throws IOException
     * @throws GitException
     */
    public Collection<Revision> getCandidateRevisions(boolean isPollCall, String singleBranch,
                                                      IGitAPI git, TaskListener listener, BuildData data)
        throws GitException, IOException {

        verbose(listener, "getCandidateRevisions({0},{1},,,{2}) considering branches to build", isPollCall,
            singleBranch, data);

        // if the branch name contains more wildcards then the simple usecase
        // does not apply and we need to skip to the advanced usecase
        if (singleBranch == null || singleBranch.contains("*")) {
            return getAdvancedCandidateRevisions(listener, new GitUtils(listener, git), data);
        }

        // check if we're trying to build a specific commit
        // this only makes sense for a build, there is no
        // reason to poll for a commit
        if (!isPollCall && singleBranch.matches("[0-9a-f]{6,40}")) {
            try {
                ObjectId sha1 = git.revParse(singleBranch);
                Revision revision = new Revision(sha1);
                revision.getBranches().add(new Branch("detached", sha1));
                verbose(listener, "Will build the detached SHA1 {0}", sha1);
                return Collections.singletonList(revision);
            } catch (GitException e) {
                // revision does not exist, may still be a branch
                // for example a branch called "badface" would show up here
                verbose(listener, "Not a valid SHA1 {0}", singleBranch);
            }
        }

        // if it doesn't contain '/' then it could be either a tag or an unqualified branch
        if (!singleBranch.contains("/")) {
            // the 'branch' could actually be a tag:
            Set<String> tags = git.getTagNames(singleBranch);
            if (tags.size() == 0) {
                // its not a tag, so lets fully qualify the branch
                String repository = gitSCM.getRepositories().get(0).getName();
                singleBranch = repository + "/" + singleBranch;
                verbose(listener, "{0} is not a tag. Qualifying with the repository {1} a a branch", singleBranch,
                    repository);
            }
        }

        try {
            ObjectId sha1 = git.revParse(singleBranch);
            verbose(listener, "rev-parse {0} -> {1}", singleBranch, sha1);

            // if polling for changes don't select something that has
            // already been built as a build candidate
            if (isPollCall && data.hasBeenBuilt(sha1)) {
                verbose(listener, "{0} has already been built", sha1);
                return emptyList();
            }

            verbose(listener, "Found a new commit {0} to be built on {1}", sha1, singleBranch);

            Revision revision = new Revision(sha1);
            revision.getBranches().add(new Branch(singleBranch, sha1));
            return Collections.singletonList(revision);
        } catch (GitException e) {
            // branch does not exist, there is nothing to build
            verbose(listener, "Failed to rev-parse: {0}", singleBranch);
            return emptyList();
        }
    }

    /**
     * In order to determine which Revisions to build.
     * <p/>
     * Does the following :
     * 1. Find all the branch revisions
     * 2. Filter out branches that we don't care about from the revisions.
     * Any Revisions with no interesting branches are dropped.
     * 3. Get rid of any revisions that are wholly subsumed by another
     * revision we're considering.
     * 4. Get rid of any revisions that we've already built.
     * <p/>
     * NB: Alternate BuildChooser implementations are possible - this
     * may be beneficial if "only 1" branch is to be built, as much of
     * this work is irrelevant in that usecase.
     *
     * @throws IOException
     * @throws GitException
     */
    private Collection<Revision> getAdvancedCandidateRevisions(TaskListener listener,
                                                               GitUtils utils, BuildData data)
        throws GitException, IOException {
        // 1. Get all the (branch) revisions that exist
        Collection<Revision> revs = utils.getAllBranchRevisions();
        verbose(listener, "Starting with all the branches: {0}", revs);

        // 2. Filter out any revisions that don't contain any branches that we
        // actually care about (spec)
        for (Iterator<Revision> i = revs.iterator(); i.hasNext();) {
            Revision r = i.next();

            // filter out uninteresting branches
            for (Iterator<Branch> j = r.getBranches().iterator(); j.hasNext();) {
                Branch b = j.next();
                boolean keep = false;
                for (BranchSpec bspec : gitSCM.getBranches()) {
                    if (bspec.matches(b.getName())) {
                        keep = true;
                        break;
                    }
                }

                if (!keep) {
                    verbose(listener, "Ignoring {0} because it doesn't match branch specifier", b);
                    j.remove();
                }
            }

            if (r.getBranches().size() == 0) {
                verbose(listener, "Ignoring {0} because we don't care about any of the branches that point to it", r);
                i.remove();
            }
        }

        verbose(listener, "After branch filtering: {0}", revs);

        // 3. We only want 'tip' revisions
        revs = utils.filterTipBranches(revs);
        verbose(listener, "After non-tip filtering: {0}", revs);

        // 4. Finally, remove any revisions that have already been built.
        verbose(listener, "Removing what's already been built: {0}", data.getBuildsByBranchName());
        for (Iterator<Revision> i = revs.iterator(); i.hasNext();) {
            Revision r = i.next();

            if (data.hasBeenBuilt(r.getSha1())) {
                i.remove();
            }
        }
        verbose(listener, "After filtering out what's already been built: {0}", revs);
        return revs;
    }

    /**
     * Write the message to the listener only when the verbose mode is on.
     */
    private void verbose(TaskListener listener, String format, Object... args) {
        if (GitSCM.VERBOSE) {
            listener.getLogger().println(MessageFormat.format(format, args));
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildChooserDescriptor {
        @Override
        public String getDisplayName() {
            return "Default";
        }

        @Override
        public String getLegacyId() {
            return "Default";
        }
    }
}
