package bybjerrum.utils;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Purpose: Utility class to read properties from a file
 * Author: Thomas Hartmann
 */
public class Utils {
    public static void main(String[] args) {
        System.out.println(getPropertyValue("db.name", "config.properties"));
    }
    public static String getPropertyValue(String propName, String ressourceName)  {
        try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(ressourceName)) {
            if (is == null) {
                throw new RuntimeException(String.format("Could not find resource %s", ressourceName));
            }
            Properties prop = new Properties();
            prop.load(is);
            String value = prop.getProperty(propName);
            if (value == null) {
                throw new RuntimeException(String.format("Could not find property %s in %s", propName, ressourceName));
            }
            return value;
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Could not read property %s from %s", propName, ressourceName), ex);
        }
    }


}