/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2;

import static ja2.Initialization.toString;
import ja2.platform.desktop.Main;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import ja2.callback.VmCallback;
import ja2.clazz.JavaObject;
import ja2.clazz.JavaObject.JClassInstance;
import ja2.member.MethodAccessFlag;
import ja2.member.MethodCallInfo;
import ja2.member.MethodInfo;
import ja2.vm.BytecodeInstruction;
import ja2.vm.Bytecodes;
import ja2.vm.VmContext;

/**
 *
 * @author Attila
 */
public class JThread {

    private final boolean logtick = Main.getBooleanConfig("vm.log.tick");
    public LinkedList<MethodCallInfo> stackTrace = new LinkedList<>();
    private static final boolean LOG_PERF = Main.getBooleanConfig("vm.log.perf");
    public boolean runnable = false;
    public JClassInstance object;
    public int priority;
    public final List<JThread> runningThreadsRef;
            public JClassInstance waitingOn;
    public long wait;
    public static final long WAIT_NO = 0;
    public static final long WAIT_INFINITE = -1;
    public static final long WAIT_NATIVE = -2;

    public JThread(List<JThread> runningThreadsRef) {
        this.runningThreadsRef = runningThreadsRef;
    }

    public void executeMethod(MethodInfo mi, JavaObject.JClassInstance thiz, Object[] args, VmCallback<Object> callback) {
        if (mi.accessFlags.contains(MethodAccessFlag.ABSTRACT))
            Initialization.error(this, "java/lang/AbstractMethodError", "Tried to invoke abstract method: " + mi);
        MethodCallInfo call = new MethodCallInfo(mi, stackTrace.peek(), thiz, args, this, callback);
        if (h)
            Initialization.debugger.methodInvoked(call);
        stackTrace.push(call);
        if (mi.accessFlags.contains(MethodAccessFlag.NATIVE)) {
            call.vmContext.isLocked = true;
            NativeMethodExecuter.executeNativeMethod(call.vmContext);
        }
        //System.err.println("exec " + mi.toString() + " on " + this);
    }

    public void tick() {
        if (stackTrace.isEmpty()) {
            runnable = false;
            return;
        }
        if (logtick)
            System.out.println("Tick " + stackTrace.peek());
        if (stackTrace.peek().vmContext.isLocked)
            return;
        if (wait < 0)
            return;
        if (wait > 0)
            if (System.currentTimeMillis() < wait)
                return;
            else {
                unlock(true);
            }
        long perfMeasureStartNSTime__ = LOG_PERF ? System.nanoTime() : 0;
        String statementName = null;
        try {
            VmContext ctx = stackTrace.peek().vmContext;
            int instructionCode = ctx.in.read();
            BytecodeInstruction runner = Bytecodes.i[instructionCode];
            if (runner == null)
                throw new UnsupportedOperationException("Unknown statement: "
                        + instructionCode + " (in " + ctx.method.name + ")");
            if (h && Bytecodes.mnemonics[instructionCode] != null) {
                statementName = Bytecodes.mnemonics[instructionCode];
                ctx.log(0, (ctx.mcIn.pc - 1) + ": " + statementName);
            }
            runner.run(ctx, ctx.operandStack, instructionCode);
        } catch (JException jex) {
            String ts = Initialization.toString((JClassInstance) jex.ex.fieldValues.get(
                    "detailMessage"));
            System.out.println("Java Exception: " + jex.ex.classInfo.name
                    + ";message=" + ts);
            for (MethodCallInfo stackTrace1 : stackTrace) {
                System.out.println("  at "+stackTrace1);
            }
            runnable = false;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if (LOG_PERF) {
            long endTime = System.nanoTime();
            System.out.println(statementName + ": " + (endTime - perfMeasureStartNSTime__) + " ns");
        }
    }

    public  void unlock(boolean removeFromJCI) {
        wait = WAIT_NO;
        if(waitingOn != null) {
            if(removeFromJCI)
            waitingOn.lock.remove(this);
            waitingOn = null;
        }
        popMethod(VmContext.VOID);
    }
    
    public final boolean h = Initialization.debugger != null;
    private static int tidgen;
    private int tid = tidgen++;

    @Override
    public String toString() {
        return "VmThread: " + tid + " (" + wait + ")";
    }

    public void popMethod(Object result) {
        stackTrace.pop().returnCallback.run(result);
    }

}
