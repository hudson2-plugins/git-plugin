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

package hudson.plugins.git;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.RemoteConfig;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestPluginManager;
import org.jvnet.hudson.test.recipes.Recipe;

/**
 * Base test case for Git related stuff.
 *
 * @author Kohsuke Kawaguchi
 * @author ishaaq
 */
public abstract class AbstractGitTestCase extends HudsonTestCase {
    protected File workDir;
    protected GitAPI git;
    protected TaskListener listener;
    private EnvVars envVars;
    protected FilePath workspace;
    protected final PersonIdent johnDoe = new PersonIdent("John Doe", "john@doe.com");
    protected final PersonIdent janeDoe = new PersonIdent("Jane Doe", "jane@doe.com");

    @Override
    protected void setUp() throws Exception {
        System.setProperty("hudson.PluginStrategy", "hudson.ClassicPluginStrategy");
        super.setUp();
        workDir = createTmpDir();
        listener = new StreamTaskListener();
        envVars = new EnvVars();
        setAuthor(johnDoe);
        setCommitter(johnDoe);
        workspace = new FilePath(workDir);
        GitTool.onLoaded();
        git = new GitAPI("git", workspace, listener, envVars);
        git.init();
    }

    protected void setAuthor(final PersonIdent author) {
        envVars.put("GIT_AUTHOR_NAME", author.getName());
        envVars.put("GIT_AUTHOR_EMAIL", author.getEmailAddress());
    }

    protected void setCommitter(final PersonIdent committer) {
        envVars.put("GIT_COMMITTER_NAME", committer.getName());
        envVars.put("GIT_COMMITTER_EMAIL", committer.getEmailAddress());
    }

    protected void commit(final String fileName, final PersonIdent committer, final String message)
        throws GitException {
        setAuthor(committer);
        setCommitter(committer);
        FilePath file = workspace.child(fileName);
        try {
            file.write(fileName, null);
        } catch (Exception e) {
            throw new GitException("unable to write file", e);
        }

        git.add(fileName);
        git.launchCommand("commit", "-m", message);
    }

    protected void commit(final String fileName, final PersonIdent author, final PersonIdent committer,
                          final String message) throws GitException {
        setAuthor(author);
        setCommitter(committer);
        FilePath file = workspace.child(fileName);
        try {
            file.write(fileName, null);
        } catch (Exception e) {
            throw new GitException("unable to write file", e);
        }
        git.add(fileName);
        git.launchCommand("commit", "-m", message);
    }

    protected List<RemoteConfig> createRemoteRepositories(String relativeTargetDir) throws IOException,
        Descriptor.FormException {
        return GitSCM.DescriptorImpl.createRepositoryConfigurations(
            new String[]{workDir.getAbsolutePath()},
            new String[]{"origin"},
            new String[]{""}
        );
    }

}
