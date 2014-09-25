package ja2.clazz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import ja2.JThread;
import ja2.Initialization;
import ja2.JavaType;
import ja2.platform.desktop.Main;
import ja2.callback.ErrorCallback;
import ja2.callback.VmCallback;
import ja2.member.MethodInfo;

/**
 *
 * @author Attila
 */
public class ClassLoadHelper {

    // TODO same class dereference in multiple threads at same time can cause multiple <clinit>
    public static final Map<String, ClassInfo> classCache = new HashMap<>();
    static boolean testing = false;

    public static void loadClass(String name, JThread thread, VmCallback<ClassInfo> callback, ErrorCallback ec) {
        ClassInfo cached = classCache.get(name);
        if (cached != null) {
            callback.run(cached);
            return;
        }
        if(name.startsWith("[")) {
            callback.run(ArrayTypeClassInfo.of(JavaType.getType(name.substring(1)), thread));
            return;
        }
        try {
            loadClassPrivate(name, (result) -> {
                classCache.put(name, result);
                if (!testing)
                    Initialization.initializeClass(result, thread, () -> callback.run(result), ec);
                else
                    callback.run(result);
            }, ec, thread);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    private static void loadClassPrivate(String name, VmCallback<ClassInfo> callback, ErrorCallback ec, JThread thread) throws IOException {
        ClassLoadInfo classLoadInfo = BootstrapClassLoader.loadClass(name);
  
        if (classLoadInfo == null) {
            if(Main.getBooleanConfig("vm.load.enableUserClassLoader"))
               loadUserClass(name, callback, ec, thread);
            else Initialization.error(thread, "java/lang/NoClassDefFoundError", "Class '"+name+"' not found and vm.load.enableUserClassLoader turned off");
            return;
        }
        callback.run(ClassFileParser.parseClass(classLoadInfo));
    }

    private static void loadUserClass(String name, VmCallback<ClassInfo> callback, ErrorCallback ec, JThread thread) {
        // TODO ClassLoader object why constant?
        Initialization.USER_CLASSLOADER.classInfo
                .getMethod("loadClass", "(Ljava/lang/String;)V", thread, (loadClassMethod) -> {
                    Object[] param = new Object[]{Initialization.convertString(thread, name)};
                    thread.executeMethod(loadClassMethod, Initialization.USER_CLASSLOADER, param,
                            (result)
                            -> callback.run((ClassInfo) Initialization.getClassInfo((JavaObject.JClassInstance) result)));
                }, ec);
    }

    public static ClassInfo instantLoadClass(String name, JThread thread) {
        ClassInfo cached = classCache.get(name);
        if (cached != null)
            return cached;
        try {
            ClassLoadInfo classLoadInfo = BootstrapClassLoader.loadClass(name);
            if(classLoadInfo == null)
                Initialization.error(thread, "java/lang/NoClassDefFoundError", "Class not found (instantLoadClass): "+name);
            cached = ClassFileParser.parseClass(classLoadInfo);
            classCache.put(name, cached);
            return cached;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
