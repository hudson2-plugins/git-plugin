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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.transport.RemoteConfig;

/**
 * Class is used as legacy RemoteConfig for backward compatibility with org.spearce.jgit library
 * <p/>
 * Date: 7/1/11
 *
 * @author Nikita Levyankov
 */
class LegacyRemoteConfig extends Config implements Externalizable {
    private static final String KEY_URL = "url";
    private static final String KEY_FETCH = "fetch";
    private static final String KEY_PUSH = "push";
    private static final String KEY_UPLOAD_PACK = "uploadpack";
    private static final String KEY_RECEIVE_PACK = "receivepack";
    private static final String KEY_TAG_OPT = "tagopt";
    private String name;
    private String uploadpack = "git-upload-pack";
    private String receivepack = "git-receive-pack";
    private String tagopt;
    private List<String> uris = new ArrayList<String>();
    private List<String> fetch = new ArrayList<String>();
    private List<String> push = new ArrayList<String>();
    private final Map<String, String> stringsMap = new HashMap<String, String>();
    private final Map<String, List<String>> stringsListMap = new HashMap<String, List<String>>();

    /**
     * Create remote config proxy
     */
    public LegacyRemoteConfig() {
        stringsMap.put(KEY_RECEIVE_PACK, receivepack);
        stringsMap.put(KEY_UPLOAD_PACK, uploadpack);
        stringsMap.put(KEY_TAG_OPT, tagopt);
        stringsListMap.put(KEY_URL, uris);
        stringsListMap.put(KEY_FETCH, fetch);
        stringsListMap.put(KEY_PUSH, push);
    }

    public void setTagopt(String tagopt) {
        this.tagopt = tagopt;
        stringsMap.put(KEY_TAG_OPT, tagopt);
    }

    public void setReceivepack(String receivepack) {
        this.receivepack = receivepack;
        stringsMap.put(KEY_RECEIVE_PACK, receivepack);
    }

    public void setUploadpack(String uploadpack) {
        this.uploadpack = uploadpack;
        stringsMap.put(KEY_UPLOAD_PACK, uploadpack);
    }

    public void setFetch(List<String> fetch) {
        this.fetch = fetch;
        stringsListMap.put(KEY_FETCH, fetch);
    }

    public void setPush(List<String> push) {
        this.push = push;
        stringsListMap.put(KEY_PUSH, push);
    }

    public void setUris(List<String> uris) {
        this.uris = uris;
        stringsListMap.put(KEY_URL, uris);
    }

    public String getString(String section, String subsection, String name) {
        String result = stringsMap.get(name);
        if (null != result) {
            return result;
        }
        return super.getString(section, subsection, name);
    }

    public String[] getStringList(String section, String subsection,
                                  String name) {
        List<String> result = stringsListMap.get(name);
        if (null != result) {
            return result.toArray(new String[result.size()]);
        }
        return super.getStringList(section, subsection, name);
    }

    private void fromMap(Map<String, List<String>> map) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if (CollectionUtils.isNotEmpty(values)) {
                if (KEY_URL.equals(key)) {
                    setUris(values);
                } else if (KEY_FETCH.equals(key)) {
                    setFetch(values);
                } else if (KEY_PUSH.equals(key)) {
                    setPush(values);
                } else if (KEY_UPLOAD_PACK.equals(key)) {
                    setUploadpack(values.get(0));
                } else if (KEY_RECEIVE_PACK.equals(key)) {
                    setReceivepack(values.get(0));
                } else if (KEY_TAG_OPT.equals(key)) {
                    setTagopt(values.get(0));
                }
            }
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        final int items = in.readInt();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (int i = 0; i < items; i++) {
            String key = in.readUTF();
            String value = in.readUTF();
            List<String> values = map.get(key);
            if (null == values) {
                values = new ArrayList<String>();
                map.put(key, values);
            }
            values.add(value);
        }
        fromMap(map);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        throw new IOException("writeExternal not supported");
    }

    /**
     * @return RemoteConfig
     * @throws URISyntaxException if any.
     */
    public RemoteConfig toRemote() throws URISyntaxException {
        return new RemoteConfig(this, name);
    }

}