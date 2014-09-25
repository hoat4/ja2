/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.member;

import ja2.clazz.ClassInfo;
import ja2.io.U2Pair;
import ja2.vm.VmContext;
import java.util.Arrays;
import java.util.Deque;

/**
 *
 * @author attila
 */
public class MethodHandleInfo {

    public Kind reference_kind;
    private final ClassInfo clazz;
    private int ref_index;

    public MethodHandleInfo(int ref_kind, ClassInfo clazz, int ref_index) {
        this.reference_kind = Kind.of(ref_kind);
        this.ref_index = ref_index;
        this.clazz = clazz;
    }

    public Object target() {
        return clazz.cp[ref_index];
    }

    @Override
    public String toString() {
        return "MethodHandleInfo {" + "reference_kind=" + reference_kind + ", target=" + clazz.constantDetails(ref_index) + '}';
    }

    public void execute(VmContext ctx, Deque operandStack, Runnable callback) {
        reference_kind.execute(this, ctx, operandStack, callback);
    }

    public static enum Kind {

        REF_getField {

                    @Override
                    protected void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                }, REF_getStatic {

                    @Override
                    protected void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                }, REF_putField {

                    @Override
                    protected void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                }, REF_putStatic {

                    @Override
                    protected void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                },
        REF_invokeVirtual {

                    @Override
                    protected void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                }, REF_invokeStatic {

                    @Override
                    protected void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback) {
                        U2Pair u2p = (U2Pair) mh.target();// methodref
                        System.out.println("mr: " + u2p);
                        String classname = (String) ctx.constantPool[(int) ctx.constantPool[u2p.a]];
                        System.out.println("classname:" + classname);
                        U2Pair nat = (U2Pair) ctx.constantPool[u2p.b];
                        System.out.println("nat: " + nat);
                        String name = (String) ctx.constantPool[nat.a];
                        String descriptor = (String) ctx.constantPool[nat.b];
                        System.out.println("nm:" + name);
                        System.out.println("d:" + descriptor);
                        ctx.lookup(classname, name, descriptor, (method) -> {
                            ctx.thread.executeMethod(method, null, new Object[0], (result) -> {
                                System.out.println("result:" + result);
                            });
                        });
                    }
                }, REF_invokeSpecial {

                    @Override
                    protected void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                },
        REF_newInvokeSpecial {

                    @Override
                    protected void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                }, REF_invokeInterface {

                    @Override
                    protected void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                };

        public static Kind of(int data) {
            return values()[data - 1];
        }

        protected abstract void execute(MethodHandleInfo mh, VmContext ctx, Deque operandStack, Runnable callback);
    }

    public static class BootstrapMethodInfo {

        public MethodHandleInfo method;
        public Object[] args;

        @Override
        public String toString() {
            return "BootstrapMethodInfo{" + "method=" + method + ", args=" + Arrays.toString(args) + '}';
        }

    }
}
