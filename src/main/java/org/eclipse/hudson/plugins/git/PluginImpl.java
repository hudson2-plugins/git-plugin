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

import hudson.Plugin;
import java.io.IOException;

/**
 * Plugin entry point.
 *
 * @author Nigel Magnay
 * @plugin
 */
public class PluginImpl extends Plugin {
    @Override
    public void postInitialize() throws IOException {
        GitTool.onLoaded();
    }
}
