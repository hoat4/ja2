/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javainterpreter.member;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

/**
 *
 * @author Attila
 */
public enum FieldAccessFlag {

    PUBLIC(0x0001), PRIVATE(0x0002), PROTECTED(0x0004), STATIC(0x0008),
    FINAL(0x0010), VOLATILE(0x0040), TRANSIENT(0x0080), SYNTHETIC(0x1000),
    ENUM(0x4000);
    public final int bitmask;

    private FieldAccessFlag(int bitmask) {
        this.bitmask = bitmask;
    }

    @Override
    public String toString() {
        return ' ' + name().toLowerCase();
    }

    public static EnumSet<FieldAccessFlag> fromBitmask(int bitmask) {
      EnumSet<FieldAccessFlag> result = EnumSet.noneOf(FieldAccessFlag.class);
           for (FieldAccessFlag fieldAccessFlag : values()) {
            if ((fieldAccessFlag.bitmask & bitmask) > 0)
                result.add(fieldAccessFlag);
        }
        return result;
    }
}
