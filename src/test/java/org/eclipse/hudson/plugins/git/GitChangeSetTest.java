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

package org.eclipse.hudson.plugins.git;

import hudson.scm.EditType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.eclipse.hudson.plugins.git.GitChangeSet.Path;

public class GitChangeSetTest extends TestCase {
    
    public GitChangeSetTest(String testName) {
        super(testName);
    }

    private GitChangeSet genChangeSet(boolean authorOrCommitter, boolean useLegacyFormat) {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("Some header junk we should ignore...");
        lines.add("header line 2");
        lines.add("commit 123abc456def");
        lines.add("tree 789ghi012jkl");
        lines.add("parent 345mno678pqr");
        lines.add("author John Author <jauthor@nospam.com> 1234567 -0600");
        lines.add("committer John Committer <jcommitter@nospam.com> 1234567 -0600");
        lines.add("");
        lines.add("    Commit title.");
        lines.add("    Commit extended description.");
        lines.add("");
        if (useLegacyFormat) {
            lines.add("123abc456def");
            lines.add(" create mode 100644 some/file1");
            lines.add(" delete mode 100644 other/file2");
        }
        lines.add(":000000 123456 0000000000000000000000000000000000000000 123abc456def789abc012def345abc678def901a A\tsrc/test/add.file");
        lines.add(":123456 000000 123abc456def789abc012def345abc678def901a 0000000000000000000000000000000000000000 D\tsrc/test/deleted.file");
        lines.add(":123456 789012 123abc456def789abc012def345abc678def901a bc234def567abc890def123abc456def789abc01 M\tsrc/test/modified.file");
        lines.add(":123456 789012 123abc456def789abc012def345abc678def901a bc234def567abc890def123abc456def789abc01 R012\tsrc/test/renamedFrom.file\tsrc/test/renamedTo.file");
        lines.add(":000000 123456 bc234def567abc890def123abc456def789abc01 123abc456def789abc012def345abc678def901a C100\tsrc/test/original.file\tsrc/test/copyOf.file");

        return new GitChangeSet(lines, authorOrCommitter);
    }
    
    public void testLegacyChangeSet() {
        GitChangeSet changeSet = genChangeSet(false, true);
        assertChangeSet(changeSet);
    }
    
    public void testChangeSet() {
    	GitChangeSet changeSet = genChangeSet(false, false);
    	assertChangeSet(changeSet);
    }

	private void assertChangeSet(GitChangeSet changeSet) {
		Assert.assertEquals("123abc456def", changeSet.getId());
        Assert.assertEquals("Commit title.", changeSet.getMsg());
        Assert.assertEquals("Commit title.\nCommit extended description.\n", changeSet.getComment());
        HashSet<String> expectedAffectedPaths = new HashSet<String>(7);
        expectedAffectedPaths.add("src/test/add.file");
        expectedAffectedPaths.add("src/test/deleted.file");
        expectedAffectedPaths.add("src/test/modified.file");
        expectedAffectedPaths.add("src/test/renamedFrom.file");
        expectedAffectedPaths.add("src/test/renamedTo.file");
        expectedAffectedPaths.add("src/test/copyOf.file");
        Assert.assertEquals(expectedAffectedPaths, changeSet.getAffectedPaths());

        Collection<Path> actualPaths = changeSet.getPaths();
        Assert.assertEquals(6, actualPaths.size());
        for (Path path : actualPaths) {
            if ("src/test/add.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.ADD, path.getEditType());
                Assert.assertNull(path.getSrc());
                Assert.assertEquals("123abc456def789abc012def345abc678def901a", path.getDst());
            } else if ("src/test/deleted.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.DELETE, path.getEditType());
                Assert.assertEquals("123abc456def789abc012def345abc678def901a", path.getSrc());
                Assert.assertNull(path.getDst());
            } else if ("src/test/modified.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.EDIT, path.getEditType());
                Assert.assertEquals("123abc456def789abc012def345abc678def901a", path.getSrc());
                Assert.assertEquals("bc234def567abc890def123abc456def789abc01", path.getDst());
            } else if ("src/test/renamedFrom.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.DELETE, path.getEditType());
                Assert.assertEquals("123abc456def789abc012def345abc678def901a", path.getSrc());
                Assert.assertEquals("bc234def567abc890def123abc456def789abc01", path.getDst());
            } else if ("src/test/renamedTo.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.ADD, path.getEditType());
                Assert.assertEquals("123abc456def789abc012def345abc678def901a", path.getSrc());
                Assert.assertEquals("bc234def567abc890def123abc456def789abc01", path.getDst());
            } else if ("src/test/copyOf.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.ADD, path.getEditType());
                Assert.assertEquals("bc234def567abc890def123abc456def789abc01", path.getSrc());
                Assert.assertEquals("123abc456def789abc012def345abc678def901a", path.getDst());
            } else {
                Assert.fail("Unrecognized path.");
            }
        }
	}

    public void testAuthorOrCommitter() {
        GitChangeSet committerCS = genChangeSet(false, false);

        Assert.assertEquals("John Committer", committerCS.getAuthorName());

        GitChangeSet authorCS = genChangeSet(true, false);

        Assert.assertEquals("John Author", authorCS.getAuthorName());
    }
}
