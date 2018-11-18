package fr.eseo.villes.utils;

import fr.klemek.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Utility class that store useful misc functions.
 */
public final class Utils {

    private static final ResourceBundle RELEASE_BUNDLE = ResourceBundle.getBundle("release");

    private static int wordCount = 0;

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
     * Transform a JSONArray into a List of wanted class.
     *
     * @param src the source JSONArray
     * @return a list
     */
    public static <T> List<T> jarrayToList(JSONArray src) {
        List<T> lst = new ArrayList<>(src.length());
        try {
            for (int i = 0; i < src.length(); i++)
                lst.add((T) src.get(i));
        } catch (ClassCastException | NullPointerException e) {
            throw new JSONException("Cannot cast class", e);
        }
        return lst;
    }

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
     * Try to parse a String as long value.
     *
     * @param text the String value
     * @return the parsed Long or null
     */
    public static Long tryParseLong(String text) {
        try {
            return Long.parseLong(text);
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
     * Read the number of words to be selected at random.
     */
    public static void initRandomWords() {
        wordCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Utils.class.getClassLoader().getResourceAsStream("words.txt")))) {
            wordCount = (int) reader.lines().count();
            Logger.log(Level.INFO, "{0} words loaded for room names", wordCount);
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    /**
     * Get a random word from the list file.
     *
     * @return the random select word or a random string if there is none
     */
    public static String getRandomWord() {
        if (wordCount > 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Utils.class.getClassLoader().getResourceAsStream("words.txt")))) {
                String word;
                int tries = 3;
                while (tries > 0) {
                    int position = ThreadLocalRandom.current().nextInt(wordCount);
                    word = reader.lines().skip(position).findFirst().orElse("");
                    if (word.trim().length() > 0)
                        return word.trim();
                    tries--;
                }
            } catch (IOException e) {
                Logger.log(e);
            }
        }
        return Utils.getRandomString(6, "I", "l", "O", "0");
    }

    /**
     * Generate a random string with numbers, uppercase and lowercase letters.
     *
     * @param length  the length of the string
     * @param avoided every substring or char to avoid
     * @return the generated string
     */
    public static String getRandomString(int length, String... avoided) {
        boolean correct;
        String generated;
        do {
            generated = Utils.getRandomString(length);
            correct = true;
            for (String avoidedUnit : avoided) {
                if (generated.contains(avoidedUnit)) {
                    correct = false;
                    break;
                }
            }
        } while (!correct);
        return generated;
    }

    /**
     * Generate a random string with numbers, uppercase and lowercase letters.
     *
     * @param length the length of the string
     * @return the generated string
     */
    public static String getRandomString(int length) {
        StringBuilder output = new StringBuilder();
        int pos;
        for (int i = 0; i < length; i++) { // 48-57 65-90 97-122
            pos = ThreadLocalRandom.current().nextInt(62);
            if (pos < 10)
                output.append((char) (pos + 48)); // numbers
            else if (pos < 36)
                output.append((char) (pos + 55)); // uppercase letters
            else
                output.append((char) (pos + 61)); // lowercase letters
        }
        return output.toString();
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
