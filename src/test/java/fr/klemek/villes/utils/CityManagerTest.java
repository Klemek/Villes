package fr.eseo.villes.utils;

import fr.eseo.villes.TestUtils;
import fr.eseo.villes.model.City;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"java.*", "javax.*", "org.*",
        "fr.eseo.villes.model.*", "fr.eseo.villes.utils.DatabaseManager", "fr.eseo.villes.utils.Utils"})
@PrepareForTest({HttpUtils.class})
public class CityManagerTest {

    @Before
    public void setUp() {
        TestUtils.prepareTestClass(true);
    }

    @Test
    public void testListRegions() {
        HttpUtils.HttpResult hr = new HttpUtils.HttpResult(200, "[{\"nom\":\"Guadeloupe\",\"code\":\"01\"},{\"nom\":\"Pays de la Loire\",\"code\":\"52\"}]", null);

        PowerMockito.mockStatic(HttpUtils.class);
        Mockito.when(HttpUtils.executeRequest(Mockito.any(), Mockito.any()))
                .thenReturn(new HttpUtils.HttpResult(500, null, null));
        Mockito.when(HttpUtils.executeRequest(Mockito.eq("GET"), Mockito.eq("https://geo.api.gouv.fr/regions")))
                .thenReturn(hr);

        List<Integer> lst = CityManager.listRegions();
        assertEquals(1, lst.size());
        assertEquals((Integer) 52, lst.get(0));
    }

    @Test
    public void testListDepartments() {
        HttpUtils.HttpResult hr = new HttpUtils.HttpResult(200, "[{\"nom\":\"Loire-Atlantique\",\"code\":\"44\",\"codeRegion\":\"52\"},{\"nom\":\"Maine-et-Loire\",\"code\":\"49\",\"codeRegion\":\"52\"},{\"nom\":\"Mayenne\",\"code\":\"53\",\"codeRegion\":\"52\"},{\"nom\":\"Sarthe\",\"code\":\"72\",\"codeRegion\":\"52\"},{\"nom\":\"Vendée\",\"code\":\"85\",\"codeRegion\":\"52\"}]", null);

        PowerMockito.mockStatic(HttpUtils.class);
        Mockito.when(HttpUtils.executeRequest(Mockito.any(), Mockito.any()))
                .thenReturn(new HttpUtils.HttpResult(500, null, null));
        Mockito.when(HttpUtils.executeRequest(Mockito.eq("GET"), Mockito.eq("https://geo.api.gouv.fr/regions/52/departements")))
                .thenReturn(hr);

        List<Integer> lst = CityManager.listDepartments(52);
        assertEquals(5, lst.size());
        assertEquals((Integer) 44, lst.get(0));
        assertEquals((Integer) 49, lst.get(1));
        assertEquals((Integer) 53, lst.get(2));
        assertEquals((Integer) 72, lst.get(3));
        assertEquals((Integer) 85, lst.get(4));
    }

    @Test
    public void testLoadDepartment() {
        HttpUtils.HttpResult hr = new HttpUtils.HttpResult(200, "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"nom\":\"Brissac Loire Aubance\",\"code\":\"49050\",\"codeDepartement\":\"49\",\"codeRegion\":\"52\",\"codesPostaux\":[\"49250\",\"49320\"],\"population\":10714},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-0.409809663865546,47.34791981512604]}}]}", null);

        PowerMockito.mockStatic(HttpUtils.class);
        Mockito.when(HttpUtils.executeRequest(Mockito.any(), Mockito.any()))
                .thenReturn(new HttpUtils.HttpResult(500, null, null));
        Mockito.when(HttpUtils.executeRequest(Mockito.eq("GET"), Mockito.eq("https://geo.api.gouv.fr/departements/01/communes?format=geojson")))
                .thenReturn(hr);

        assertTrue(CityManager.loadDepartment(1));

        List<City> lst = City.getAll();
        assertEquals(1, lst.size());
        City city = lst.get(0);
        assertEquals("Brissac Loire Aubance", city.getName());
        assertEquals(49050, city.getCode());
        assertEquals("49250,49320", city.getPostalCodes());
        assertEquals(-0.409809663865546d, city.getGeoLat(), 0.000001d);
        assertEquals(47.34791981512604d, city.getGeoLong(), 0.000001d);
    }

    @Test
    public void testLoadCities() {
        HttpUtils.HttpResult hr1 = new HttpUtils.HttpResult(200, "[{\"nom\":\"Pays de la Loire\",\"code\":\"52\"}]", null);
        HttpUtils.HttpResult hr2 = new HttpUtils.HttpResult(200, "[{\"nom\":\"Maine-et-Loire\",\"code\":\"49\",\"codeRegion\":\"52\"}]", null);
        HttpUtils.HttpResult hr3 = new HttpUtils.HttpResult(200, "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"nom\":\"Brissac Loire Aubance\",\"code\":\"49050\",\"codeDepartement\":\"49\",\"codeRegion\":\"52\",\"codesPostaux\":[\"49250\",\"49320\"],\"population\":10714},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-0.409809663865546,47.34791981512604]}}]}", null);
        PowerMockito.mockStatic(HttpUtils.class);
        Mockito.when(HttpUtils.executeRequest(Mockito.any(), Mockito.any()))
                .thenReturn(new HttpUtils.HttpResult(500, null, null));
        Mockito.when(HttpUtils.executeRequest(Mockito.eq("GET"), Mockito.eq("https://geo.api.gouv.fr/regions")))
                .thenReturn(hr1);
        Mockito.when(HttpUtils.executeRequest(Mockito.eq("GET"), Mockito.eq("https://geo.api.gouv.fr/regions/52/departements")))
                .thenReturn(hr2);
        Mockito.when(HttpUtils.executeRequest(Mockito.eq("GET"), Mockito.eq("https://geo.api.gouv.fr/departements/49/communes?format=geojson")))
                .thenReturn(hr3);

        CityManager.loadCities();
        List<City> lst = City.getAll();
        assertEquals(1, lst.size());
        assertTrue(DatabaseManager.areCitiesLoaded());
    }
}