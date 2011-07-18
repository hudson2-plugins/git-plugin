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

package org.eclipse.hudson.plugins.git;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Util;
import hudson.XmlFile;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Items;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.util.FormValidation;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.hudson.plugins.git.browser.GitRepositoryBrowser;
import org.eclipse.hudson.plugins.git.browser.GitWeb;
import org.eclipse.hudson.plugins.git.converter.ObjectIdConverter;
import org.eclipse.hudson.plugins.git.converter.RemoteConfigConverter;
import org.eclipse.hudson.plugins.git.opt.PreBuildMergeOptions;
import org.eclipse.hudson.plugins.git.util.Build;
import org.eclipse.hudson.plugins.git.util.BuildChooser;
import org.eclipse.hudson.plugins.git.util.BuildChooserDescriptor;
import org.eclipse.hudson.plugins.git.util.BuildData;
import org.eclipse.hudson.plugins.git.util.DefaultBuildChooser;
import org.eclipse.hudson.plugins.git.util.GitConstants;
import org.eclipse.hudson.plugins.git.util.GitUtils;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import static hudson.Util.fixEmptyAndTrim;

/**
 * Git SCM.
 *
 * @author Nigel Magnay
 */
public class GitSCM extends SCM implements Serializable {
    private static final String HUDSON_SCM_GITSCM_ALIAS_NAME = "hudson.plugins.git.GitSCM";
    private static final String HUDSON_SCM_GITSCM_DESCRIPTOR_ALIAS_NAME = HUDSON_SCM_GITSCM_ALIAS_NAME
        + "$DescriptorImpl";

    public static final String GIT_BRANCH = "GIT_BRANCH";
    public static final String GIT_COMMIT = "GIT_COMMIT";

    private static final long serialVersionUID = 1L;
    static final String DEFAULT_BRANCH = Constants.MASTER;

    // old fields are left so that old config data can be read in, but
    // they are deprecated. transient so that they won't show up in XML
    // when writing back
    @Deprecated
    transient String source;
    @Deprecated
    transient String branch;

    /**
     * Store a config version so we're able to migrate config on various
     * functionality upgrades.
     */
    private Long configVersion;

    /**
     * All the remote repositories that we know about.
     */
    private List<RemoteConfig> remoteRepositories;

    /**
     * All the branches that we wish to care about building.
     */
    private List<BranchSpec> branches;

    /**
     * Optional local branch to work on.
     */
    private String localBranch;

    /**
     * Options for merging before a build.
     */
    private PreBuildMergeOptions mergeOptions;

    /**
     * Use --recursive flag on submodule commands - requires git>=1.6.5
     */
    private boolean recursiveSubmodules;

    private boolean doGenerateSubmoduleConfigurations;
    private boolean authorOrCommitter;

    private boolean clean;
    private boolean wipeOutWorkspace;

    private boolean pruneBranches;

    /**
     * @deprecated Replaced by {@link #buildChooser} instead.
     */
    private transient String choosingStrategy;

    private BuildChooser buildChooser;

    private String gitTool = null;

    private GitRepositoryBrowser browser;

    private Collection<SubmoduleConfig> submoduleCfg;

    private String relativeTargetDir;

    private String excludedRegions;

    private String excludedUsers;

    private String gitConfigName;

    private String gitConfigEmail;

    private boolean skipTag;

    public Collection<SubmoduleConfig> getSubmoduleCfg() {
        return submoduleCfg;
    }

    public void setSubmoduleCfg(Collection<SubmoduleConfig> submoduleCfg) {
        this.submoduleCfg = submoduleCfg;
    }

    /**
     * A convenience constructor that sets everything to default.
     *
     * @param repositoryUrl Repository URL to clone from.
     * @throws java.io.IOException exception.
     */
    public GitSCM(String repositoryUrl) throws IOException, Descriptor.FormException {
        this(
            DescriptorImpl.createRepositoryConfigurations(new String[]{repositoryUrl}, new String[]{null},
                new String[]{null}),
            Collections.singletonList(new BranchSpec("")),
            new PreBuildMergeOptions(), false, Collections.<SubmoduleConfig>emptyList(), false,
            false, new DefaultBuildChooser(), null, null, false, null,
            null, null, null, false, false, null, null, false);
    }

    @DataBoundConstructor
    public GitSCM(
        List<RemoteConfig> repositories,
        List<BranchSpec> branches,
        PreBuildMergeOptions mergeOptions,
        boolean doGenerateSubmoduleConfigurations,
        Collection<SubmoduleConfig> submoduleCfg,
        boolean clean,
        boolean wipeOutWorkspace,
        BuildChooser buildChooser, GitRepositoryBrowser browser,
        String gitTool,
        boolean authorOrCommitter,
        String relativeTargetDir,
        String excludedRegions,
        String excludedUsers,
        String localBranch,
        boolean recursiveSubmodules,
        boolean pruneBranches,
        String gitConfigName,
        String gitConfigEmail,
        boolean skipTag) {

        // normalization
        this.branches = branches;

        this.localBranch = Util.fixEmptyAndTrim(localBranch);
        this.remoteRepositories = repositories;
        this.browser = browser;

        this.mergeOptions = mergeOptions;

        this.doGenerateSubmoduleConfigurations = doGenerateSubmoduleConfigurations;
        this.submoduleCfg = submoduleCfg;

        this.clean = clean;
        this.wipeOutWorkspace = wipeOutWorkspace;
        this.configVersion = 1L;
        this.gitTool = gitTool;
        this.authorOrCommitter = authorOrCommitter;
        this.buildChooser = buildChooser;
        this.relativeTargetDir = relativeTargetDir;
        this.excludedRegions = excludedRegions;
        this.excludedUsers = excludedUsers;
        this.recursiveSubmodules = recursiveSubmodules;
        this.pruneBranches = pruneBranches;
        this.gitConfigName = gitConfigName;
        this.gitConfigEmail = gitConfigEmail;
        this.skipTag = skipTag;
        buildChooser.gitSCM = this; // set the owner
    }

