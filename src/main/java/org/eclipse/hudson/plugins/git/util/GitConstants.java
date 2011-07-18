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

package org.eclipse.hudson.plugins.git.util;

/**
 * Common class to store common constants.
 * <p/>
 * Copyright (C) 2011 Hudson-CI.org
 * <p/>
 * Date: 4/7/11
 *
 * @author Anton Kozak
 */
public class GitConstants {
    public static final String GIT_COMMITTER_NAME_ENV_VAR = "GIT_COMMITTER_NAME";
    public static final String GIT_AUTHOR_NAME_ENV_VAR = "GIT_AUTHOR_NAME";
    public static final String GIT_COMMITTER_EMAIL_ENV_VAR = "GIT_COMMITTER_EMAIL";
    public static final String GIT_AUTHOR_EMAIL_ENV_VAR = "GIT_AUTHOR_EMAIL";
    public static final String INTERNAL_TAG_COMMENT_PREFIX = "Hudson Build #";
    public static final String HYPHEN_SYMBOL = "-";
    public static final String INTERNAL_TAG_NAME_PREFIX = "hudson";
}
