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

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.browsers.QueryBuilder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.eclipse.hudson.plugins.git.GitChangeSet;
import org.eclipse.hudson.plugins.git.GitChangeSet.Path;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Git Browser URLs
 */
public class GitWeb extends GitRepositoryBrowser {

    private static final long serialVersionUID = 1L;
    private final URL url;

    @DataBoundConstructor
    public GitWeb(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
        return new URL(url, url.getPath() + param().add("a=commit").add("h=" + changeSet.getId()).toString());
    }

    private QueryBuilder param() {
        return new QueryBuilder(url.getQuery());
    }

    /**
     * Creates a link to the file diff.
     * http://[GitWeb URL]?a=blobdiff;f=[path];fp=[path];h=[dst];hp=[src];hb=[commit];hpb=[parent commit]
     *
     * @param path affected file path
     * @return diff link
     * @throws IOException
     */
    @Override
    public URL getDiffLink(Path path) throws IOException {
        if (path.getEditType() != EditType.EDIT || path.getSrc() == null || path.getDst() == null
            || path.getChangeSet().getParentCommit() == null) {
            return null;
        }
        GitChangeSet changeSet = path.getChangeSet();
        String spec = param().add("a=blobdiff").add("f=" + path.getPath()).add("fp=" + path.getPath())
            .add("h=" + path.getSrc()).add("hp=" + path.getDst())
            .add("hb=" + changeSet.getId()).add("hpb=" + changeSet.getParentCommit()).toString();
        return new URL(url, url.getPath() + spec);
    }

    /**
     * Creates a link to the file.
     * http://[GitWeb URL]?a=blob;f=[path];h=[dst, or src for deleted files];hb=[commit]
     *
     * @param path file
     * @return file link
     * @throws IOException
     */
    @Override
    public URL getFileLink(Path path) throws IOException {
        String h = (path.getDst() != null) ? path.getDst() : path.getSrc();
        String spec = param().add("a=blob").add("f=" + path.getPath())
            .add("h=" + h).add("hb=" + path.getChangeSet().getId()).toString();
        return new URL(url, url.getPath() + spec);
    }

    @Extension
    public static class GitWebDescriptor extends Descriptor<RepositoryBrowser<?>> {
        public String getDisplayName() {
            return "gitweb";
        }

        @Override
        public GitWeb newInstance(StaplerRequest req, JSONObject jsonObject) throws Descriptor.FormException {
            return req.bindParameters(GitWeb.class, "gitweb.");
        }

        public FormValidation doCheckUrl(@QueryParameter(fixEmpty = true) final String url)
            throws IOException, ServletException {
            return new GitUrlChecker(url, "gitweb").check();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GitWeb gitWeb = (GitWeb) o;

        if (url != null ? !url.equals(gitWeb.url) : gitWeb.url != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
