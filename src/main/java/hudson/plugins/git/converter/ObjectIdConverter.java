package hudson.plugins.git.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.eclipse.jgit.lib.ObjectId;

/**
 * Custom converter for ObjectId class. Supports legacy conversion from org.spearce.jgit library.
 * <p/>
 * Date: 6/30/11
 *
 * @author Nikita Levyankov
 */
public class ObjectIdConverter implements Converter, LegacyConverter<ObjectId> {
    private static final String LEGACY_NODE_KEY = "byte-array";
    private Base64Encoder base64;

    /**
     * Create ObjectId converter
     */
    public ObjectIdConverter() {
        base64 = new Base64Encoder();
    }

    public boolean canConvert(Class type) {
        return ObjectId.class == type;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        writer.setValue(((ObjectId) source).name());
    }

    public Object unmarshal(HierarchicalStreamReader reader,
                            final UnmarshallingContext context) {

        if (isLegacyNode(reader, context)) {
            return legacyUnmarshal(reader, context);
        }
        return ObjectId.fromString(reader.getValue());
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLegacyNode(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return reader.hasMoreChildren() && LEGACY_NODE_KEY.equals(reader.peekNextChild());
    }

    /**
     * {@inheritDoc}
     */
    public ObjectId legacyUnmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        reader.moveDown();
        ObjectId sha1 = ObjectId.fromRaw(base64.decode(reader.getValue()));
        reader.moveUp();
        return sha1;
    }
}
