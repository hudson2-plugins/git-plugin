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

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.util.CustomObjectInputStream;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import hudson.util.RobustReflectionConverter;
import java.io.NotActiveException;
import java.io.ObjectInputValidation;
import java.util.Map;
import org.eclipse.jgit.transport.RemoteConfig;

/**
 * Converter for RemoteConfig. Supports legacy org.spearce.jgit library
 * <p/>
 * Date: 7/1/11
 *
 * @author Nikita Levyankov
 */
public class RemoteConfigConverter extends RobustReflectionConverter implements LegacyConverter<RemoteConfig> {

    public RemoteConfigConverter(Mapper mapper, ReflectionProvider provider) {
        super(mapper, provider);
    }

    public boolean canConvert(Class type) {
        return RemoteConfig.class == type || org.spearce.jgit.transport.RemoteConfig.class == type;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLegacyNode(HierarchicalStreamReader reader, final UnmarshallingContext context) {
        return reader.getNodeName().startsWith("org.spearce");
    }

    /**
     * {@inheritDoc}
     */
    public RemoteConfig legacyUnmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final org.spearce.jgit.transport.RemoteConfig remoteConfig = new org.spearce.jgit.transport.RemoteConfig();
        CustomObjectInputStream.StreamCallback callback = new LegacyInputStreamCallback(reader, context, remoteConfig);
        try {
            CustomObjectInputStream objectInput = CustomObjectInputStream.getInstance(context, callback);
            remoteConfig.readExternal(objectInput);
            objectInput.popCallback();
            return remoteConfig.toRemote();
        } catch (Exception e) {
            throw new ConversionException("Unmarshal failed", e);
        }
    }

    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        if (isLegacyNode(reader, context)) {
            return legacyUnmarshal(reader, context);
        }
        return super.unmarshal(reader, context);
    }

    @Override
    protected Object instantiateNewInstance(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return reflectionProvider.newInstance(RemoteConfig.class);
    }

    private class LegacyInputStreamCallback implements CustomObjectInputStream.StreamCallback {

        private HierarchicalStreamReader reader;
        private UnmarshallingContext context;
        private org.spearce.jgit.transport.RemoteConfig remoteConfig;

        private LegacyInputStreamCallback(HierarchicalStreamReader reader,
                                          final UnmarshallingContext context,
                                          org.spearce.jgit.transport.RemoteConfig remoteConfig) {
            this.reader = reader;
            this.context = context;
            this.remoteConfig = remoteConfig;
        }

        public Object readFromStream() {
            reader.moveDown();
            Class type = HierarchicalStreams.readClassType(reader, mapper);
            Object streamItem = context.convertAnother(remoteConfig, type);
            reader.moveUp();
            return streamItem;
        }

        public Map readFieldsFromStream() {
            throw new UnsupportedOperationException();
        }

        public void defaultReadObject() {
            throw new UnsupportedOperationException();
        }

        public void registerValidation(ObjectInputValidation validation, int priority) throws NotActiveException {
            throw new NotActiveException();
        }

        public void close() {
            throw new UnsupportedOperationException();
        }
    }
}
