/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.clazz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

/**
 *
 * @author Attila
 */
public enum ClassAccessFlag {

    PUBLIC(0x0001), PRIVATE(0x0002), PROTECTED(0x0004), STATIC(0x0008), 
    FINAL(0x0010), SUPER(0x0020), INTERFACE(0x0200),
    ABSTRACT(0x0400), SYNTHETIC(0x1000), ANNOTATION(0x2000),
    ENUM(0x4000);
    public final int bitmask;

    private ClassAccessFlag(int bitmask) {
        this.bitmask = bitmask;
    }

    @Override
    public String toString() {
        return ' '+name().toLowerCase();
    }

    public static EnumSet<ClassAccessFlag> fromBitmask(int bitmask) {
        EnumSet<ClassAccessFlag> result = EnumSet.noneOf(ClassAccessFlag.class);
        for (ClassAccessFlag classAccessFlag : values()) {
            if ((classAccessFlag.bitmask & bitmask) > 0)
                result.add(classAccessFlag);
        }
        return result;
    }
}
