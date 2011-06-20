/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Andrew Bayer, Anton Kozak, Nikita Levyankov
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

package hudson.plugins.git;

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
                        r.add(parseCommit(lines, authorOrCommitter));
                    }
                    lines = new ArrayList<String>();
                }

                if (lines != null) {
                    lines.add(line);
                }
            }

            if (lines != null) {
                r.add(parseCommit(lines, authorOrCommitter));
            }

            return new GitChangeSetList(build, r);
        } finally {
            rdr.close();
        }
    }

    private GitChangeSet parseCommit(List<String> lines, boolean authorOrCommitter) {
        return new GitChangeSet(lines, authorOrCommitter);
    }

}
