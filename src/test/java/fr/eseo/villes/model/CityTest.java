package fr.eseo.villes.model;

import fr.eseo.villes.TestUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CityTest {

    private City c = new City(78646, "Versailles", "78000", 2.130538595041323, 48.8019453719008);

    @Before
    public void setUp() {
        TestUtils.prepareTestClass(true);
    }

    @Test
    public void testSave() throws SQLException {
        c.save();
        try (ResultSet rs = TestUtils.getConnection().createStatement().executeQuery("SELECT * FROM City WHERE 1")) {
            assertTrue(rs.first());
            assertEquals(c.getCode(), rs.getInt("code"));
            assertEquals(c.getPostalCodes(), rs.getString("postal_codes"));
            assertEquals(c.getName(), rs.getString("name"));
            assertEquals(c.getGeoLat(), rs.getDouble("geo_lat"), 0.001d);
            assertEquals(c.getGeoLong(), rs.getDouble("geo_long"), 0.001d);
        }
    }

    @Test
    public void testUpdate() throws SQLException {
        c.save();
        c.setName("Versailles2");
        c.update();
        try (ResultSet rs = TestUtils.getConnection().createStatement().executeQuery("SELECT * FROM City WHERE 1")) {
            assertTrue(rs.first());
            assertEquals(c.getName(), rs.getString("name"));
        }
    }

    @Test
    public void testDelete() throws SQLException {
        c.save();
        try (ResultSet rs = TestUtils.getConnection().createStatement().executeQuery("SELECT * FROM City WHERE 1")) {
            assertTrue(rs.first());
        }
        c.delete();
        try (ResultSet rs = TestUtils.getConnection().createStatement().executeQuery("SELECT * FROM City WHERE 1")) {
            assertFalse(rs.first());
        }
    }

    @Test
    public void testFindById() {
        c.save();

        City c2 = City.findByCode(c.getCode());
        assertEquals(c, c2);

    }

    @Test
    public void testGetAll() {
        c.save();

        List<City> lst = City.getAll();

        assertEquals(1, lst.size());
        assertEquals(c, lst.get(0));
    }
}