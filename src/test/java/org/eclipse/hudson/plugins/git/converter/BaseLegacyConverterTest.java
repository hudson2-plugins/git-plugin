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
        GitSCM.DescriptorImpl.beforeLoad();
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
