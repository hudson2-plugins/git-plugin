/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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