    public Object readResolve() {
        // Migrate data

        // Default unspecified to v0
        if (configVersion == null) {
            configVersion = 0L;
        }


        if (source != null) {
            remoteRepositories = new ArrayList<RemoteConfig>();
            branches = new ArrayList<BranchSpec>();
            doGenerateSubmoduleConfigurations = false;
            mergeOptions = new PreBuildMergeOptions();

            recursiveSubmodules = false;

            remoteRepositories.add(
                newRemoteConfig("origin", source, new RefSpec("+refs/heads/*:refs/remotes/origin/*")));
            if (branch != null) {
                branches.add(new BranchSpec(branch));
            } else {
                branches.add(new BranchSpec(DEFAULT_BRANCH));
            }
        }


        if (configVersion < 1 && branches != null) {
            // Migrate the branch specs from
            // single * wildcard, to ** wildcard.
            for (BranchSpec branchSpec : branches) {
                String name = branchSpec.getName();
                name = name.replace("*", "**");
                branchSpec.setName(name);
            }
        }

        if (mergeOptions.doMerge() && mergeOptions.getMergeRemote() == null) {
            mergeOptions.setMergeRemote(remoteRepositories.get(0));
        }

        if (choosingStrategy != null && buildChooser == null) {
            for (BuildChooserDescriptor d : BuildChooser.all()) {
                if (choosingStrategy.equals(d.getLegacyId())) {
                    try {
                        buildChooser = d.clazz.newInstance();
                    } catch (InstantiationException e) {
                        LOGGER.log(Level.WARNING, "Failed to instantiate the build chooser", e);
                    } catch (IllegalAccessException e) {
                        LOGGER.log(Level.WARNING, "Failed to instantiate the build chooser", e);
                    }
                }
            }
        }
        if (buildChooser == null) {
            buildChooser = new DefaultBuildChooser();
        }
        buildChooser.gitSCM = this;

        return this;
    }

    public String getExcludedRegions() {
        return excludedRegions;
    }

    public String[] getExcludedRegionsNormalized() {
        return (excludedRegions == null || excludedRegions.trim().equals(""))
            ? null : excludedRegions.split("[\\r\\n]+");
    }

    public String getExcludedUsers() {
        return excludedUsers;
    }

    public Set<String> getExcludedUsersNormalized() {
        String s = fixEmptyAndTrim(excludedUsers);
        if (s == null) {
            return Collections.emptySet();
        }

        Set<String> users = new HashSet<String>();
        for (String user : s.split("[\\r\\n]+")) {
            users.add(user.trim());
        }
        return users;
    }

    @Override
    public GitRepositoryBrowser getBrowser() {
        return browser;
    }

    public String getGitConfigName() {
        return gitConfigName;
    }

    public String getGitConfigEmail() {
        return gitConfigEmail;
    }

    public String getGitConfigNameToUse() {
        String confName;
        String globalConfigName = ((DescriptorImpl) getDescriptor()).getGlobalConfigName();
        if ((globalConfigName != null) && (gitConfigName == null) && (!fixEmptyAndTrim(globalConfigName).equals(""))) {
            confName = globalConfigName;
        } else {
            confName = gitConfigName;
        }

        return fixEmptyAndTrim(confName);
    }

    public String getGitConfigEmailToUse() {
        String confEmail;
        String globalConfigEmail = ((DescriptorImpl) getDescriptor()).getGlobalConfigEmail();
        if ((globalConfigEmail != null) && (gitConfigEmail == null) && (!fixEmptyAndTrim(globalConfigEmail).equals(
            ""))) {
            confEmail = globalConfigEmail;
        } else {
            confEmail = gitConfigEmail;
        }

        return fixEmptyAndTrim(confEmail);
    }

    public boolean getSkipTag() {
        return this.skipTag;
    }

    public boolean getPruneBranches() {
        return this.pruneBranches;
    }

    public boolean getWipeOutWorkspace() {
        return this.wipeOutWorkspace;
    }

    public boolean getClean() {
        return this.clean;
    }

    public BuildChooser getBuildChooser() {
        return buildChooser;
    }

    /**
     * Expand parameters in {@link #remoteRepositories} with the parameter values provided in the given build
     * and return them.
     *
     * @return can be empty but never null.
     */
    public List<RemoteConfig> getParamExpandedRepos(AbstractBuild<?, ?> build) {
        List<RemoteConfig> expandedRepos = new ArrayList<RemoteConfig>();

        for (RemoteConfig oldRepo : Util.fixNull(remoteRepositories)) {
            expandedRepos.add(newRemoteConfig(oldRepo.getName(),
                oldRepo.getURIs().get(0).toPrivateString(),
                new RefSpec(getRefSpec(oldRepo, build))));
        }

        return expandedRepos;
    }

    public RemoteConfig getRepositoryByName(String repoName) {
        for (RemoteConfig r : getRepositories()) {
            if (r.getName().equals(repoName)) {
                return r;
            }
        }

        return null;
    }

    public List<RemoteConfig> getRepositories() {
        // Handle null-value to ensure backwards-compatibility, ie project configuration missing the <repositories/> XML element
        if (remoteRepositories == null) {
            return new ArrayList<RemoteConfig>();
        }
        return remoteRepositories;
    }

