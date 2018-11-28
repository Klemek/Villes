package fr.klemek.villes.api;

import fr.klemek.villes.utils.CityManager;
import fr.klemek.villes.utils.DatabaseManager;
import fr.klemek.villes.utils.Utils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"java.*", "javax.*", "org.*"})
@PrepareForTest({DatabaseManager.class, CityManager.class, Utils.class})
public class ContextListenerTest {

    @Test
    public void testContextInitializedSuccess() {
        PowerMockito.mockStatic(DatabaseManager.class);
        when(DatabaseManager.init(any())).thenReturn(true);

        PowerMockito.mockStatic(CityManager.class);

        ServletContext sc = Mockito.mock(ServletContext.class);

        Mockito.when(sc.getInitParameter(Mockito.anyString())).thenReturn(null);
        Mockito.when(sc.getInitParameter(Mockito.eq("app.path"))).thenReturn("app.path");

        ServletContextEvent sce = Mockito.mock(ServletContextEvent.class);
        Mockito.when(sce.getServletContext()).thenReturn(sc);

        new ContextListener().contextInitialized(sce);

        PowerMockito.verifyStatic(DatabaseManager.class);
        DatabaseManager.init(Utils.getString("db_connection_string"));

        Mockito.verify(sce).getServletContext();
        Mockito.verify(sc).getInitParameter("app.path");
    }

    @Test
    public void testContextInitializedDBError() {
        PowerMockito.mockStatic(DatabaseManager.class);
        when(DatabaseManager.init(any())).thenReturn(false);

        PowerMockito.mockStatic(CityManager.class);

        try {
            new ContextListener().contextInitialized(null);
            fail("No error on db update fail");
        } catch (IllegalStateException e) {
        }
    }

}
