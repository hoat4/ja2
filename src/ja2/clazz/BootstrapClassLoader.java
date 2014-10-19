package ja2.clazz;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import ja2.platform.desktop.Main;

/**
 *
 * @author Attila
 */
public class BootstrapClassLoader {

    private static final ClassLoadSource SYS_LIB, USER_LIB;

    static {
        try {
            SYS_LIB = ClassLoadSource.get(new File(Main.getStringConfig("vm.lib.rt")));
            USER_LIB = ClassLoadSource.get(new File(Main.getStringConfig("vm.lib.user")));
        } catch (IOException ex) {
            System.err.println("Error: cannot open jar. JVM exit");
            System.exit(2);
            throw new RuntimeException("This code never executed");
        }
    }

    public static ClassLoadInfo loadClass(String internalName) throws
            IOException {
        ClassLoadInfo result = new ClassLoadInfo();
        String fileName = internalName + ".class";
        InputStream input;
        if ((input = SYS_LIB.open(fileName)) == null) {
            result.systemClass = false;
            result.in = USER_LIB.open(fileName);
            if (result.in == null)
                return null;
        } else {
            result.systemClass = true;
            result.in = input;
        }
        if(result.in != null)
            result.in = new BufferedInputStream(result.in);
        return result;
    }

    private static interface ClassLoadSource {

        InputStream open(String name) throws IOException;

        public static ClassLoadSource get(File file) throws IOException {
            return file.isDirectory() ? new FromDir(file) : new FromJar(file);
        }
    }

    private static class FromJar implements ClassLoadSource {

        private final ZipFile z;

        public FromJar(File file) throws IOException {
            this.z = new ZipFile(file);
        }

        @Override
        public InputStream open(String name) throws IOException {
            ZipEntry entry = z.getEntry(name);
            if (entry == null)
                return null;
            return z.getInputStream(entry);
        }

    }

    private static class FromDir implements ClassLoadSource {

        private final File z;

        public FromDir(File file) throws IOException {
            this.z = file;
        }

        @Override
        public InputStream open(String name) throws FileNotFoundException {
            File file = new File(z, name);
            if (!file.exists())
                return null;
            return new FileInputStream(file);
        }

    }

}
