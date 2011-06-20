/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Mirko Friedenhagen, Andrew Bayer, Anton Kozak, Nikita Levyankov
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
package hudson.plugins.git.browser;

import hudson.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebApp;
import org.mockito.Mockito;

/**
 * 
 * This class tests switching between the different browser implementation.
 * 
 * @author mfriedenhagen
 */
public class BrowserChooserTest extends TestCase {

    private final Stapler stapler = Mockito.mock(Stapler.class);

    private final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);

    @SuppressWarnings("unchecked")
    private final StaplerRequest staplerRequest = new RequestImpl(stapler, servletRequest, Collections.EMPTY_LIST, null);

    {
        final WebApp webApp = Mockito.mock(WebApp.class);
        Mockito.when(webApp.getClassLoader()).thenReturn(this.getClass().getClassLoader());
        Mockito.when(stapler.getWebApp()).thenReturn(webApp);
    }

    public void testRedmineWeb() throws IOException {
        testExistingBrowser(RedmineWeb.class);
    }

    public void testGithubWeb() throws IOException {
        testExistingBrowser(GithubWeb.class);
    }

    public void testGitWeb() throws IOException {
        testExistingBrowser(GitWeb.class);
    }

    public void testNonExistingBrowser() throws IOException {
        final JSONObject json = readJson();
        try {
            createBrowserFromJson(json);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertSame(e.getCause().getCause().getClass(), ClassNotFoundException.class);
        }
    }

    /**
     * @param browserClass
     * @throws IOException
     */
    void testExistingBrowser(final Class<? extends GitRepositoryBrowser> browserClass) throws IOException {
        final JSONObject json = readJson(browserClass);
        assertSame(browserClass, createBrowserFromJson(json).getClass());
    }

    /**
     * @param json
     * @return
     */
    GitRepositoryBrowser createBrowserFromJson(final JSONObject json) {
        GitRepositoryBrowser browser = staplerRequest.bindJSON(GitRepositoryBrowser.class,
                json.getJSONObject("browser"));
        return browser;
    }

    /**
     * Reads the request data from file scm.json and replaces the invalid browser class in the JSONObject with the class
     * specified as parameter.
     * 
     * @param browserClass
     * @return
     * @throws IOException
     */
    JSONObject readJson(Class<? extends GitRepositoryBrowser> browserClass) throws IOException {
        final JSONObject json = readJson();
        json.getJSONObject("browser").element("stapler-class", browserClass.getName());
        return json;
    }

    /**
     * Reads the request data from file scm.json.
     * 
     * @return
     * @throws IOException
     */
    JSONObject readJson() throws IOException {
        final InputStream stream = this.getClass().getResourceAsStream("scm.json");
        final String scmString;
        try {
            scmString = IOUtils.toString(stream);
        } finally {
            stream.close();
        }
        final JSONObject json = (JSONObject) JSONSerializer.toJSON(scmString);
        return json;
    }
}
