/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Anton Kozak, Nikita Levyankov
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

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Verifies equals and hashCode methods of SubmoduleConfig objects.
 * </p>
 *
 * Date: 10/6/11
 */
public class SubmoduleConfigEqualsHashCodeTest {
    @Test
    public void testEquals() {
        SubmoduleConfig config1 = new SubmoduleConfig();
        SubmoduleConfig config2 = new SubmoduleConfig();
        config1.setSubmoduleName("submodule");
        config2.setSubmoduleName("submodule");
        String[] branches1 = new String[] {"branch1", "branch2"};
        String[] branches2 = new String[] {"branch2", "branch1"};
        config1.setBranches(branches1);
        assertFalse(config1.equals(config2));
        config2.setBranches(branches2);
        assertEquals(config1, config2);
    }

    @Test
    public void testHashCode() {
        SubmoduleConfig config1 = new SubmoduleConfig();
        SubmoduleConfig config2 = new SubmoduleConfig();
        config1.setSubmoduleName("submodule");
        config2.setSubmoduleName("submodule");
        String[] branches1 = new String[] {"branch1", "branch2",};
        config1.setBranches(branches1);
        assertFalse(config1.hashCode() == config2.hashCode());
    }
}
