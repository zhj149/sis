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
package org.apache.sis.referencing.factory;

import org.opengis.util.NameFactory;
import org.opengis.util.FactoryException;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.crs.GeographicCRS;
import org.apache.sis.internal.system.DefaultFactories;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.io.wkt.Convention;

// Test imports
import org.apache.sis.test.DependsOnMethod;
import org.apache.sis.test.TestCase;
import org.junit.Test;

import static org.apache.sis.test.MetadataAssert.*;


/**
 * Tests {@link CommonAuthorityFactory}.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.7
 * @version 0.7
 * @module
 */
public final strictfp class CommonAuthorityFactoryTest extends TestCase {
    /**
     * The factory to test.
     */
    private final CommonAuthorityFactory factory;

    /**
     * Initializes the factory to test.
     */
    public CommonAuthorityFactoryTest() {
        factory = new CommonAuthorityFactory(DefaultFactories.forBuildin(NameFactory.class));
    }

    /**
     * Checks the authority names.
     */
    @Test
    public void testAuthority() {
        final Citation authority = factory.getAuthority();
        assertTrue (Citations.identifierMatches(authority, "CRS"));
        assertTrue (Citations.identifierMatches(authority, "OGC"));
        assertFalse(Citations.identifierMatches(authority, "OGP"));
        assertFalse(Citations.identifierMatches(authority, "EPSG"));
        assertFalse(Citations.identifierMatches(authority, "AUTO"));
        assertFalse(Citations.identifierMatches(authority, "AUTO2"));
    }

    /**
     * Tests the CRS:84 code.
     *
     * @throws FactoryException if an error occurred while creating a CRS.
     */
    @Test
    @DependsOnMethod("testAuthority")
    public void testCRS84() throws FactoryException {
        GeographicCRS crs = factory.createGeographicCRS("CRS:84");
        assertSame   (crs,  factory.createGeographicCRS("84"));
        assertSame   (crs,  factory.createGeographicCRS("CRS84"));
        assertSame   (crs,  factory.createGeographicCRS("CRS:CRS84"));
        assertSame   (crs,  factory.createGeographicCRS("crs : crs84"));
        assertSame   (crs,  factory.createGeographicCRS("OGC:84"));         // Not in real use as far as I know.
        assertSame   (crs,  factory.createGeographicCRS("OGC:CRS84"));
        assertNotSame(crs,  factory.createGeographicCRS("CRS:83"));
    }

    /**
     * Tests the CRS:83 code.
     *
     * @throws FactoryException if an error occurred while creating a CRS.
     */
    @Test
    @DependsOnMethod("testAuthority")
    public void testCRS83() throws FactoryException {
        GeographicCRS crs = factory.createGeographicCRS("CRS:83");
        assertSame   (crs,  factory.createGeographicCRS("83"));
        assertSame   (crs,  factory.createGeographicCRS("CRS83"));
        assertSame   (crs,  factory.createGeographicCRS("CRS:CRS83"));
        assertNotSame(crs,  factory.createGeographicCRS("CRS:84"));
        assertNotDeepEquals(CommonCRS.WGS84.normalizedGeographic(), crs);
    }

    /**
     * Tests the WKT formatting. The main purpose of this test is to ensure that
     * the authority name is "CRS" and not "Web Map Service CRS".
     *
     * @throws FactoryException if an error occurred while creating the CRS.
     */
    @Test
    @DependsOnMethod("testCRS84")
    public void testWKT() throws FactoryException {
        GeographicCRS crs = factory.createGeographicCRS("CRS:84");
        assertWktEquals(Convention.WKT1,
                "GEOGCS[“WGS 84”,\n" +
                "  DATUM[“World Geodetic System 1984”,\n" +
                "    SPHEROID[“WGS 84”, 6378137.0, 298.257223563]],\n" +
                "    PRIMEM[“Greenwich”, 0.0],\n" +
                "  UNIT[“degree”, 0.017453292519943295],\n" +
                "  AXIS[“Longitude”, EAST],\n" +
                "  AXIS[“Latitude”, NORTH],\n" +
                "  AUTHORITY[“CRS”, “84”]]", crs);

        assertWktEquals(Convention.WKT2,
                "GEODCRS[“WGS 84”,\n" +
                "  DATUM[“World Geodetic System 1984”,\n" +
                "    ELLIPSOID[“WGS 84”, 6378137.0, 298.257223563, LENGTHUNIT[“metre”, 1]]],\n" +
                "    PRIMEM[“Greenwich”, 0.0, ANGLEUNIT[“degree”, 0.017453292519943295]],\n" +
                "  CS[ellipsoidal, 2],\n" +
                "    AXIS[“Longitude (L)”, east, ORDER[1]],\n" +
                "    AXIS[“Latitude (B)”, north, ORDER[2]],\n" +
                "    ANGLEUNIT[“degree”, 0.017453292519943295],\n" +
                "  AREA[“World”],\n" +
                "  BBOX[-90.00, -180.00, 90.00, 180.00],\n" +
                "  ID[“CRS”, 84, CITATION[“OGC”]]]", crs);
    }
}