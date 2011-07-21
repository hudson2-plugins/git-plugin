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

package org.eclipse.hudson.plugins.git.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link GitUtils}
 * <p/>
 * Date: 6/20/11
 *
 * @author Anton Kozak
 */
public class GitUtilsTest {
    @Test
    public void testIsEmpty(){
        assertTrue(GitUtils.isEmpty(new String[]{}));
        assertTrue(GitUtils.isEmpty(new String[]{"",""}));
        assertFalse(GitUtils.isEmpty(new String[]{"url1", ""}));
    }
}
