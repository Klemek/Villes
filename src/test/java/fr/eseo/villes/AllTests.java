package fr.eseo.villes;

import fr.eseo.villes.api.ApiServletTest;
import fr.eseo.villes.api.ContextListenerTest;
import fr.eseo.villes.model.CityTest;
import fr.eseo.villes.utils.DatabaseManagerErrorsTest;
import fr.eseo.villes.utils.DatabaseManagerTest;
import fr.eseo.villes.utils.HttpUtilsTest;
import fr.eseo.villes.utils.ServletUtilsTest;
import fr.eseo.villes.utils.UtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UtilsTest.class, ServletUtilsTest.class, HttpUtilsTest.class,
        DatabaseManagerTest.class, DatabaseManagerErrorsTest.class,
        CityTest.class,
        ContextListenerTest.class, ApiServletTest.class
})
public class AllTests {
}