/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Andrew Bayer, Anton Kozak, Nikita Levyankov
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

import hudson.scm.RepositoryBrowser;
import hudson.util.FormValidation;
import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import org.eclipse.hudson.plugins.git.GitChangeSet;

public abstract class GitRepositoryBrowser extends RepositoryBrowser<GitChangeSet> {
    /**
     * Determines the link to the diff between the version
     * in the specified revision of {@link GitChangeSet.Path} to its previous version.
     *
     * @param path affected file path
     * @return null if the browser doesn't have any URL for diff.
     * @throws IOException
     */
    public abstract URL getDiffLink(GitChangeSet.Path path) throws IOException;

    /**
     * Determines the link to a single file under Git.
     * This page should display all the past revisions of this file, etc.
     *
     * @param path affected file path
     * @return null if the browser doesn't have any suitable URL.
     * @throws IOException
     */
    public abstract URL getFileLink(GitChangeSet.Path path) throws IOException;

    private static final long serialVersionUID = 1L;

    protected static class GitUrlChecker extends FormValidation.URLCheck {

        private String url;
        private String browserName;

        public GitUrlChecker(String url, String gitBrowserName) {
            this.url = url;
            this.browserName = gitBrowserName;
        }

        @Override
        protected FormValidation check() throws IOException, ServletException {
            if (null == url) {
                return FormValidation.ok();
            }
            String v = url;
            if (!v.endsWith("/")) {
                v += '/';
            }
            try {
                if (findText(open(new URL(v)), browserName)) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("This is a valid URL but it doesn't look like " + browserName);
                }
            } catch (IOException e) {
                return handleIOException(v, e);
            }
        }
    }
}
