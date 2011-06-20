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

import java.util.ArrayList;
import java.util.Collection;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.spearce.jgit.lib.ObjectId;

/**
 * A Revision is a SHA1 in the object tree, and the collection of branches
 * that share this ID. Unlike other SCMs, git can have >1 branches point
 * at the _same_ commit.
 *
 * @author magnayn
 */
@ExportedBean(defaultVisibility = 999)
public class Revision implements java.io.Serializable, Cloneable {
    private static final long serialVersionUID = -7203898556389073882L;

    ObjectId sha1;
    Collection<Branch> branches;

    public Revision(ObjectId sha1) {
        this.sha1 = sha1;
        this.branches = new ArrayList<Branch>();
    }

    public Revision(ObjectId sha1, Collection<Branch> branches) {
        this.sha1 = sha1;
        this.branches = branches;
    }

    public ObjectId getSha1() {
        return sha1;
    }

    @Exported(name = "SHA1")
    public String getSha1String() {
        return sha1 == null ? "" : sha1.name();
    }

    public void setSha1(ObjectId sha1) {
        this.sha1 = sha1;
    }

    @Exported(name = "branch")
    public Collection<Branch> getBranches() {
        return branches;
    }

    public void setBranches(Collection<Branch> branches) {
        this.branches = branches;
    }

    public boolean containsBranchName(String name) {
        for (Branch b : branches) {
            if (b.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        String s = "Revision " + sha1.name() + " (";
        for (Branch br : branches) {
            s += br.getName() + ", ";
        }
        if (s.endsWith(", ")) {
            s = s.substring(0, s.length() - 2);
        }
        s += ")";

        return s;
    }

    @Override
    public Revision clone() {
        Revision clone;
        try {
            clone = (Revision) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error cloning Revision", e);
        }
        clone.branches = new ArrayList<Branch>(branches);
        return clone;
    }

}
