/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Anton Kozak, Nikita Levyankov
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
