package fr.eseo.villes.utils;

import fr.eseo.villes.TestUtils;
import fr.eseo.villes.model.City;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DatabaseManagerTest {

    private City c = new City(78646, "Versailles", 78000, 2.130538595041323, 48.8019453719008);

    @Before
    public void setUp() throws Exception {
        TestUtils.prepareTestClass();
    }

    @Test
    public void testUpdateDatabase() throws SQLException, IOException {
        try (Connection conn = DatabaseManager.openConnection(true)) {
            TestUtils.cleanDatabase(conn);
        }

        assertFalse(DatabaseManager.tableExists(TestUtils.getConnection(), "db_info"));

        assertTrue(DatabaseManager.updateDatabase());

        assertTrue(DatabaseManager.tableExists(TestUtils.getConnection(), "db_info"));

        try (Statement st = TestUtils.getConnection().createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT * FROM db_info")) {
                rs.first();
                assertEquals(Utils.getInt("db_version"), rs.getInt("version"));
            }
        }
    }

    @Test
    public void testTableExists() throws SQLException {
        assertTrue(DatabaseManager.tableExists(TestUtils.getConnection(), "db_info"));
        assertFalse(DatabaseManager.tableExists(TestUtils.getConnection(), "invalid_table_name"));
    }

    @Test
    public void testDatabaseOpenConnectionError() throws SQLException {
        try {
            DatabaseManager.setDefaultConnectionString(null);
            DatabaseManager.openConnection();
            fail("No error");
        } catch (ExceptionInInitializerError e) {
        }
        DatabaseManager.setDefaultConnectionString(TestUtils.DB_CONNECTION_STRING);
    }


    @Test
    public void testGetFirstFromSessionQueryNamed() throws SQLException {
        TestUtils.emptyDatabase();

        c.save();

        HashMap<String, Object> params = new HashMap<>();
        params.put("code", c.getCode());

        Object obj = DatabaseManager.getFirstFromSessionQueryNamed("FROM City WHERE code = :code", params);

        assertTrue(obj instanceof City);
        City c2 = (City) obj;
        assertEquals(c, c2);
    }


    @Test
    public void testGetFirstFromSessionQuery() throws SQLException {
        TestUtils.emptyDatabase();

        c.save();

        Object obj = DatabaseManager.getFirstFromSessionQuery("FROM City WHERE code = ?", c.getCode());

        assertTrue(obj instanceof City);
        City c2 = (City) obj;
        assertEquals(c, c2);
    }


    @Test
    public void testGetFirstFromSessionQueryError() throws SQLException {
        TestUtils.emptyDatabase();

        c.save();

        Object obj = DatabaseManager.getFirstFromSessionQuery("FROM Cityd WHERE code = ?0", c.getCode());

        assertNull(obj);
    }


    @Test
    public void testGetFirstFromSessionQueryError2() throws SQLException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        TestUtils.emptyDatabase();

        c.save();

        Field sessionFactory = DatabaseManager.class.getDeclaredField("sessionFactory");
        SessionFactory tmp = DatabaseManager.getSessionFactory();
        sessionFactory.setAccessible(true);
        sessionFactory.set(DatabaseManager.class, null);

        Object obj = DatabaseManager.getFirstFromSessionQuery("FROM City WHERE code = ?0", c.getCode());

        assertNull(obj);

        sessionFactory.set(DatabaseManager.class, tmp);
    }


    @Test
    public void testGetFirstFromSessionQueryNoResult() throws SQLException {
        TestUtils.emptyDatabase();

        c.save();

        Object obj = DatabaseManager.getFirstFromSessionQuery("FROM City WHERE code = ?0", c.getCode() + 1);

        assertNull(obj);
    }


    @Test
    public void testGetRowsFromSessionQueryNamed() throws SQLException {
        TestUtils.emptyDatabase();

        c.save();

        HashMap<String, Object> params = new HashMap<>();
        params.put("code", c.getCode());

        List<Object> lst = DatabaseManager.getRowsFromSessionQueryNamed("FROM City WHERE code = :code", params);

        assertEquals(1, lst.size());
        assertTrue(lst.get(0) instanceof City);
        City c2 = (City) lst.get(0);
        assertEquals(c, c2);
    }


    @Test
    public void testGetRowsFromSessionQuery() throws SQLException {
        TestUtils.emptyDatabase();

        c.save();

        List<Object> lst = DatabaseManager.getRowsFromSessionQuery("FROM City WHERE code = ?", c.getCode());

        assertEquals(1, lst.size());
        assertTrue(lst.get(0) instanceof City);
        City c2 = (City) lst.get(0);
        assertEquals(c, c2);
    }


    @Test
    public void testGetRowsFromSessionQueryError() throws SQLException {
        TestUtils.emptyDatabase();

        c.save();

        List<Object> lst = DatabaseManager.getRowsFromSessionQuery("FROM Cityd WHERE code = ?", c.getCode());

        assertEquals(0, lst.size());
    }


    @Test
    public void testGetRowsFromSessionQueryError2() throws SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        TestUtils.emptyDatabase();
        c.save();

        Field sessionFactory = DatabaseManager.class.getDeclaredField("sessionFactory");
        SessionFactory tmp = DatabaseManager.getSessionFactory();
        sessionFactory.setAccessible(true);
        sessionFactory.set(DatabaseManager.class, null);

        List<Object> lst = DatabaseManager.getRowsFromSessionQuery("FROM City WHERE code = ?0", c.getCode());

        assertEquals(0, lst.size());

        sessionFactory.set(DatabaseManager.class, tmp);
    }


    @Test
    public void testGetRowsFromSessionQueryNotFound() throws SQLException {
        TestUtils.emptyDatabase();

        c.save();


        List<Object> lst = DatabaseManager.getRowsFromSessionQuery("FROM City WHERE code = ?0", c.getCode() + 1);

        assertEquals(0, lst.size());
    }

}
