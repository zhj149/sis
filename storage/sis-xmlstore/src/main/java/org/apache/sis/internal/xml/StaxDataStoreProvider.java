/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.sis.storage.DataStoreProvider;
import org.apache.sis.xml.MarshallerPool;


/**
 * The provider of {@link StaxStreamReader} instances.
 *
 * @author  Johann Sorel (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.8
 * @version 0.8
 * @module
 */
public abstract class StaxDataStoreProvider extends DataStoreProvider {
    /**
     * Pool of JAXB marshallers shared by all data stores created by this provider.
     * This pool is created only when first needed; it will never be instantiated
     * if the data stores do not use JAXB.
     */
    private volatile MarshallerPool jaxb;

    /**
     * Creates a new provider.
     */
    protected StaxDataStoreProvider() {
    }

    /**
     * Returns the JAXB context for the data store, or {@code null} if the data stores
     * {@linkplain #open created} by this provided do not use JAXB.
     *
     * <p>The default implementation returns {@code null}.</p>
     *
     * @return the JAXB context, or {@code null} if none.
     * @throws JAXBException if an error occurred while creating the JAXB context.
     */
    protected JAXBContext getJAXBContext() throws JAXBException {
        return null;
    }

    /**
     * Returns the (un)marshaller pool, creating it when first needed.
     * If the subclass does not define a JAXB context, then this method returns {@code null}.
     */
    final MarshallerPool getMarshallerPool() throws JAXBException {
        MarshallerPool pool = jaxb;
        if (pool == null) {
            synchronized (this) {
                pool = jaxb;
                if (pool == null) {
                    final JAXBContext context = getJAXBContext();
                    if (context != null) {
                        jaxb = pool = new MarshallerPool(context, null);
                    }
                }
            }
        }
        return pool;
    }
}