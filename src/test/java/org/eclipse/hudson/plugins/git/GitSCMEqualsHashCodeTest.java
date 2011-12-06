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
 * Anton Kozak, Nikita Levyankov
 *
 *******************************************************************************/
package org.eclipse.hudson.plugins.git;

import org.eclipse.hudson.plugins.git.browser.CGit;
import org.eclipse.hudson.plugins.git.browser.GitRepositoryBrowser;
import org.eclipse.hudson.plugins.git.browser.GithubWeb;
import org.eclipse.hudson.plugins.git.opt.PreBuildMergeOptions;
import org.eclipse.hudson.plugins.git.util.BuildChooser;
import org.eclipse.hudson.plugins.git.util.DefaultBuildChooser;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.transport.RemoteConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static junit.framework.Assert.assertEquals;

/**
 * Verifies equals and hashCode methods of GitSCM objects.
 * </p>
 *
 * Date: 10/6/11
 */
@RunWith(Parameterized.class)
public class GitSCMEqualsHashCodeTest {

    private GitSCM defaultGitSCM;
    private List<RemoteConfig> repos;
    private GitSCM gitSCM;
    private boolean expectedResult;

    public GitSCMEqualsHashCodeTest(GitSCM gitSCM, boolean expectedResult) {
        this.gitSCM = gitSCM;
        this.expectedResult = expectedResult;
    }

    @Before
    public void setUp() throws URISyntaxException, MalformedURLException {
        Config config = new Config();
        config.setString(GitRepository.REMOTE_SECTION, "name", "url", "http://git.com");
        config.setString(GitRepository.REMOTE_SECTION, "name", "fetch", "refSpec1");
        repos = GitRepository.getAllGitRepositories(config);
        Collection<SubmoduleConfig> configs = new ArrayList<SubmoduleConfig>();
        SubmoduleConfig submoduleConfig = new SubmoduleConfig();
        submoduleConfig.setSubmoduleName("submoduleName");
        configs.add(submoduleConfig);

        defaultGitSCM = new GitSCM(repos, GitSCM.DescriptorImpl.createBranches(new String[]{"master"}),
            new PreBuildMergeOptions(), false, configs, false, false, new DefaultBuildChooser(),
            new GithubWeb("http://git.com"), "git", false, "excludedRegions", "excludedUsers", "localBranch",
            false, false, "user.name", "user.email", true, "includedRegions");
    }


    @Parameterized.Parameters
    public static Collection generateData() throws MalformedURLException, URISyntaxException {

        Config config = new Config();
        config.setString(GitRepository.REMOTE_SECTION, "name", "url", "http://git.com");
        config.setString(GitRepository.REMOTE_SECTION, "name", "fetch", "refSpec1");
        List<RemoteConfig> repos = GitRepository.getAllGitRepositories(config);

        config = new Config();
        config.setString(GitRepository.REMOTE_SECTION, "name1", "url", "http://git.com");
        config.setString(GitRepository.REMOTE_SECTION, "name1", "fetch", "refSpec1");
        List<RemoteConfig> repos1 = GitRepository.getAllGitRepositories(config);

        List<BranchSpec> branches = GitSCM.DescriptorImpl.createBranches(new String[]{"master"});
        GitRepositoryBrowser browser = new GithubWeb("http://git.com");
        BuildChooser buildChooser = new DefaultBuildChooser();
        PreBuildMergeOptions options = new PreBuildMergeOptions();
        PreBuildMergeOptions options1 = new PreBuildMergeOptions();
        options1.setMergeTarget("target");

        Collection<SubmoduleConfig> configs = new ArrayList<SubmoduleConfig>();
        SubmoduleConfig submoduleConfig = new SubmoduleConfig();
        submoduleConfig.setSubmoduleName("submoduleName");
        configs.add(submoduleConfig);

        Collection<SubmoduleConfig> configs1 = new ArrayList<SubmoduleConfig>();
        SubmoduleConfig submoduleConfig1 = new SubmoduleConfig();
        submoduleConfig.setSubmoduleName("submoduleName");
        configs1.add(submoduleConfig1);

        return Arrays.asList(new Object[][]{
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), true},
            {new GitSCM(repos1, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(null, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, GitSCM.DescriptorImpl.createBranches(new String[]{"origin"}), options, false, configs,
                false, false, buildChooser, browser, "git", false, "excludedRegions", "excludedUsers", "localBranch",
                false, false, "user.name", "user.email", true, "includedRegions"), false},
            {new GitSCM(repos, null, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options1, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, null, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, true, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs1, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, null, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, true, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, true, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser,
                new CGit("http://cgit.com"), "git", false, "excludedRegions", "excludedUsers", "localBranch", false,
                false, "user.name", "user.email", true, "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, null, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git1", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, null, false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", true,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions1", "excludedUsers", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                null, "excludedUsers", "localBranch", false, false, "user.name", "user.email", true, "includedRegions"),
                false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers1", "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", null, "localBranch", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch1", false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", null, false, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", true, false, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, true, "user.name", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name1", "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, null, "user.email", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email1", true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", null, true,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", false,
                "includedRegions"), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", false,
                null), false},
            {new GitSCM(repos, branches, options, false, configs, false, false, buildChooser, browser, "git", false,
                "excludedRegions", "excludedUsers", "localBranch", false, false, "user.name", "user.email", false,
                "includedRegions1"), false},
        });
    }

    @Test
    public void testEquals() throws MalformedURLException {
        assertEquals(expectedResult, defaultGitSCM.equals(gitSCM));
    }

    @Test
    public void testHashCode() throws MalformedURLException {
        assertEquals(expectedResult, defaultGitSCM.hashCode() == gitSCM.hashCode());
    }
}
