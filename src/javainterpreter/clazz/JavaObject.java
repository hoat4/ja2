package javainterpreter.clazz;

import java.util.ArrayList;
import javainterpreter.callback.VmCallback;
import javainterpreter.callback.ErrorCallback;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javainterpreter.JThread;
import javainterpreter.JavaInterpreter;
import static javainterpreter.JavaInterpreter.NO_PARAMETERS;
import javainterpreter.JavaType;
import javainterpreter.clazz.ClassInfo;
import javainterpreter.member.FieldInfo;
import javainterpreter.member.MethodInfo;
import javainterpreter.vm.VmContext;

/**
 *
 * @author Attila
 */
public abstract class JavaObject {

    public static class JArray extends JClassInstance {

        public Object[] array;
        public JavaType elemType;

        public JArray(final int arraylength, final JavaType elemType, JThread thread) {
            super(javainterpreter.clazz.ClassLoadHelper.instantLoadClass("java/lang/Object", thread));
            array = new Object[arraylength];
            if (elemType.primitive)
                for (int i = 0; i < arraylength; i++)
                    array[i] = elemType.defaultValue;
            this.elemType = elemType;
            if (JavaInterpreter.LOG_MISC)
                System.out.println("Array created: " + elemType + "["
                        + arraylength
                        + "]");
        }

        @Override
        public String toString() {
            return "JArray" + Arrays.toString(array);
        }

        @Override
        public void instanceOf(String className, JThread thread, VmCallback<Boolean> callback, ErrorCallback ec) {
            if (className.equals("java/lang/Object") || className.equals("[" + elemType.typeDescriptor))
                callback.run(true);
            else
                callback.run(false);
        }

    }

    public static class JClassInstance extends JavaObject {

        private static int idGenerator = 0;
        public final ClassInfo classInfo;
        public final Map<String, Object> fieldValues = new HashMap<>();
        public final int id = idGenerator++;
        @SuppressWarnings("PublicField")
        public Object transfer;
        public List<JThread> lock = new ArrayList<>();

        public JClassInstance(ClassInfo clazz) {
            classInfo = clazz;
            FieldInfo[] clazzfields = clazz.fields;
            for (FieldInfo field : clazzfields)
                fieldValues.put(field.name,
                        JavaType.getType(field.type).defaultValue);
        }

        public JClassInstance(ClassInfo clazz, JThread thread, VmCallback<JClassInstance> callback, ErrorCallback ec) {
            classInfo = clazz;
            initFields(clazz, thread, callback, ec);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final JClassInstance other = (JClassInstance) obj;
            if (this.id != other.id)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            if (classInfo.name.equals("java/lang/String"))
                return '"' + JavaInterpreter.toString(this) + '"';
            else
                return classInfo.name;
        }

        void initFields(ClassInfo clazz, JThread thread, VmCallback<JClassInstance> callback, ErrorCallback ec) {
            clazz.getFields(thread, (fields) -> {
                for (FieldInfo field : fields)
                    fieldValues.put(field.name,
                            JavaType.getType(field.type).defaultValue);
                callback.run(this);
            }, ec);
        }

        public static JClassInstance createInstant(ClassInfo clazz) {
            return new JClassInstance(clazz);
        }

        public void instanceOf(String className, JThread thread, VmCallback<Boolean> callback, ErrorCallback ec) {
            classInfo.instanceOf(className, thread, callback, ec);
        }

        public void invokeConstructor(JThread thread, VmCallback<JavaObject.JClassInstance> callback, ErrorCallback errorCallback) {
            classInfo.getMethod("<init>", "()V", thread, (initMethod) -> {
                thread.executeMethod(initMethod, this, NO_PARAMETERS, (result) -> callback.run(this));
            }, errorCallback);
        }

        public void invokeConstructor(String descriptor, JThread thread, VmCallback<JavaObject.JClassInstance> callback, ErrorCallback errorCallback, Object... parameters) {
            classInfo.getMethod("<init>", descriptor, thread, (initMethod) -> {
                thread.executeMethod(initMethod, this, parameters, (result) -> callback.run(this));
            }, errorCallback);
        }

    }

    public static interface Evaluation {

        void evaluate(VmContext ctx, VmCallback callback);
    }

}
