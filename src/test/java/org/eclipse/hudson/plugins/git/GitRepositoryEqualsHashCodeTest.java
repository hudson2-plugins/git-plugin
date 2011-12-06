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

import java.net.URISyntaxException;
import org.eclipse.jgit.lib.Config;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Verifies equals and hashCode methods of GitRepository objects.
 * </p>
 * Date: 10/6/11
 */
public class GitRepositoryEqualsHashCodeTest {
    private GitRepository repo1;
    private GitRepository repo2;
    private GitRepository repo3;

    @Before
    public void setUp() throws URISyntaxException {
        Config config = new Config();

        config.setString(GitRepository.REMOTE_SECTION, "name1", "url", "http://git1.com");
        config.setString(GitRepository.REMOTE_SECTION, "name1", "fetch", "refSpec1");
        config.setString(GitRepository.REMOTE_SECTION, "name1", GitRepository.TARGET_DIR_KEY, "dir");

        config.setString(GitRepository.REMOTE_SECTION, "name2", "url", "http://git2.com");
        config.setString(GitRepository.REMOTE_SECTION, "name2", "fetch", "refSpec2");
        config.setString(GitRepository.REMOTE_SECTION, "name2", GitRepository.TARGET_DIR_KEY, "dir2");

        repo1 = new GitRepository(config, "name1");
        repo2 = new GitRepository(config, "name1");
        repo3 = new GitRepository(config, "name2");

    }

    @Test
    public void testEquals() {
        assertEquals(repo1, repo2);
        assertFalse(repo1.equals(repo3));

    }

    @Test
    public void testHashCode() {
        assertEquals(repo1.hashCode(), repo2.hashCode());
        assertFalse(repo1.hashCode() == repo3.hashCode());
    }

}
