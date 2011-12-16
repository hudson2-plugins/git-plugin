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
 * Andrew Bayer, Anton Kozak, Nikita Levyankov, Winston Prakash
 *
 *******************************************************************************/

package org.eclipse.hudson.plugins.git;

import hudson.Plugin;
import hudson.XmlFile;
import hudson.model.Items;
import hudson.model.Run;
import java.io.IOException;
import org.eclipse.hudson.plugins.git.browser.CGit;
import org.eclipse.hudson.plugins.git.browser.GitRepositoryBrowser;
import org.eclipse.hudson.plugins.git.browser.GitWeb;
import org.eclipse.hudson.plugins.git.browser.GithubWeb;
import org.eclipse.hudson.plugins.git.browser.RedmineWeb;
import org.eclipse.hudson.plugins.git.browser.ViewGitWeb;
import org.eclipse.hudson.plugins.git.converter.ObjectIdConverter;
import org.eclipse.hudson.plugins.git.converter.RemoteConfigConverter;
import org.eclipse.hudson.plugins.git.util.Build;
import org.eclipse.hudson.plugins.git.util.BuildData;
import org.eclipse.hudson.plugins.git.util.DefaultBuildChooser;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.hudson.plugins.git.GitSCM.DescriptorImpl;

/**
 * Plugin entry point.
 *
 * @author Nigel Magnay
 * @plugin
 */
public class PluginImpl extends Plugin {
    
    @Override
    public void start() throws IOException {
        setXtreamAliasForBackwardCompatibility();
    }
    
    @Override
    public void postInitialize() throws IOException {
        GitTool.onLoaded();
    }
    
    /**
     * Register XStream aliases for backward compatibility - should be removed eventually
     */
    public static void setXtreamAliasForBackwardCompatibility(){
        Items.XSTREAM.alias("ObjectId", ObjectId.class);
        Items.XSTREAM.alias("RemoteConfig", RemoteConfig.class);
        Items.XSTREAM.alias("RemoteConfig", org.spearce.jgit.transport.RemoteConfig.class);
        Items.XSTREAM.alias("RemoteConfig", GitRepository.class);
        Items.XSTREAM.registerConverter(
                new RemoteConfigConverter(Items.XSTREAM.getMapper(), Items.XSTREAM.getReflectionProvider()));
        Items.XSTREAM.alias("hudson.plugins.git.GitSCM", GitSCM.class);
        Items.XSTREAM.alias("hudson.plugins.git.BranchSpec", BranchSpec.class);
        Items.XSTREAM.alias("hudson.plugins.git.util.DefaultBuildChooser", DefaultBuildChooser.class);
        Items.XSTREAM.alias("hudson.plugins.git.GitPublisher", GitPublisher.class);
        Items.XSTREAM.alias("hudson.plugins.git.browser.CGit", CGit.class);
        Items.XSTREAM.alias("hudson.plugins.git.browser.GithubWeb", GithubWeb.class);
        Items.XSTREAM.alias("hudson.plugins.git.browser.GitRepositoryBrowser", GitRepositoryBrowser.class);
        Items.XSTREAM.alias("hudson.plugins.git.browser.GitWeb", GitWeb.class);
        Items.XSTREAM.alias("hudson.plugins.git.browser.RedmineWeb", RedmineWeb.class);
        Items.XSTREAM.alias("hudson.plugins.git.browser.ViewGitWeb", ViewGitWeb.class);

        Run.XSTREAM.registerConverter(new ObjectIdConverter());
        Run.XSTREAM.alias("hudson.plugins.git.util.BuildData", BuildData.class);
        Run.XSTREAM.alias("hudson.plugins.git.util.Build", Build.class);
        Run.XSTREAM.alias("hudson.plugins.git.Branch", Branch.class);
        Run.XSTREAM.alias("hudson.plugins.git.GitChangeLogParser", GitChangeLogParser.class);

        XmlFile.DEFAULT_XSTREAM.alias("hudson.plugins.git.GitSCM$DescriptorImpl", DescriptorImpl.class);
        XmlFile.DEFAULT_XSTREAM.alias("hudson.plugins.git.GitTool$DescriptorImpl", org.eclipse.hudson.plugins.git.GitTool.DescriptorImpl.class);
        XmlFile.DEFAULT_XSTREAM.alias("hudson.plugins.git.GitTool", GitTool.class);
    }
    
}
