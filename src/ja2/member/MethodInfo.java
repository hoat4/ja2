package ja2.member;

import ja2.Initialization;
import ja2.clazz.ClassInfo;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import ja2.JavaType;

/**
 *
 * @author Attila
 */
public class MethodInfo {

    public static String toStringParameters(JavaType[] paramTypes) {
        String ptString = Initialization.toString(paramTypes);
        return '(' + ptString.substring(1, ptString.length() - 1) + ')';
    }
    public final EnumSet<MethodAccessFlag> accessFlags;
    public final String name;
    public final JavaType[] parameterTypes;
    public final JavaType returnType;
    public short[] code;
    public final String descriptor;
    public final ClassInfo clazz;
    public int maxLocalVariables;
    public int rawModifiers;
    public int[] lineNumberTable;

    public MethodInfo(int accessFlags, String name,
            String descriptor, ClassInfo clazz) {
        this.accessFlags = MethodAccessFlag.fromBitmask(accessFlags);
        this.rawModifiers = accessFlags;
        this.name = name;
        this.returnType = JavaType.getType(descriptor.substring(descriptor.
                indexOf(')') + 1));
     //   System.out.println("Method descriptor: " + descriptor);
        this.parameterTypes = parseDescriptor(descriptor);
        this.descriptor = descriptor;
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MethodAccessFlag methodAccessFlag : accessFlags)
            sb.append(methodAccessFlag.toString());
        sb.append(' ').append(returnType).append(' ').append(name).append(' ').
                append(toStringParameters(
                parameterTypes))/*.append(
                 " {\n  ").append(Initialization.toString(code)).append("\n }")*/;
        return sb.toString();
    }

    public static JavaType[] parseDescriptor(String descriptor) {
        List<JavaType> result = new ArrayList<>();
        String argTypesStr = descriptor.substring(1, descriptor.indexOf(')'));
        for (int i = 0; i < argTypesStr.length();) {
            TypeInt ppt = parseParamType(argTypesStr, i);
            i = ppt.i;
            result.add(ppt.type);
        }
        return result.toArray(new JavaType[result.size()]);
    }

    private static TypeInt parseParamType(String argTypesStr, int i) {
        char ch = argTypesStr.charAt(i);
        if (ch == 'L') {
            int end = argTypesStr.indexOf(';', i) + 1;
            return new TypeInt(JavaType.getType(argTypesStr.substring(i, end)),
                    end);
        } else if (ch == '[') {
            TypeInt ppt = parseParamType(argTypesStr, i + 1);
            return new TypeInt(JavaType.array(ppt.type), ppt.i);
        } else {
            return new TypeInt(JavaType.getType(String.valueOf(ch)), i+1);
        }
    }
    private static class TypeInt {

        public JavaType type;
        public int i;

        public TypeInt(JavaType type, int i) {
            this.type = type;
            this.i = i;
        }
    }
}
