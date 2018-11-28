package fr.klemek.villes.utils;

import fr.klemek.villes.TestUtils;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
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
        HttpServletRequest request = TestUtils.createMockRequest("OPTIONS", "/api/test/test2", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(new StringWriter());

        assertTrue(ServletUtils.handleCrossOrigin(request, response));
    }

    @Test
    public void testHandleCrossOrigin2() {
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/test/test2", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(new StringWriter());

        assertFalse(ServletUtils.handleCrossOrigin(request, response));
    }

    @Test
    public void testReadParameters() {
        HashMap<String, String> inputParams = new HashMap<>();
        inputParams.put("testkey", "testvalue");

        HttpServletRequest request = TestUtils.createMockRequest("PUT", "/api/test/test2", inputParams, null, null);

        Map<String, String> outputParams = ServletUtils.readParameters(request);
        assertEquals(1, outputParams.size());
        assertTrue(outputParams.containsKey("testkey"));
        assertEquals("testvalue", outputParams.get("testkey"));
    }

    @Test
    public void testSendOk() {
        StringWriter writer = new StringWriter();
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        ServletUtils.sendOk(response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(HttpServletResponse.SC_OK, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");
        assertNotNull(value);
        assertTrue(value.has("success"));
        assertTrue(value.getBoolean("success"));
    }

    @Test
    public void testSendError() {
        StringWriter writer = new StringWriter();
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");
        assertNotNull(value);
        assertTrue(value.has("error"));
        assertNotEquals(0, value.getString("error").length());
    }

    @Test
    public void testSendErrorMessage() {
        StringWriter writer = new StringWriter();
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "custom error");

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");
        assertNotNull(value);
        assertTrue(value.has("error"));
        assertEquals("custom error", value.getString("error"));
    }

    @Test
    public void testSendJsonResponse() {
        StringWriter writer = new StringWriter();
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        JSONObject send = new JSONObject();
        send.put("testkey", "testvalue");

        ServletUtils.sendJsonResponse(response, send);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(HttpServletResponse.SC_OK, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");
        assertNotNull(value);
        assertTrue(value.has("testkey"));
        assertEquals("testvalue", value.getString("testkey"));
    }

    @Test
    public void testSendJsonArrayResponse() {
        StringWriter writer = new StringWriter();
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        JSONArray send = new JSONArray();
        send.put(0);
        send.put(1);

        ServletUtils.sendJsonResponse(response, send);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(HttpServletResponse.SC_OK, res.getInt("code"));
        JSONArray value = res.getJSONArray("value");
        assertNotNull(value);
        assertEquals(0, value.getInt(0));
        assertEquals(1, value.getInt(1));
    }


    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass());
    }
}
