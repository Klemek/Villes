package fr.klemek.villes.api;

import fr.klemek.logger.Logger;
import fr.klemek.villes.utils.CityManager;
import fr.klemek.villes.utils.DatabaseManager;
import fr.klemek.villes.utils.Utils;

import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Class which listen to server init and closure.
 */
@WebListener
public class ContextListener implements ServletContextListener {

    private static String appPath;

    public ContextListener() {
        super();
    }

    /**
     * Called at the server launch.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ContextListener.loadParameters(sce);
            Logger.init("logging.properties");
            Logger.log(Level.INFO, "app.path={0}", ContextListener.appPath);
            Logger.log(Level.INFO, "Server starting");
            if (!DatabaseManager.init(Utils.getString("db_connection_string")))
                throw new IllegalStateException("Database cannot be initialized");
            CityManager.loadCities();
            Logger.log(Level.INFO, "Server started");
        } catch (Exception e) {
            Logger.log(e);
            throw new IllegalStateException("There was an error during initialization", e);
        }
    }

    /**
     * Called at the server closure.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Logger.log(Level.INFO, "Server closed");
    }

    private static void loadParameters(ServletContextEvent sce) {
        ContextListener.appPath = sce.getServletContext().getInitParameter("app.path");
    }

    public static String getAppPath() {
        return appPath;
    }

}
