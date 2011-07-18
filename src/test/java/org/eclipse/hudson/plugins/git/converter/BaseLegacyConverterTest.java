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

import com.thoughtworks.xstream.XStream;
import hudson.XmlFile;
import hudson.matrix.MatrixProject;
import hudson.model.FreeStyleProject;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import org.eclipse.hudson.plugins.git.GitSCM;
import org.junit.Before;

/**
 * BaseLegacyConverterTest.
 * <p/>
 * Date: 7/18/11
 *
 * @author Nikita Levyankov
 */
public abstract class BaseLegacyConverterTest {
    private File sourceConfigFile;
    private File targetConfigFile;
    protected XStream defaultXSTREAM;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        sourceConfigFile = new File(this.getClass().getResource(getResourceName()).toURI());
        //Create target config file in order to perform marshall operation
        targetConfigFile = new File(sourceConfigFile.getParent(), "target_" + getResourceName());
        FileUtils.copyFile(sourceConfigFile, targetConfigFile);
        GitSCM.DescriptorImpl.initXSTREAM();
        //Empty implementation of xstream without any aliased from git
        defaultXSTREAM = new XStream2();
        defaultXSTREAM.alias("project", FreeStyleProject.class);
        defaultXSTREAM.alias("matrix-project", MatrixProject.class);
    }

    protected abstract String getResourceName();

    protected XmlFile getSourceConfigFile(XStream XSTREAM) {
        return new XmlFile(XSTREAM, sourceConfigFile);
    }

    protected XmlFile getTargetConfigFile(XStream XSTREAM) {
        return new XmlFile(XSTREAM, targetConfigFile);
    }

}
