/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2;

import ja2.clazz.JavaObject;

/**
 *
 * @author attila
 */
public class JException extends RuntimeException {
    public final JavaObject.JClassInstance ex;

    public JException(JavaObject.JClassInstance ex) {
        super("Java Exception");
        this.ex = ex;
    }
    
}
