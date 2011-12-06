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
import org.eclipse.hudson.plugins.git.browser.GitWeb;
import org.eclipse.hudson.plugins.git.browser.GithubWeb;
import org.eclipse.hudson.plugins.git.browser.RedmineWeb;
import org.eclipse.hudson.plugins.git.browser.ViewGitWeb;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static junit.framework.Assert.assertEquals;

/**
 * Verifies equals and hashcode of GitRepositoryBrowser objects.
 * </p>
 *
 * @author Date: 10/06/2011
 */
@RunWith(Parameterized.class)
public class GitRepositoryBrowserEqualsHashCodeTest {

    private GitRepositoryBrowser defaultBrowser;
    private GitRepositoryBrowser browser;
    private boolean expectedResult;

    public GitRepositoryBrowserEqualsHashCodeTest(GitRepositoryBrowser browser, boolean expectedResult) {
        this.browser = browser;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection generateData() throws MalformedURLException {
        return Arrays.asList(new Object[][]{
            {new CGit("http://cgit.com"), true},
            {new GithubWeb("http://github.com"), false},
            {new GitWeb("http://git.com"), false},
            {new RedmineWeb("http://redmine.com"), false},
            {new ViewGitWeb("http://viewgit.com", "name"), false}
        });
    }

    @Before
    public void setUp() throws MalformedURLException {
        defaultBrowser = new CGit("http://cgit.com");
    }

    @Test
    public void testEquals() {
        assertEquals(expectedResult, defaultBrowser.equals(browser));
    }

    @Test
    public void testHashCode() {
        assertEquals(expectedResult, defaultBrowser.hashCode() == browser.hashCode());
    }
}
