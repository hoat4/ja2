/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package javainterpreter.vm;

/**
 *
 * @author attila
 */
public class VoidObject {
    VoidObject() {
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public int hashCode() {
        return 1;
    }
    
    
}
