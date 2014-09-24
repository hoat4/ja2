/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javainterpreter;

import com.sun.glass.utils.NativeLibLoader;
import java.io.IOException;
import java.util.Properties;
import javainterpreter.env.JZ;
import javainterpreter.env.NativeLibs;

/**
 *
 * @author Attila
 */
public class Main {

    public static final Properties config = new Properties();

    static {
        try {
            config.load(Main.class.getResourceAsStream("vm.properties"));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("VM config not loaded. VM will exit. ");
            System.exit(212);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, ReflectiveOperationException {
        initNativeLoaders();
        Class<?> mainClass = Class.forName("javainterpreter." + config.getProperty("vm.main"));
        mainClass.getMethod("main", String[].class).invoke(null, (Object) args);
    }

    public static boolean getBooleanConfig(String name) {
        return Boolean.parseBoolean(config.getProperty(name));
    }

    public static String getStringConfig(String name) {
        return config.getProperty(name);
    }

    private static void initNativeLoaders() {
        NativeLibs.loaders.add(null);
        NativeLibs.loaders.add(JZ::register);
    }
}
