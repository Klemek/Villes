package fr.klemek.villes;

import fr.klemek.villes.api.ApiServletTest;
import fr.klemek.villes.api.ContextListenerTest;
import fr.klemek.villes.model.CityTest;
import fr.klemek.villes.utils.CityManagerTest;
import fr.klemek.villes.utils.DatabaseManagerErrorsTest;
import fr.klemek.villes.utils.DatabaseManagerTest;
import fr.klemek.villes.utils.HttpUtilsTest;
import fr.klemek.villes.utils.ServletUtilsTest;
import fr.klemek.villes.utils.UtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UtilsTest.class, ServletUtilsTest.class, HttpUtilsTest.class,
        DatabaseManagerTest.class, DatabaseManagerErrorsTest.class,
        CityTest.class, CityManagerTest.class,
        ContextListenerTest.class, ApiServletTest.class
})
public class AllTests {
}
