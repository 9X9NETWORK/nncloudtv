package com.nncloudtv.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.service.MsoConfigManager;


public class NnUtilStringTest {
    
    protected static final Logger log = Logger.getLogger(NnUtilStringTest.class.getName());
    
    MockHttpServletRequest req;
    
    private final String chId = "28087";
    private final String epId = "e49675";
    
    @Before
    public void setUp() {
        
        req = new MockHttpServletRequest();
    }
    
    @Test
    public void testStringToBool() {
        
        assertTrue("'1' should evaluate to true.", NnStringUtil.stringToBool("1"));
        assertFalse("'0' should evaluate to false.", NnStringUtil.stringToBool("0"));
        
        try {
            NnStringUtil.stringToBool("true");
            fail("'true' is not legal for convention and should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // expected behavior
        }
        
        try {
            NnStringUtil.stringToBool("false");
            fail("'false' is not legal for convention and should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // expected behavior
        }
        
        try {
            NnStringUtil.stringToBool(null);
            fail("null is not legal for convention and should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected behavior
        }
    }
    
    @Test
    public void testIsDigits() {
        
        assertTrue("'1234' is Digits.", NnStringUtil.isDigits("1234"));
        assertTrue("'0001' is Digits.", NnStringUtil.isDigits("0001"));
        assertFalse("'-1234' is not Digits.", NnStringUtil.isDigits("-1234"));
        assertFalse("null is not Digits.", NnStringUtil.isDigits(null));
    }
    
    @Test
    public void testUrlencode() {
        
        String expected = "%5B%20SPA%20SE%20%5D";
        String input = "[ SPA SE ]";
        String output = NnStringUtil.urlencode(input);
        log.info("urlencod('" + input + "') = '" + output + "'");
        
        assertEquals("Spaces should be replaced by '%20'.", expected, output);
    }
    
    @Test
    public void testCapitalize() {
        
        assertEquals("first character should upper case.", "Title", NnStringUtil.capitalize("title"));
        assertEquals("null should as itself returned.", null, NnStringUtil.capitalize(null));
        
        try {
            NnStringUtil.capitalize("");
            fail("'' is not valid string and should throw StringIndexOutOfBoundsException.");
        } catch (StringIndexOutOfBoundsException e) {
            // expected behavior
        }
    }
    
    @Test
    public void testTruncateUTF8() {
        
        assertEquals("truncate error when char < 0x007f", "", NnStringUtil.truncateUTF8("abcd", 0));
        assertEquals("truncate error when char < 0x007f", "a", NnStringUtil.truncateUTF8("abcd", 1));
        assertEquals("truncate error when char < 0x007f", "ab", NnStringUtil.truncateUTF8("abcd", 2));
        assertEquals("truncate error when char < 0x007f", "abc", NnStringUtil.truncateUTF8("abcd", 3));
        assertEquals("truncate error when char < 0x007f", "abcd", NnStringUtil.truncateUTF8("abcd", 4));
        assertEquals("truncate error when char < 0x007f", "abcd", NnStringUtil.truncateUTF8("abcd", 5));
        
        assertEquals("truncate error when char < 0x07FF", "", NnStringUtil.truncateUTF8("a\u0080b", 0));
        assertEquals("truncate error when char < 0x07FF", "a", NnStringUtil.truncateUTF8("a\u0080b", 1));
        assertEquals("truncate error when char < 0x07FF", "a", NnStringUtil.truncateUTF8("a\u0080b", 2));
        assertEquals("truncate error when char < 0x07FF", "a\u0080", NnStringUtil.truncateUTF8("a\u0080b", 3));
        assertEquals("truncate error when char < 0x07FF", "a\u0080b", NnStringUtil.truncateUTF8("a\u0080b", 4));
        assertEquals("truncate error when char < 0x07FF", "a\u0080b", NnStringUtil.truncateUTF8("a\u0080b", 5));
        
        assertEquals("truncate error when char < 0xd7ff", "", NnStringUtil.truncateUTF8("a\u0800b", 0));
        assertEquals("truncate error when char < 0xd7ff", "a", NnStringUtil.truncateUTF8("a\u0800b", 1));
        assertEquals("truncate error when char < 0xd7ff", "a", NnStringUtil.truncateUTF8("a\u0800b", 2));
        assertEquals("truncate error when char < 0xd7ff", "a", NnStringUtil.truncateUTF8("a\u0800b", 3));
        assertEquals("truncate error when char < 0xd7ff", "a\u0800", NnStringUtil.truncateUTF8("a\u0800b", 4));
        assertEquals("truncate error when char < 0xd7ff", "a\u0800b", NnStringUtil.truncateUTF8("a\u0800b", 5));
        assertEquals("truncate error when char < 0xd7ff", "a\u0800b", NnStringUtil.truncateUTF8("a\u0800b", 6));
        
        assertEquals("truncate error when char < 0xDFFF", "", NnStringUtil.truncateUTF8("\uD834\uDD1E", 0));
        assertEquals("truncate error when char < 0xDFFF", "", NnStringUtil.truncateUTF8("\uD834\uDD1E", 1));
        assertEquals("truncate error when char < 0xDFFF", "", NnStringUtil.truncateUTF8("\uD834\uDD1E", 2));
        assertEquals("truncate error when char < 0xDFFF", "", NnStringUtil.truncateUTF8("\uD834\uDD1E", 3));
        assertEquals("truncate error when char < 0xDFFF", "\uD834\uDD1E", NnStringUtil.truncateUTF8("\uD834\uDD1E", 4));
        assertEquals("truncate error when char < 0xDFFF", "\uD834\uDD1E", NnStringUtil.truncateUTF8("\uD834\uDD1E", 5));
    }
    
    @Test
    public void testGetDateString() {
        
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a");
        String expected = dateFormat.format(now);
        
        assertEquals("date format should use 'EEE, MMM d, yyyy hh:mm a'.", expected, NnStringUtil.getDateString(now));
    }
    
    @Test
    public void testGetDelimitedStr() {
        
        assertEquals("", NnStringUtil.getDelimitedStr(new String[] {}));
        assertEquals("a", NnStringUtil.getDelimitedStr(new String[] {"a"}));
        assertEquals("use '\\t' as delimiter", "a\tb", NnStringUtil.getDelimitedStr(new String[] {"a", "b"}));
        assertEquals("null should replace to ''", "a\t\tb", NnStringUtil.getDelimitedStr(new String[] {"a", null, "b"}));
        assertEquals("'\\t' '\\n' '\\r' should replace to ' '", "a\t \tb\t \tc\t \td",
                NnStringUtil.getDelimitedStr(new String[] {"a", "\t", "b", "\n", "c", "\r", "d"}));
        
        try {
            NnStringUtil.getDelimitedStr(null);
            fail("null is not legal for input and should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected behavior
        }
    }
    
    @Test
    public void testEscapeDoubleQuote() {
        
        String respMsg = "'\"' and '\\'";
        String expected = "\"'\\\"' and '\\\\'\"";
        //assertEquals(respMsg + " should properly handled in JSON resp.", expected, NnStringUtil.escapeDoubleQuote(respMsg));
        
        // should be return "\"" + str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + "\"";
    }
    
    @Test
    public void testGetSharingUrl() {
        
        assertEquals("standard sharing URL is not matched.",
                            "http://" + MsoConfigManager.getServerDomain() + "/view/p" + chId + "/" + epId,
                            NnStringUtil.getSharingUrl(false, null, chId, epId));
        
        assertEquals("flipr URL is not matched.",
                "flipr://" + MsoConfigManager.getServerDomain() + "/view/p" + chId + "/" + epId,
                NnStringUtil.getSharingUrl(true, null, chId, epId));
        
    }
}