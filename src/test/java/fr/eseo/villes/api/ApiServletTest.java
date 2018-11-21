package fr.eseo.villes.api;

import fr.eseo.villes.TestUtils;
import fr.eseo.villes.model.City;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApiServletTest {

    @Before
    public void setUp() {
        TestUtils.prepareTestClass(true);
    }

    @Test
    public void testServletOptions() throws ServletException, IOException {
        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("OPTIONS", "", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new ApiServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
    }

    @Test
    public void testAPIInfo() throws Exception {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new ApiServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);

        assertEquals(200, res.getInt("code"));

        JSONObject value = res.getJSONObject("value");

        assertTrue(value.has("db_version"));
        assertTrue(value.has("db_last_update"));
        assertTrue(value.has("cities_loaded"));
        assertTrue(value.has("cities_count"));
    }

    @Test
    public void testSearchBadRequest() throws Exception {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/search", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new ApiServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);

        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testSearch() throws Exception {
        new City(78646, "Versailles", "78000,78100", 2.130538595041323, 48.8019453719008).save();

        HashMap<String, String> params = new HashMap<>();
        params.put("query", "78000");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/search", params, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new ApiServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);

        assertEquals(200, res.getInt("code"));

        JSONArray value = res.getJSONArray("value");
        assertEquals(1, value.length());
        assertEquals(78646, value.getJSONObject(0).getInt("code"));
    }
}
