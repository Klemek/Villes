package fr.klemek.villes.utils;

import fr.klemek.logger.Logger;
import fr.klemek.villes.model.City;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CityManager {

    private CityManager() {
    }

    public static void loadCities() {
        if (!DatabaseManager.areCitiesLoaded()) {
            Logger.log("Loading cities...");
            boolean nofail = true;
            boolean once = false;
            for (int rcode : listRegions()) {
                for (int dcode : listDepartments(rcode)) {
                    once = true;
                    nofail = nofail && loadDepartment(dcode);
                }
            }
            if (once && nofail) {
                DatabaseManager.setCitiesLoaded(true);
                Logger.log("Successfully loaded cities");
            }
        }
        Logger.log("{0} cities in database", City.getAll().size());
    }

    static List<Integer> listRegions() {
        Logger.log("Listing regions...");
        ArrayList<Integer> regions = new ArrayList<>();
        HttpUtils.HttpResult hr = HttpUtils.executeRequest("GET", "https://geo.api.gouv.fr/regions");
        if (hr.code != 200) {
            Logger.log("Error {0} in regions request", hr.code);
            return regions;
        }
        try {
            JSONArray list = hr.getJSONArray();
            for (int i = 0; i < list.length(); i++) {
                JSONObject r = list.getJSONObject(i);
                int code = r.getInt("code");
                if (code > 10)
                    regions.add(code);
            }
        } catch (JSONException e) {
            Logger.log(e);
        }
        return regions;
    }

    static List<Integer> listDepartments(int rcode) {
        Logger.log("Loading region {0}...", rcode);
        ArrayList<Integer> departments = new ArrayList<>();
        String url = String.format("https://geo.api.gouv.fr/regions/%02d/departements", rcode);
        HttpUtils.HttpResult hr = HttpUtils.executeRequest("GET", url);
        if (hr.code != 200) {
            Logger.log("Error {0} in region request", hr.code);
            return departments;
        }
        try {
            JSONArray list = hr.getJSONArray();
            for (int i = 0; i < list.length(); i++) {
                JSONObject r = list.getJSONObject(i);
                departments.add(r.getInt("code"));
            }
        } catch (JSONException e) {
            Logger.log(e);
        }
        return departments;
    }

    static boolean loadDepartment(int dcode) {
        Logger.log("Loading department {0}...", dcode);
        String url = String.format("https://geo.api.gouv.fr/departements/%02d/communes?format=geojson", dcode);
        HttpUtils.HttpResult hr = HttpUtils.executeRequest("GET", url);
        if (hr.code != 200) {
            Logger.log("Error {0} in department request", hr.code);
            return false;
        }

        boolean nofail = true;

        try {
            JSONObject res = hr.getJSON();
            JSONArray list = res.getJSONArray("features");
            for (int i = 0; i < list.length(); i++) {
                JSONObject jsonCity = list.getJSONObject(i).getJSONObject("properties");
                List<String> lstCodes = Utils.jarrayToList(jsonCity.getJSONArray("codesPostaux"));
                List<Double> coordinates = Utils.jarrayToList(list.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates"));
                int code = jsonCity.getInt("code");
                if (City.findByCode(code) == null) {
                    City city = new City(
                            code,
                            jsonCity.getString("nom"),
                            String.join(",", lstCodes),
                            coordinates.get(0),
                            coordinates.get(1)
                    );
                    nofail = nofail && city.save();
                }
            }
        } catch (JSONException e) {
            Logger.log(e);
            return false;
        }

        return true;
    }
}
