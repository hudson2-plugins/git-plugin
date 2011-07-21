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
 * Andrew Bayer, Anton Kozak, Nikita Levyankov
 *
 *******************************************************************************/
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
