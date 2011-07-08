/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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
package hudson.plugins.git;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Comparison test for old and new implementation of IGitAPI
 * <p/>
 * Date: 7/8/11
 *
 * @author Nikita Levyankov
 */
public class GitvsJGitComparisonTest extends AbstractGitTestCase {

    private JGitAPI jGit;

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        jGit = new JGitAPI("git", workspace, listener, envVars);
        FreeStyleProject project = setupSimpleProject("master");

        // create initial commit and then run the build against it:
        final String commitFile1 = "commitFile1";
        commit(commitFile1, johnDoe, "Commit number 1");
        build(project, Result.SUCCESS, commitFile1);
    }

    protected void initGit() {
        git = new GitAPI("git", workspace, listener, envVars);
        git.init();
    }

    @Test
    public void testHasGitRepo() throws IOException {
        Assert.assertEquals(git.hasGitRepo(), jGit.hasGitRepo());
    }

    @Test
    public void testGetRepository() throws IOException {
        Assert.assertEquals(git.getRepository().toString(), jGit.getRepository().toString());
    }

    @Test
    public void testGetRemoteBranches() throws IOException {
        Assert.assertTrue(git.getRemoteBranches().containsAll(jGit.getRemoteBranches()));
        Assert.assertTrue(jGit.getRemoteBranches().containsAll(git.getRemoteBranches()));
    }

    @Test
    public void testGetBranches() throws IOException {
        Assert.assertTrue(git.getBranches().containsAll(jGit.getBranches()));
        Assert.assertTrue(jGit.getBranches().containsAll(git.getBranches()));
    }
}
