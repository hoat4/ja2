package ja2;

import ja2.callback.ErrorCallback;
import ja2.callback.VmCallback;
import ja2.clazz.AbstractClassInfo;
import ja2.clazz.ArrayTypeClassInfo;
import ja2.clazz.ClassLoadHelper;
import ja2.clazz.PrimitiveTypeClassInfo;

/**
 * JavaType class. Primitive type table: http://goo.gl/6YzXb
 * <p/>
 * @author Attila
 */
public class JavaType {

    public static final JavaType VOID = new JavaType("void", 'V', null), BYTE
            = new JavaType("byte", 'B', 0), CHAR = new JavaType("char", 'C', '\u0000'), DOUBLE
            = new JavaType("double", 'D', 0.0d), FLOAT = new JavaType("float", 'F', 0.0f), INT
            = new JavaType("int", 'I', 0), LONG = new JavaType("long", 'J', 0L), SHORT
            = new JavaType("short", 'S', 0), BOOLEAN = new JavaType("boolean", 'Z', 0);
    private static final JavaType[] TYPES = new JavaType[]{VOID, BYTE, CHAR, FLOAT, LONG, INT, DOUBLE, SHORT, BOOLEAN};

    public static JavaType clazz(String internalClassName) {
        return getType('L' + internalClassName + ';');
    }

    public static JavaType array(JavaType elemType) {
        return getType('[' + elemType.typeDescriptor);
    }
    public final boolean primitive;
    public final String typeDescriptor;
    public final Object defaultValue;

    private JavaType(String name) {
        this.internalClassName = name;
        this.typeDescriptor = 'L' + name + ';';
        this.arrayElementType = null;
        this.primitive = false;
        this.defaultValue = null;
    }

    private JavaType(String name, char c, Object defaultValue) {
        this.typeDescriptor = String.valueOf(c);
        this.internalClassName = name;
        this.arrayElementType = null;
        this.primitive = true;
        this.defaultValue = defaultValue;
    }

    private JavaType(JavaType arrayElementType) {
        this.typeDescriptor = '[' + arrayElementType.typeDescriptor;
        this.internalClassName = null;
        this.arrayElementType = arrayElementType;
        this.primitive = false;
        this.defaultValue = null;
    }
    public final String internalClassName;
    public final JavaType arrayElementType;

    public static JavaType getType(String name) {
        switch (name.charAt(0)) {
            case 'V':
                return VOID;
            case 'B':
                return BYTE;
            case 'C':
                return CHAR;
            case 'D':
                return DOUBLE;
            case 'F':
                return FLOAT;
            case 'I':
                return INT;
            case 'J':
                return LONG;
            case 'S':
                return SHORT;
            case 'Z':
                return BOOLEAN;
            case '[':
                return new JavaType(JavaType.getType(name.substring(1)));
            case 'L':
                return new JavaType(name.substring(1, name.length() - 1));
        }
        switch (name) {
            case "void":
                return VOID;
            case "boolean":
                return BOOLEAN;
            case "char":
                return CHAR;
            case "double":
                return DOUBLE;
            case "float":
                return FLOAT;
            case "int":
                return INT;
            case "long":
                return LONG;
            case "short":
                return SHORT;
            case "byte":
                return BYTE;
        }
        return new JavaType(name);
    }

    @Override
    public String toString() {
        if (arrayElementType == null)
            return internalClassName;
        else
            return arrayElementType + "[]";
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public static JavaType fromConstantPool(Object[] constantPool, int index) {
        Object obj = constantPool[index];
        if (obj instanceof Number)
            return getType((String) constantPool[(int) obj]);
        else
            return getType((String) obj);
    }

    public void asClassInfo(JThread thread, VmCallback<AbstractClassInfo> callback, ErrorCallback ec) {
        if (primitive)
            callback.run(PrimitiveTypeClassInfo.get(internalClassName));
        else if (arrayElementType != null)
            callback.run(ArrayTypeClassInfo.of(arrayElementType, thread));
        else
            ClassLoadHelper.loadClass(internalClassName, thread, (VmCallback) callback, ec);
    }
}
