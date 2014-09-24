/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package javainterpreter.test;

/**
 *
 * @author attila
 */
public class SunClinitTest {
    static class A {
        static {
            System.out.println("Aclinit begin");
            B.nop();
            System.out.println("Aclinit end");
        }
        public static void nop() {
            System.out.println("A");
        }
    }
    static class B {
        static {
            System.out.println("Bclinit begin");
            A.nop();
            System.out.println("Bclinit end");
        }
        public static void nop() {
            System.out.println("B");
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        A.nop();
        System.out.println("user.timezone".hashCode());
    }
    
}
