/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.clazz;

import ja2.JThread;
import ja2.Initialization;
import ja2.JavaType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author attila
 */
public class ArrayTypeClassInfo extends ClassInfo {

    public final JavaType elemType;

    private ArrayTypeClassInfo(JavaType elemType, JThread thread) {
        this.elemType = elemType;
        superClassName = "java/lang/Object";
        name = "[" + elemType.typeDescriptor;
        asType = JavaType.array(elemType);
        Initialization.initializeArrayClass(this, thread);
    }

    @Override
    public String toString() {
        return asType.arrayElementType + "[]";
    }
    private static final Map<JavaType, ArrayTypeClassInfo> cache = new HashMap<>();

    public static ArrayTypeClassInfo of(JavaType elemType, JThread thread) {
        if (cache.containsKey(elemType))
            return cache.get(elemType);
        ArrayTypeClassInfo atci = new ArrayTypeClassInfo(elemType, thread);
        cache.put(elemType, atci);
        return atci;
    }
}
