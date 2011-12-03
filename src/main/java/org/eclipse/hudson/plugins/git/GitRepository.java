package org.eclipse.hudson.plugins.git;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.transport.RemoteConfig;

/**
 * Git repository config.
 * <p/>
 * Date: 8/8/11
 *
 * @author Nikita Levyankov
 */
public class GitRepository extends RemoteConfig {

    static final String REMOTE_SECTION = "remote";

    static final String TARGET_DIR_KEY = "targetDir";

    private static final String DEFAULT_TARGET_DIR = "";

    private String relativeTargetDir;

    public GitRepository(Config config, String name) throws URISyntaxException {
        super(config, name);
        String val = config.getString(REMOTE_SECTION, name, TARGET_DIR_KEY);
        if (val == null) {
            val = DEFAULT_TARGET_DIR;
        }
        relativeTargetDir = val;
    }

    public static List<RemoteConfig> getAllGitRepositories(final Config rc) throws URISyntaxException {
        final List<String> names = new ArrayList<String>(rc.getSubsections(REMOTE_SECTION));
        Collections.sort(names);

        final List<RemoteConfig> result = new ArrayList<RemoteConfig>(names.size());
        for (final String name : names) {
            result.add(new GitRepository(rc, name));
        }
        return result;
    }

    public String getRelativeTargetDir() {
        return relativeTargetDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GitRepository that = (GitRepository) o;

        if (!CollectionUtils.isEqualCollection(this.getFetchRefSpecs(), that.getFetchRefSpecs())) {
            return false;
        }

        if (!CollectionUtils.isEqualCollection(this.getPushRefSpecs(), that.getPushRefSpecs())) {
            return false;
        }

        if (!CollectionUtils.isEqualCollection(this.getPushURIs(), that.getPushURIs())) {
            return false;
        }

        if (!CollectionUtils.isEqualCollection(this.getURIs(), that.getURIs())) {
            return false;
        }

        return new EqualsBuilder()
            .append(getName(), that.getName())
            .append(getReceivePack(), that.getReceivePack())
            .append(getTagOpt(), that.getTagOpt())
            .append(getTimeout(), that.getTimeout())
            .append(getUploadPack(), that.getUploadPack())
            .append(isMirror(), that.isMirror())
            .append(relativeTargetDir, that.relativeTargetDir)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getFetchRefSpecs())
            .append(getPushRefSpecs())
            .append(getPushURIs())
            .append(getURIs())
            .append(getName())
            .append(getReceivePack())
            .append(getTagOpt())
            .append(getTimeout())
            .append(getUploadPack())
            .append(isMirror())
            .append(relativeTargetDir)
            .toHashCode();

    }

}
