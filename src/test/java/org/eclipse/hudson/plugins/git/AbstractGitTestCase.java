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

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Cause;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.util.StreamTaskListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.eclipse.hudson.plugins.git.opt.PreBuildMergeOptions;
import org.eclipse.hudson.plugins.git.util.DefaultBuildChooser;
import org.eclipse.hudson.plugins.git.util.GitConstants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.RemoteConfig;
import org.jvnet.hudson.test.CaptureEnvironmentBuilder;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * Base test case for Git related stuff.
 *
 * @author Kohsuke Kawaguchi
 * @author ishaaq
 */
public abstract class AbstractGitTestCase extends HudsonTestCase {
    protected File workDir;
    protected GitAPI git;
    protected TaskListener listener;
    protected EnvVars envVars;
    protected FilePath workspace;
    protected final PersonIdent johnDoe = new PersonIdent("John Doe", "john@doe.com");
    protected final PersonIdent janeDoe = new PersonIdent("Jane Doe", "jane@doe.com");

    @Override
    protected void setUp() throws Exception {
        System.setProperty("hudson.PluginStrategy", "hudson.ClassicPluginStrategy");
        super.setUp();
        workDir = createTmpDir();
        listener = new StreamTaskListener();
        envVars = new EnvVars();
        setAuthor(johnDoe);
        setCommitter(johnDoe);
        workspace = new FilePath(workDir);
        GitTool.onLoaded();
        initGit();
    }

    protected void initGit() {
        git = new GitAPI("git", workspace, listener, envVars);
        git.init();
    }

    protected void setAuthor(final PersonIdent author) {
        envVars.put(GitConstants.GIT_AUTHOR_NAME_ENV_VAR, author.getName());
        envVars.put(GitConstants.GIT_AUTHOR_EMAIL_ENV_VAR, author.getEmailAddress());
    }

    protected void setCommitter(final PersonIdent committer) {
        envVars.put(GitConstants.GIT_COMMITTER_NAME_ENV_VAR, committer.getName());
        envVars.put(GitConstants.GIT_COMMITTER_EMAIL_ENV_VAR, committer.getEmailAddress());
    }

    protected void commit(final String fileName, final PersonIdent committer, final String message)
        throws GitException {
        commit(fileName, committer, committer, message);
    }

    protected void commit(final String fileName, final PersonIdent author, final PersonIdent committer,
                          final String message) throws GitException {
        setAuthor(author);
        setCommitter(committer);
        FilePath file = workspace.child(fileName);
        try {
            file.write(fileName, null);
        } catch (Exception e) {
            throw new GitException("unable to write file", e);
        }
        git.add(fileName);
        git.commit(message);
    }

    protected List<RemoteConfig> createRemoteRepositories(String relativeTargetDir) throws IOException,
        Descriptor.FormException {
        return GitSCM.DescriptorImpl.createRepositoryConfigurations(
            new String[]{workDir.getAbsolutePath()},
            new String[]{"origin"},
            new String[]{""}
        );
    }


    protected FreeStyleProject setupProject(String branchString, boolean authorOrCommitter) throws Exception {
        return setupProject(branchString, authorOrCommitter, null);
    }

    protected FreeStyleProject setupProject(String branchString, boolean authorOrCommitter,
                                          String relativeTargetDir) throws Exception {
        return setupProject(branchString, authorOrCommitter, relativeTargetDir, null, null);
    }

    protected FreeStyleProject setupProject(String branchString, boolean authorOrCommitter,
                                          String relativeTargetDir,
                                          String excludedRegions,
                                          String excludedUsers) throws Exception {
        return setupProject(branchString, authorOrCommitter, relativeTargetDir, excludedRegions, excludedUsers, null);
    }

    protected FreeStyleProject setupProject(String branchString, boolean authorOrCommitter,
                                          String relativeTargetDir, String excludedRegions,
                                          String excludedUsers, String localBranch) throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.setScm(new GitSCM(
            createRemoteRepositories(relativeTargetDir),
            Collections.singletonList(new BranchSpec(branchString)),
            new PreBuildMergeOptions(), false, Collections.<SubmoduleConfig>emptyList(), false,
            false, new DefaultBuildChooser(), null, null, authorOrCommitter, relativeTargetDir,
            excludedRegions, excludedUsers, localBranch, false, false, null, null, false));
        project.getBuildersList().add(new CaptureEnvironmentBuilder());
        return project;
    }

    protected FreeStyleProject setupSimpleProject(String branchString) throws Exception {
        return setupProject(branchString, false);
    }

    protected FreeStyleBuild build(final FreeStyleProject project, final Result expectedResult,
                                 final String... expectedNewlyCommittedFiles) throws Exception {
        final FreeStyleBuild build = project.scheduleBuild2(0, new Cause.UserCause()).get();
        for (final String expectedNewlyCommittedFile : expectedNewlyCommittedFiles) {
            assertTrue(build.getWorkspace().child(expectedNewlyCommittedFile).exists());
        }
        if (expectedResult != null) {
            assertBuildStatus(expectedResult, build);
        }
        return build;
    }

    protected FreeStyleBuild build(final FreeStyleProject project, final String parentDir, final Result expectedResult,
                                 final String... expectedNewlyCommittedFiles) throws Exception {
        final FreeStyleBuild build = project.scheduleBuild2(0, new Cause.UserCause()).get();
        for (final String expectedNewlyCommittedFile : expectedNewlyCommittedFiles) {
            assertTrue(build.getWorkspace().child(parentDir).child(expectedNewlyCommittedFile).exists());
        }
        if (expectedResult != null) {
            assertBuildStatus(expectedResult, build);
        }
        return build;
    }

    protected EnvVars getEnvVars(FreeStyleProject project) {
        for (hudson.tasks.Builder b : project.getBuilders()) {
            if (b instanceof CaptureEnvironmentBuilder) {
                return ((CaptureEnvironmentBuilder) b).getEnvVars();
            }
        }
        return new EnvVars();
    }

    protected void setVariables(Node node, EnvironmentVariablesNodeProperty.Entry... entries) throws IOException {
        node.getNodeProperties().replaceBy(
            Collections.singleton(new EnvironmentVariablesNodeProperty(entries)));

    }

}
