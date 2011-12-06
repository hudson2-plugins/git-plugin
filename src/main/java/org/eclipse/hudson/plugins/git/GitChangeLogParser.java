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
 * Andrew Bayer, Anton Kozak, Nikita Levyankov, rogerhu
 *
 *******************************************************************************/

package org.eclipse.hudson.plugins.git;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;

/**
 * Parse the git log
 *
 * @author Nigel Magnay
 */
public class GitChangeLogParser extends ChangeLogParser {

    private boolean authorOrCommitter;

    public GitChangeLogParser(boolean authorOrCommitter) {
        super();
        this.authorOrCommitter = authorOrCommitter;
    }

    public GitChangeSetList parse(AbstractBuild build, File changelogFile)
        throws IOException, SAXException {

        ArrayList<GitChangeSet> r = new ArrayList<GitChangeSet>();

        // Parse the log file into GitChangeSet items - each one is a commit

        BufferedReader rdr = new BufferedReader(new FileReader(changelogFile));

        try {
            String line;
            // We use the null value to determine whether at least one commit was
            // present in the changelog. If it stays null, there is no commit line.
            List<String> lines = null;

            while ((line = rdr.readLine()) != null) {
                if (line.startsWith("commit ")) {
                    if (lines != null) {
                        r.add(new GitChangeSet(lines, authorOrCommitter));
                    }
                    lines = new ArrayList<String>();
                }

                if (lines != null) {
                    lines.add(line);
                }
            }

            if (lines != null) {
                r.add(new GitChangeSet(lines, authorOrCommitter));
            }

            return new GitChangeSetList(build, r);
        } finally {
            rdr.close();
        }
    }
}
