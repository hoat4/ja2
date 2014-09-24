/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ja2.clazz;

import ja2.JavaType;

/**
 *
 * @author attila
 */
public class ArrayTypeClassInfo extends ClassInfo {
    public final JavaType elemType;
    public ArrayTypeClassInfo(JavaType elemType) {
        this.elemType = elemType;
        superClassName = "java/lang/Object";
        name = "["+elemType.typeDescriptor;
        asType = JavaType.array(elemType);
    }
    
}
