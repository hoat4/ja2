/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.platform.desktop;

import com.sun.glass.utils.NativeLibLoader;
import java.io.IOException;
import java.util.Properties;
import ja2.env.JZ;
import ja2.env.NativeLibs;
import java.io.FileInputStream;

/**
 *
 * @author Attila
 */
public class Main {

    public static final Properties config = new Properties();

    static {
        try {
            config.load(new FileInputStream("vm.properties"));
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
        initNatívLoaders();
        Class<?> mainClass = Class.forName("ja2." + config.getProperty("vm.main"));
        mainClass.getMethod("main", String[].class).invoke(null, (Object) args);
    }

    public static boolean getBooleanConfig(String name) {
        return Boolean.parseBoolean(config.getProperty(name));
    }

    public static String getStringConfig(String name) {
        return config.getProperty(name);
    }

    private static void initNatívLoaders() {
        NativeLibs.loaders.add(null);
        NativeLibs.loaders.add(JZ::register);
    }
}
