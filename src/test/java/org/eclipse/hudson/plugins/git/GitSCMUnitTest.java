/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation Anton Kozak.
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

