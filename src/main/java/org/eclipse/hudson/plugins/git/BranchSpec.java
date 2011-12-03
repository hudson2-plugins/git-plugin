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
 * Andrew Bayer, Anton Kozak, Nikita Levyankov
 *
 *******************************************************************************/
package org.eclipse.hudson.plugins.git;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * A specification of branches to build. Rather like a refspec.
 * <p/>
 * eg:
 * master
 * origin/master
 * origin/ *
 * origin/ * /thing
 */
public class BranchSpec implements Serializable {
    private static final long serialVersionUID = -6177158367915899356L;

    private String name;
    private transient Pattern pattern;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public BranchSpec(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        } else if (name.length() == 0) {
            this.name = GitSCM.DEFAULT_BRANCH;
        } else {
            this.name = name;
        }
    }

    public boolean matches(String item) {
        return getPattern().matcher(item).matches();
    }

    public List<String> filterMatching(Collection<String> branches) {
        List<String> items = new ArrayList<String>();

        for (String b : branches) {
            if (matches(b)) {
                items.add(b);
            }
        }

        return items;
    }

    public List<Branch> filterMatchingBranches(Collection<Branch> branches) {
        List<Branch> items = new ArrayList<Branch>();

        for (Branch b : branches) {
            if (matches(b.getName())) {
                items.add(b);
            }
        }

        return items;
    }

    private Pattern getPattern() {
        // return the saved pattern if available
        if (pattern != null) {
            return pattern;
        }

        // if an unqualified branch was given add a "*/" so it will match branches
        // from remote repositories as the user probably intended
        String qualifiedName;
        if (!name.contains("**") && !name.contains("/")) {
            qualifiedName = "*/" + name;
        } else {
            qualifiedName = name;
        }

        // build a pattern into this builder
        StringBuilder builder = new StringBuilder();

        // was the last token a wildcard?
        boolean foundWildcard = false;

        // split the string at the wildcards
        StringTokenizer tokenizer = new StringTokenizer(qualifiedName, "*", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            // is this token is a wildcard?
            if (token.equals("*")) {
                // yes, was the previous token a wildcard?
                if (foundWildcard) {
                    // yes, we found "**"
                    // match over any number of characters
                    builder.append(".*");
                    foundWildcard = false;
                } else {
                    // no, set foundWildcard to true and go on
                    foundWildcard = true;
                }
            } else {
                // no, was the previous token a wildcard?
                if (foundWildcard) {
                    // yes, we found "*" followed by a non-wildcard
                    // match any number of characters other than a "/"
                    builder.append("[^/]*");
                    foundWildcard = false;
                }
                // quote the non-wildcard token before adding it to the phrase
                builder.append(Pattern.quote(token));
            }
        }

        // if the string ended with a wildcard add it now
        if (foundWildcard) {
            builder.append("[^/]*");
        }

        // save the pattern
        pattern = Pattern.compile(builder.toString());

        return pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BranchSpec that = (BranchSpec) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
