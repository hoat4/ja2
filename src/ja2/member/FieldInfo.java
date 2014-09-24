package ja2.member;

import ja2.clazz.ClassInfo;
import java.util.EnumSet;
import ja2.JavaType;

/**
 *
 * @author Attila
 */
public class FieldInfo {

    public final EnumSet<FieldAccessFlag> access;
    public final String type, name;
    public final ClassInfo clazz;
    public final int index;
    public final int rawModifiers;

    public FieldInfo(int access, String type, String name, ClassInfo clazz, int index) {
        this.access = FieldAccessFlag.fromBitmask(access);
        this.rawModifiers = access;
        this.type = type;
        this.name = name;
        this.clazz = clazz;
        this.index = index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (FieldAccessFlag fieldAccessFlag : access) {
            sb.append(fieldAccessFlag.toString());
        }
        sb.append(' ').append(JavaType.getType(type)).append(' ').append(name);
        return sb.toString();
    }
}
