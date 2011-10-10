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
package hudson.plugins.git;

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
