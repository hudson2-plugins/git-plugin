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
 * Nikita Levyankov
 *
 *******************************************************************************/
package org.eclipse.hudson.plugins.git.converter;

import hudson.model.Run;
import org.junit.Test;

/**
 * Test cases for ObjectID converter with legacy and current ObjectID support
 * <p/>
 * Date: 7/4/11
 *
 * @author Nikita Levyankov
 */
public class ObjectIdConverterTest extends BaseLegacyConverterTest {

    @Override
    protected String getResourceName() {
        return "build.xml";
    }

    @Test(expected = Exception.class)
    public void testFailedUnmarshall() throws Exception {
        //Config contains legacy ObjectId class. Should fail without custom converter
        getSourceConfigFile(defaultXSTREAM).read();
    }

    @Test
    public void testLegacyUnmarshall() throws Exception {
        getSourceConfigFile(Run.XSTREAM).read();
    }

    @Test
    public void testMarshall() throws Exception {
        //read object from config
        Object item = getSourceConfigFile(Run.XSTREAM).read();
        //save to new config file
        getTargetConfigFile(Run.XSTREAM).write(item);
        getTargetConfigFile(Run.XSTREAM).read();
    }

}
