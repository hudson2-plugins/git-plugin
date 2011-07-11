/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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
import hudson.model.TaskListener;
import hudson.plugins.git.util.GitConstants;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;

/**
 * Implementation for Git API functionality based on JGit library
 * <p/>
 * Date: 7/8/11
 *
 * @author Nikita Levyankov
 */
public class JGitAPI extends GitAPI implements IGitAPI {

    private Git jGitDelegate;
    private PersonIdent author;
    private PersonIdent committer;

    public JGitAPI(String gitExe, FilePath workspace, TaskListener listener, EnvVars environment) {
        super(gitExe, workspace, listener, environment);
        if (hasGitRepo()) {//Wrap file repository if exists in order to perform operations and initialize jGitDelegate
            try {
                File gitDir = RepositoryCache.FileKey.resolve(new File(workspace.getRemote()), FS.DETECTED);
                jGitDelegate = Git.wrap(new FileRepositoryBuilder().setGitDir(gitDir).build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns author
     *
     * @return {@link PersonIdent}
     */
    protected PersonIdent getAuthor() {
        return author;
    }

    /**
     * Returns committer
     *
     * @return {@link PersonIdent}
     */
    protected PersonIdent getCommitter() {
        return committer;
    }

    public String getGitExe() {
        return gitExe;
    }

    public EnvVars getEnvironment() {
        return environment;
    }

    public void init() throws GitException {
        if (hasGitRepo()) {
            throw new GitException(Messages.GitAPI_Repository_FailedInitTwiceMsg());
        }
        jGitDelegate = Git.init().setDirectory(new File(workspace.getRemote())).call();
    }

    public boolean hasGitRepo() throws GitException {
        return hasGitRepo(Constants.DOT_GIT);
    }

    public boolean hasGitRepo(String gitDir) throws GitException {
        try {
            FilePath dotGit = workspace.child(gitDir);
            return dotGit.exists();
        } catch (SecurityException ex) {
            throw new GitException(Messages.GitAPI_Repository_SecurityFailureCheckMsg(), ex);
        } catch (Exception e) {
            throw new GitException(Messages.GitAPI_Repository_FailedCheckMsg(), e);
        }
    }

    @Override
    public void clean() throws GitException {
        verifyGitRepository();
        jGitDelegate.clean().call();
    }

    @Override
    public boolean isCommitInRepo(String sha1) {
        RevWalk revWalk = new RevWalk(jGitDelegate.getRepository());
        try {
            revWalk.parseCommit(ObjectId.fromString(sha1));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public void add(String filePattern) throws GitException {
        verifyGitRepository();
        try {
            jGitDelegate.add().addFilepattern(filePattern).call();
        } catch (NoFilepatternException ex) {
            throw new GitException(ex);
        }
    }

    public void commit(String message) throws GitException {
        verifyGitRepository();
        parseEnvVars(getEnvironment());
        try {
            jGitDelegate.commit()
                .setMessage(message)
                .setAuthor(getAuthor())
                .setCommitter(getCommitter())
                .call();
        } catch (Exception e) {
            throw new GitException(Messages.GitAPI_Commit_FailedMsg(message), e);
        }
    }

    @Override
    public void clone(final RemoteConfig remoteConfig) throws GitException {
        listener.getLogger().println(Messages.GitAPI_Repository_CloningRepositoryMsg(remoteConfig.getName()));
        try {
            workspace.deleteRecursive();
        } catch (Exception e) {
            e.printStackTrace(listener.error(Messages.GitAPI_Workspace_FailedCleanupMsg()));
            throw new GitException(Messages.GitAPI_Workspace_FailedDeleteMsg(), e);
        }

        // Assume only 1 URL for this repository
        final URIish source = remoteConfig.getURIs().get(0);

        try {
            workspace.act(new FilePath.FileCallable<String>() {

                private static final long serialVersionUID = 1L;

                public String invoke(File workspace,
                                     VirtualChannel channel) throws IOException {
                    jGitDelegate = Git.cloneRepository()
                        .setDirectory(workspace.getAbsoluteFile())
                        .setURI(source.toPrivateString())
                        .setRemote(remoteConfig.getName())
                        .call();
                    return Messages.GitAPI_Repository_CloneSuccessMsg(source.toPrivateString(),
                        workspace.getAbsolutePath());
                }
            });
        } catch (Exception e) {
            throw new GitException(Messages.GitAPI_Repository_FailedCloneMsg(source), e);
        }
    }

    @Override
    public void branch(String name) throws GitException {
        verifyGitRepository();
        try {
            jGitDelegate.branchCreate().setName(name).call();
        } catch (GitAPIException e) {
            throw new GitException(Messages.GitAPI_Branch_CreateErrorMsg(name), e);
        }
    }

    @Override
    public void checkout(String commitish) throws GitException {
        checkoutBranch(null, commitish);
    }

    @Override
    public void checkoutBranch(String branch, String commitish) throws GitException {
        verifyGitRepository();
        try {
            // First, checkout to detached HEAD, so we can delete the branch.
            launchCommand("checkout", "-f", commitish);

            if (null != branch) {
                jGitDelegate.checkout()
                    .setForce(true)
                    .setStartPoint(commitish)
                    .setName(branch)
                    .setCreateBranch(true)
                    .call();
            }
        } catch (GitAPIException e) {
            throw new GitException(Messages.GitAPI_Branch_CheckoutErrorMsg(branch, commitish), e);
        }
    }

    @Override
    public List<Branch> getBranches() throws GitException {
        verifyGitRepository();
        List<Ref> refList = jGitDelegate.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        return parseRefList(refList);
    }

    @Override
    public List<Branch> getRemoteBranches() throws GitException, IOException {
        verifyGitRepository();
        List<Ref> refList = jGitDelegate.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        return parseRefList(refList);
    }

    @Override
    public void deleteBranch(String name) throws GitException {
        verifyGitRepository();
        try {
            jGitDelegate.branchDelete().setBranchNames(name).call();
        } catch (GitAPIException e) {
            throw new GitException(Messages.GitAPI_Branch_DeleteErrorMsg(name), e);
        }
    }

    @Override
    protected Repository getRepository() throws IOException {
        verifyGitRepository();
        return jGitDelegate.getRepository();
    }

    private void verifyGitRepository() {
        if (!hasGitRepo() || null == jGitDelegate) {
            throw new GitException(Messages.GitAPI_Repository_InvalidStateMsg());
        }
    }

    protected List<Branch> parseRefList(List<Ref> refList) {
        List<Branch> result = new ArrayList<Branch>();
        if (CollectionUtils.isNotEmpty(refList)) {
            for (Ref ref : refList) {
                Branch buildBranch = new Branch(ref);
                result.add(buildBranch);
                listener.getLogger().println(Messages.GitAPI_Branch_BranchInRepoMsg(buildBranch.getName()));
            }
        }
        return result;
    }

    /**
     * Check environment variables for authors and committers data. If name or email is not empty -
     * instantiate author/commiter as {@link PersonIdent}
     *
     * @param envVars environment variables.
     * @see GitConstants#GIT_AUTHOR_NAME_ENV_VAR
     * @see GitConstants#GIT_AUTHOR_EMAIL_ENV_VAR
     * @see GitConstants#GIT_COMMITTER_NAME_ENV_VAR
     * @see GitConstants#GIT_COMMITTER_EMAIL_ENV_VAR
     */
    protected void parseEnvVars(EnvVars envVars) {
        author = buildPersonIdent(envVars, GitConstants.GIT_AUTHOR_NAME_ENV_VAR, GitConstants.GIT_AUTHOR_EMAIL_ENV_VAR);
        committer = buildPersonIdent(envVars, GitConstants.GIT_COMMITTER_NAME_ENV_VAR,
            GitConstants.GIT_COMMITTER_EMAIL_ENV_VAR);
    }

    /**
     * Create person from environment vars by name key and email key.
     *
     * @param envVars EnvVars.
     * @param nameEnvKey String.
     * @param emailEnvKey String.
     * @return person instance
     */
    private PersonIdent buildPersonIdent(EnvVars envVars, String nameEnvKey, String emailEnvKey) {
        PersonIdent result = null;
        if (null != envVars) {
            String name = envVars.get(nameEnvKey);
            String email = envVars.get(emailEnvKey);
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(email)) {
                result = new PersonIdent(name, email);
            }
        }
        return result;
    }
}
