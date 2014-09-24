/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javainterpreter.member;

import javainterpreter.JThread;
import javainterpreter.callback.VmCallback;
import javainterpreter.clazz.JavaObject;
import javainterpreter.vm.VmContext;

/**
 *
 * @author Attila
 */
public class MethodCallInfo {
    public static final Object[] ZERO_PARAMETERS = new Object[0];
    public final MethodCallInfo caller;
    public final MethodInfo method;
    public final VmContext vmContext;
    public final Object[] args;
    public final VmCallback<Object> returnCallback;
    public MethodCallInfo(MethodInfo method, MethodCallInfo caller, JavaObject.JClassInstance thiz, Object[] args, JThread thread, VmCallback<Object> callback) {
        this.method = method;
        this.caller = caller;
        this.args = args;
        this.vmContext = new VmContext(this, thiz, thread);
        this.returnCallback = callback;
    }

    @Override
    public String toString() {
        return method.clazz.name + "." + method.toString();
    }
}
