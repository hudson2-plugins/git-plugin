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
package org.eclipse.hudson.plugins.git;

public class GitException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GitException() {
        super();
    }

    public GitException(String message) {
        super(message);
    }

    public GitException(Throwable cause) {
        super(cause);
    }

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }
}
