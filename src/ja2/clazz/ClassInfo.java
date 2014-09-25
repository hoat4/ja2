package ja2.clazz;

import java.util.ArrayList;
import ja2.JavaType;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ja2.JThread;
import ja2.clazz.JavaObject.JArray;
import ja2.callback.ErrorCallback;
import ja2.callback.VmCallback;
import ja2.io.U2Pair;
import ja2.member.FieldInfo;
import ja2.member.MethodHandleInfo;
import ja2.member.MethodInfo;

/**
 *
 * @author Attila
 */
public class ClassInfo extends AbstractClassInfo {

    public String superClassName;
    public String[] implementedInterfaces;
    public FieldInfo[] fields;
    public Map<String, Object> staticFieldValues = new HashMap<>();
    public MethodInfo clinitMethod;
    public MethodInfo[] decraledMethods;
    public Object[] cp;
    public ConstantPoolElemType[] contextConstantPoolElementTypes;
    public boolean systemClass = false;
    private Object JavaObject;
    public EnumSet<ClassAccessFlag> access;
    public int rawModifiers;
    public String sourceFile, signature;
    public List<InnerClassInfo> innerClasses = new ArrayList<>();
    public List<MethodHandleInfo.BootstrapMethodInfo> bootstrapMethods = new ArrayList<>();

    public ClassInfo() {
        super(false);
    }

    public MethodInfo getDecraledMethod(String name, String descriptor) throws NoSuchMethodException {
        for (MethodInfo method : decraledMethods)
            if (method.name.equals(name) && method.descriptor.equals(descriptor))
                return method;
        throw new NoSuchMethodException("Cannot find " + name + " " + descriptor + " in class " + name);
    }

    public void getMethod(final String name, final JavaType[] paramTypes, JThread thread, final VmCallback<MethodInfo> callback, final ErrorCallback errorCallback) {
        getMethodPrivate(this, name, paramTypes, thread, callback, errorCallback);
    }

    private void getMethodPrivate(ClassInfo origClass, final String name, final JavaType[] paramTypes, JThread thread, final VmCallback<MethodInfo> callback, final ErrorCallback errorCallback) {
        if (decraledMethods != null)
            for (MethodInfo method : decraledMethods)
                if (method.name.equals(name) && Arrays.equals(method.parameterTypes,
                        paramTypes)) {
                    callback.run(method);
                    return;
                }
        if (superClassName != null)
            ja2.clazz.ClassLoadHelper.loadClass(superClassName, thread, (ClassInfo superClass) -> {
                superClass.getMethodPrivate(origClass, name, paramTypes, thread, callback, errorCallback);
            }, errorCallback);
        else
            errorCallback.onError(new NoSuchMethodException("Method " + name
                    + " with parameter types " + MethodInfo.toStringParameters(
                            paramTypes) + " not found in " + origClass));
    }

    public void getMethod(String name, String descriptor, JThread thread, VmCallback<MethodInfo> callback, ErrorCallback errorCallback) {
        getMethod(name, MethodInfo.parseDescriptor(descriptor), thread, callback, errorCallback);
    }

    public void getField(String name, JThread thread, VmCallback<FieldInfo> callback, ErrorCallback errorCallback) {
        for (FieldInfo field : fields)
            if (field.name.equals(name)) {
                callback.run(field);
                return;
            }
        if (superClassName != null)
            ja2.clazz.ClassLoadHelper.loadClass(superClassName, thread, (clazz) -> {
                clazz.getField(name, thread, callback, errorCallback);
            }, errorCallback);
        else
            errorCallback.onError(new NoSuchFieldException("Field " + name + " not found in " + this));
    }

    @Override
    public String toString() {
        return "class " + name;
    }

    // TODO instanceof not working with interface inheritance
    public void instanceOf(String otherClassName, JThread thread, VmCallback<Boolean> callback, ErrorCallback errorCallback) {
        if (this.name.equals(otherClassName)) {
            callback.run(true);
            return;
        }
        for (String implementedInterface : implementedInterfaces)
            if (implementedInterface.equals(otherClassName)) {
                callback.run(true);
                return;
            }
        if (superClassName != null)
            ClassLoadHelper.instantLoadClass(superClassName, thread).instanceOf(otherClassName, thread, callback, errorCallback);
        else
            callback.run(false);
    }

