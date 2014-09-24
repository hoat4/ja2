package ja2;

import ja2.platform.desktop.Main;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static ja2.platform.desktop.Main.getBooleanConfig;
import ja2.callback.ErrorCallback;
import ja2.callback.VmCallback;
import ja2.clazz.AbstractClassInfo;
import ja2.clazz.ClassInfo;
import ja2.clazz.ClassLoadHelper;
import ja2.clazz.JavaObject;
import ja2.clazz.JavaObject.JArray;
import ja2.clazz.JavaObject.JClassInstance;
import ja2.clazz.PrimitiveTypeClassInfo;
import ja2.member.MethodAccessFlag;
import ja2.member.MethodCallInfo;
import ja2.member.MethodInfo;
import ja2.vm.Bytecodes;

/**
 *
 * @author Attila
 */
public class JavaInterpreter {

    public static final List<MethodCallInfo> methodCalls = new ArrayList<>();
    public static final boolean logStatementCode = getBooleanConfig("vm.methodLogging.logInstructionCode");
    public static final boolean logMethodInvoke = getBooleanConfig("vm.log.methodInvoke");
    private static boolean debugMode = getBooleanConfig("vm.debugging.consoleDebbugger");
    public static JClassInstance USER_CLASSLOADER;
    public static final boolean LOG_MISC = getBooleanConfig("vm.log.misc");
    public static final boolean logNativeMethod = getBooleanConfig("vm.log.nativeMethod");
    private static final Object DONT_RETURN = new Object();
    private static final boolean LOG_ERROR = getBooleanConfig("vm.log.error");
    public static JThread currentThread;
    public static volatile boolean enable = getBooleanConfig("vm.enable");

