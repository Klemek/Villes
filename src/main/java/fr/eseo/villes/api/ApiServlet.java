package fr.eseo.villes.api;

import fr.eseo.villes.model.City;
import fr.eseo.villes.utils.DatabaseManager;
import fr.eseo.villes.utils.ServletUtils;
import fr.klemek.logger.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet({"/api/*",})
public class ApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public ApiServlet() {
        super();
    }

    /**
     * Service at /api/*.
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            LinkedHashMap<String, Runnable> map = new LinkedHashMap<>();
            map.put("GET /api", () -> getAPIInfo(response));
            ServletUtils.mapRequest(request, response, map);
        } catch (Exception e) {
            Logger.log(e);
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void getAPIInfo(HttpServletResponse response) {
        JSONObject result = new JSONObject();

        int dbVersion = -1;
        Timestamp dbUpdate = null;
        try (Connection conn = DatabaseManager.openConnection()) {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT * FROM db_info")) {
                    if (rs.first()) {
                        dbVersion = rs.getInt("version");
                        dbUpdate = rs.getTimestamp("update_date");
                    }
                }
            }
        } catch (SQLException e) {
            Logger.log(e);
        }
        result.put("db_version", dbUpdate == null ? "unkown" : dbVersion);
        result.put("db_last_update", dbUpdate == null ? "unkown" : dbUpdate);
        result.put("cities_count", City.getAll().size());
        result.put("cities_loaded", DatabaseManager.areCitiesLoaded());

        ServletUtils.sendJsonResponse(response, result);
    }

}
