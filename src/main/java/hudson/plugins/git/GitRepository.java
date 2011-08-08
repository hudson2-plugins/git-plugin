package hudson.plugins.git;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
}
