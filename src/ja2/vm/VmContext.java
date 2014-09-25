/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.vm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import ja2.JThread;
import ja2.Initialization;
import static ja2.Initialization.error;
import ja2.JavaType;
import ja2.platform.desktop.Main;
import ja2.callback.ErrorCallback;
import ja2.callback.VmCallback;
import ja2.clazz.ClassInfo;
import ja2.clazz.ClassLoadHelper;
import ja2.clazz.ConstantPoolElemType;
import ja2.clazz.JavaObject;
import ja2.io.ByteInput;
import ja2.io.MethodCodeInputStream;
import ja2.io.U2Pair;
import ja2.member.FieldInfo;
import ja2.member.MethodCallInfo;
import ja2.member.MethodInfo;

/**
 *
 * @author Attila
 */
public class VmContext {

    public static final VoidObject VOID = new VoidObject();
    public final MethodCodeInputStream mcIn;
    public final ByteInput in;
    private final StringBuilder methodLog = new StringBuilder();
    public final Deque<Object> operandStack = new LinkedList<>();
    public final Object[] constantPool;
    public final ConstantPoolElemType[] constantPoolElementTypes;
    public final MethodInfo method;
    public final Object[] localVariables;
    public final Object[] args;
    public final ClassInfo clazz;
    public boolean wideMode;
    public final JThread thread;
    public final MethodCallInfo call;
    public boolean isLocked;
    public final boolean logging = Initialization.debugger != null;
    public final ErrorCallback ec = Initialization::handleError;
    public final JavaObject.JClassInstance thiz;

    public VmContext(MethodCallInfo methodCall, JavaObject.JClassInstance thiz, JThread thread) {
        this.method = methodCall.method;
        this.clazz = method.clazz;
        this.thread = thread;
        this.thiz = thiz;
        Object[] margs = methodCall.args;
        this.args = margs;
        if (methodCall.method.code != null) {
            this.mcIn = new MethodCodeInputStream(methodCall.method.code);
            this.in = new ByteInput(mcIn);

            localVariables = new Object[methodCall.method.maxLocalVariables];
            if (thiz != null) {
                localVariables[0] = thiz;
                System.arraycopy(margs, 0, localVariables, 1, margs.length);
            } else
                System.arraycopy(margs, 0, localVariables, 0, margs.length);
            if (logging) {
                log(-2, Arrays.toString(localVariables));
                log(-1, "Caller: " + methodCall.caller);
            }
        } else {
            mcIn = null;
            in = null;
            localVariables = null;
        }
        this.constantPool = clazz.cp;
        this.constantPoolElementTypes = method.clazz.contextConstantPoolElementTypes;
        call = methodCall;
    }

    public void relativeJumpMinus3(int to) {
        mcIn.relativeJumpMinus3(to);
    }

    public void relativeJumpMinus3(short to) {
        mcIn.relativeJumpMinus3(to);
    }

    public JavaType typeFromConstantPool(int index) {
        return JavaType.fromConstantPool(constantPool, index);
    }

    public void log(int indent, Object msg) {
        if (!logging)
            throw new IllegalStateException("[Perf-Bug] VmContext.log(String) called but it's disabled. ");
        for (int i = 0; i < indent + 2; i++)
            methodLog.append(' ');
        methodLog.append(msg).append('\n');
        //System.out.println("Logged: "+msg);
    }

    public void lookupMethod(int methodRefIndex, final VmCallback<MethodInfo> callback, ErrorCallback ec) {
        final U2Pair methodref = (U2Pair) constantPool[methodRefIndex];
        ClassLoadHelper.loadClass((String) clazz.cp[(int) clazz.cp[methodref.a]], thread, (clasz) -> {
            U2Pair nATD = (U2Pair) clazz.cp[methodref.b];
            String name = (String) clazz.cp[nATD.a];
            String descriptor = (String) clazz.cp[nATD.b];
            clasz.getMethod(name, descriptor, thread, callback, (error) -> {
                Initialization.error(thread, "java/lang/NoSuchMethodError", error.toString());
            });
        }, ec);
    }

    public void lookupMethod(JavaObject.JClassInstance object, int methodRefIndex, final VmCallback<MethodInfo> callback, ErrorCallback ec) {
        if (object == null) {
            lookupMethod(methodRefIndex, callback, ec);
            return;
        }
        final U2Pair methodref = (U2Pair) constantPool[methodRefIndex];
        ClassInfo clasz = object.classInfo;
        U2Pair nATD = (U2Pair) clazz.cp[methodref.b];
        String name = (String) clazz.cp[nATD.a];
        String descriptor = (String) clazz.cp[nATD.b];
        clasz.getMethod(name, descriptor, thread, callback, (error) -> {
            Initialization.error(thread, "java/lang/NoSuchMethodError", error.toString());
        });
    }
    /*public static MethodInfo getMethod(ClassInfo currentClass,
     ClassInfo methodClass, int methodRefIndex) {
     String name;
     U2Pair methodref
     = (U2Pair) currentClass.contextConstantPool[methodRefIndex];
     U2Pair nATD = (U2Pair) currentClass.contextConstantPool[methodref.b];
     name = (String) currentClass.contextConstantPool[nATD.a];
     //System.out.println("getmethod: currclass:" + currentClass
     //        + ";methodClass:" + methodClass + ";name:" + name);
     String descriptor = (String) currentClass.contextConstantPool[nATD.b];
     try {
     return methodClass.getMethod(name, descriptor);
     } catch (NoSuchMethodException ex) {
     error("java/lang/NoSuchMethodError", ex.getMessage());
     throw new AssertionError();
     }
     }*/

    public void lookupField(int fieldRefIndex, final VmCallback<FieldInfo> callback, ErrorCallback ec) {
        final U2Pair fieldref = (U2Pair) constantPool[fieldRefIndex];
        ClassLoadHelper.loadClass((String) clazz.cp[(int) clazz.cp[fieldref.a]], thread, (clasz) -> {
            U2Pair fieldReference = (U2Pair) constantPool[fieldRefIndex];
            U2Pair nametype = (U2Pair) constantPool[fieldReference.b];
            clasz.getField((String) constantPool[nametype.a], thread, callback, (error) -> {
                Initialization.error(thread, "java/lang/NoSuchMethodError", error.toString());
            });
        }, ec);
    }

    public void getMethod(int methodRefIndex, ClassInfo clasz, VmCallback<MethodInfo> callback, ErrorCallback ec) {
        final U2Pair methodref = (U2Pair) constantPool[methodRefIndex];
        U2Pair nATD = (U2Pair) clazz.cp[methodref.b];
        String name = (String) clazz.cp[nATD.a];
        String descriptor = (String) clazz.cp[nATD.b];
        clasz.getMethod(name, descriptor, thread, callback, (error) -> {
            Initialization.error(thread, "java/lang/NoSuchMethodError", error.toString());
        });
    }

    public String getLog() {
        return methodLog.toString();
    }

    public void absoluteJump(int i) {
        mcIn.pc = i;
    }

    public void lookup(String classname, String name, String descriptor, VmCallback<MethodInfo> callback) {
        ClassLoadHelper.loadClass(classname, thread, (clazz) -> {
            clazz.getMethod(name, descriptor, thread, callback, ec);
        }, ec);
    }

}
