package fr.eseo.villes.utils;

import fr.eseo.villes.TestUtils;
import fr.eseo.villes.utils.HttpUtils.HttpResult;
import fr.klemek.logger.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HttpUtilsTest {

    @Test
    public void testExecuteRequestError0() {
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.orga/");
        assertEquals(0, hr.code);
    }

    @Test
    public void testExecuteRequestError404() {
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/404");
        assertEquals(404, hr.code);
    }

    @Test
    public void testExecuteRequestGET() {
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/get");
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        JSONObject obj = hr.getJSON();
        assertEquals("http://httpbin.org/get", obj.getString("url"));
    }

    @Test
    public void testExecuteRequestPUTFile() throws IOException {
        File f = new File("test.txt");
        if (f.exists())
            assertTrue(f.delete());
        assertTrue(f.createNewFile());

        try (FileWriter fileWriter = new FileWriter(f)) {
            fileWriter.write("hello");
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Testheader", "testvalue");

        assertTrue(HttpUtils.executePutRequest("https://jsonplaceholder.typicode.com/posts/1", headers, "test.txt"));
        assertTrue(f.delete());
    }

    @Test
    public void testExecuteRequestGETRedirect() {
        HttpResult hr = HttpUtils.executeRequest("GET", "http://java.sun.com/xml/ns/javaee");
        assertEquals(200, hr.code);
    }

    @Test
    public void testExecuteRequestGETParameters() {
        HashMap<String, String[]> params = new HashMap<>();
        params.put("testparam", new String[]{"testvalue"});
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/get", params);
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        JSONObject obj = hr.getJSON();
        assertTrue(obj.getJSONObject("args").has("testparam"));
        assertEquals("testvalue", obj.getJSONObject("args").get("testparam"));
    }

    @Test
    public void testExecuteRequestGETParameters2() {
        HashMap<String, String[]> params = new HashMap<>();
        params.put("testparam", new String[]{"testvalue", "testvalue2"});
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/get", params);
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        JSONObject obj = hr.getJSON();
        assertTrue(obj.getJSONObject("args").has("testparam"));
        assertEquals(2, obj.getJSONObject("args").getJSONArray("testparam").length());
    }

    @Test
    public void testExecuteRequestGETHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Testheader", "testvalue");
        HttpResult hr = HttpUtils.executeRequest("GET", "http://httpbin.org/headers", null, headers);
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        JSONObject obj = hr.getJSON();
        assertTrue(obj.getJSONObject("headers").has("Testheader"));
        assertEquals("testvalue", obj.getJSONObject("headers").get("Testheader"));
    }

    @Test
    public void testExecuteRequestPOSTData() {
        JSONObject data = new JSONObject();
        data.put("testdata", "testvalue");
        HttpResult hr = HttpUtils.executeRequest("POST", "http://httpbin.org/post", data);
        assertEquals(200, hr.code);
        assertNotNull(hr.result);
        JSONObject obj = hr.getJSON();
        assertTrue(obj.getJSONObject("json").has("testdata"));
        assertEquals("testvalue", obj.getJSONObject("json").get("testdata"));
    }

    @Test
    public void testGetBasicAuthHeaders() {
        Map<String, String> headers = HttpUtils.getBasicAuthHeaders("groupe08@vsphere.etudis", "Xd2K5MgWInRc");
        assertTrue(headers.containsKey("Authorization"));
        assertEquals("Basic Z3JvdXBlMDhAdnNwaGVyZS5ldHVkaXM6WGQySzVNZ1dJblJj", headers.get("Authorization"));
    }

    @Test
    public void fakeTestConstructor() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Constructor<?> ctr = HttpUtils.class.getDeclaredConstructors()[0];
        ctr.setAccessible(true);
        ctr.newInstance();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger.init("logging.properties", TestUtils.LOG_LEVEL);
    }

}
