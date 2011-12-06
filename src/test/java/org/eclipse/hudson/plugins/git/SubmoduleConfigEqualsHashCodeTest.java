/*******************************************************************************
 *
 * Copyright (c) 2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * Anton Kozak, Nikita Levyankov
 *
 *******************************************************************************/
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
