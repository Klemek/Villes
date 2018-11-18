package fr.eseo.villes.utils;

import fr.eseo.villes.TestUtils;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ServletUtilsTest {
    @Test
    public void testMatchingURI() {
        assertTrue(ServletUtils.matchingUri("/api/test/{}/test", "/api2/test/bla/test", 2, 0));
        assertTrue(ServletUtils.matchingUri("/api/test/{}/test", "/test/test/test/api2/test/bla/test", 2, 3));
        assertFalse(ServletUtils.matchingUri("/api", "/api2", 1, 0));
        assertFalse(ServletUtils.matchingUri("/api/group/{}", "/api/group", 2, 0));
    }

    @Test
    public void testMapRequestSuccess() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/test/bla", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        Map<String, Runnable> map = new LinkedHashMap<>();
        map.put("PUT /api/test/{}", () -> {
            fail("Invalid mapping");
        });
        map.put("DELETE /api/test/{}", () -> {
            Integer.parseInt("a");
        });

        try {
            ServletUtils.mapRequest(request, response, map);
            fail("Invalid mapping");
        } catch (NumberFormatException ignored) {
        }
    }

    @Test
    public void testMapRequestSuccess2() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/vm/templates", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        Map<String, Runnable> map = new LinkedHashMap<>();
        map.put("GET /api/vm/templates", () -> {
            Integer.parseInt("a");
        });
        map.put("GET /api/vm/{}", () -> {
            fail("Invalid mapping");
        });

        try {
            ServletUtils.mapRequest(request, response, map);
            fail("Invalid mapping");
        } catch (NumberFormatException ignored) {
        }
    }

    @Test
    public void testMapRequestWrongMethod() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/test/bla", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        Map<String, Runnable> map = new LinkedHashMap<>();
        map.put("PUT /api/test/{}", () -> {
            fail("Invalid mapping");
        });
        map.put("DELETE /api/test/{}", () -> {
            fail("Invalid mapping");
        });
        map.put("GET /api/test/bla/{}", () -> {
            fail("Invalid mapping");
        });

        ServletUtils.mapRequest(request, response, map);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, res.getInt("code"));
    }

    @Test
    public void testMapRequestNotFound() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/test/test2", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        Map<String, Runnable> map = new LinkedHashMap<>();
        map.put("GET /api/test/test", () -> {
            fail("Invalid mapping");
        });
        map.put("GET /api/test/test3", () -> {
            fail("Invalid mapping");
        });

        ServletUtils.mapRequest(request, response, map);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, res.getInt("code"));
    }

    @Test
    public void testHandleCrossOrigin() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("OPTIONS", "/api/test/test2", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        assertTrue(ServletUtils.handleCrossOrigin(request, response));
    }

    @Test
    public void testHandleCrossOrigin2() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/test/test2", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        assertFalse(ServletUtils.handleCrossOrigin(request, response));
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass());
    }
}