    public String toNormalName() {
        return name.replace('/', '.');
    }
    private List<FieldInfo> fieldsCache;

    public void getFields(JThread thread, VmCallback<Collection<FieldInfo>> callback, ErrorCallback errorCallback) {
        if (fieldsCache == null) {
            fieldsCache = new LinkedList<>();
            //System.out.println("fieldsCache = " + fieldsCache);
            fieldsCache.addAll(Arrays.asList(fields));
            if (superClassName != null)
                ClassLoadHelper.loadClass(superClassName, thread, (superClazz) -> {
                    superClazz.getFields(thread, (superFields) -> {
                        fieldsCache.addAll(superFields);
                        callback.run(fieldsCache);
                    }, errorCallback);
                }, errorCallback);
            else
                callback.run(fieldsCache);
        } else
            callback.run(fieldsCache);
    }
    private List<MethodInfo> methodsCache;

    public void getMethods(JThread thread, VmCallback<Collection<MethodInfo>> callback, ErrorCallback errorCallback) {
        if (methodsCache == null) {
            methodsCache = new LinkedList<>();
            //System.out.println("methodsCache = " + methodsCache);
            methodsCache.addAll(Arrays.asList(decraledMethods));
            if (superClassName != null)
                ja2.clazz.ClassLoadHelper.loadClass(superClassName, thread, (superClazz) -> {
                    superClazz.getMethods(thread, (superMethod) -> {
                        methodsCache.addAll(superMethod);
                        callback.run(methodsCache);
                    }, errorCallback);
                }, errorCallback);
            else
                callback.run(methodsCache);
        }
    }

    public void instantiate(JThread thread, VmCallback<JavaObject.JClassInstance> callback, ErrorCallback errorCallback) {
        new JavaObject.JClassInstance(this, thread, callback, errorCallback);
    }

    public void instantiateAndInit(JThread thread, VmCallback<JavaObject.JClassInstance> callback, ErrorCallback errorCallback) {
        instantiate(thread, (JavaObject.JClassInstance object) -> {
            object.invokeConstructor(thread, callback, errorCallback);
        }, errorCallback);
    }

    public JArray instantiateArray(int length, JThread thread) {
        return new JArray(length, asType, thread);
    }

    public void setName(String name) {
        this.name = name;
        asType = JavaType.clazz(name);
    }

    public List<MethodInfo> constructors() {
        List<MethodInfo> result = new ArrayList<>();
        for (MethodInfo decraledMethod : decraledMethods)
            if (decraledMethod.name.equals("<init>"))
                result.add(decraledMethod);
        return result;
    }

    public String constantDetails(int ref_index) {
        ConstantPoolElemType type = contextConstantPoolElementTypes[ref_index];
        Object value = cp[ref_index];
        value = enhanceU2PairInfo(value);
        return "Constant pool of " + this + "[" + ref_index + " (" + type + ") = " + value + "]";
    }

    private Object enhanceU2PairInfo(Object value) {
        if (value instanceof U2Pair) {
            U2Pair u2p = (U2Pair) value;
            Object a = enhanceU2PairInfo(cp[u2p.a]);
            Object b = enhanceU2PairInfo(cp[u2p.b]);
            value = "U2Pair (reference) [a=" + a + ", b=" + b + "]";
        }
        return value;
    }

    /*private static class InterfaceIterator {

        private ClassInfo clazz;
        private int idx;
        private final JThread thread;
        private final VmCallback<MethodInfo> callback;
        private final ErrorCallback ec;

        public InterfaceIterator(ClassInfo clazz, JThread thread, VmCallback<MethodInfo> callback, ErrorCallback ec) {
            this.clazz = clazz;
            this.thread = thread;
            this.callback = callback;
            this.ec = ec;
        }

        public void step() {
            String iname = clazz.implementedInterfaces[idx++];
            ClassLoadHelper.loadClass(iname, thread, (iface) -> {
                iface.get
            }, ec);
        }
    }*/
}
