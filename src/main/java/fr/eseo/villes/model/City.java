package fr.eseo.villes.model;

import fr.eseo.villes.utils.DatabaseManager;
import fr.klemek.logger.Logger;

import java.util.List;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;

@Entity
@Table(name = "City")
public class City {

    @Id
    @Column(name = "code")
    private int code;

    @Column(name = "name")
    private String name;

    @Column(name = "postal_codes")
    private String postalCodes;

    @Column(name = "geo_lat")
    private Double geoLat;

    @Column(name = "geo_long")
    private Double geoLong;

    public City() {

    }

    public City(int code, String name, String postalCodes, Double geoLat, Double geoLong) {
        this.code = code;
        this.name = name;
        this.postalCodes = postalCodes;
        this.geoLat = geoLat;
        this.geoLong = geoLong;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostalCodes() {
        return postalCodes;
    }

    public Double getGeoLat() {
        return geoLat;
    }

    public Double getGeoLong() {
        return geoLong;
    }

    public boolean save() {
        if (!DatabaseManager.isHibernateInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, cannot save object");
            return false;
        }
        Transaction tx = null;
        Session session = DatabaseManager.getSessionFactory().getCurrentSession();
        try {
            tx = session.beginTransaction();
            session.save(this);
            tx.commit();
            return true;
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            Logger.log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }

    public boolean update() {
        if (!DatabaseManager.isHibernateInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, cannot save object");
            return false;
        }
        Transaction tx = null;
        Session session = DatabaseManager.getSessionFactory().getCurrentSession();
        try {
            tx = session.beginTransaction();
            session.update(this);
            tx.commit();
            return true;
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            Logger.log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }

    /**
     * Delete the row.
     *
     * @return true if operation is successful
     */
    public boolean delete() {
        if (!DatabaseManager.isHibernateInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, cannot delete object");
            return false;
        }
        Transaction tx = null;
        Session session = DatabaseManager.getSessionFactory().getCurrentSession();
        try {
            tx = session.beginTransaction();
            session.delete(this);
            tx.commit();
            return true;
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            Logger.log(Level.SEVERE, e.toString(), e);
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + code;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof City) {
            City other = (City) obj;
            //Fix hibernate anonymous class
            String thisSimpleName = this.getClass().getSimpleName();
            String objSimpleName = obj.getClass().getSimpleName();
            if (!thisSimpleName.startsWith(objSimpleName) && !objSimpleName.startsWith(thisSimpleName))
                return false;
            if (code == 0 || other.code == 0) {
                return false;
            } else return code == (int) other.code;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", this.getClass().getSimpleName(), this.code);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("name", name);
        json.put("postalCodes", postalCodes);
        json.put("geoLat", geoLat);
        json.put("geoLong", geoLong);
        return json;
    }

    /**
     * Return a row by its id.
     *
     * @param code          the id to find
     * @return the object or null if not found
     */
    public static City findByCode(int code) {
        return DatabaseManager.getFirstFromSessionQuery("FROM City WHERE code = ?0", code);
    }

    public static List<City> search(String query) {
        return DatabaseManager.getRowsFromSessionQuery("FROM City WHERE LOWER(name) LIKE ?0 OR postalCodes LIKE ?0", "%" + query.toLowerCase() + "%");
    }

    /**
     * Return all rows from table.
     *
     * @return all the rows from the database
     */
    public static List<City> getAll() {
        return DatabaseManager.getRowsFromSessionQuery("FROM City");
    }
}
