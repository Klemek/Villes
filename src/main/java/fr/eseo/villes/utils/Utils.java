package fr.eseo.villes.utils;

import fr.klemek.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Utility class that store useful misc functions.
 */
public final class Utils {

    private static final ResourceBundle RELEASE_BUNDLE = ResourceBundle.getBundle("release");

    private Utils() {
    }

    /**
     * Get a configuration string by its key.
     *
     * @param key the key in the config file
     * @return the string or null if not found
     */
    public static String getString(String key) {
        try {
            return Utils.RELEASE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            Logger.log(Level.SEVERE, "Missing configuration string {0}", key);
            return null;
        }
    }

    /*
     * Other
     */

    /**
     * Try to parse a String as int value.
     *
     * @param text the String value
     * @return the parsed Int or null
     */
    public static Integer tryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    /**
     * Return the class name from the calling class in th stack trace.
     *
     * @param stackLevel the level in the stack trace
     * @return the classname of th calling class
     */
    public static String getCallingClassName(int stackLevel) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackLevel >= stackTrace.length)
            return null;
        String[] source = stackTrace[stackLevel].getClassName().split("\\.");
        return source[source.length - 1];
    }

    /**
     * Get the first not-null item from arguments.
     *
     * @param items the items to take from
     * @param <T>   the class of the items
     * @return the first not-null item or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T coalesce(T... items) {
        for (T i : items) if (i != null) return i;
        return null;
    }

    /**
     * Check if a String is alphanumeric including some chars.
     *
     * @param source   the String to test
     * @param included included chars other than alphanumerics
     * @return true if it passes
     */
    public static boolean isAlphaNumeric(String source, Character... included) {
        if (source == null)
            return true;
        List<Character> includedList = Arrays.asList(included);
        for (char c : source.toCharArray())
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && !includedList.contains(c))
                return false;
        return true;
    }

    /**
     * Check if the first string contains the second in a non case sensitive way.
     *
     * @param s1 first string
     * @param s2 second string
     * @return the result of the check
     */
    public static boolean containsIgnoreCase(String s1, String s2) {
        return s1.toLowerCase().contains(s2.toLowerCase());
    }

    /**
     * Returns a millis time duration into hours and minutes.
     *
     * @param millis the time duration
     * @return a text of the duration in hours and minutes
     */
    public static String getNiceDuration(long millis) {
        long minutes = millis / 60000;
        long hours = minutes / 60;
        minutes %= 60;

        String output = String.format("%02d min.", minutes);
        if (hours > 0) {
            output = String.format("%d hour%s ", hours, hours > 1 ? "s" : "") + output;
        }
        return output;
    }

    /**
     * Convert a timestamp into a readable datetime.
     *
     * @param time the long timestamp
     * @return a string readable datetime
     */
    public static String convertTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(time);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy 'at' HH:mm", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(cal.getTime());
    }
}
