package fr.eseo.villes.utils;

import fr.eseo.villes.ContextListener;
import fr.klemek.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class that store useful servlet functions.
 */
public final class ServletUtils {

    private static final String VALUE_KEY = "value";

    private static final Map<Long, String> currentRequests = new HashMap<>();

    private static int uriOffset = -1;

    private ServletUtils() {
    }

    /**
     * Compute an http code into a message.
     *
     * @param code the http code
     * @return the message of the http code
     */
    private static String getError(int code) {
        switch (code) {
            case HttpServletResponse.SC_BAD_REQUEST:
                return "Bad request - a parameter MAY be missing";
            case HttpServletResponse.SC_UNAUTHORIZED:
                return "Unauthorized - authentication needed";
            case HttpServletResponse.SC_FORBIDDEN:
                return "Forbidden - Insufficient rights";
            case HttpServletResponse.SC_NOT_FOUND:
                return "The resource was not found on the server";
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
                return "An error has occurred, check server logs for more details";
            default:
                return "Unknown error";
        }
    }

    /**
     * Send the content via the response.
     *
     * @param response the servlet response
     * @param code     the http status
     * @param result   the json object sent
     */
    private static void sendContent(HttpServletResponse response, int code, JSONObject result) {
        response.setStatus(code);
        if (!result.has(ServletUtils.VALUE_KEY)) {
            JSONObject temp = result;
            result = new JSONObject();
            result.put(ServletUtils.VALUE_KEY, temp);
        }
        result.put("code", code);
        response.setHeader("Content-Type", "application/json");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(result);
            response.flushBuffer();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    /**
     * Send an error via the response and log it.
     *
     * @param response the servlet response
     * @param code     the code of the error
     */

    public static void sendError(HttpServletResponse response, int code) {
        ServletUtils.sendError(response, code, null);
    }

    /**
     * Send an error via the response and log it.
     *
     * @param response the servlet response
     * @param code     the code of the error
     * @param message  the message of the error
     */
    public static void sendError(HttpServletResponse response, int code, String message) {
        String className = ServletUtils.class.getSimpleName();
        String source;
        int stackTraceLevel = 3;
        do {
            source = Utils.getCallingClassName(stackTraceLevel++);
        } while (className.equals(source));
        if (code != HttpServletResponse.SC_UNAUTHORIZED)
            Logger.log(Level.WARNING, "[{0}] Error {1} sent : {2} (request : {3})",
                    source, code, message == null ? "" : ": " + message, ServletUtils.getCurrentRequest());
        JSONObject error = new JSONObject();
        error.put("error", message == null ? ServletUtils.getError(code) : message);
        ServletUtils.sendContent(response, code, error);
    }

    /**
     * Send a ok response.
     *
     * @param response the response
     */
    public static void sendOk(HttpServletResponse response) {
        JSONObject json = new JSONObject();
        json.put("success", true);
        ServletUtils.sendJsonResponse(response, json);
    }

    /**
     * Send a JSONObject as a response.
     *
     * @param response the servlet response
     * @param result   the json object to send
     */
    public static void sendJsonResponse(HttpServletResponse response, JSONObject result) {
        ServletUtils.sendContent(response, HttpServletResponse.SC_OK, result);
    }

    /**
     * Send a JSONArray as a response (will be sent as a JSONObject containing a
     * field "value").
     *
     * @param response  the servlet response
     * @param jsonArray the array to send
     */
    public static void sendJsonResponse(HttpServletResponse response, JSONArray jsonArray) {
        JSONObject result = new JSONObject();
        result.put(ServletUtils.VALUE_KEY, jsonArray);
        ServletUtils.sendContent(response, HttpServletResponse.SC_OK, result);
    }

    /**
     * Handle a cross origin request (OPTIONS) and respond to it.
     *
     * @param request  the servlet request
     * @param response the servlet response
     * @return true if this is a cross origin request and the response should be sent as is
     */
    public static boolean handleCrossOrigin(HttpServletRequest request,
                                            HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        if ("OPTIONS".equals(request.getMethod())) {
            ServletUtils.sendOk(response);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Map a request to the given functions and handle wrong method and invalid url.
     *
     * @param request  the servlet request
     * @param response the servlet response
     * @param map      the mapping as "METHOD /api/path" - Method to call
     */
    public static void mapRequest(HttpServletRequest request, HttpServletResponse response,
                                  Map<String, Runnable> map) {
        if (ServletUtils.handleCrossOrigin(request, response))
            return;
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        //must be rewritten by mapped function
        boolean matchingWrongMethod = false;
        boolean matchingDone = false;

        if (ServletUtils.uriOffset < 0) {
            String path = ContextListener.getAppPath();
            ServletUtils.uriOffset = (path.length() == 0 || path.equals("/")) ? 0 : path.split("/").length - 1;
        }

        ServletUtils.currentRequests.put(Thread.currentThread().getId(),
                ServletUtils.requestToJson(request).toString());
        for (Map.Entry<String, Runnable> entry : map.entrySet()) {
            String[] mapping = entry.getKey().split(" ");
            if (mapping.length != 2)
                throw new IllegalArgumentException(String.format("Wrongly mapped URI : '%s'",
                        entry.getKey()));
            if (ServletUtils.matchingUri(mapping[1], request.getRequestURI(), 2, ServletUtils.uriOffset)) {
                if (request.getMethod().equalsIgnoreCase(mapping[0])) {
                    entry.getValue().run();
                    matchingDone = true;
                    break;
                } else {
                    matchingWrongMethod = true;
                }
            }
        }
        if (!matchingDone) {
            if (matchingWrongMethod) {
                ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid method");
            } else {
                ServletUtils.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                        "Invalid api url");
            }
        }
    }

    /**
     * Read paramaters from a PUT request url encoded.
     *
     * @param request the request
     * @return all parameters in a Map
     */
    public static Map<String, String> readParameters(HttpServletRequest request) {
        Map<String, String> out = new HashMap<>();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            String data = br.readLine();
            if (data != null && !data.isEmpty())
                for (String parameter : data.split("&")) {
                    String[] spl = parameter.split("=", 2);
                    if (spl.length == 2)
                        out.put(spl[0].trim(), URLDecoder.decode(spl[1].trim(), "UTF-8"));
                }
        } catch (IOException e) {
            Logger.log(Level.WARNING, "Cannot open input stream on {0} request",
                    request.getMethod());
        }
        for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet())
            if (!out.containsKey(parameter.getKey()))
                out.put(parameter.getKey(), parameter.getValue().length > 0
                        ? parameter.getValue()[0] : "");

        ServletUtils.currentRequests.put(Thread.currentThread().getId(),
                ServletUtils.requestToJson(request, out).toString());

        return out;
    }

