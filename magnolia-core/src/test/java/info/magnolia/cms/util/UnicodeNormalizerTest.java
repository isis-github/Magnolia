/**
 * This file Copyright (c) 2009-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

/**
 * @version $Id$
 */
public class UnicodeNormalizerTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UnicodeNormalizerTest.class);

    private static final byte[] MACROMAN_BYTES = new byte[]{47, 121, -118, -118, -118, -118, -118};
    private static final byte[] NFC_BYTES = new byte[]{47, 121, -61, -92, -61, -92, -61, -92, -61, -92, -61, -92};
    private static final byte[] NFD_BYTES = new byte[]{47, 121, 97, -52, -120, 97, -52, -120, 97, -52, -120, 97, -52, -120, 97, -52, -120};
    private static final String MACROMAN;
    private static final String NFC;
    private static final String NFD;

    static {
        try {
            MACROMAN = new String(MACROMAN_BYTES, "MacRoman");
            NFC = new String(NFC_BYTES, "UTF-8");
            NFD = new String(NFD_BYTES, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setUp() throws Exception {
        SystemProperty.clear();
        ComponentsTestUtil.clear();
        ComponentsTestUtil.setImplementation(info.magnolia.cms.util.UnicodeNormalizer.Normalizer.class, "info.magnolia.cms.util.UnicodeNormalizer$AutoDetectNormalizer");
        SystemProperty.setProperty("magnolia.utf8.enabled", "true");
    }

    @After
    public void tearDown() throws Exception {
        SystemProperty.clear();
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }
    
    @Test
    public void testAsciiStringsShouldPassThroughWithAutoDetect() {
        assertEquals("hello", UnicodeNormalizer.normalizeNFC("hello"));
    }

    @Test
    public void testNFCStringIsStillEqualsAfterNormalizeCallWithAutoDetect() {
        assertEquals(NFC, UnicodeNormalizer.normalizeNFC(NFC));
    }

    @Test
    public void testNormalizingNFDStringMakesItEqualsToNFCStringWithAutoDetect() {
        assertEquals(NFC, UnicodeNormalizer.normalizeNFC(NFD));
    }

    @Test
    public void testNormalizationAlsoWorksForStringsThatWereOriginallyNotUTF8WithAutoDetect() {
        assertEquals(MACROMAN, NFC);
        assertEquals(NFC, UnicodeNormalizer.normalizeNFC(MACROMAN));
    }

    @Test
    public void testNonNormalizer() {
        final UnicodeNormalizer.Normalizer n = new UnicodeNormalizer.NonNormalizer();
        assertEquals(NFD, n.normalizeNFC(NFD));
        assertEquals(NFC, n.normalizeNFC(MACROMAN));
        assertEquals("hello", n.normalizeNFC("hello"));
    }

    @Test
    public void testJava6Normalizer() { 
        final UnicodeNormalizer.Normalizer n = new UnicodeNormalizer.Java6Normalizer();
        assertEquals(NFC, n.normalizeNFC(NFD));
        assertEquals(NFC, n.normalizeNFC(MACROMAN));
        assertEquals("hello", n.normalizeNFC("hello"));
    }
}
