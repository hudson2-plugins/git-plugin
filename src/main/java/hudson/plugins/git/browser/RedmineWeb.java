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
 * Git Browser for <a href="http://www.redmine.org/">Redmine</a>.
 *
 * @author mfriedenhagen
 */
public class RedmineWeb extends GitRepositoryBrowser {

    private static final long serialVersionUID = 1L;

    private final URL url;

    @DataBoundConstructor
    public RedmineWeb(String url) throws MalformedURLException {
        this.url = normalizeToEndWithSlash(new URL(url));
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
        return new URL(url, "diff?rev=" + changeSet.getId().toString());
    }

    /**
     * Creates a link to the file diff.
     * <p/>
     * https://SERVER/PATH/projects/PROJECT/repository/revisions/a9182a07750c9a0dfd89a8461adf72ef5ef0885b/diff/pom.xml
     * <p/>
     * Returns a diff link for {@link EditType#DELETE} and {@link EditType#EDIT}, for {@link EditType#ADD} returns an
     * {@link RedmineWeb#getFileLink(Path)}.
     *
     * @param path affected file path
     * @return diff link
     * @throws IOException
     */
    @Override
    public URL getDiffLink(Path path) throws IOException {
        final GitChangeSet changeSet = path.getChangeSet();
        final URL changeSetLink = new URL(url, "revisions/" + changeSet.getId().toString());
        final URL difflink;
        if (path.getEditType().equals(EditType.ADD)) {
            difflink = getFileLink(path);
        } else {
            difflink = new URL(changeSetLink, changeSetLink.getPath() + "/diff/" + path.getPath());
        }
        return difflink;
    }

    /**
     * Creates a link to the file.
     * https://SERVER/PATH/projects/PROJECT/repository/revisions/a9182a07750c9a0dfd89a8461adf72ef5ef0885b/entry/pom.xml
     * For deleted files just returns a diff link, which will have /dev/null as target file.
     *
     * @param path file
     * @return file link
     * @throws IOException
     */
    @Override
    public URL getFileLink(Path path) throws IOException {
        if (path.getEditType().equals(EditType.DELETE)) {
            return getDiffLink(path);
        } else {
            final String spec = "revisions/" + path.getChangeSet().getId() + "/entry/" + path.getPath();
            return new URL(url, url.getPath() + spec);
        }
    }

    @Extension
    public static class RedmineWebDescriptor extends Descriptor<RepositoryBrowser<?>> {
        public String getDisplayName() {
            return "redmineweb";
        }

        @Override
        public RedmineWeb newInstance(StaplerRequest req, JSONObject jsonObject) throws Descriptor.FormException {
            return req.bindParameters(RedmineWeb.class, "redmineweb.");
        }

        public FormValidation doCheckUrl(@QueryParameter(fixEmpty = true) final String url)
            throws IOException, ServletException {
            return new GitUrlChecker(url, "redmine").check();
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

        RedmineWeb that = (RedmineWeb) o;

        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
