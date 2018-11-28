package fr.klemek.villes.utils;

import fr.klemek.villes.TestUtils;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void testStringToIntegerSuccess() {
        String test = "123456";
        assertEquals(Integer.valueOf(123456), Utils.tryParseInt(test));
    }

    @Test
    public void testStringToIntegerFail() {
        String test = "test";
        assertNull(Utils.tryParseInt(test));
    }

    @Test
    public void testContainsIgnoreCase() {
        assertTrue(Utils.containsIgnoreCase("abcdef", "def"));
        assertTrue(Utils.containsIgnoreCase("abcDef", "def"));
        assertTrue(Utils.containsIgnoreCase("abcdef", "dEf"));
        assertTrue(Utils.containsIgnoreCase("abcdeF", "Def"));
        assertFalse(Utils.containsIgnoreCase("abcdef", "aef"));
    }

    @Test
    public void testCoalesceNull() {
        assertNull(Utils.coalesce());
        assertNull(Utils.coalesce((String) null));
        assertNull(Utils.coalesce((String) null, null));
    }

    @Test
    public void testCoalesceNotNull() {
        assertEquals("a", Utils.coalesce(null, "a"));
        assertEquals("a", Utils.coalesce(null, "a", "b"));
    }

    @Test
    public void testIsAlphaNumeric() {
        assertTrue(Utils.isAlphaNumeric("aBc"));
        assertTrue(Utils.isAlphaNumeric("123"));
        assertTrue(Utils.isAlphaNumeric("1B2a3Z45bc"));
        assertFalse(Utils.isAlphaNumeric(" -;!:,%"));
        assertFalse(Utils.isAlphaNumeric("1B2a3Z4 5bc"));
        assertTrue(Utils.isAlphaNumeric(""));
        assertTrue(Utils.isAlphaNumeric(null));
        assertTrue(Utils.isAlphaNumeric("1B2a3Z4 5bc", ' '));
        assertTrue(Utils.isAlphaNumeric(" -;!:,%", ' ', '-', ';', '!', ':', ',', '%'));
    }

    @Test
    public void testGetString() {
        assertEquals("testvalue", Utils.getString("testkey"));
        assertNull(Utils.getString("invalidkey"));
    }

    @Test
    public void getNiceDuration() {
        assertEquals("1 hour 12 min.", Utils.getNiceDuration(4321000));
    }

    @Test
    public void convertTime() {
        assertEquals("05/09/18 at 15:08", Utils.convertTime(1536160106079L));
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass());
    }
}
