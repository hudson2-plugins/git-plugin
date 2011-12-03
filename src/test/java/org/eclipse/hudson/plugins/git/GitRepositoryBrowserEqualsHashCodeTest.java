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
