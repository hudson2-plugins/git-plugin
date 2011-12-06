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
 * Verifies equals and hashcode of BranchSpec objects.
 * </p>
 * Date: 10/06/2011
 */
public class BranchSpecEqualsHashCodeTest {

    private BranchSpec spec = new BranchSpec("name");

    @Test
    public void testEquals() {
        assertEquals(spec, new BranchSpec("name"));
        assertFalse(spec.equals(new BranchSpec("name1")));
    }

    @Test
    public void testHashCode() {
        assertEquals(spec.hashCode(), new BranchSpec("name").hashCode());
        assertFalse(spec.hashCode() == new BranchSpec("name1").hashCode());
    }
}
