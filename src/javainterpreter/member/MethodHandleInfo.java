/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javainterpreter.member;

/**
 *
 * @author attila
 */
public class MethodHandleInfo {

    public Kind reference_kind;
    public int reference_index;

    public MethodHandleInfo(int reference_kind, int reference_index) {
        this.reference_kind = Kind.of(reference_kind);
        this.reference_index = reference_index;
    }

    @Override
    public String toString() {
        return "MethodHandleInfo{" + "reference_kind=" + reference_kind + ", reference_index=" + reference_index + '}';
    }

    public static enum Kind {

        REF_getField, REF_getStatic, REF_putField, REF_putStatic,
        REF_invokeVirtual, REF_invokeStatic, REF_invokeSpecial,
        REF_newInvokeSpecial, REF_invokeInterface;

        public static Kind of(int data) {
            return values()[data - 1];
        }
    }
}