    private static JSONObject requestToJson(HttpServletRequest request) {
        JSONObject params = new JSONObject();
        for (Map.Entry<String, String[]> param : request.getParameterMap().entrySet())
            if (param.getValue().length > 0)
                params.put(param.getKey(), param.getValue()[0]);

        JSONObject json = new JSONObject();
        json.put("requested", request.getRequestURI());
        json.put("method", request.getMethod());
        json.put("params", params);

        return json;
    }

    private static JSONObject requestToJson(HttpServletRequest request, Map<String, String> params) {
        JSONObject json = new JSONObject();
        json.put("requested", request.getRequestURI());
        json.put("method", request.getMethod());
        json.put("params", params);

        return json;
    }

    private static String getCurrentRequest() {
        return ServletUtils.currentRequests.get(Thread.currentThread().getId());
    }

    /**
     * Compare given URI to reference and check if it match.
     *
     * @param ref         reference URI, can contains '{anything}' as wildcard
     * @param src         URI to check
     * @param ignoreLevel where to start the comparison
     * @param offset      where start comparison of src
     * @return true if its a match
     */
    public static boolean matchingUri(String ref, String src, int ignoreLevel, int offset) {
        String[] refPath = ref.split("/");
        String[] srcPath = src.split("/");
        if (refPath.length != srcPath.length - offset)
            return false;
        for (int i = ignoreLevel; i < refPath.length; i++)
            if (!refPath[i].startsWith("{") && !srcPath[i + offset].equals(refPath[i]))
                return false;
        return true;
    }
}
