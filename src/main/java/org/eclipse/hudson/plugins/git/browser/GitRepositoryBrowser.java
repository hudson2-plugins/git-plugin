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
