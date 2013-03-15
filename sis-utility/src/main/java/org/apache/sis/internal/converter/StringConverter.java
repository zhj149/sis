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
package org.apache.sis.internal.converter;

import java.util.Set;
import java.util.EnumSet;
import java.nio.charset.UnsupportedCharsetException;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import net.jcip.annotations.Immutable;

import org.apache.sis.math.FunctionProperty;
import org.apache.sis.util.Locales;
import org.apache.sis.util.Numbers;
import org.apache.sis.util.CharSequences;
import org.apache.sis.util.ObjectConverter;
import org.apache.sis.util.UnconvertibleObjectException;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.iso.SimpleInternationalString;


/**
 * Handles conversions between {@link String} and various kinds of objects.
 * Each inner class in this {@code StringConverter} class defines both the
 * forward and the inverse converters.
 *
 * <p>Most converters are pretty close to bijective functions, but not exactly.
 * For example conversions from {@code String} to {@link java.io.File} is not
 * completely bijective because various path separators ({@code '/'} and {@code '\'})
 * produce the same {@code File} object.</p>
 *
 * {@section Special cases}
 * Conversion table from {@link String} to {@link java.lang.Boolean}:
 *
 * <table>
 *    <tr><th>source</th>          <th>target</th></tr>
 *    <tr><td>{@code "true"}  </td><td>{@link java.lang.Boolean#TRUE}  </td></tr>
 *    <tr><td>{@code "false"} </td><td>{@link java.lang.Boolean#FALSE} </td></tr>
 *    <tr><td>{@code "yes"}   </td><td>{@link java.lang.Boolean#TRUE}  </td></tr>
 *    <tr><td>{@code "no"}    </td><td>{@link java.lang.Boolean#FALSE} </td></tr>
 *    <tr><td>{@code "on"}    </td><td>{@link java.lang.Boolean#TRUE}  </td></tr>
 *    <tr><td>{@code "off"}   </td><td>{@link java.lang.Boolean#FALSE} </td></tr>
 *    <tr><td>{@code "1"}     </td><td>{@link java.lang.Boolean#TRUE}  </td></tr>
 *    <tr><td>{@code "0"}     </td><td>{@link java.lang.Boolean#FALSE} </td></tr>
 * </table>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-2.4)
 * @version 0.3
 * @module
 */
