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

import hudson.model.Descriptor;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.transport.RemoteConfig;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link org.eclipse.hudson.plugins.git.GitSCM}.
 */
public class GitSCMUnitTest {

    @Test(expected = Descriptor.FormException.class)
    public void testCreateRepositoryConfigurationsEmptyUrls() throws Descriptor.FormException, IOException {
        List<RemoteConfig> configs = GitSCM.DescriptorImpl.createRepositoryConfigurations(new String[]{},
            new String[]{}, new String[]{});
        assertTrue(configs.isEmpty());
    }

    @Test
    public void testCreateRepositoryConfigurationsEmptyUrls2() throws Descriptor.FormException, IOException {
        List<RemoteConfig> configs = GitSCM.DescriptorImpl.createRepositoryConfigurations(new String[]{"repourl"},
            new String[]{"reponame"}, new String[]{""});
        assertFalse(configs.isEmpty());
    }
}

