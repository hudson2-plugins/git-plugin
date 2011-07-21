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

import hudson.Launcher;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.scm.NullSCM;
import hudson.tasks.BuildStepDescriptor;
import java.util.Collections;
import org.eclipse.hudson.plugins.git.GitPublisher.BranchToPush;
import org.eclipse.hudson.plugins.git.GitPublisher.TagToPush;
import org.jvnet.hudson.test.Bug;

/**
 * Tests for {@link GitPublisher}
 *
 * @author Kohsuke Kawaguchi
 */
public class GitPublisherTest extends AbstractGitTestCase {
    @Bug(5005)
    public void testMatrixBuild() throws Exception {
        final int[] run = new int[1]; // count the number of times the perform is called

        commit("a", johnDoe, "commit #1");

        MatrixProject mp = createMatrixProject("xyz");
        mp.setAxes(new AxisList(new Axis("VAR", "a", "b")));
        mp.setScm(new GitSCM(workDir.getAbsolutePath()));
        mp.getPublishersList().add(new GitPublisher(
            Collections.singletonList(new TagToPush("origin", "foo", true)),
            Collections.<BranchToPush>emptyList(), true, true) {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                throws InterruptedException {
                run[0]++;
                try {
                    return super.perform(build, launcher, listener);
                } finally {
                    // until the 3rd one (which is the last one), we shouldn't create a tag
                    if (run[0] < 3) {
                        assertFalse(existsTag("foo"));
                    }
                }
            }

            @Override
            public BuildStepDescriptor getDescriptor() {
                return (BuildStepDescriptor) Hudson.getInstance().getDescriptorOrDie(GitPublisher.class); // fake
            }

            private Object writeReplace() {
                return new NullSCM();
            }
        });

        MatrixBuild b = assertBuildStatusSuccess(mp.scheduleBuild2(0).get());
        System.out.println(b.getLog());

        assertTrue(existsTag("foo"));

        // twice for MatrixRun, which is to be ignored, then once for matrix completion
        assertEquals(3, run[0]);
    }

    private boolean existsTag(String tag) {
        String tags = git.launchCommand("tag");
        System.out.println(tags);
        return tags.contains(tag);
    }
}