@Immutable
abstract class StringConverter<T> extends SystemConverter<String, T> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -3397013355582381432L;

    /**
     * The inverse converter from the target to the source class.
     */
    private final ObjectConverter<T, String> inverse;

    /**
     * Creates a new converter for the given target class.
     *
     * @param targetClass The {@linkplain #getTargetClass() target class}.
     * @param inverse The inverse converter from the target to the source class.
     */
    StringConverter(final Class<T> targetClass) {
        super(String.class, targetClass);
        inverse = createInverse();
    }

    /**
     * Invoked by the constructor for creating the inverse converter.
     * To be overridden by classes which need a specialized instance.
     */
    ObjectConverter<T, String> createInverse() {
        return new ObjectToString<>(targetClass, this);
    }

    /**
     * Returns a predefined instance for the given target class, or {@code null} if none.
     * This method does not create any new instance.
     *
     * @param  <T> The target class.
     * @param  targetClass The target class.
     * @return An instance for the given target class, or {@code null} if none.
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    static <T> StringConverter<T> getInstance(final Class<T> targetClass) {
        switch (Numbers.getEnumConstant(targetClass)) {
            case Numbers.BOOLEAN:     return (StringConverter<T>) Boolean   .INSTANCE;
            case Numbers.BYTE:        return (StringConverter<T>) Byte      .INSTANCE;
            case Numbers.SHORT:       return (StringConverter<T>) Short     .INSTANCE;
            case Numbers.INTEGER:     return (StringConverter<T>) Integer   .INSTANCE;
            case Numbers.LONG:        return (StringConverter<T>) Long      .INSTANCE;
            case Numbers.FLOAT:       return (StringConverter<T>) Float     .INSTANCE;
            case Numbers.DOUBLE:      return (StringConverter<T>) Double    .INSTANCE;
            case Numbers.BIG_INTEGER: return (StringConverter<T>) BigInteger.INSTANCE;
            case Numbers.BIG_DECIMAL: return (StringConverter<T>) BigDecimal.INSTANCE;
        }
        if (targetClass == java.lang.Number        .class) return (StringConverter<T>) Number. INSTANCE;
        if (targetClass == java.util.Locale        .class) return (StringConverter<T>) Locale. INSTANCE;
        if (targetClass == java.nio.charset.Charset.class) return (StringConverter<T>) Charset.INSTANCE;
        if (targetClass == java.io.File            .class) return (StringConverter<T>) File   .INSTANCE;
        if (targetClass == java.nio.file.Path      .class) return (StringConverter<T>) Path   .INSTANCE;
        if (targetClass == java.net.URI            .class) return (StringConverter<T>) URI    .INSTANCE;
        if (targetClass == java.net.URL            .class) return (StringConverter<T>) URL    .INSTANCE;
        if (targetClass == org.opengis.util.InternationalString.class)
            return (StringConverter<T>) InternationalString.INSTANCE;
        return null;
    }

    /**
     * Returns the singleton instance on deserialization, if any.
     */
    @Override
    public ObjectConverter<String, T> unique() {
        assert sourceClass == String.class : sourceClass;
        final StringConverter<T> instance = getInstance(targetClass);
        return (instance != null) ? instance : this;
    }

    /**
     * While this is not a general rule for surjective functions,
     * all converters defined in this class are invertibles.
     */
    @Override
    public Set<FunctionProperty> properties() {
        return EnumSet.of(FunctionProperty.SURJECTIVE, FunctionProperty.INVERTIBLE);
    }

    /**
     * Returns the inverse converter.
     */
    @Override
    public final ObjectConverter<T, String> inverse() {
        return inverse;
    }

    /**
     * Converts the given string to the target type of this converter.
     * This method verifies that the given string is non-null and non-empty,
     * then delegates to {@link #doConvert(String)}.
     *
     * @param  source The string to convert, or {@code null}.
     * @return The converted value, or {@code null} if the given string was null or empty.
     * @throws UnconvertibleObjectException If an error occurred during the conversion.
     */
    @Override
    public final T convert(String source) throws UnconvertibleObjectException {
        source = CharSequences.trimWhitespaces(source);
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return doConvert(source);
        } catch (UnconvertibleObjectException e) {
            throw e;
        } catch (Exception e) {
            throw new UnconvertibleObjectException(formatErrorMessage(source), e);
        }
    }

    /**
     * Invoked by {@link #convert(String)} for converting the given string to the target
     * type of this converter.
     *
     * @param  source The string to convert, guaranteed to be non-null and non-empty.
     * @return The converted value.
     * @throws Exception If an error occurred during the conversion.
     */
    abstract T doConvert(String source) throws Exception;

    /**
     * Converter from {@link String} to various kinds of {@link java.lang.Number}.
     */
    @Immutable
    private static final class Number extends StringConverter<java.lang.Number> {
        private static final long serialVersionUID = 1557277544742023571L;
        static final Number INSTANCE = new Number();
        private Number() {super(java.lang.Number.class);}

        @Override java.lang.Number doConvert(String source) throws NumberFormatException {
            return Numbers.narrowestNumber(source);
        }
    }

    private static final class Double extends StringConverter<java.lang.Double> {
        private static final long serialVersionUID = -9094071164371643060L;
        static final Double INSTANCE = new Double();
        private Double() {super(java.lang.Double.class);}

        @Override java.lang.Double doConvert(String source) throws NumberFormatException {
            return java.lang.Double.parseDouble(source);
        }
    }

    private static final class Float extends StringConverter<java.lang.Float> {
        private static final long serialVersionUID = -2815192289550338333L;
        static final Float INSTANCE = new Float();
        private Float() {super(java.lang.Float.class);}

        @Override java.lang.Float doConvert(String source) throws NumberFormatException {
            return java.lang.Float.parseFloat(source);
        }
    }

    private static final class Long extends StringConverter<java.lang.Long> {
        private static final long serialVersionUID = -2171263041723939779L;
        static final Long INSTANCE = new Long();
        private Long() {super(java.lang.Long.class);}

        @Override java.lang.Long doConvert(String source) throws NumberFormatException {
            return java.lang.Long.parseLong(source);
        }
    }

    private static final class Integer extends StringConverter<java.lang.Integer> {
        private static final long serialVersionUID = 763211364703205967L;
        static final Integer INSTANCE = new Integer();
        private Integer() {super(java.lang.Integer.class);}

        @Override java.lang.Integer doConvert(String source) throws NumberFormatException {
            return java.lang.Integer.parseInt(source);
        }
    }

    private static final class Short extends StringConverter<java.lang.Short> {
        private static final long serialVersionUID = -1770870328699572960L;
        static final Short INSTANCE = new Short();
        private Short() {super(java.lang.Short.class);}

        @Override java.lang.Short doConvert(String source) throws NumberFormatException {
            return java.lang.Short.parseShort(source);
        }
    }

    private static final class Byte extends StringConverter<java.lang.Byte> {
        private static final long serialVersionUID = 2084870859391804185L;
        static final Byte INSTANCE = new Byte();
        private Byte() {super(java.lang.Byte.class);}

        @Override java.lang.Byte doConvert(String source) throws NumberFormatException {
            return java.lang.Byte.parseByte(source);
        }
    }

    private static final class BigDecimal extends StringConverter<java.math.BigDecimal> {
        private static final long serialVersionUID = -8597497425876120213L;
        static final BigDecimal INSTANCE = new BigDecimal();
        private BigDecimal() {super(java.math.BigDecimal.class);}

        @Override java.math.BigDecimal doConvert(String source) throws NumberFormatException {
            return new java.math.BigDecimal(source);
        }
    }

    private static final class BigInteger extends StringConverter<java.math.BigInteger> {
        private static final long serialVersionUID = 8658903031519526466L;
        static final BigInteger INSTANCE = new BigInteger();
        private BigInteger() {super(java.math.BigInteger.class);}

        @Override java.math.BigInteger doConvert(String source) throws NumberFormatException {
            return new java.math.BigInteger(source);
        }
    }

    private static final class Boolean extends StringConverter<java.lang.Boolean> {
        private static final long serialVersionUID = -27525398425996373L;
        static final Boolean INSTANCE = new Boolean();
        private Boolean() {super(java.lang.Boolean.class);}

        /** See {@link StringConverter} for the conversion table. */
        @Override java.lang.Boolean doConvert(final String source) throws UnconvertibleObjectException {
            switch (source.toLowerCase(java.util.Locale.ROOT)) {
                case "true":  case "yes": case "on":  case "1": return java.lang.Boolean.TRUE;
                case "false": case "no":  case "off": case "0": return java.lang.Boolean.FALSE;
            }
            throw new UnconvertibleObjectException(formatErrorMessage(source));
        }
    }

    private static final class Locale extends StringConverter<java.util.Locale> {
        private static final long serialVersionUID = -2888932450292616036L;
        static final Locale INSTANCE = new Locale();
        private Locale() {super(java.util.Locale.class);}

        @Override java.util.Locale doConvert(String source) throws IllegalArgumentException {
            return Locales.parse(source);
        }
    }

    private static final class Charset extends StringConverter<java.nio.charset.Charset> {
        private static final long serialVersionUID = 4539755855992944656L;
        static final Charset INSTANCE = new Charset();
        private Charset() {super(java.nio.charset.Charset.class);}

        @Override java.nio.charset.Charset doConvert(String source) throws UnsupportedCharsetException {
            return java.nio.charset.Charset.forName(source);
        }
    }

    private static final class File extends StringConverter<java.io.File> {
        private static final long serialVersionUID = 6445208470928432376L;
        static final File INSTANCE = new File();
        private File() {super(java.io.File.class);}

        @Override java.io.File doConvert(String source) {
            return new java.io.File(source);
        }
    }

    private static final class Path extends StringConverter<java.nio.file.Path> {
        private static final long serialVersionUID = -5227120925547132828L;
        static final Path INSTANCE = new Path();
        private Path() {super(java.nio.file.Path.class);}

        @Override java.nio.file.Path doConvert(String source) throws InvalidPathException {
            return java.nio.file.Paths.get(source);
        }
    }

    private static final class URI extends StringConverter<java.net.URI> {
        private static final long serialVersionUID = -2804405634789179706L;
        static final URI INSTANCE = new URI();
        private URI() {super(java.net.URI.class);}

        @Override java.net.URI doConvert(String source) throws URISyntaxException {
            return new java.net.URI(source);
        }
    }

    private static final class URL extends StringConverter<java.net.URL> {
        private static final long serialVersionUID = 2303928306635765592L;
        static final URL INSTANCE = new URL();
        private URL() {super(java.net.URL.class);}

        @Override java.net.URL doConvert(String source) throws MalformedURLException {
            return new java.net.URL(source);
        }
    }

    private static final class InternationalString extends StringConverter<org.opengis.util.InternationalString> {
        private static final long serialVersionUID = 730809620191573819L;
        static final InternationalString INSTANCE = new InternationalString();
        private InternationalString() {super(org.opengis.util.InternationalString.class);}

        @Override org.opengis.util.InternationalString doConvert(String source) {
            return new SimpleInternationalString(source);
        }

        /** Declares a bijective function. */
        @Override public Set<FunctionProperty> properties() {
            return EnumSet.of(FunctionProperty.INJECTIVE, FunctionProperty.SURJECTIVE,
                    FunctionProperty.ORDER_PRESERVING, FunctionProperty.INVERTIBLE);
        }
    }

    /**
     * Converter from {@link String} to {@link org.opengis.util.CodeList}.
     * This converter is particular in that it requires the target class in argument
     * to the constructor.
     *
     * <p>Instances of this class are created by
     * {@link HeuristicRegistry#createConverter(Class, Class)}.</p>
     */
    @Immutable
    static final class CodeList<T extends org.opengis.util.CodeList<T>> extends StringConverter<T> {
        /** For cross-version compatibility on serialization. */
        private static final long serialVersionUID = 3289083947166861278L;

        /** Creates a new converter for the given code list. */
        CodeList(final Class<T> targetClass) {
            super(targetClass);
        }

        /** Converts the given string to the target type of this converter. */
        @Override T doConvert(String source) {
            final T code = Types.forCodeName(targetClass, source, false);
            if (code == null) {
                throw new UnconvertibleObjectException(formatErrorMessage(source));
            }
            return code;
        }

        /** Invoked by the constructor for creating the inverse converter. */
        @Override ObjectConverter<T, String> createInverse() {
            return new ObjectToString.CodeList<>(targetClass, this);
        }
    }
}
