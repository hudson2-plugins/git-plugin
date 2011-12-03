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

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.ParametersAction;
import hudson.model.StreamBuildListener;
import hudson.model.TaskListener;
import hudson.slaves.NodeProperty;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.hudson.plugins.git.Branch;
import org.eclipse.hudson.plugins.git.BranchSpec;
import org.eclipse.hudson.plugins.git.GitException;
import org.eclipse.hudson.plugins.git.GitSCM;
import org.eclipse.hudson.plugins.git.IGitAPI;
import org.eclipse.hudson.plugins.git.Revision;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.RemoteConfig;

public class GitUtils {

    public static final String DEFAULD_REPO_NAME = Constants.DEFAULT_REMOTE_NAME;

    IGitAPI git;
    TaskListener listener;

    public GitUtils(TaskListener listener, IGitAPI git) {
        this.git = git;
        this.listener = listener;
    }

    /**
     * Return a list of "Revisions" - where a revision knows about all the branch names that refer to
     * a SHA1.
     *
     * @return
     * @throws IOException
     * @throws GitException
     */
    public Collection<Revision> getAllBranchRevisions() throws GitException, IOException {
        Map<ObjectId, Revision> revisions = new HashMap<ObjectId, Revision>();
        List<Branch> branches = git.getRemoteBranches();
        for (Branch b : branches) {
            Revision r = revisions.get(b.getSHA1());
            if (r == null) {
                r = new Revision(b.getSHA1());
                revisions.put(b.getSHA1(), r);
            }
            r.getBranches().add(b);
        }
        return revisions.values();
    }

    /**
     * Return the revision containing the branch name.
     *
     * @param branchName
     * @return
     * @throws IOException
     * @throws GitException
     */
    public Revision getRevisionContainingBranch(String branchName) throws GitException, IOException {
        for (Revision revision : getAllBranchRevisions()) {
            for (Branch b : revision.getBranches()) {
                if (b.getName().equals(branchName)) {
                    return revision;
                }
            }
        }
        return null;
    }

    public Revision getRevisionForSHA1(ObjectId sha1) throws GitException, IOException {
        for (Revision revision : getAllBranchRevisions()) {
            if (revision.getSha1().equals(sha1)) {
                return revision;
            }
        }
        return null;
    }

    /**
     * Return a list of 'tip' branches (I.E. branches that aren't included entirely within another branch).
     *
     * @param git
     * @return
     */
    public Collection<Revision> filterTipBranches(Collection<Revision> revisions) {
        // If we have 3 branches that we might want to build
        // ----A--.---.--- B
        //        \-----C

        // we only want (B) and (C), as (A) is an ancestor (old).

        List<Revision> l = new ArrayList<Revision>(revisions);

        OUTER:
        for (int i = 0; i < l.size(); i++) {
            for (int j = i + 1; j < l.size(); j++) {
                Revision ri = l.get(i);
                Revision rj = l.get(j);
                ObjectId commonAncestor = git.mergeBase(ri.getSha1(), rj.getSha1());
                if (commonAncestor == null) {
                    continue;
                }

                if (commonAncestor.equals(ri.getSha1())) {
                    LOGGER.fine("filterTipBranches: " + rj + " subsumes " + ri);
                    l.remove(i);
                    i--;
                    continue OUTER;
                }
                if (commonAncestor.equals(rj.getSha1())) {
                    LOGGER.fine("filterTipBranches: " + ri + " subsumes " + rj);
                    l.remove(j);
                    j--;
                }
            }
        }

        return l;
    }

    /**
     * An attempt to generate at least semi-useful EnvVars for polling calls, based on previous build.
     * Cribbed from various places.
     */
    public static EnvVars getPollEnvironment(AbstractProject p, FilePath ws, Launcher launcher, TaskListener listener)
        throws IOException, InterruptedException {
        EnvVars env;

        AbstractBuild b = (AbstractBuild) p.getLastBuild();

        if (b != null) {
            Node lastBuiltOn = b.getBuiltOn();

            if (lastBuiltOn != null) {
                env = lastBuiltOn.toComputer().getEnvironment().overrideAll(b.getCharacteristicEnvVars());
            } else {
                env = new EnvVars(System.getenv());
            }

            String rootUrl = Hudson.getInstance().getRootUrl();
            if (rootUrl != null) {
                env.put("HUDSON_URL", rootUrl);
                env.put("BUILD_URL", rootUrl + b.getUrl());
                env.put("JOB_URL", rootUrl + p.getUrl());
            }

            if (!env.containsKey("HUDSON_HOME")) {
                env.put("HUDSON_HOME", Hudson.getInstance().getRootDir().getPath());
            }

            if (ws != null) {
                env.put("WORKSPACE", ws.getRemote());
            }


            p.getScm().buildEnvVars(b, env);

            StreamBuildListener buildListener = new StreamBuildListener((OutputStream) listener.getLogger());

            for (NodeProperty nodeProperty : Hudson.getInstance().getGlobalNodeProperties()) {
                Environment environment = nodeProperty.setUp(b, launcher, (BuildListener) buildListener);
                if (environment != null) {
                    environment.buildEnvVars(env);
                }
            }

            if (lastBuiltOn != null) {
                for (NodeProperty nodeProperty : lastBuiltOn.getNodeProperties()) {
                    Environment environment = nodeProperty.setUp(b, launcher, buildListener);
                    if (environment != null) {
                        environment.buildEnvVars(env);
                    }
                }
            }

            EnvVars.resolve(env);
        } else {
            env = new EnvVars(System.getenv());
        }

        return env;
    }

