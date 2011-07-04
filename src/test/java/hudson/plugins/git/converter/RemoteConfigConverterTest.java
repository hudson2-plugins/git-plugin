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
package hudson.plugins.git.converter;

import com.thoughtworks.xstream.XStream;
import hudson.XmlFile;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for RemoteConfig converter with legacy and current RemoteConfig support
 * <p/>
 * Date: 7/4/11
 *
 * @author Nikita Levyankov
 */
public class RemoteConfigConverterTest {

    private File sourceConfigFile;
    private File targetConfigFile;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        sourceConfigFile = new File(this.getClass().getResource("config.xml").toURI());
        //Create target config file in order to perform marshall operation
        targetConfigFile = new File(sourceConfigFile.getParent(), "target_config.xml");
        FileUtils.copyFile(sourceConfigFile, targetConfigFile);
    }

    @Test(expected = Exception.class)
    public void testFailedUnmarshall() throws Exception {
        XStream XSTREAM = initXStream();
        //Config contains legacy RemoteConfig class. Should fail without custom converter
        getSourceConfigFile(XSTREAM).read();
    }

    @Test
    public void testLegacyUnmarshall() throws Exception {
        XStream XSTREAM = initXStream();
        //Config contains legacy RemoteConfig class. Register custom converter and alias
        XSTREAM.alias("org.spearce.jgit.transport.RemoteConfig", RemoteConfig.class);
        XSTREAM.registerConverter(new RemoteConfigConverter(XSTREAM.getMapper(), XSTREAM.getReflectionProvider()));
        getSourceConfigFile(XSTREAM).read();
    }

    @Test
    public void testMarshall() throws Exception {
        XStream XSTREAM = initXStream();
        XSTREAM.alias("org.spearce.jgit.transport.RemoteConfig", RemoteConfig.class);
        XSTREAM.registerConverter(new RemoteConfigConverter(XSTREAM.getMapper(), XSTREAM.getReflectionProvider()));
        //read object from config
        Object item = getSourceConfigFile(XSTREAM).read();
        //save to new config file
        getTargetConfigFile(XSTREAM).write(item);
        getSourceConfigFile(XSTREAM).read();
    }

    private XmlFile getSourceConfigFile(XStream XSTREAM) {
        return new XmlFile(XSTREAM, sourceConfigFile);
    }
    private XmlFile getTargetConfigFile(XStream XSTREAM) {
        return new XmlFile(XSTREAM, targetConfigFile);
    }

    private XStream initXStream() {
        XStream XSTREAM = new XStream2();
        XSTREAM.alias("project", FreeStyleProject.class);
        XSTREAM.alias("build", FreeStyleBuild.class);
        return XSTREAM;
    }
}
