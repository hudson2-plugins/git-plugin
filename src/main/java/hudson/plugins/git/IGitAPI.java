package hudson.plugins.git;

import hudson.EnvVars;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.RemoteConfig;

/**
 * Encapsulates Git operations on a particular directory through git(1).
 */
public interface IGitAPI {
    String getGitExe();

    EnvVars getEnvironment();

    /**
     * Inits git repository.
     *
     * @throws GitException if repository exists.
     */
    public void init() throws GitException;

    /**
     * Checks whether .git directory exists
     *
     * @return true if the encapsulated directory has ".git" directory, false - otherwise.
     * @throws GitException if any.
     */
    boolean hasGitRepo() throws GitException;

    /**
     * Checks whether commit presents in repository.
     *
     * @param sha1 commit
     * @return true if presents, false - otherwise.
     */
    boolean isCommitInRepo(String sha1);

    boolean hasGitModules() throws GitException;

    boolean hasGitModules(String treeIsh) throws GitException;

    List<IndexEntry> getSubmodules(String treeIsh) throws GitException;

    String getRemoteUrl(String name) throws GitException;

    void setRemoteUrl(String name, String url) throws GitException;

    String getRemoteUrl(String name, String GIT_DIR) throws GitException;

    void setRemoteUrl(String name, String url, String GIT_DIR) throws GitException;

    String getDefaultRemote(String _default_) throws GitException;

    boolean isBareRepository() throws GitException;

    boolean isBareRepository(String GIT_DIR) throws GitException;

    void submoduleInit() throws GitException;

    void submoduleUpdate(boolean recursive) throws GitException;

    void submoduleClean(boolean recursive) throws GitException;

    void submoduleSync() throws GitException;

    String getSubmoduleUrl(String name) throws GitException;

    void setSubmoduleUrl(String name, String url) throws GitException;

    void fixSubmoduleUrls(String remote, TaskListener listener) throws GitException;

    void setupSubmoduleUrls(Revision rev, TaskListener listener) throws GitException;

    void setupSubmoduleUrls(String remote, TaskListener listener) throws GitException;

    public void fetch(String repository, String refspec) throws GitException;

    void fetch(RemoteConfig remoteRepository);

    void fetch() throws GitException;

    void push(RemoteConfig repository, String revspec) throws GitException;

    void merge(String revSpec) throws GitException;

    void clone(RemoteConfig source) throws GitException;

    void clean() throws GitException;

    void prune(RemoteConfig repository) throws GitException;

    ObjectId revParse(String revName) throws GitException;

    List<Branch> getBranches() throws GitException;

    List<Branch> getRemoteBranches() throws GitException, IOException;

    List<Branch> getBranchesContaining(String revspec) throws GitException;

    List<IndexEntry> lsTree(String treeIsh) throws GitException;

    List<ObjectId> revListBranch(String branchId) throws GitException;

    List<ObjectId> revListAll() throws GitException;

    String describe(String commitIsh) throws GitException;

    List<Tag> getTagsOnCommit(String revName) throws GitException, IOException;

    void tag(String tagName, String comment) throws GitException;

    boolean tagExists(String tagName) throws GitException;

    void deleteTag(String tagName) throws GitException;

    Set<String> getTagNames(String tagPattern) throws GitException;

    void changelog(String revFrom, String revTo, OutputStream fos) throws GitException;

    /**
     * Short for {@code checkoutBranch(null, commitish)}
     *
     * @param commitish commitish value.
     * @throws GitException if any.
     */
    void checkout(String commitish) throws GitException;

    /**
     * Checks out the specified commit/ref into the workspace.
     *
     * @param branch If non-null, move/create the branch in this name at the specified commit-ish and check out that branch.
     * @param commitish commitish value.
     * @throws GitException if any.
     */
    void checkoutBranch(String branch, String commitish) throws GitException;

    void add(String filePattern) throws GitException;

    void branch(String name) throws GitException;

    void deleteBranch(String name) throws GitException;

    /**
     * @param f file with commit message.
     * @throws GitException if any.
     */
    void commit(File f) throws GitException;

    /**
     * Performs commit into repository with given message.
     *
     * @param message commit message.
     * @throws GitException if any.
     */
    void commit(String message) throws GitException;

    ObjectId mergeBase(ObjectId sha1, ObjectId sha12);

    String getAllLogEntries(String branch);

    List<String> showRevision(Revision r) throws GitException;
}
