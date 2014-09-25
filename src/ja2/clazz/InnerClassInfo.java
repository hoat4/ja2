/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.clazz;

import java.util.EnumSet;

/**
 *
 * @author attila
 */
public class InnerClassInfo {

    public String name;
    public ClassInfo container;
    public EnumSet<ClassAccessFlag> access;
    public boolean normal;
    public boolean anonymous;

    public InnerClassInfo(ClassInfo outerClass) {
        container = outerClass;
    }
}
