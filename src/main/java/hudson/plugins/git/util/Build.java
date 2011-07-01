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

package hudson.plugins.git.util;

import hudson.model.Result;
import hudson.plugins.git.Revision;
import java.io.Serializable;
import org.eclipse.jgit.lib.ObjectId;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 999)
public class Build implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    /**
     * Revision marked as being built.
     */
    public Revision revision;

    /**
     * Revision that was subject to a merge.
     */
    public Revision mergeRevision;

    public int hudsonBuildNumber;
    public Result hudsonBuildResult;

    // TODO: We don't currently store the result correctly.

    public Build(Revision revision, int buildNumber, Result result) {
        this.revision = revision;
        this.hudsonBuildNumber = buildNumber;
        this.hudsonBuildResult = result;
    }

    public ObjectId getSHA1() {
        return revision.getSha1();
    }

    @Exported
    public Revision getRevision() {
        return revision;
    }

    @Exported
    public int getBuildNumber() {
        return hudsonBuildNumber;
    }

    @Exported
    public Result getBuildResult() {
        return hudsonBuildResult;
    }

    public
    @Override
    String toString() {
        String str = "Build #" + hudsonBuildNumber + " of " + revision.toString();
        if (mergeRevision != null) {
            str += " merged with " + mergeRevision;
        }
        return str;
    }

    @Override
    public Build clone() {
        Build clone;
        try {
            clone = (Build) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error cloning Build", e);
        }

        if (revision != null) {
            clone.revision = revision.clone();
        }
        if (mergeRevision != null) {
            clone.mergeRevision = mergeRevision.clone();
        }

        return clone;
    }
}