    public static void initializeClass(AbstractClassInfo clazz, JThread thread, Runnable callback, ErrorCallback errorCallback) {
        if (Main.getBooleanConfig("vm.log.misc"))
            System.out.println("initclass: " + clazz.name);
        VmCallback<ClassInfo> lambda = (classClass) -> classClass.instantiateAndInit(thread, (instance) -> {
            clazz.classObject = instance;
            instance.transfer = clazz;
            if (clazz instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) clazz;
                VmCallback<Boolean> callbackWrapper = (b) -> {
                    if (Main.getBooleanConfig("vm.log.misc"))
                        System.out.println("inited class (" + (b ? "clinited" : "no clinit") + "): " + clazz.name);
                    callback.run();
                };

                if (classInfo.clinitMethod != null)
                    currentThread.executeMethod(classInfo.clinitMethod, null, NO_PARAMETERS, (a) -> callbackWrapper.run(true));
                else
                    callbackWrapper.run(false);
            }
            classObjectMap.put(clazz.classObject, clazz);
        }, errorCallback);
        if (clazz.name.equals("java/lang/Class"))
            lambda.run((ClassInfo) clazz);
        else
            ClassLoadHelper.loadClass("java/lang/Class", thread, lambda, errorCallback);
    }

    public static void main(final String[] args) {
        long start_perf_ns_measure_local_variable_nb___ = System.nanoTime();
        try {
            work(args);
        } finally {
        //    System.out.println("Performance: " + ((System.nanoTime() - start_perf_ns_measure_local_variable_nb___) / 1000) + " μs");
        }
    }

    private static void work(final String[] args) {
        System.out.println("JVM started");
        try {
            if ((args.length == 1 && args[0].equals("-debug")) || (args.length
                    == 2 && args[1].equals("-debug")))
                debugMode = true;
            currentThread = new JThread(new ArrayList());
            currentThread.runningThreadsRef.add(currentThread);
            currentThread.runnable = true;
            STRING = ClassLoadHelper.instantLoadClass("java/lang/String", currentThread);
            ClassLoadHelper.loadClass("java/lang/Thread", currentThread, (threadClass) -> {
                threadClass.instantiate(currentThread, (threadObject) -> {
                    threadObject.transfer = currentThread;
                    currentThread.object = threadObject;
                    currentThread.object.fieldValues.put("tid", 1);
                    currentThread.object.fieldValues.put("priority", 5);
                    ClassLoadHelper.loadClass("java/lang/ThreadGroup", currentThread, (tgClass) -> {
                        tgClass.instantiateAndInit(currentThread, (mainTG) -> {
                            threadObject.invokeConstructor("(Ljava/lang/ThreadGroup;Ljava/lang/String;)V", currentThread, (ignored) -> {
                                initializeClass(STRING, currentThread, () -> {
                                    initPrimitives(currentThread, () -> {
                                        ClassLoadHelper.loadClass("java/lang/System", currentThread, (systemClass) -> {
                                            systemClass.getMethod("initializeSystemClass", "()V", currentThread, (sysInitMethod) -> {
                                                currentThread.executeMethod(sysInitMethod, null, NO_PARAMETERS, (ignored2) -> {
                                                    System.out.println("VM booted. ");
                                                    ClassLoadHelper.loadClass(Main.getStringConfig("vm.start.main"), currentThread, (clazz) -> {
                                                        clazz.getMethod("main", "([Ljava/lang/String;)V", currentThread, (mainMethod) -> {
                                                            JavaType stringClass = JavaType.clazz("java/lang/String");
                                                            JArray argArray = new JArray(args.length, stringClass, currentThread);
                                                            // TODO args beszúrása argArray-be
                                                            currentThread.executeMethod(mainMethod, null, new Object[]{argArray}, (a) -> {
                                                                System.out.println("Application main method returned. ");
                                                            });
                                                        }, Throwable::printStackTrace);
                                                    }, Throwable::printStackTrace);
                                                });
                                            }, Throwable::printStackTrace);
                                        }, Throwable::printStackTrace);
                                    }, Throwable::printStackTrace);
                                }, Throwable::printStackTrace);
                            }, Throwable::printStackTrace, mainTG, convertString(currentThread, "Application main thread"));
                        }, Throwable::printStackTrace);
                    }, Throwable::printStackTrace);
                }, Throwable::printStackTrace);
            }, Throwable::printStackTrace);

            VmLifecycle vm = new VmLifecycle(currentThread);
            vm.run();
        } catch (JException jex) {
            String ts = toString((JClassInstance) jex.ex.fieldValues.get(
                    "detailMessage"));
            System.out.println("Java Exception: " + jex.ex.classInfo.name
                    + ";message=" + ts);
        }
    }
    public static final Object[] NO_PARAMETERS = MethodCallInfo.ZERO_PARAMETERS;

    public static String toString(JClassInstance string) {
        if (string == null)
            return "null";
        if (!string.classInfo.name.equals("java/lang/String"))
            throw new RuntimeException("Cannot get string value from "
                    + string.classInfo.name);
        if (string.fieldValues.get("value") == null)
            return null;
        Object[] stringContent = ((JArray) string.fieldValues.get("value")).array;
        return new String(toCharArray(stringContent));
    }

    private static char[] toCharArray(Object[] array) {
        char[] result = new char[array.length];
        for (int i = 0; i < array.length; i++)
            result[i] = (char) Bytecodes.toInt(array[i]);
        return result;
    }

    public static void throwException(JavaObject.JClassInstance ex) {
        throw new JException(ex);
    }

    public static JClassInstance convertString(JThread thread, String string) {
        JArray chararray = new JArray(string.length(), JavaType.CHAR, thread);
        for (int i = 0; i < string.length(); i++)
            chararray.array[i] = string.charAt(i);
        if (STRING == null)
            throw new IllegalStateException("String class not loaded");
        JClassInstance result = JClassInstance.createInstant(STRING);
        result.fieldValues.put("value", chararray);
        return result;
    }

    public static void error(JThread thread, String errorClass, String message) {
        if (LOG_ERROR) {
            System.out.println("Error:" + errorClass + ": " + message);
            System.out.println("Thread: " + thread);
            System.out.println("Stack trace: ");
            for (MethodCallInfo methodCallInfo : thread.stackTrace)
                System.out.println(methodCallInfo);
            System.out.flush();
            throw new RuntimeException("VM Stack trace");
        }
        if (debugger != null)
            debugger.onError(getStackTraceTop());
//        debugger.methodInvoked(new MethodCallInfo(
//                new MethodInfo(EnumSet.noneOf(MethodAccessFlag.class), "exception", "()V",
//                loadClass("java/lang/Class")), getStackTraceTop()));
        ClassLoadHelper.loadClass(errorClass, thread, (clazz) -> {
            JClassInstance ex = JClassInstance.createInstant(clazz);
            clazz.getMethod("<init>", "(Ljava/lang/String;)V", thread,
                    (initMethod) -> {
                        thread.executeMethod(initMethod, ex, new Object[]{convertString(thread, message)},
                                (notused1) -> {
                                    throwException(ex);
                                }
                        );
                    }, (fatalError) -> {
                        throw new RuntimeException("FATAL ERROR: cannot dispatch exception", fatalError);
                    });
        }, JavaInterpreter::handleError);
    }

    private static void setMethodArgumentVariables(JClassInstance thiz,
            Object[] args, Object[] localVariables) {
        int i = 0;
        if (thiz != null)
            localVariables[i++] = thiz;
        for (Object arg : args)
            localVariables[i++] = arg;
    }
    private static final Map<JClassInstance, AbstractClassInfo> classObjectMap
            = new HashMap<>();

    public static JClassInstance getClassLoader(JClassInstance classObject) {
        AbstractClassInfo aci = classObjectMap.get(classObject);
        if (aci.isPrimitiveClass)
            return null;
        ClassInfo clazz = (ClassInfo) aci;
        if (clazz.systemClass)
            return null;
        return USER_CLASSLOADER;
    }

    public static AbstractClassInfo getClassInfo(JClassInstance classObject) {
        return classObjectMap.get(classObject);
    }

    private static void createStackTraceElement(MethodInfo method, JThread thread, VmCallback<JClassInstance> callback, ErrorCallback ec) {
        ClassLoadHelper.instantLoadClass("java/lang/StackTraceElement", thread).instantiate(thread, (result) -> {
            result.fieldValues.put("declaringClass", convertString(thread, currentThread.stackTrace.peek().method.clazz.
                    toNormalName()));
            result.fieldValues.put("methodName", method.name);
            result.fieldValues.put("lineNumber", method.accessFlags.contains(
                    MethodAccessFlag.NATIVE) ? -2 : -1);
            callback.run(result);
        }, ec);
    }
    public static VmDebugger debugger;

    public static void setDebugger(VmDebugger listener) {
        debugger = listener;
    }

    private static MethodCallInfo getStackTraceTop() {
        return currentThread.stackTrace.peek();
    }

    public static void handleError(Exception error) {
        throw new RuntimeException(error);
    }
    public static ClassInfo STRING;

    private static void initPrimitives(JThread thread, Runnable callback, ErrorCallback ec) {
        class Helper {

            int counter, max = PrimitiveTypeClassInfo.array.length;
        }
        ClassLoadHelper.loadClass("java/lang/Class", thread, (classClass) -> {
            Helper helper = new Helper();

            for (PrimitiveTypeClassInfo cinfo : PrimitiveTypeClassInfo.array)
                classClass.instantiate(thread, (instance) -> {
                    classObjectMap.put(instance, cinfo);
                    cinfo.classObject = instance;
                    if (++helper.counter >= helper.max)
                        callback.run();
                }, ec);
        }, ec);
    }

    private JavaInterpreter() {
    }

    private static class JException extends RuntimeException {

        public final JavaObject.JClassInstance ex;

        public JException(JavaObject.JClassInstance ex) {
            super("Java Exception");
            this.ex = ex;
        }
    }
}
