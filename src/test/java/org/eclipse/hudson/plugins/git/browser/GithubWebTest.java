/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Mirko Friedenhagen, Andrew Bayer, Anton Kozak, Nikita Levyankov
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
package org.eclipse.hudson.plugins.git.browser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.hudson.plugins.git.GitChangeLogParser;
import org.eclipse.hudson.plugins.git.GitChangeSet;
import org.eclipse.hudson.plugins.git.GitChangeSet.Path;
import org.xml.sax.SAXException;

/**
 * @author mirko
 *
 */
public class GithubWebTest extends TestCase {

    /**
     *
     */
    private static final String GITHUB_URL = "http://github.com/USER/REPO";
    private final GithubWeb githubWeb;

    {
        try {
            githubWeb = new GithubWeb(GITHUB_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test method for {@link org.eclipse.hudson.plugins.git.browser.GithubWeb#getUrl()}.
     * @throws MalformedURLException
     */
    public void testGetUrl() throws MalformedURLException {
        assertEquals(String.valueOf(githubWeb.getUrl()), GITHUB_URL  + "/");
    }

    /**
     * Test method for {@link org.eclipse.hudson.plugins.git.browser.GithubWeb#getUrl()}.
     * @throws MalformedURLException
     */
    public void testGetUrlForRepoWithTrailingSlash() throws MalformedURLException {
        assertEquals(String.valueOf(new GithubWeb(GITHUB_URL + "/").getUrl()), GITHUB_URL  + "/");
    }

    /**
     * Test method for {@link org.eclipse.hudson.plugins.git.browser.GithubWeb#getChangeSetLink(org.eclipse.hudson.plugins.git.GitChangeSet)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetChangeSetLinkGitChangeSet() throws IOException, SAXException {
        final URL changeSetLink = githubWeb.getChangeSetLink(createChangeSet("rawchangelog"));
        assertEquals(GITHUB_URL + "/commit/396fc230a3db05c427737aa5c2eb7856ba72b05d", changeSetLink.toString());
    }

    /**
     * Test method for {@link org.eclipse.hudson.plugins.git.browser.GithubWeb#getDiffLink(org.eclipse.hudson.plugins.git.GitChangeSet.Path)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetDiffLinkPath() throws IOException, SAXException {
        final HashMap<String, Path> pathMap = createPathMap("rawchangelog");
        final Path path1 = pathMap.get("src/main/java/hudson/plugins/git/browser/GithubWeb.java");
        assertEquals(GITHUB_URL + "/commit/396fc230a3db05c427737aa5c2eb7856ba72b05d#diff-0", githubWeb.getDiffLink(path1).toString());
        final Path path2 = pathMap.get("src/test/java/hudson/plugins/git/browser/GithubWebTest.java");
        assertEquals(GITHUB_URL + "/commit/396fc230a3db05c427737aa5c2eb7856ba72b05d#diff-1", githubWeb.getDiffLink(path2).toString());
        final Path path3 = pathMap.get("src/test/resources/hudson/plugins/git/browser/rawchangelog-with-deleted-file");
        assertNull("Do not return a diff link for added files.", githubWeb.getDiffLink(path3));
    }

    /**
     * Test method for {@link org.eclipse.hudson.plugins.git.browser.GithubWeb#getFileLink(org.eclipse.hudson.plugins.git.GitChangeSet.Path)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetFileLinkPath() throws IOException, SAXException {
        final HashMap<String,Path> pathMap = createPathMap("rawchangelog");
        final Path path = pathMap.get("src/main/java/hudson/plugins/git/browser/GithubWeb.java");
        final URL fileLink = githubWeb.getFileLink(path);
        assertEquals(GITHUB_URL  + "/blob/396fc230a3db05c427737aa5c2eb7856ba72b05d/src/main/java/hudson/plugins/git/browser/GithubWeb.java", String.valueOf(fileLink));
    }

    /**
     * Test method for {@link org.eclipse.hudson.plugins.git.browser.GithubWeb#getFileLink(org.eclipse.hudson.plugins.git.GitChangeSet.Path)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetFileLinkPathForDeletedFile() throws IOException, SAXException {
        final HashMap<String,Path> pathMap = createPathMap("rawchangelog-with-deleted-file");
        final Path path = pathMap.get("bar");
        final URL fileLink = githubWeb.getFileLink(path);
        assertEquals(GITHUB_URL + "/commit/fc029da233f161c65eb06d0f1ed4f36ae81d1f4f#diff-0", String.valueOf(fileLink));
    }

    private GitChangeSet createChangeSet(String rawchangelogpath) throws IOException, SAXException {
        final File rawchangelog = new File(GithubWebTest.class.getResource(rawchangelogpath).getFile());
        final GitChangeLogParser logParser = new GitChangeLogParser(false);
        final List<GitChangeSet> changeSetList = logParser.parse(null, rawchangelog).getLogs();
        return changeSetList.get(0);
    }

    /**
     * @param changelog
     * @return
     * @throws IOException
     * @throws SAXException
     */
    private HashMap<String, Path> createPathMap(final String changelog) throws IOException, SAXException {
        final HashMap<String, Path> pathMap = new HashMap<String, Path>();
        final Collection<Path> changeSet = createChangeSet(changelog).getPaths();
        for (final Path path : changeSet) {
            pathMap.put(path.getPath(), path);
        }
        return pathMap;
    }


}
