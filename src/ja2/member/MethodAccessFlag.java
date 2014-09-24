package ja2.member;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

/**
 *
 * @author Attila
 */
public enum MethodAccessFlag {
    PUBLIC(0x0001),PRIVATE(0x0002), PROTECTED(0x0004), STATIC(0x0008), FINAL(0x0010), 
    SYNCHRONIZED(0x0020), BRIDGE(0x0040), VARARGS(0x0080), NATIVE(0x0100), ABSTRACT(0x0400),
    STRICTFP(0x0800),SYNTHETIC(0x1000);
     public final int bitmask;

    private MethodAccessFlag(int bitmask) {
        this.bitmask = bitmask;
    }

    @Override
    public String toString() {
        return ' ' + name().toLowerCase();
    }

    public static EnumSet<MethodAccessFlag> fromBitmask(int bitmask) {
        EnumSet<MethodAccessFlag> result = EnumSet.noneOf(MethodAccessFlag.class);
        for (MethodAccessFlag methodAccessFlag : values()) {
            if ((methodAccessFlag.bitmask & bitmask) > 0)
                result.add(methodAccessFlag);
        }
        return result;
    }
    
}
