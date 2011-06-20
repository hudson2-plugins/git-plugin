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

import junit.framework.Assert;
import org.junit.Test;

public class TestBranchSpec{
    @Test
    public void testMatch() {
        BranchSpec l = new BranchSpec("master");
        Assert.assertTrue(l.matches("origin/master"));
        Assert.assertFalse(l.matches("origin/something/master"));
        Assert.assertFalse(l.matches("master"));
        Assert.assertFalse(l.matches("dev"));
        
        
        BranchSpec est = new BranchSpec("origin/*/dev");
        
        Assert.assertFalse(est.matches("origintestdev"));
        Assert.assertTrue(est.matches("origin/test/dev"));
        Assert.assertFalse(est.matches("origin/test/release"));
        Assert.assertFalse(est.matches("origin/test/somthing/release"));
        
        BranchSpec s = new BranchSpec("origin/*");
        
        Assert.assertTrue(s.matches("origin/master"));
      
        BranchSpec m = new BranchSpec("**/magnayn/*");
        
        Assert.assertTrue(m.matches("origin/magnayn/b1"));
        Assert.assertTrue(m.matches("remote/origin/magnayn/b1"));
      
        BranchSpec n = new BranchSpec("*/my.branch/*");
        
        Assert.assertTrue(n.matches("origin/my.branch/b1"));
        Assert.assertFalse(n.matches("origin/my-branch/b1"));
        Assert.assertFalse(n.matches("remote/origin/my.branch/b1"));
      
        BranchSpec o = new BranchSpec("**");
        
        Assert.assertTrue(o.matches("origin/my.branch/b1"));
        Assert.assertTrue(o.matches("origin/my-branch/b1"));
        Assert.assertTrue(o.matches("remote/origin/my.branch/b1"));
      
        BranchSpec p = new BranchSpec("*");

        Assert.assertTrue(p.matches("origin/x"));
        Assert.assertFalse(p.matches("origin/my-branch/b1"));
    }

    @Test
    public void testEmptyBranch() {
        BranchSpec l = new BranchSpec("");
        Assert.assertTrue(l.matches("origin/master"));
    }

}
