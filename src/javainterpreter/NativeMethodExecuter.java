package javainterpreter;

import com.sun.glass.utils.NativeLibLoader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javainterpreter.JavaInterpreter.convertString;
import static javainterpreter.JavaInterpreter.error;
import javainterpreter.callback.VmCallback;
import javainterpreter.clazz.ClassAccessFlag;
import javainterpreter.clazz.ClassInfo;
import javainterpreter.clazz.ClassLoadHelper;
import javainterpreter.clazz.JavaObject;
import javainterpreter.clazz.JavaObject.Evaluation;
import javainterpreter.clazz.JavaObject.JArray;
import javainterpreter.clazz.JavaObject.JClassInstance;
import javainterpreter.clazz.PrimitiveTypeClassInfo;
import javainterpreter.env.DirectoryData;
import javainterpreter.env.FileData;
import javainterpreter.env.FileException;
import javainterpreter.env.Files;
import javainterpreter.env.INode;
import javainterpreter.env.NativeLibs;
import javainterpreter.env.NormalFileLayout;
import javainterpreter.member.FieldInfo;
import javainterpreter.member.MethodCallInfo;
import javainterpreter.member.MethodInfo;
import javainterpreter.vm.Bytecodes;
import javainterpreter.vm.VmContext;
import static javainterpreter.vm.VmContext.VOID;

/**
 *
 *
 * @author Attila
 */
