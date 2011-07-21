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

import hudson.model.Items;
import org.junit.Test;

/**
 * Test cases for RemoteConfig converter with legacy and current RemoteConfig support
 * <p/>
 * Date: 7/4/11
 *
 * @author Nikita Levyankov
 */
public class RemoteConfigConverterTest extends BaseLegacyConverterTest {

    @Override
    protected String getResourceName() {
        return "config.xml";
    }

    @Test
    public void testLegacyUnmarshall() throws Exception {
        //Config contains legacy RemoteConfig class. Register custom converter and alias
        getSourceConfigFile(Items.XSTREAM).read();
    }

    @Test
    public void testMarshall() throws Exception {
        //read object from config
        Object item = getSourceConfigFile(Items.XSTREAM).read();
        //save to new config file
        getTargetConfigFile(Items.XSTREAM).write(item);
        getTargetConfigFile(Items.XSTREAM).read();
    }

}
