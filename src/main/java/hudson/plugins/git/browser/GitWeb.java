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

package hudson.plugins.git.browser;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.browsers.QueryBuilder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
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
