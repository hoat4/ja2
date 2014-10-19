/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.clazz;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author attila
 */
public class ClassMeta {

    public String sourceFile, signature;
    public List<InnerClassInfo> innerClasses = new ArrayList<>();
    public String enclosingClassName, enclosingMethodName, enclosingMethodDescriptor;
}
