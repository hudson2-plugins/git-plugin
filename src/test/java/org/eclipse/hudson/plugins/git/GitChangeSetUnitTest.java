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
 * Anton Kozak
 *
 *******************************************************************************/
package org.eclipse.hudson.plugins.git;

import hudson.model.User;
import hudson.model.UserProperty;
import java.io.IOException;
import java.util.ArrayList;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.*;

/**
 * Unit tests for {@link GitChangeSet}
 *
 * @author Anton Kozak
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(User.class)
public class GitChangeSetUnitTest {
    private static final String AUTHOR_EMAIL = "root@gmail.com";
    private static final String AUTHOR_NAME = "John Doe";


    @Test
    public void testFindOrCreateUserBasedOnEmail() throws IOException {
        mockStatic(User.class);
        User user = createMock(User.class);
        user.setFullName(AUTHOR_NAME);
        user.save();
        user.addProperty(EasyMock.<UserProperty>anyObject());
        expect(User.get(AUTHOR_EMAIL, true)).andReturn(user);
        replay(User.class, user);
        GitChangeSet changeSet = new GitChangeSet(new ArrayList<String>(), true);
        changeSet.findOrCreateUser(AUTHOR_NAME, AUTHOR_EMAIL, true);
        verify(User.class, user);
    }

    @Test
    public void testFindOrCreateUserBasedOnName() throws IOException {
        mockStatic(User.class);
        User user = createMock(User.class);
        user.addProperty(EasyMock.<UserProperty>anyObject());
        expect(User.get(AUTHOR_NAME, true)).andReturn(user);
        replay(User.class, user);
        GitChangeSet changeSet = new GitChangeSet(new ArrayList<String>(), false);
        changeSet.findOrCreateUser(AUTHOR_NAME, AUTHOR_EMAIL, false);
        verify(User.class, user);
    }

    @Test
    public void testFindOrCreateUserWithEmptyEmail() throws IOException {
        mockStatic(User.class);
        User user = createMock(User.class);
        expect(User.get(AUTHOR_NAME, true)).andReturn(user);
        replay(User.class, user);
        GitChangeSet changeSet = new GitChangeSet(new ArrayList<String>(), false);
        changeSet.findOrCreateUser(AUTHOR_NAME, "", false);
        verify(User.class, user);
    }
}