    /**
     * Builds branch envvars. If several branches or repos are present, env key will be generated based on pattern:
     * GIT_BRANCH_$repoIdx_$branchIdx. If there is only one branch and repo - {@link GitSCM#GIT_BRANCH} will be used.
     *
     * @param build {@link AbstractBuild}
     * @param env environment variables.
     * @param repositories list of repos.
     * @param branches list of {@link BranchSpec}
     */
    public static void buildBranchEnvVar(AbstractBuild<?, ?> build, Map<String, String> env,
                                         List<RemoteConfig> repositories, List<BranchSpec> branches) {
        if (CollectionUtils.isNotEmpty(repositories) && CollectionUtils.isNotEmpty(branches)) {
            int branchesSize = branches.size();
            int repoSize = repositories.size();
            if (1 == branchesSize && 1 == repoSize) {
                String branch = getSingleBranch(build, repositories, branches);
                if (branch != null) {
                    env.put(GitSCM.GIT_BRANCH, branch);
                }
            } else if (branchesSize > 1 || repoSize > 1) {
                int repoIdx = 1;
                int branchIdx = 1;
                for (RemoteConfig repo : repositories) {
                    for (BranchSpec branchSpec : branches) {
                        // replace repository wildcard with repository name
                        String branchName = buildBranch(build, repo.getName(), branchSpec.getName());
                        if (null != branchName) {
                            String key = GitSCM.GIT_BRANCH + "_" + repoIdx + "_" + branchIdx;
                            env.put(key, repo.getName() + "/" + branchName);
                            branchIdx++;
                        }
                    }
                    repoIdx++;
                    branchIdx = 1;
                }
            }
        }
    }

    /**
     * If the configuration is such that we are tracking just one branch of one repository
     * return that branch specifier (in the form of something like "origin/master"
     * <p/>
     * Otherwise return null.
     */
    public static String getSingleBranch(AbstractBuild<?, ?> build, List<RemoteConfig> repositories,
                                         List<BranchSpec> branches) {
        // if we have multiple branches skip to advanced usecase
        if (branches.size() != 1 || repositories.size() != 1) {
            return null;
        }

        String branch = branches.get(0).getName();
        String repository = repositories.get(0).getName();
        return buildBranch(build, repository, branch);
    }

    /**
     * Builds branch based on params.Applied build params if any. Returns null if branch contains *,
     *
     * @param build {@link AbstractBuild}
     * @param repository repo name
     * @param branchName branch name
     * @return branch name
     */
    public static String buildBranch(AbstractBuild<?, ?> build, String repository, String branchName) {
        // replace repository wildcard with repository name
        if (branchName.startsWith("*/")) {
            branchName = repository + branchName.substring(1);
        }

        // if the branchName name contains more wildcards then the simple usecase
        // does not apply and we need to skip to the advanced usecase
        if (branchName.contains("*")) {
            return null;
        }

        // substitute build parameters if available
        ParametersAction parameters = build.getAction(ParametersAction.class);
        if (parameters != null) {
            branchName = parameters.substitute(build, branchName);
        }
        return branchName;
    }

    public static String[] fixupNames(String[] names, String[] urls) {
        String[] returnNames = new String[urls.length];
        Set<String> usedNames = new HashSet<String>();

        for (int i = 0; i < urls.length; i++) {
            String name = names[i];

            if (name == null || name.trim().length() == 0) {
                name = DEFAULD_REPO_NAME;
            }

            String baseName = name;
            int j = 1;
            while (usedNames.contains(name)) {
                name = baseName + (j++);
            }

            usedNames.add(name);
            returnNames[i] = name;
        }


        return returnNames;
    }

    /**
     * Verifies whether array of the repository Urls is empty.
     *
     * @param urls repository Urls.
     * @return true if array is empty.
     */
    public static boolean isEmpty(String[] urls) {
        if (ArrayUtils.isEmpty(urls)) {
            return true;
        }
        for (String url : urls) {
            if (StringUtils.isNotEmpty(url)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies if first collection equals second without ordering.
     * @param c1 collection
     * @param c2 collection
     * @return true if collections equal.
     */
    public static boolean isEqualCollection(Collection c1, Collection c2) {
        if (c1 == c2) {
            return true;
        }
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null || c2 == null) {
            return false;
        }
        return CollectionUtils.isEqualCollection(c1, c2);
    }

    /**
     * Verifies if first array equals second without ordering.
     * @param o1 array
     * @param o2 array
     * @return true if arrays equal.
     */
    public static boolean isEqualArray(Object[] o1, Object[] o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return CollectionUtils.isEqualCollection(Arrays.asList(o1), Arrays.asList(o2));
    }

    private static final Logger LOGGER = Logger.getLogger(GitUtils.class.getName());
}
