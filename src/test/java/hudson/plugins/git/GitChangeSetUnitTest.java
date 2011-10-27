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
