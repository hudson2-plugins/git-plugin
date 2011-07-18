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
