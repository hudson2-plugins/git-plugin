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

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * This interface is used for legacy classes conversion
 * <p/>
 * Date: 7/1/11
 *
 * @author Nikita Levyankov
 */
public interface LegacyConverter<T> {
    /**
     * Is the current reader node a legacy node?
     *
     * @param reader HierarchicalStreamReader.
     * @param context UnmarshallingContext.
     * @return true if legacy, false otherwise
     */
    boolean isLegacyNode(HierarchicalStreamReader reader, UnmarshallingContext context);

    /**
     * Unmarshall legacy object.
     *
     * @param reader HierarchicalStreamReader.
     * @param context UnmarshallingContext.
     * @return unmarshalled object of specified type
     */
    T legacyUnmarshal(HierarchicalStreamReader reader, UnmarshallingContext context);
}
