/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Anton Kozak, Nikita Levyankov
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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.User;
import java.util.Set;

public class GitExcludeIncludeTest extends AbstractGitTestCase {


    public void testBasicIncludedRegion() throws Exception {
        FreeStyleProject project = setupProject("master", false, null, null, null, null, ".*3");

        // create initial commit and then run the build against it:
        final String commitFile1 = "commitFile1";
        commit(commitFile1, johnDoe, "Commit number 1");
        build(project, Result.SUCCESS, commitFile1);

        assertFalse("scm polling should not detect any more changes after build", project.pollSCMChanges(listener));

        final String commitFile2 = "commitFile2";
        commit(commitFile2, janeDoe, "Commit number 2");
        assertFalse("scm polling detected commit2 change, which should not have been included",
            project.pollSCMChanges(listener));

        final String commitFile3 = "commitFile3";
        commit(commitFile3, johnDoe, "Commit number 3");
        assertTrue("scm polling did not detect commit3 change", project.pollSCMChanges(listener));
        //... and build it...
        final FreeStyleBuild build2 = build(project, Result.SUCCESS, commitFile2, commitFile3);
        final Set<User> culprits = build2.getCulprits();
        assertEquals("The build should have two culprit", 2, culprits.size());
        assertTrue("User janeDoe is absent in culprits", doesCulpritsContainUser(culprits, janeDoe.getName()));
        assertTrue("User johnDoe is absent in culprits", doesCulpritsContainUser(culprits, johnDoe.getName()));
        assertTrue(build2.getWorkspace().child(commitFile2).exists());
        assertTrue(build2.getWorkspace().child(commitFile3).exists());
        assertBuildStatusSuccess(build2);
        assertFalse("scm polling should not detect any more changes after build", project.pollSCMChanges(listener));
    }

    public void testBasicExcludedRegion() throws Exception {
        FreeStyleProject project = setupProject("master", false, null, ".*2", null);

        // create initial commit and then run the build against it:
        final String commitFile1 = "commitFile1";
        commit(commitFile1, johnDoe, "Commit number 1");
        build(project, Result.SUCCESS, commitFile1);

        assertFalse("scm polling should not detect any more changes after build", project.pollSCMChanges(listener));

        final String commitFile2 = "commitFile2";
        commit(commitFile2, janeDoe, "Commit number 2");
        assertFalse("scm polling detected commit2 change, which should have been excluded",
            project.pollSCMChanges(listener));

        final String commitFile3 = "commitFile3";
        commit(commitFile3, johnDoe, "Commit number 3");
        assertTrue("scm polling did not detect commit3 change", project.pollSCMChanges(listener));
        //... and build it...
        final FreeStyleBuild build2 = build(project, Result.SUCCESS, commitFile2, commitFile3);
        final Set<User> culprits = build2.getCulprits();
        assertEquals("The build should have two culprit", 2, culprits.size());
        assertTrue("User janeDoe is absent in culprits", doesCulpritsContainUser(culprits, janeDoe.getName()));
        assertTrue("User johnDoe is absent in culprits", doesCulpritsContainUser(culprits, johnDoe.getName()));
        assertTrue(build2.getWorkspace().child(commitFile2).exists());
        assertTrue(build2.getWorkspace().child(commitFile3).exists());
        assertBuildStatusSuccess(build2);
        assertFalse("scm polling should not detect any more changes after build", project.pollSCMChanges(listener));
    }

    public void testBasicExcludedUser() throws Exception {
        FreeStyleProject project = setupProject("master", false, null, null, "Jane Doe");

        // create initial commit and then run the build against it:
        final String commitFile1 = "commitFile1";
        commit(commitFile1, johnDoe, "Commit number 1");
        build(project, Result.SUCCESS, commitFile1);

        assertFalse("scm polling should not detect any more changes after build", project.pollSCMChanges(listener));

        final String commitFile2 = "commitFile2";
        commit(commitFile2, janeDoe, "Commit number 2");
        assertFalse("scm polling detected commit2 change, which should have been excluded",
            project.pollSCMChanges(listener));
        final String commitFile3 = "commitFile3";
        commit(commitFile3, johnDoe, "Commit number 3");
        assertTrue("scm polling did not detect commit3 change", project.pollSCMChanges(listener));
        //... and build it...
        final FreeStyleBuild build2 = build(project, Result.SUCCESS, commitFile2, commitFile3);
        final Set<User> culprits = build2.getCulprits();
        assertEquals("The build should have two culprit", 2, culprits.size());
        assertTrue("User janeDoe is absent in culprits", doesCulpritsContainUser(culprits, janeDoe.getName()));
        assertTrue("User johnDoe is absent in culprits", doesCulpritsContainUser(culprits, johnDoe.getName()));
        assertTrue(build2.getWorkspace().child(commitFile2).exists());
        assertTrue(build2.getWorkspace().child(commitFile3).exists());
        assertBuildStatusSuccess(build2);
        assertFalse("scm polling should not detect any more changes after build", project.pollSCMChanges(listener));

    }

    private boolean doesCulpritsContainUser(Set<User> culprits, String name) {
        for (User culprit : culprits) {
            if (name.equals(culprit.getFullName())) {
                return true;
            }
        }
        return false;
    }
}