public class NativeMethodExecuter {

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void executeNativeMethod(VmContext ctx) {
        MethodInfo method = ctx.method;
        Object[] parameters = ctx.call.args;
        JClassInstance thiz = ctx.thiz;
        if (method.name.equals("registerNatives")) {
            ctx.thread.popMethod(VmContext.VOID);
            return;
        }
        Object p0 = parameters.length > 0 ? parameters[0] : null;
        switch (method.clazz.name + "::" + method.name) {
            case "java/lang/Class::getClassLoader0":
                ctx.thread.popMethod(JavaInterpreter.getClassLoader(thiz));
                break;
            case "java/lang/Class::desiredAssertionStatus0":
                ctx.thread.popMethod(0);
                break;
            case "java/lang/Class::getPrimitiveClass":
                ctx.thread.popMethod(PrimitiveTypeClassInfo.get(JavaInterpreter.
                        toString((JavaObject.JClassInstance) p0)));
                break;
            case "java/lang/Class::getDeclaredFields0":
                if (!thiz.classInfo.name.equals("java/lang/Class"))
                    error(ctx.thread, "InternalError", "getDeclaredField0 invoked on a non-class object");
                FieldInfo[] arr = ((ClassInfo) thiz.transfer).fields;
                JavaObject.JArray array = new JavaObject.JArray(arr.length, JavaType.clazz("java/lang/reflect/Field"), ctx.thread);
                ClassLoadHelper.loadClass("java/lang/reflect/Field", ctx.thread, (fieldClass) -> {
                    for (int i = 0; i < arr.length; i++) {
                        FieldInfo fieldInfo = arr[i];
                        JClassInstance object = JClassInstance.createInstant(fieldClass);
                        object.transfer = fieldInfo;
                        object.fieldValues.put("override", 0); // setAccessible(false)
                        object.fieldValues.put("clazz", thiz);
                        object.fieldValues.put("name", internString(convertString(ctx.thread, fieldInfo.name)));
                        object.fieldValues.put("type", (Evaluation) (ctx2, callback) -> {
                            JavaType.getType(fieldInfo.type).asClassInfo(ctx2.thread, (ci) -> {
                                callback.run(ci.classObject);
                            }, ctx2.ec);
                        });
                        int fid = idgen++;
                        fieldIDMap.put(fid, fieldInfo);
                        object.fieldValues.put("slot", fid);
                        object.fieldValues.put("signature", convertString(ctx.thread, fieldInfo.type));
                        object.fieldValues.put("annotations", 0);
                        object.fieldValues.put("root", null);
                        object.fieldValues.put("modifiers", fieldInfo.rawModifiers);
                        array.array[i] = object;
                    }
                    ctx.thread.popMethod(array);
                }, ctx.ec);
                break;
            case "java/lang/Class::getDeclaredConstructors0":
                if (!thiz.classInfo.name.equals("java/lang/Class"))
                    error(ctx.thread, "InternalError", "getDeclaredField0 invoked on a non-class object");
                List<MethodInfo> carr = ((ClassInfo) thiz.transfer).constructors();
                array = new JavaObject.JArray(carr.size(), JavaType.clazz("java/lang/reflect/Constructor"), ctx.thread);
                ClassLoadHelper.loadClass("java/lang/reflect/Constructor", ctx.thread, (ClassInfo fieldClass) -> {
                    for (int i = 0; i < carr.size(); i++) {
                        MethodInfo methodInfo = carr.get(i);
                        JClassInstance object = JClassInstance.createInstant(fieldClass);
                        object.transfer = methodInfo;
                        object.fieldValues.put("clazz", thiz);
                        int fid = idgen++;
                        object.fieldValues.put("slot", fid);
                        mIDMap.put(fid, methodInfo);
                        JavaObject.JArray parr = new JavaObject.JArray(methodInfo.parameterTypes.length, JavaType.clazz("java/lang/Class"), ctx.thread);
                        for (int j = 0; j < methodInfo.parameterTypes.length; j++) {
                            JavaType parameterType = methodInfo.parameterTypes[j];
                            parr.array[i] = (Evaluation) (ctx2, callback) -> {
                                parameterType.asClassInfo(ctx2.thread, (cinfo) -> callback.run(cinfo.classObject), ctx2.ec);
                            };
                        }
                        object.fieldValues.put("parameterTypes", parr);
                        object.fieldValues.put("signature", convertString(ctx.thread, methodInfo.descriptor));
                        object.fieldValues.put("annotations", new JavaObject.JArray(0, JavaType.BYTE, ctx.thread));
                        object.fieldValues.put("parameterAnnotations", new JavaObject.JArray(0, JavaType.BYTE, ctx.thread));
                        array.array[i] = object;
                    }
                    ctx.thread.popMethod(array);
                }, ctx.ec);
                break;
            case "java/lang/Class::forName0":
                ClassLoadHelper.loadClass(JavaInterpreter.toString((JClassInstance) p0).replace('.', '/'), ctx.thread, (clazz) -> {
                    ctx.thread.popMethod(clazz.classObject);
                }, ctx.ec);
                break;
            case "java/lang/Class::isInterface":
                ctx.thread.popMethod(((ClassInfo) thiz.transfer).access.contains(ClassAccessFlag.INTERFACE) ? 1 : 0);
                break;
            case "java/lang/Class::getName0":
                ctx.thread.popMethod(convertString(ctx.thread, ((ClassInfo) thiz.transfer).asType.internalClassName));
                break;
            case "java/lang/Class::getModifiers":
                ctx.thread.popMethod(((ClassInfo) thiz.transfer).rawModifiers);
                break;
            case "java/lang/Class::getSuperclass":
                ClassInfo ci = (ClassInfo) thiz.transfer;
                if (ci.superClassName == null)
                    ctx.thread.popMethod(null);
                else
                    ClassLoadHelper.loadClass(ci.superClassName, ctx.thread, (clazz) -> {
                        ctx.thread.popMethod(clazz.classObject);
                    }, ctx.ec);
                break;

            case "sun/reflect/NativeConstructorAccessorImpl::newInstance0":
                MethodInfo c = mIDMap.get(((JClassInstance) p0).fieldValues.get("slot"));
                c.clazz.instantiate(ctx.thread, (jci) -> {
                    JArray p1 = (JArray) parameters[1];
                    if (p1 == null)
                        p1 = new JArray(0, JavaType.clazz("java/lang/Object"), ctx.thread);
                    ctx.thread.executeMethod(c, jci, p1.array, (ignoredNull) -> {
                        ctx.thread.popMethod(jci);
                    });
                }, ctx.ec);
                break;

            case "java/lang/System::arraycopy":
                arraycopy(parameters, ctx.thread);
                ctx.thread.popMethod(VmContext.VOID);
                break;
            case "java/lang/System::identityHashCode":
                ctx.thread.popMethod(p0 == null ? 0 : ((JClassInstance) p0).hashCode());
                break;
            case "java/lang/System::currentTimeMillis":
                ctx.thread.popMethod(System.currentTimeMillis());
                break;
            case "java/lang/System::nanoTime":
                ctx.thread.popMethod(System.nanoTime());
                break;
            case "java/lang/System::initProperties":
                JClassInstance po0 = (JClassInstance) parameters[0];
                ClassInfo clasz = po0.classInfo;
                clasz.getMethod("setProperty", "(Ljava/lang/String;Ljava/lang/String;)V", ctx.thread, (s) -> {
                    JThread t = ctx.thread;
                    VmCallback<Object> n = VmCallback.NOP;
                    t.executeMethod(s, po0, new Object[]{convertString(t, "ja2.vm_type"), convertString(t, "Java")}, (ignored) -> {
                        t.popMethod(po0);
                    });
                    t.executeMethod(s, po0, new Object[]{convertString(t, "file.encoding"), convertString(t, "UTF-8")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "file.separator"), convertString(t, "/")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.class.path"), convertString(t, "/lib/classes")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.class.version"), convertString(t, "52.0")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.endorsed.dir"), convertString(t, "/lib/endorsed")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.ext.dirs"), convertString(t, "/lib/ext")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.home"), convertString(t, "/lib/ja2")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.io.tmpdir"), convertString(t, "/tmp")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.library.path"), convertString(t, "/lib/native")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "sun.boot.library.path"), convertString(t, "/lib/native")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.runtime.name"), convertString(t, "Ja2")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.runtime.version"), convertString(t, "0.1")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.specification.name"), convertString(t, "Java Platform API Specification")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.specification.vendor"), convertString(t, "Oracle Corporation")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.specification.version"), convertString(t, "1.8")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.vendor"), convertString(t, "Hontvári Attila")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.vendor.url"), convertString(t, "http://attila.hontvari.net/")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.vendor.url.bug"), convertString(t, "mailto:attila@hontvari.net")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.vm.vendor"), convertString(t, "Hontvári Attila")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.vm.name"), convertString(t, "Ja2")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "java.vm.version"), convertString(t, "0.1")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "line.separator"), convertString(t, "\n")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "os.arch"), convertString(t, "class")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "os.name"), convertString(t, "Ja2 isolated environment")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "os.version"), convertString(t, "0.0")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "path.separator"), convertString(t, ":")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "user.country"), convertString(t, "HU")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "user.dir"), convertString(t, "/")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "user.home"), convertString(t, "/user")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "user.language"), convertString(t, "hu")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "user.name"), convertString(t, "user")}, n);
                    t.executeMethod(s, po0, new Object[]{convertString(t, "user.timezone"), convertString(t, "")}, n);
                }, ctx.ec);
                break;
            case "java/lang/System::setIn0":
                ctx.method.clazz.staticFieldValues.put("in", p0);
                ctx.thread.popMethod(VOID);
                break;
            case "java/lang/System::setOut0":
                ctx.method.clazz.staticFieldValues.put("out", p0);
                ctx.thread.popMethod(VOID);
                break;
            case "java/lang/System::setErr0":
                ctx.method.clazz.staticFieldValues.put("err", p0);
                ctx.thread.popMethod(VOID);
                break;
            case "java/lang/System::mapLibraryName":
                ctx.thread.popMethod(convertString(ctx.thread, JavaInterpreter.toString((JClassInstance) p0) + ".lib"));
                break;

            case "java/lang/ClassLoader$NativeLibrary::findBuiltinLib":
                String inputLibName = JavaInterpreter.toString((JClassInstance) p0);
                if (inputLibName.equals("zip.lib"))
                    ctx.thread.popMethod(convertString(ctx.thread, "/lib/native/zip.lib"));
                else
                    ctx.thread.popMethod(null);
                break;
            case "java/lang/ClassLoader$NativeLibrary::load":
                NativeLibs.load(ctx, fs, (JClassInstance) p0);
                thiz.fieldValues.put("loaded", 1);
                ctx.thread.popMethod(VOID);
                break;

            case "sun/misc/Signal::findSignal":
                // http://people.cs.pitt.edu/~alanjawi/cs449/code/shell/UnixSignals.htm
                switch (JavaInterpreter.toString((JClassInstance) p0)) {
                    case "HUP":
                        ctx.thread.popMethod(1);
                        break;
                    case "INT":
                        ctx.thread.popMethod(2);
                        break;
                    case "TERM":
                        ctx.thread.popMethod(15);
                        break;
                    // TODO more signal codes
                    default:
                        ctx.thread.popMethod(0);
                        break;
                }
                break;
            case "sun/misc/Signal::handle0":
                // TODO implement
                ctx.thread.popMethod(2L);
                break;

            case "javainterpreter/test/Test::sysoutint":
                System.out.println(p0);
                ctx.thread.popMethod(VmContext.VOID);
                return;

            case "java/lang/Throwable::fillInStackTrace":
                thiz.transfer = Collections.
                        unmodifiableCollection(ctx.thread.stackTrace);
                ClassLoadHelper.
                        loadClass("java/lang/Object", ctx.thread, (clazz) -> {
                            clazz.instantiate(ctx.thread, (backtrace) -> {
                                thiz.fieldValues.put("backtrace", backtrace);
                                ctx.thread.popMethod(null);
                            }, JavaInterpreter::handleError);
                        }, JavaInterpreter::handleError);
                break;
            case "java/lang/Throwable::getStackTraceDepth":
                ctx.thread.popMethod(((Collection) thiz.transfer).size());
                break;
            case "java/lang/Throwable::getStackTraceElement":
                ctx.thread.popMethod(((Collection) thiz.transfer).toArray()[(int) p0]);
                break;

            case "java/lang/Float::floatToRawIntBits":
                ctx.thread.popMethod(Float.floatToRawIntBits((float) p0));
                break;
            case "java/lang/Double::doubleToRawLongBits":
                ctx.thread.popMethod(Double.doubleToRawLongBits((double) p0));
                break;
            case "java/lang/Double::longBitsToDouble":
                ctx.thread.popMethod(Double.longBitsToDouble((long) p0));
                break;

            case "java/lang/Object::hashCode":
                ctx.thread.popMethod(thiz.hashCode());
                break;
            case "java/lang/Object::clone":
                if (thiz instanceof JArray) {
                    Object[] origa = ((JArray) thiz).array;
                    JArray newa = new JArray(origa.length, ((JArray) thiz).elemType, ctx.thread);
                    for (int i = 0; i < origa.length; i++)
                        newa.array[i] = origa[i];
                    ctx.thread.popMethod(newa);
                } else
                    thiz.instanceOf("java/lang/Cloneable", ctx.thread, (cloneable) -> {
                        if (!cloneable)
                            error(ctx.thread, "java/lang/CloneNotSupportedException", thiz.classInfo.name + " doesn't implements java/lang/Cloneable");
                        JClassInstance newInstance = JClassInstance.createInstant(thiz.classInfo);
                        for (String fieldName : thiz.fieldValues.keySet())
                            newInstance.fieldValues.put(fieldName, thiz.fieldValues.get(fieldName));
                        ctx.thread.popMethod(newInstance);
                    }, ctx.ec);
                break;

            case "java/lang/Thread::currentThread":
                ctx.thread.popMethod(ctx.thread.object);
                break;

            case "java/lang/Runtime::freeMemory":
                ctx.thread.popMethod(Runtime.getRuntime().freeMemory());
                break;

            case "sun/misc/Unsafe::arrayBaseOffset":
                ctx.thread.popMethod(0);
                break;
            case "sun/misc/Unsafe::arrayIndexScale":
                ctx.thread.popMethod(1);
                break;
            case "sun/misc/Unsafe::addressSize":
                ctx.thread.popMethod(4);
                break;
            case "sun/misc/Unsafe::objectFieldOffset":
                po0 = (JClassInstance) p0;
                FieldInfo name = fieldIDMap.get(po0.fieldValues.get("slot"));
                ctx.thread.popMethod((name).index);
                break;
            case "sun/misc/Unsafe::compareAndSwapObject":
            case "sun/misc/Unsafe::compareAndSwapInt":
                JClassInstance o = (JClassInstance) p0;
                int offset = (int) parameters[1];
                Object expected = parameters[2];
                Object x = parameters[3];
                FieldInfo finfo = o.classInfo.fields[offset];
                boolean bresult = false;
                if (o.fieldValues.get(finfo.name) == expected) {
                    bresult = true;
                    o.fieldValues.put(finfo.name, x);
                }
                ctx.thread.popMethod(bresult ? 1 : 0);
                break;
            case "sun/misc/Unsafe::getIntVolatile":
                o = (JClassInstance) p0;
                offset = (int) parameters[1];
                finfo = o.classInfo.fields[offset];
                ctx.thread.popMethod(o.fieldValues.get(finfo.name));
                break;
            case "sun/misc/Unsafe::allocateMemory":
                ctx.thread.popMethod(MemorySpace.allocate((long) parameters[0]));
                break;
            case "sun/misc/Unsafe::putLong":
                MemorySpace.putLong((long) parameters[0], (long) parameters[1]);
                ctx.thread.popMethod(VOID);
                break;
            case "sun/misc/Unsafe::getByte":
                ctx.thread.popMethod(MemorySpace.getByte((long) parameters[0]));
                break;
            case "sun/misc/Unsafe::freeMemory":
                MemorySpace.free((long) parameters[0]);
                ctx.thread.popMethod(VOID);
                break;

            case "sun/reflect/Reflection::getCallerClass":
                int index = 2;
                ClassInfo result;
                do {
                    result = ctx.thread.stackTrace.get(index).method.clazz;
                } while (result.name.equals("sun/reflect/NativeMethodAccessorImpl"));
                ctx.thread.popMethod(result.classObject);
                break;
            case "sun/reflect/Reflection::getClassAccessFlags":
                ctx.thread.popMethod(((ClassInfo) ((JClassInstance) p0).transfer).rawModifiers);
                break;
            case "sun/misc/VM::initialize":
                ctx.thread.popMethod(VmContext.VOID);
                break;

            case "java/io/FileInputStream::initIDs":
            case "java/io/FileDescriptor::initIDs":
            case "java/io/FileOutputStream::initIDs":
            case "java/io/UnixFileSystem::initIDs":
                ctx.thread.popMethod(VmContext.VOID);
                break;
            case "java/io/UnixFileSystem::getBooleanAttributes0":
                String fpath = JavaInterpreter.toString((JClassInstance) ((JClassInstance) p0).fieldValues.get("path"));
                if (!fs.exists(fpath))
                    ctx.thread.popMethod(0);
                else
                    try {
                        INode inode = fs.inode(fpath);
                        int baresult = 1;
                        if (inode instanceof FileData)
                            baresult |= 0x02;
                        else if (inode instanceof DirectoryData)
                            baresult |= 0x04;
                        if (inode.hidden)
                            baresult |= 0x08;
                        ctx.thread.popMethod(baresult);
                    } catch (FileException ex) {
                        error(ctx.thread, "java/lang/InternalError", ex.getMessage());
                    }
                break;
            case "java/io/FileOutputStream::writeBytes":
                int fhandle = Bytecodes.toInt(((JClassInstance) thiz.fieldValues.get("fd")).fieldValues.get("handle"));
                fs.writers.get(fhandle).write((JArray) p0, Bytecodes.toInt(parameters[1]), Bytecodes.toInt(parameters[2]));
                ctx.thread.popMethod(VOID);
                break;

            case "java/security/AccessController::doPrivileged":
                switch (ctx.method.parameterTypes.length) {
                    case 1:
                        JClassInstance par1instance = (JClassInstance) ctx.args[0];
                        // TODO real AccessController
                        par1instance.classInfo.getMethod("run", "()V", ctx.thread, (runMethod) -> {
                            ctx.thread.executeMethod(runMethod, par1instance, MethodCallInfo.ZERO_PARAMETERS, (resultUmpteen) -> {
                                ctx.thread.popMethod(resultUmpteen);
                            });
                        }, ctx.ec);
                        break;
                    default:
                        JavaInterpreter
                                .error(ctx.thread, "java/lang/UnsatisfiedLinkError", "AccessController.doPrivileged with " + ctx.method.parameterTypes.length + "parameters");
                }
                break;
            case "java/security/AccessController::getStackAccessControlContext":
                ctx.thread.popMethod(null);// fake "privileged system code"
                break;

            case "java/lang/Thread::setPriority0":
                if (thiz.transfer == null)
                    thiz.transfer = new JThread(ctx.thread.runningThreadsRef);
                ((JThread) thiz.transfer).priority = (int) parameters[0];
                ctx.thread.popMethod(VmContext.VOID);
                break;
            case "java/lang/Thread::isAlive":
                ctx.thread.popMethod(((JThread) thiz.transfer).runnable ? 1 : 0);
                break;
            case "java/lang/Thread::start0":
                JThread newThread = (JThread) thiz.transfer;
                thiz.classInfo.getMethod("run", "()", ctx.thread, (runMethod) -> {
                    newThread.executeMethod(runMethod, thiz, MethodCallInfo.ZERO_PARAMETERS, (ignored) -> {
                        if (Main.getBooleanConfig("vm.log.misc"))
                            System.out.println("App-launched thread " + newThread + " returned!");
                    });
                    ctx.thread.runningThreadsRef.add(newThread);
                    ctx.thread.popMethod(VmContext.VOID);
                }, ctx.ec);
                break;
            case "java/lang/Object::wait":
                long waitTime = (long) parameters[0];
                if (waitTime == 0)
                    ctx.thread.wait = JThread.WAIT_INFINITE;
                else
                    ctx.thread.wait = System.currentTimeMillis() + waitTime;
                thiz.lock.add(ctx.thread);
                ctx.thread.waitingOn = thiz;
                break;
            case "java/lang/Object::notifyAll":
                for (JThread thread : thiz.lock)
                    thread.unlock(false);
                thiz.lock.clear();
                ctx.thread.popMethod(VOID);
                break;
            case "java/lang/Object::getClass":
                ctx.thread.popMethod(thiz.classInfo.classObject);
                break;
            case "java/lang/String::intern":
                ctx.thread.popMethod(internString(thiz));
                break;
            default:
                JavaInterpreter.error(ctx.thread, "java/lang/UnsatisfiedLinkError", method
                        + " in class " + method.clazz);
        }
    }
    private static final Map<String, JClassInstance> stringIntern = new HashMap<>();
    private static final Map<Integer, FieldInfo> fieldIDMap = new HashMap<>();
    private static final Map<Integer, MethodInfo> mIDMap = new HashMap<>();
    private static int idgen;
    private static final Files fs = NormalFileLayout.create();

    private static JClassInstance internString(JClassInstance instance) {
        String s = JavaInterpreter.toString(instance);
        if (stringIntern.containsKey(s))
            return stringIntern.get(s);
        else {
            stringIntern.put(s, instance);
            return instance;
        }
    }

    private static void arraycopy(Object[] parameters, JThread thread) {
        JavaObject.JArray src = (JavaObject.JArray) parameters[0];
        int srcPos = (int) parameters[1];
        JavaObject.JArray dst = (JavaObject.JArray) parameters[2];
        int dstPos = (int) parameters[3];
        int length = (int) parameters[4];
        //System.out.println("stack#:" + Arrays.
//                toString(JavaInterpreter.STACK_TRACE.toArray()));
        if (srcPos + length > src.array.length || dstPos + length
                > dst.array.length)
            JavaInterpreter.error(thread, "java/lang/ArrayIndexOutOfBoundsException",
                    "in System.arraycopy:src.length=" + src.array.length
                    + ",dst.length:" + dst.array.length + ",srcPos:" + srcPos
                    + ",dstPos" + dstPos + ",length=" + length);
        for (int i = 0; i < length; i++)
            dst.array[dstPos + i] = src.array[srcPos + i];
    }

    private static class MemorySpace {

        private static long IDgen;
        private static final Map<Long, Byte> map = new HashMap<>();
        private static final Map<Long, Long> len = new HashMap<>();

        public static long allocate(long size) {
            long result = IDgen;
            IDgen += size;
            len.put(result, size);
            return result;
        }

        public static void putLong(long index, long value) {
            putByte(index, (byte) (value >> 56));
            putByte(index + 1, (byte) (value >> 48));
            putByte(index + 2, (byte) (value >> 40));
            putByte(index + 3, (byte) (value >> 32));
            putByte(index + 4, (byte) (value >> 24));
            putByte(index + 6, (byte) (value >> 16));
            putByte(index + 7, (byte) (value >> 8));
            putByte(index + 8, (byte) (value));
        }

        public static byte getByte(long index) {
            if (!map.containsKey(index))
                return 0;
            return map.get(index);
        }

        public static void putByte(long index, byte value) {
            if (value == 0)
                return;
            map.put(index, value);
        }

        public static void free(long index) {
            long size = len.get(index);
            for (int i = 0; i < size; i++)
                map.remove(index + i);
        }
    }
}
