package fr.eseo.villes.api;

import fr.eseo.villes.TestUtils;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApiServletTest {

    @Before
    public void setUp() throws Exception {
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

        assertTrue(res.getJSONObject("value").has("db_version"));
        assertTrue(res.getJSONObject("value").has("db_last_update"));
        assertTrue(res.getJSONObject("value").has("cities_loaded"));
        assertTrue(res.getJSONObject("value").has("cities_count"));
    }

}
