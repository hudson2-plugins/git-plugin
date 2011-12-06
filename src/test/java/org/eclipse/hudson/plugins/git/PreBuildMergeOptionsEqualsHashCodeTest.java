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

import org.eclipse.hudson.plugins.git.opt.PreBuildMergeOptions;
import java.net.URISyntaxException;
import java.util.List;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.transport.RemoteConfig;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Verifies equals and hashCode methods of PreBuildMergeOptions objects.
 * </p>
 *
 * Date: 10/6/11
 */
public class PreBuildMergeOptionsEqualsHashCodeTest {

    private List<RemoteConfig> remoteConfigs;
    private PreBuildMergeOptions options1 = new PreBuildMergeOptions();
    private PreBuildMergeOptions options2 = new PreBuildMergeOptions();

    @Before
    public void setUp() throws URISyntaxException {
        Config config = new Config();
        config.setString(GitRepository.REMOTE_SECTION, "name1", "url", "http://git1.com");
        config.setString(GitRepository.REMOTE_SECTION, "name1", "fetch", "refSpec1");
        config.setString(GitRepository.REMOTE_SECTION, "name2", "url", "http://git2.com");
        config.setString(GitRepository.REMOTE_SECTION, "name2", "fetch", "refSpec2");
        remoteConfigs = GitRepository.getAllGitRepositories(config);
        config = new Config();
        config.setString(GitRepository.REMOTE_SECTION, "name1", "url", "http://git1.com");
        config.setString(GitRepository.REMOTE_SECTION, "name1", "fetch", "refSpec1");
        remoteConfigs.addAll(GitRepository.getAllGitRepositories(config));
    }

    @Test
    public void testEquals() {
        assertEquals(options1, options2);

        options1.setMergeTarget("target");
        assertFalse(options1.equals(options2));

        options2.setMergeTarget("target1");
        assertFalse(options1.equals(options2));

        options2.setMergeTarget("target");
        assertEquals(options1, options2);

        options1.setMergeRemote(remoteConfigs.get(0));
        assertFalse(options1.equals(options2));

        options2.setMergeRemote(remoteConfigs.get(1));
        assertFalse(options1.equals(options2));

        options2.setMergeRemote(remoteConfigs.get(2));
        assertEquals(options1, options2);
    }

    @Test
    public void testHashCode() {
        assertEquals(options1.hashCode(), options2.hashCode());

        options1.setMergeTarget("target");
        assertFalse(options1.hashCode() == options2.hashCode());

        options2.setMergeTarget("target1");
        assertFalse(options1.hashCode() == options2.hashCode());

        options2.setMergeTarget("target");
        assertEquals(options1.hashCode(), options2.hashCode());

        options1.setMergeRemote(remoteConfigs.get(0));
        assertFalse(options1.hashCode() == options2.hashCode());

        options2.setMergeRemote(remoteConfigs.get(1));
        assertFalse(options1.hashCode() == options2.hashCode());

        options2.setMergeRemote(remoteConfigs.get(2));
        assertEquals(options1.hashCode(), options2.hashCode());
    }

}