    public String getGitTool() {
        return gitTool;
    }

    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> abstractBuild, Launcher launcher,
                                                   TaskListener taskListener) throws IOException, InterruptedException {
        return SCMRevisionState.NONE;
    }

    public RemoteConfig getSubmoduleRepository(IGitAPI parentGit,
                                               RemoteConfig orig,
                                               String name) throws GitException {
        // The first attempt at finding the URL in this new code relies on
        // submoduleInit, submoduleSync, fixSubmoduleUrls already being executed
        // since the last fetch of the super project.  (This is currently done
        // by calling git.setupSubmoduleUrls(...). )
        String refUrl = parentGit.getSubmoduleUrl(name);
        return newRemoteConfig(name, refUrl, orig.getFetchRefSpecs().get(0));
    }

    /**
     * Exposing so that we can get this from GitPublisher.
     *
     * @param builtOn {@link Node}.
     * @param listener {@link TaskListener}.
     */
    public String getGitExe(Node builtOn, TaskListener listener) {
        GitTool[] gitToolInstallations = Hudson.getInstance()
            .getDescriptorByType(GitTool.DescriptorImpl.class)
            .getInstallations();
        for (GitTool t : gitToolInstallations) {
            //If gitTool is null, use first one.
            if (gitTool == null) {
                gitTool = t.getName();
            }

            if (t.getName().equals(gitTool) && builtOn != null) {
                try {
                    String s = t.forNode(builtOn, listener).getGitExe();
                    return s;
                } catch (IOException e) {
                    listener.getLogger().println("Failed to get git executable");
                } catch (InterruptedException e) {
                    listener.getLogger().println("Failed to get git executable");
                }
            }
        }
        return null;
    }

    /**
     * Use either author or committer.
     *
     * @return if true, use the commit author as the changeset author, rather
     *         than the committer.
     */
    public boolean getAuthorOrCommitter() {
        return authorOrCommitter;
    }

    @Override
    public boolean checkout(final AbstractBuild build, Launcher launcher,
                            final FilePath workspace, final BuildListener listener, File changelogFile)
        throws IOException, InterruptedException {
        BuildConfig buildConfig;

        listener.getLogger()
            .println(
                "Checkout:" + workspace.getName() + " / " + workspace.getRemote() + " - " + workspace.getChannel());
        listener.getLogger().println("Using strategy: " + buildChooser.getDisplayName());

        final FilePath workingDirectory = workingDirectory(workspace);

        if (!workingDirectory.exists()) {
            workingDirectory.mkdirs();
        }

        //TODO find way to delete global variables and don't break master-slave serialization
        final int buildNumber = build.getNumber();
        final String gitExe = getGitExe(build.getBuiltOn(), listener);
        final String internalTagName = new StringBuilder()
            .append(GitConstants.INTERNAL_TAG_NAME_PREFIX)
            .append(GitConstants.HYPHEN_SYMBOL)
            .append(build.getProject().getName())
            .append(GitConstants.HYPHEN_SYMBOL)
            .append(build.getNumber())
            .toString();

        final String internalTagComment = new StringBuilder()
            .append(GitConstants.INTERNAL_TAG_COMMENT_PREFIX)
            .append(build.getNumber()).toString();


        final BuildData buildData = getBuildData(build.getPreviousBuild(), true);

        if (buildData.lastBuild != null) {
            listener.getLogger().println("Last Built Revision: " + buildData.lastBuild.revision);
        }

        EnvVars environment = build.getEnvironment(listener);

        String confName = getGitConfigNameToUse();
        if ((confName != null) && (!confName.equals(""))) {
            environment.put(GitConstants.GIT_COMMITTER_NAME_ENV_VAR, confName);
            environment.put(GitConstants.GIT_AUTHOR_NAME_ENV_VAR, confName);
        }
        String confEmail = getGitConfigEmailToUse();
        if ((confEmail != null) && (!confEmail.equals(""))) {
            environment.put(GitConstants.GIT_COMMITTER_EMAIL_ENV_VAR, confEmail);
            environment.put(GitConstants.GIT_AUTHOR_EMAIL_ENV_VAR, confEmail);
        }

        final String singleBranch = getSingleBranch(build);
        final String paramLocalBranch = getParamLocalBranch(build);
        Revision tempParentLastBuiltRev = null;

        if (build instanceof MatrixRun) {
            MatrixBuild parentBuild = ((MatrixRun) build).getParentBuild();
            if (parentBuild != null) {
                BuildData parentBuildData = parentBuild.getAction(BuildData.class);
                if (parentBuildData != null) {
                    tempParentLastBuiltRev = parentBuildData.getLastBuiltRevision();
                }
            }
        }

        final List<RemoteConfig> paramRepos = getParamExpandedRepos(build);

        final Revision parentLastBuiltRev = tempParentLastBuiltRev;

        final RevisionParameterAction rpa = build.getAction(RevisionParameterAction.class);

        final Revision revToBuild = gerRevisionToBuild(listener, workingDirectory, gitExe, buildData, environment,
            singleBranch, paramRepos,
            parentLastBuiltRev, rpa);

        if (revToBuild == null) {
            // getBuildCandidates should make the last item the last build, so a re-build
            // will build the last built thing.
            listener.error("Nothing to do");
            return false;
        }
        listener.getLogger().println("Commencing build of " + revToBuild);
        environment.put(GIT_COMMIT, revToBuild.getSha1String());

        if (mergeOptions.doMerge() && !revToBuild.containsBranchName(mergeOptions.getRemoteBranchName())) {
            buildConfig = getMergedBuildConfig(listener, workingDirectory, buildNumber, gitExe, buildData,
                environment, paramLocalBranch, revToBuild, internalTagName, internalTagComment);
            build.addAction(buildConfig.getBuildData());
            return changeLogResult(buildConfig.getChangeLog(), changelogFile);
        }

        // No merge
        buildConfig = getBuildConfig(listener, workingDirectory, buildNumber, gitExe, buildData, environment,
            paramLocalBranch, paramRepos, revToBuild, internalTagName, internalTagComment);


        build.addAction(buildConfig.getBuildData());

        return changeLogResult(buildConfig.getChangeLog(), changelogFile);
    }

    /**
     * Returns build config.
     *
     * @param listener listener.
     * @param workingDirectory workingDirectory.
     * @param buildNumber buildNumber.
     * @param gitExe gitExe.
     * @param buildData buildData.
     * @param environment environment.
     * @param paramLocalBranch paramLocalBranch.
     * @param paramRepos paramRepos.
     * @param revToBuild revToBuild.
     * @param internalTagName internalTagName
     * @param internalTagComment internalTagComment.
     * @return buildConfig with change log and build data.
     * @throws IOException          IOException.
     * @throws InterruptedException InterruptedException.
     */
    private BuildConfig getBuildConfig(final BuildListener listener,
                                       FilePath workingDirectory,
                                       final int buildNumber, final String gitExe, final BuildData buildData,
                                       final EnvVars environment,
                                       final String paramLocalBranch, final List<RemoteConfig> paramRepos,
                                       final Revision revToBuild, final String internalTagName,
                                       final String internalTagComment)
        throws IOException, InterruptedException {
        return workingDirectory.act(new FileCallable<BuildConfig>() {
            private static final long serialVersionUID = 1L;

            public BuildConfig invoke(File localWorkspace, VirtualChannel channel)
                throws IOException {
                IGitAPI git = new GitAPI(gitExe, new FilePath(localWorkspace), listener, environment);

                // Straight compile-the-branch
                listener.getLogger().println("Checking out " + revToBuild);

                if (getClean()) {
                    listener.getLogger().println("Cleaning workspace");
                    git.clean();
                }

                git.checkoutBranch(paramLocalBranch, revToBuild.getSha1().name());

                if (git.hasGitModules()) {
                    // Git submodule update will only 'fetch' from where it
                    // regards as 'origin'. However,
                    // it is possible that we are building from a
                    // RemoteRepository with changes
                    // that are not in 'origin' AND it may be a new module that
                    // we've only just discovered.
                    // So - try updating from all RRs, then use the submodule
                    // Update to do the checkout
                    //
                    // Also, only do this if we're not doing recursive submodules, since that'll
                    // theoretically be dealt with there anyway.
                    if (!recursiveSubmodules) {
                        for (RemoteConfig remoteRepository : paramRepos) {
                            fetchSubmodulesFrom(git, localWorkspace, listener, remoteRepository);
                        }
                    }

                    // This ensures we don't miss changes to submodule paths and allows
                    // seamless use of bare and non-bare superproject repositories.
                    git.setupSubmoduleUrls(revToBuild, listener);
                    git.submoduleUpdate(recursiveSubmodules);

                }

                // if(compileSubmoduleCompares)
                if (doGenerateSubmoduleConfigurations) {
                    SubmoduleCombinator combinator = new SubmoduleCombinator(
                        git, listener, localWorkspace, submoduleCfg);
                    combinator.createSubmoduleCombinations();
                }

                createInternalTag(git, internalTagName, internalTagComment);

                String changeLog = computeChangeLog(git, revToBuild, listener, buildData);

                buildData.saveBuild(new Build(revToBuild, buildNumber, null));

                // Fetch the diffs into the changelog file
                return new BuildConfig(changeLog, buildData);
            }
        });
    }

    /**
     * Returns build config after merging.
     *
     * @param listener listener.
     * @param workingDirectory workingDirectory.
     * @param buildNumber buildNumber.
     * @param gitExe gitExe.
     * @param buildData buildData.
     * @param environment environment.
     * @param paramLocalBranch paramLocalBranch.
     * @param revToBuild revToBuild.
     * @param internalTagName internalTagName
     * @param internalTagComment internalTagComment.
     * @return buildConfig with change log and build data.
     * @throws IOException          IOException.
     * @throws InterruptedException InterruptedException.
     */
    private BuildConfig getMergedBuildConfig(final BuildListener listener,
                                             FilePath workingDirectory,
                                             final int buildNumber, final String gitExe, final BuildData buildData,
                                             final EnvVars environment,
                                             final String paramLocalBranch, final Revision revToBuild,
                                             final String internalTagName, final String internalTagComment)
        throws IOException, InterruptedException {
        return workingDirectory.act(new FileCallable<BuildConfig>() {
            private static final long serialVersionUID = 1L;

            public BuildConfig invoke(File localWorkspace, VirtualChannel channel)
                throws IOException {
                IGitAPI git = new GitAPI(gitExe, new FilePath(localWorkspace), listener, environment);

                // Do we need to merge this revision onto MergeTarget

                // Only merge if there's a branch to merge that isn't
                // us..
                listener.getLogger().println(
                    "Merging " + revToBuild + " onto "
                        + mergeOptions.getMergeTarget());

                // checkout origin/blah
                ObjectId target = git.revParse(mergeOptions.getRemoteBranchName());

                git.checkoutBranch(paramLocalBranch, target.name());

                try {
                    git.merge(revToBuild.getSha1().name());
                } catch (Exception ex) {
                    listener
                        .getLogger()
                        .println(
                            "Branch not suitable for integration as it does not merge cleanly");

                    // We still need to tag something to prevent
                    // repetitive builds from happening - tag the
                    // candidate
                    // branch.
                    git.checkoutBranch(paramLocalBranch, revToBuild.getSha1().name());

                    createInternalTag(git, internalTagName, internalTagComment);

                    buildData.saveBuild(new Build(revToBuild, buildNumber, Result.FAILURE));
                    return new BuildConfig(null, buildData);
                }

                if (git.hasGitModules()) {
                    // This ensures we don't miss changes to submodule paths and allows
                    // seamless use of bare and non-bare superproject repositories.
                    git.setupSubmoduleUrls(revToBuild, listener);
                    git.submoduleUpdate(recursiveSubmodules);
                }

                createInternalTag(git, internalTagName, internalTagComment);

                String changeLog = computeChangeLog(git, revToBuild, listener, buildData);

                Build build = new Build(revToBuild, buildNumber, null);
                buildData.saveBuild(build);
                GitUtils gu = new GitUtils(listener, git);
                build.mergeRevision = gu.getRevisionForSHA1(target);
                if (getClean()) {
                    listener.getLogger().println("Cleaning workspace");
                    git.clean();
                    if (git.hasGitModules()) {
                        git.submoduleClean(recursiveSubmodules);
                    }
                }

                // Fetch the diffs into the changelog file
                return new BuildConfig(changeLog, buildData);
            }
        });
    }

    private Revision gerRevisionToBuild(final BuildListener listener, FilePath workingDirectory, final String gitExe,
                                        final BuildData buildData, final EnvVars environment, final String singleBranch,
                                        final List<RemoteConfig> paramRepos, final Revision parentLastBuiltRev,
                                        final RevisionParameterAction rpa) throws IOException, InterruptedException {
        return workingDirectory.act(new FileCallable<Revision>() {
            private static final long serialVersionUID = 1L;

            public Revision invoke(File localWorkspace, VirtualChannel channel)
                throws IOException {
                FilePath ws = new FilePath(localWorkspace);
                listener.getLogger()
                    .println("Checkout:" + ws.getName() + " / " + ws.getRemote() + " - " + ws.getChannel());
                IGitAPI git = new GitAPI(gitExe, ws, listener, environment);

                if (wipeOutWorkspace) {
                    listener.getLogger().println("Wiping out workspace first.");
                    try {
                        ws.deleteContents();
                    } catch (InterruptedException e) {
                        // I don't really care if this fails.
                    }
                }

                if (git.hasGitRepo()) {
                    // It's an update

                    // Do we want to prune first?
                    if (pruneBranches) {
                        listener.getLogger().println("Pruning obsolete local branches");
                        for (RemoteConfig remoteRepository : paramRepos) {
                            git.prune(remoteRepository);
                        }
                    }

                    listener.getLogger().println("Fetching changes from the remote Git repository");

                    boolean fetched = false;

                    for (RemoteConfig remoteRepository : paramRepos) {
                        if (fetchFrom(git, listener, remoteRepository)) {
                            fetched = true;
                        }
                    }

                    if (!fetched) {
                        listener.error("Could not fetch from any repository");
                        throw new GitException("Could not fetch from any repository");
                    }

                } else {
                    listener.getLogger().println("Cloning the remote Git repository");

                    // Go through the repositories, trying to clone from one
                    //
                    boolean successfullyCloned = false;
                    for (RemoteConfig rc : paramRepos) {
                        try {
                            git.clone(rc);
                            successfullyCloned = true;
                            break;
                        } catch (GitException ex) {
                            listener.error("Error cloning remote repo '%s' : %s", rc.getName(), ex.getMessage());
                            if (ex.getCause() != null) {
                                listener.error("Cause: %s", ex.getCause().getMessage());
                            }
                            // Failed. Try the next one
                            listener.getLogger().println("Trying next repository");
                        }
                    }

                    if (!successfullyCloned) {
                        listener.error("Could not clone repository");
                        throw new GitException("Could not clone");
                    }

                    boolean fetched = false;
                    // Also do a fetch
                    for (RemoteConfig remoteRepository : paramRepos) {
                        try {
                            git.fetch(remoteRepository);
                            fetched = true;
                        } catch (Exception e) {
                            listener.error(
                                "Problem fetching from " + remoteRepository.getName()
                                    + " / " + remoteRepository.getName()
                                    + " - could be unavailable. Continuing anyway");
                        }
                    }

                    if (!fetched) {
                        listener.error("Could not fetch from any repository");
                        throw new GitException("Could not fetch from any repository");
                    }

                    if (getClean()) {
                        listener.getLogger().println("Cleaning workspace");
                        git.clean();
                        if (git.hasGitModules()) {
                            git.submoduleClean(recursiveSubmodules);
                        }
                    }
                }

                if (parentLastBuiltRev != null) {
                    return parentLastBuiltRev;
                }

                if (rpa != null) {
                    return rpa.toRevision(git);
                }

                Collection<Revision> candidates = buildChooser.getCandidateRevisions(
                    false, singleBranch, git, listener, buildData);
                if (candidates.size() == 0) {
                    return null;
                }
                return candidates.iterator().next();
            }
        });
    }


    public void buildEnvVars(AbstractBuild<?, ?> build, java.util.Map<String, String> env) {
        super.buildEnvVars(build, env);
        String branch = getSingleBranch(build);
        if (branch != null) {
            env.put(GIT_BRANCH, branch);
        }
        BuildData bd = fixNull(getBuildData(build, false));
        if (bd != null && bd.getLastBuiltRevision() != null) {
            String commit = bd.getLastBuiltRevision().getSha1String();
            if (commit != null) {
                env.put(GIT_COMMIT, commit);
            }
        }

    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new GitChangeLogParser(getAuthorOrCommitter());
    }

    @Extension
    public static final class DescriptorImpl extends SCMDescriptor<GitSCM> {

        private String gitExe;
        private String globalConfigName;
        private String globalConfigEmail;

        public DescriptorImpl() {
            super(GitSCM.class, GitRepositoryBrowser.class);
            initXSTREAM();
            load();
        }

        @Override
        public String getId() {
            return HUDSON_SCM_GITSCM_ALIAS_NAME;
        }

        public static void initXSTREAM() {
            //Aliased for backward compatibility after moving to org.eclipse.*
            Items.XSTREAM.alias("ObjectId", ObjectId.class);
            Items.XSTREAM.alias("RemoteConfig", RemoteConfig.class);
            Items.XSTREAM.alias("RemoteConfig", org.spearce.jgit.transport.RemoteConfig.class);
            Items.XSTREAM.registerConverter(
                new RemoteConfigConverter(Items.XSTREAM.getMapper(), Items.XSTREAM.getReflectionProvider()));
            Items.XSTREAM.alias("RemoteConfig", RemoteConfig.class);
            Items.XSTREAM.alias("RemoteConfig", org.spearce.jgit.transport.RemoteConfig.class);
            Items.XSTREAM.alias("hudson.plugins.git.GitSCM", GitSCM.class);
            Items.XSTREAM.alias("hudson.plugins.git.BranchSpec", BranchSpec.class);
            Items.XSTREAM.alias("hudson.plugins.git.util.DefaultBuildChooser", DefaultBuildChooser.class);
            Run.XSTREAM.registerConverter(new ObjectIdConverter());
            Run.XSTREAM.alias("hudson.plugins.git.util.BuildData", BuildData.class);
            Run.XSTREAM.alias("hudson.plugins.git.util.Build", Build.class);
            Run.XSTREAM.alias("hudson.plugins.git.Branch", Branch.class);
            Run.XSTREAM.alias("hudson.plugins.git.GitChangeLogParser", GitChangeLogParser.class);
            XmlFile.DEFAULT_XSTREAM.alias(HUDSON_SCM_GITSCM_DESCRIPTOR_ALIAS_NAME, DescriptorImpl.class);
        }

        public String getDisplayName() {
            return "Git";
        }

        public List<BuildChooserDescriptor> getBuildChooserDescriptors() {
            return BuildChooser.all();
        }

        /**
         * Lists available toolinstallations.
         *
         * @return list of available git tools
         */
        public List<GitTool> getGitTools() {
            GitTool[] gitToolInstallations = Hudson.getInstance()
                .getDescriptorByType(GitTool.DescriptorImpl.class)
                .getInstallations();
            return Arrays.asList(gitToolInstallations);
        }

        /**
         * Path to git executable.
         *
         * @see GitTool
         * @deprecated
         */
        @Deprecated
        public String getGitExe() {
            return gitExe;
        }

        /**
         * Global setting to be used in call to "git config user.name".
         */
        public String getGlobalConfigName() {
            return globalConfigName;
        }

        /**
         * Global setting to be used in call to "git config user.email".
         */
        public String getGlobalConfigEmail() {
            return globalConfigEmail;
        }

        /**
         * Old configuration of git executable - exposed so that we can
         * migrate this setting to GitTool without deprecation warnings.
         */
        public String getOldGitExe() {
            return gitExe;
        }

        /**
         * Determine the browser from the scmData contained in the {@link StaplerRequest}.
         *
         * @param scmData
         * @return
         */
        private GitRepositoryBrowser getBrowserFromRequest(final StaplerRequest req, final JSONObject scmData) {
            if (scmData.containsKey("browser")) {
                return req.bindJSON(GitRepositoryBrowser.class, scmData.getJSONObject("browser"));
            } else {
                return null;
            }
        }

        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            List<RemoteConfig> remoteRepositories;
            try {
                remoteRepositories = createRepositoryConfigurations(req.getParameterValues("git.repo.url"),
                    req.getParameterValues("git.repo.name"),
                    req.getParameterValues("git.repo.refspec"));
            } catch (IOException e1) {
                throw new GitException(Messages.GitSCM_Repository_CreationExceptionMsg(), e1);
            }
            List<BranchSpec> branches = createBranches(req.getParameterValues("git.branch"));

            // Make up a repo config from the request parameters

            PreBuildMergeOptions mergeOptions = createMergeOptions(req.getParameter("git.doMerge"),
                req.getParameter("git.mergeRemote"), req.getParameter("git.mergeTarget"),
                remoteRepositories);

            Collection<SubmoduleConfig> submoduleCfg = new ArrayList<SubmoduleConfig>();

            final GitRepositoryBrowser gitBrowser = getBrowserFromRequest(req, formData);
            String gitTool = req.getParameter("git.gitTool");

            return new GitSCM(
                remoteRepositories,
                branches,
                mergeOptions,
                req.getParameter("git.generate") != null,
                submoduleCfg,
                req.getParameter("git.clean") != null,
                req.getParameter("git.wipeOutWorkspace") != null,
                req.bindJSON(BuildChooser.class, formData.getJSONObject("buildChooser")),
                gitBrowser,
                gitTool,
                req.getParameter("git.authorOrCommitter") != null,
                req.getParameter("git.relativeTargetDir"),
                req.getParameter("git.excludedRegions"),
                req.getParameter("git.excludedUsers"),
                req.getParameter("git.localBranch"),
                req.getParameter("git.recursiveSubmodules") != null,
                req.getParameter("git.pruneBranches") != null,
                req.getParameter("git.gitConfigName"),
                req.getParameter("git.gitConfigEmail"),
                req.getParameter("git.skipTag") != null);
        }

        public static List<RemoteConfig> createRepositoryConfigurations(String[] urls, String[] repoNames,
                                                                        String[] refSpecs)
            throws IOException, FormException {
            if (GitUtils.isEmpty(urls)) {
                throw new FormException(Messages.GitSCM_Repository_MissedRepositoryExceptionMsg(),
                    "git.repo.url");
            }
            List<RemoteConfig> remoteRepositories = new ArrayList<RemoteConfig>();
            if (!GitUtils.isEmpty(urls)) {
                Config repoConfig = new Config();
                // Make up a repo config from the request parameters
                repoNames = GitUtils.fixupNames(repoNames, urls);
                if (repoNames != null) {
                    for (int i = 0; i < repoNames.length; i++) {
                        String name = StringUtils.replaceChars(repoNames[i], ' ', '_');
                        if (StringUtils.isEmpty(refSpecs[i])) {
                            refSpecs[i] = "+refs/heads/*:refs/remotes/" + name + "/*";
                        }
                        repoConfig.setString("remote", name, "url", urls[i]);
                        repoConfig.setString("remote", name, "fetch", refSpecs[i]);
                    }
                }
                try {
                    remoteRepositories = RemoteConfig.getAllRemoteConfigs(repoConfig);
                } catch (Exception e) {
                    throw new GitException(Messages.GitSCM_Repository_CreationExceptionMsg(), e);
                }
            }
            return remoteRepositories;
        }

        /**
         * Creates git branches based.
         *
         * @param branchParams parameters.
         * @return list pf {@link BranchSpec}.
         */
        public static List<BranchSpec> createBranches(String[] branchParams) {
            List<BranchSpec> branches = new ArrayList<BranchSpec>();
            if (ArrayUtils.isEmpty(branchParams)) {
                branches.add(new BranchSpec(DEFAULT_BRANCH));
            } else {
                for (String branchParam : branchParams) {
                    branches.add(new BranchSpec(branchParam));
                }
            }
            return branches;
        }

        public static PreBuildMergeOptions createMergeOptions(String doMerge, String pMergeRemote,
                                                              String mergeTarget,
                                                              List<RemoteConfig> remoteRepositories)
            throws FormException {
            PreBuildMergeOptions mergeOptions = new PreBuildMergeOptions();
            if (doMerge != null && doMerge.trim().length() > 0) {
                RemoteConfig mergeRemote = null;
                String mergeRemoteName = pMergeRemote.trim();
                if (mergeRemoteName.length() == 0) {
                    mergeRemote = remoteRepositories.get(0);
                } else {
                    for (RemoteConfig remote : remoteRepositories) {
                        if (remote.getName().equals(mergeRemoteName)) {
                            mergeRemote = remote;
                            break;
                        }
                    }
                }
                if (mergeRemote == null) {
                    throw new FormException("No remote repository configured with name '" + mergeRemoteName + "'",
                        "git.mergeRemote");
                }
                mergeOptions.setMergeRemote(mergeRemote);

                mergeOptions.setMergeTarget(mergeTarget);
            }

            return mergeOptions;
        }

        public static GitWeb createGitWeb(String url) {
            GitWeb gitWeb = null;
            String gitWebUrl = url;
            if (gitWebUrl != null && gitWebUrl.length() > 0) {
                try {
                    gitWeb = new GitWeb(gitWebUrl);
                } catch (MalformedURLException e) {
                    throw new GitException("Error creating GitWeb", e);
                }
            }
            return gitWeb;
        }

        public FormValidation doGitRemoteNameCheck(StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {
            String mergeRemoteName = req.getParameter("value");
            boolean isMerge = req.getParameter("isMerge") != null;

            // Added isMerge because we don't want to allow empty remote names for tag/branch pushes.
            if (mergeRemoteName.length() == 0 && isMerge) {
                return FormValidation.ok();
            }

            String[] urls = req.getParameterValues("git.repo.url");
            String[] names = req.getParameterValues("git.repo.name");

            names = GitUtils.fixupNames(names, urls);

            for (String name : names) {
                if (name.equals(mergeRemoteName)) {
                    return FormValidation.ok();
                }
            }

            return FormValidation.error("No remote repository configured with name '" + mergeRemoteName + "'");
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {
            req.bindJSON(this, formData);
            save();
            return true;
        }

        /**
         * Validates the remote repository URL.
         *
         * @param value value to validate.
         * @return {@link FormValidation}.
         * @throws java.io.IOException            IOException.
         * @throws javax.servlet.ServletException ServletException.
         */
        public FormValidation doCheckRepositoryUrl(@QueryParameter String value)
            throws IOException, ServletException {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.error(
                    Messages.GitSCM_Repository_IncorrectRepositoryFormatMsg());
            }
            Config repoConfig = new Config();
            repoConfig.setString("remote", GitUtils.DEFAULD_REPO_NAME, "url", value);
            try {
                RemoteConfig.getAllRemoteConfigs(repoConfig);
            } catch (Exception e) {
                return FormValidation.error(
                    Messages.GitSCM_Repository_IncorrectRepositoryFormatMsg() + ": "
                        + e.getMessage());
            }
            return FormValidation.ok();
        }

    }

    public boolean getRecursiveSubmodules() {
        return this.recursiveSubmodules;
    }

    public boolean getDoGenerate() {
        return this.doGenerateSubmoduleConfigurations;
    }

    public List<BranchSpec> getBranches() {
        return branches;
    }

    public PreBuildMergeOptions getMergeOptions() {
        return mergeOptions;
    }

    /**
     * Look back as far as needed to find a valid BuildData.  BuildData
     * may not be recorded if an exception occurs in the plugin logic.
     *
     * @param build build.
     * @param clone is clone.
     * @return the last recorded build data
     */
    public BuildData getBuildData(Run build, boolean clone) {
        BuildData buildData = null;
        while (build != null) {
            buildData = build.getAction(BuildData.class);
            if (buildData != null) {
                break;
            }
            build = build.getPreviousBuild();
        }

        if (buildData == null) {
            return clone ? new BuildData() : null;
        }

        if (clone) {
            return buildData.clone();
        } else {
            return buildData;
        }
    }

    public String getLocalBranch() {
        return Util.fixEmpty(localBranch);
    }

    public String getParamLocalBranch(AbstractBuild<?, ?> build) {
        String branch = getLocalBranch();
        // substitute build parameters if available
        ParametersAction parameters = build.getAction(ParametersAction.class);
        if (parameters != null) {
            branch = parameters.substitute(build, branch);
        }
        return branch;
    }

    public String getRelativeTargetDir() {
        return relativeTargetDir;
    }

    /**
     * Given the workspace, gets the working directory, which will be the workspace
     * if no relative target dir is specified. Otherwise, it'll be "workspace/relativeTargetDir".
     *
     * @param workspace
     * @return working directory
     */
    protected FilePath workingDirectory(final FilePath workspace) {

        if (relativeTargetDir == null || relativeTargetDir.length() == 0 || relativeTargetDir.equals(".")) {
            return workspace;
        }
        return workspace.child(relativeTargetDir);
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher,
                                                      FilePath workspace, final TaskListener listener,
                                                      SCMRevisionState baseline)
        throws IOException, InterruptedException {
        // Poll for changes. Are there any unbuilt revisions that Hudson ought to build ?

        listener.getLogger().println("Using strategy: " + buildChooser.getDisplayName());

        final AbstractBuild lastBuild = project.getLastBuild();

        if (lastBuild != null) {
            listener.getLogger().println("[poll] Last Build : #" + lastBuild.getNumber());
        } else {
            // If we've never been built before, well, gotta build!
            listener.getLogger().println("[poll] No previous build, so forcing an initial build.");
            return PollingResult.BUILD_NOW;
        }

        final BuildData buildData = fixNull(getBuildData(lastBuild, false));

        if (buildData != null && buildData.lastBuild != null) {
            listener.getLogger().println("[poll] Last Built Revision: " + buildData.lastBuild.revision);
        }

        final String gitExe;
        {
            //If this project is tied onto a node, it's built always there. On other cases,
            //polling is done on the node which did the last build.
            //
            Label label = project.getAssignedLabel();
            if (label != null && label.isSelfLabel()) {
                if (label.getNodes().iterator().next() != project.getLastBuiltOn()) {
                    listener.getLogger().println("Last build was not on tied node, forcing rebuild.");
                    return PollingResult.BUILD_NOW;
                }
                gitExe = getGitExe(label.getNodes().iterator().next(), listener);
            } else {
                gitExe = getGitExe(project.getLastBuiltOn(), listener);
            }
        }

        FilePath workingDirectory = workingDirectory(workspace);

        // Rebuild if the working directory doesn't exist
        // I'm actually not 100% sure about this, but I'll leave it in for now.
        // Update 9/9/2010 - actually, I think this *was* needed, since we weren't doing a better check
        // for whether we'd ever been built before. But I'm fixing that right now anyway.
        if (!workingDirectory.exists()) {
            return PollingResult.BUILD_NOW;
        }

        final EnvVars environment = GitUtils.getPollEnvironment(project, workspace, launcher, listener);
        final List<RemoteConfig> paramRepos = getParamExpandedRepos(lastBuild);
        final String singleBranch = getSingleBranch(lastBuild);

        boolean pollChangesResult = workingDirectory.act(new FileCallable<Boolean>() {
            private static final long serialVersionUID = 1L;

            public Boolean invoke(File localWorkspace, VirtualChannel channel) throws IOException {
                IGitAPI git = new GitAPI(gitExe, new FilePath(localWorkspace), listener, environment);

                if (git.hasGitRepo()) {
                    // Repo is there - do a fetch
                    listener.getLogger().println("Fetching changes from the remote Git repositories");

                    // Fetch updates
                    for (RemoteConfig remoteRepository : paramRepos) {
                        fetchFrom(git, listener, remoteRepository);
                    }

                    listener.getLogger().println("Polling for changes in");

                    Collection<Revision> origCandidates = buildChooser.getCandidateRevisions(
                        true, singleBranch, git, listener, buildData);

                    List<Revision> candidates = new ArrayList<Revision>();

                    for (Revision c : origCandidates) {
                        if (!isRevExcluded(git, c, listener)) {
                            candidates.add(c);
                        }
                    }

                    return (candidates.size() > 0);
                } else {
                    listener.getLogger().println("No Git repository yet, an initial checkout is required");
                    return true;
                }
            }
        });

        return pollChangesResult ? PollingResult.SIGNIFICANT : PollingResult.NO_CHANGES;
    }

    private RemoteConfig newRemoteConfig(String name, String refUrl, RefSpec refSpec) {
        try {
            Config repoConfig = new Config();
            // Make up a repo config from the request parameters
            repoConfig.setString("remote", name, "url", refUrl);
            repoConfig.setString("remote", name, "fetch", refSpec.toString());
            return RemoteConfig.getAllRemoteConfigs(repoConfig).get(0);
        } catch (Exception ex) {
            throw new GitException("Remote's configuration is invalid", ex);
        }
    }

    private boolean changeLogResult(String changeLog, File changelogFile) throws IOException {
        if (changeLog == null) {
            return false;
        } else {
            changelogFile.delete();

            FileOutputStream fos = new FileOutputStream(changelogFile);
            fos.write(changeLog.getBytes());
            fos.close();
            // Write to file
            return true;
        }
    }

    private Pattern[] getExcludedRegionsPatterns() {
        String[] excluded = getExcludedRegionsNormalized();
        if (excluded != null) {
            Pattern[] patterns = new Pattern[excluded.length];

            int i = 0;
            for (String excludedRegion : excluded) {
                patterns[i++] = Pattern.compile(excludedRegion);
            }

            return patterns;
        }

        return new Pattern[0];
    }

    private String getRefSpec(RemoteConfig repo, AbstractBuild<?, ?> build) {
        String refSpec = repo.getFetchRefSpecs().get(0).toString();

        ParametersAction parameters = build.getAction(ParametersAction.class);
        if (parameters != null) {
            refSpec = parameters.substitute(build, refSpec);
        }

        return refSpec;
    }

    /**
     * If the configuration is such that we are tracking just one branch of one repository
     * return that branch specifier (in the form of something like "origin/master"
     * <p/>
     * Otherwise return null.
     */
    private String getSingleBranch(AbstractBuild<?, ?> build) {
        // if we have multiple branches skip to advanced usecase
        if (getBranches().size() != 1 || getRepositories().size() != 1) {
            return null;
        }

        String branch = getBranches().get(0).getName();
        String repository = getRepositories().get(0).getName();

        // replace repository wildcard with repository name
        if (branch.startsWith("*/")) {
            branch = repository + branch.substring(1);
        }

        // if the branch name contains more wildcards then the simple usecase
        // does not apply and we need to skip to the advanced usecase
        if (branch.contains("*")) {
            return null;
        }

        // substitute build parameters if available
        ParametersAction parameters = build.getAction(ParametersAction.class);
        if (parameters != null) {
            branch = parameters.substitute(build, branch);
        }
        return branch;
    }

    private BuildData fixNull(BuildData bd) {
        return bd != null ? bd : new BuildData() /*dummy*/;
    }

    /**
     * Fetch information from a particular remote repository.
     *
     * @param git
     * @param listener
     * @param remoteRepository
     * @return true if fetch goes through, false otherwise.
     * @throws
     */
    private boolean fetchFrom(IGitAPI git,
                              TaskListener listener,
                              RemoteConfig remoteRepository) {
        try {
            git.fetch(remoteRepository);
            return true;
        } catch (GitException ex) {
            listener.error(
                "Problem fetching from " + remoteRepository.getName()
                    + " / " + remoteRepository.getName()
                    + " - could be unavailable. Continuing anyway");
            listener.error(" (Underlying report) : " + ex.getMessage());
        }
        return false;
    }

    /**
     * Fetch submodule information from relative to a particular remote repository.
     *
     * @param git
     * @param listener
     * @param remoteRepository
     * @return true if fetch goes through, false otherwise.
     * @throws
     */
    private boolean fetchSubmodulesFrom(IGitAPI git,
                                        File workspace,
                                        TaskListener listener,
                                        RemoteConfig remoteRepository) {
        boolean fetched = true;

        try {
            // This ensures we don't miss changes to submodule paths and allows
            // seamless use of bare and non-bare superproject repositories.
            git.setupSubmoduleUrls(remoteRepository.getName(), listener);

            /* with the new re-ordering of "git checkout" and the submodule
             * commands, it appears that this test will always succeed... But
             * we'll keep it anyway for now. */
            boolean hasHead = true;
            try {
                git.revParse("HEAD");
            } catch (GitException e) {
                hasHead = false;
            }

            if (hasHead) {
                List<IndexEntry> submodules = git.getSubmodules("HEAD");

                for (IndexEntry submodule : submodules) {
                    try {
                        RemoteConfig submoduleRemoteRepository
                            = getSubmoduleRepository(git,
                            remoteRepository,
                            submodule.getFile());
                        File subdir = new File(workspace, submodule.getFile());

                        listener.getLogger().println(
                            "Trying to fetch " + submodule.getFile() + " into " + subdir);

                        IGitAPI subGit = new GitAPI(git.getGitExe(),
                            new FilePath(subdir),
                            listener, git.getEnvironment());

                        subGit.fetch(submoduleRemoteRepository);
                    } catch (Exception ex) {
                        listener.getLogger().println(
                            "Problem fetching from submodule "
                                + submodule.getFile()
                                + " - could be unavailable. Continuing anyway");
                    }
                }
            }
        } catch (GitException ex) {
            ex.printStackTrace(listener.error(
                "Problem fetching submodules from a path relative to "
                    + remoteRepository.getName()
                    + " / " + remoteRepository.getName()
                    + " - could be unavailable. Continuing anyway"
            ));
            fetched = false;
        }

        return fetched;
    }

    /**
     * Creates internal tag.
     *
     * @param git {@link IGitAPI}.
     * @param tagName tag name.
     * @param tagComment tag comment.
     */
    private void createInternalTag(IGitAPI git, String tagName, String tagComment) {
        if (!getSkipTag()) {
            git.tag(tagName, tagComment);
        }
    }

    /**
     * Build up change log from all the branches that we've merged into {@code revToBuild}
     *
     * @param git Used for invoking Git
     * @param revToBuild Points to the revisiont we'll be building. This includes all the branches we've merged.
     * @param listener Used for writing to build console
     * @param buildData Information that captures what we did during the last build. We need this for changelog,
     * or else we won't know where to stop.
     */
    private String computeChangeLog(IGitAPI git, Revision revToBuild, BuildListener listener, BuildData buildData)
        throws IOException {
        int histories = 0;

        StringBuilder changeLog = new StringBuilder();
        try {
            for (Branch b : revToBuild.getBranches()) {
                Build lastRevWas = buildChooser.prevBuildForChangelog(b.getName(), buildData, git);
                if (lastRevWas != null) {
                    if (git.isCommitInRepo(lastRevWas.getSHA1().name())) {
                        changeLog.append(putChangelogDiffsIntoFile(git, b.name, lastRevWas.getSHA1().name(),
                            revToBuild.getSha1().name()));
                        histories++;
                    } else {
                        listener.getLogger()
                            .println("Could not record history. Previous build's commit, " + lastRevWas.getSHA1().name()
                                + ", does not exist in the current repository.");
                    }
                } else {
                    listener.getLogger().println("No change to record in branch " + b.getName());
                }
            }
        } catch (GitException ge) {
            changeLog.append("Unable to retrieve changeset");
        }

        if (histories > 1) {
            listener.getLogger().println("Warning : There are multiple branch changesets here");
        }

        return changeLog.toString();
    }

    private String putChangelogDiffsIntoFile(IGitAPI git, String branchName, String revFrom,
                                             String revTo) throws IOException {
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        // fos.write("<data><![CDATA[".getBytes());
        String changeset = "Changes in branch " + branchName + ", between " + revFrom + " and " + revTo + "\n";
        fos.write(changeset.getBytes());

        git.changelog(revFrom, revTo, fos);
        // fos.write("]]></data>".getBytes());
        fos.close();
        return fos.toString("UTF-8");
    }


    /**
     * Given a Revision, check whether it matches any exclusion rules.
     *
     * @param git IGitAPI object
     * @param r Revision object
     * @param listener
     * @return true if any exclusion files are matched, false otherwise.
     */
    private boolean isRevExcluded(IGitAPI git, Revision r, TaskListener listener) {
        try {
            List<String> revShow = git.showRevision(r);

            // If the revision info is empty, something went weird, so we'll just
            // return false.
            if (revShow.size() == 0) {
                return false;
            }

            GitChangeSet change = new GitChangeSet(revShow, authorOrCommitter);

            Pattern[] excludedPatterns = getExcludedRegionsPatterns();
            Set<String> excludedUsers = getExcludedUsersNormalized();

            String author = change.getAuthorName();
            if (excludedUsers.contains(author)) {
                // If the author is an excluded user, don't count this entry as a change
                listener.getLogger()
                    .println("Ignored commit " + r.getSha1String() + ": Found excluded author: " + author);
                return true;
            }

            List<String> paths = new ArrayList<String>(change.getAffectedPaths());
            if (paths.isEmpty()) {
                // If there weren't any changed files here, we're just going to return false.
                return false;
            }

            List<String> excludedPaths = new ArrayList<String>();
            if (excludedPatterns.length > 0) {
                for (String path : paths) {
                    for (Pattern pattern : excludedPatterns) {
                        if (pattern.matcher(path).matches()) {
                            excludedPaths.add(path);
                            break;
                        }
                    }
                }
            }

            // If every affected path is excluded, return true.
            if (paths.size() == excludedPaths.size()) {
                listener.getLogger().println("Ignored commit " + r.getSha1String()
                    + ": Found only excluded paths: "
                    + Util.join(excludedPaths, ", "));
                return true;
            }
        } catch (GitException e) {
            // If an error was hit getting the revision info, assume something
            // else entirely is wrong and we don't care, so return false.
            return false;
        }

        // By default, return false.
        return false;
    }

    private static final Logger LOGGER = Logger.getLogger(GitSCM.class.getName());

    /**
     * Set to true to enable more logging to build's {@link TaskListener}.
     * Used by various classes in this package.
     */
    public static boolean VERBOSE = Boolean.getBoolean(GitSCM.class.getName() + ".verbose");

    /**
     * Class to encapsulate configuration data.
     */
    private class BuildConfig implements Serializable {

        private BuildData buildData;
        private String changeLog;

        /**
         * Constructor with change log and build data.
         *
         * @param changeLog changeLog.
         * @param buildData buildData.
         */
        public BuildConfig(String changeLog, BuildData buildData) {
            this.changeLog = changeLog;
            this.buildData = buildData;
        }

        /**
         * Returns build data.
         *
         * @return build data.
         */
        public BuildData getBuildData() {
            return buildData;
        }

        /**
         * Returns change log.
         *
         * @return change log.
         */
        public String getChangeLog() {
            return changeLog;
        }
    }
}



