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

import hudson.model.Descriptor;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class BuildChooserDescriptor extends Descriptor<BuildChooser> {
    /**
     * Before this extension point is formalized, existing {@link BuildChooser}s had
     * a hard-coded ID name used for the persistence.
     * <p/>
     * This method returns those legacy ID, if any, to keep compatibility with existing data.
     */
    public String getLegacyId() {
        return null;
    }
}
