/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.test;

/**
 *
 * @author attila
 */
public class SunSwitchPerf {

    public static void main(String[] args) {
        System.out.println("Preparing...");
        Runnable[] array = new Runnable[]{new A(), new B()};
        int[] code = prepare();
        run(array, code);
        System.out.println("Starting...");
        System.out.println();
        long perfMeasureStartNSTime__ = System.nanoTime();
        run(array, code);
        System.out.println("Class: " + (double)(System.nanoTime() - perfMeasureStartNSTime__) / COUNT + " ns");

        A a = new A();
        B b = new B();
        array = new Runnable[]{a::run, b::run};
        perfMeasureStartNSTime__ = System.nanoTime();
        run(array, code);
        System.out.println("Lambda: " + (double)(System.nanoTime() - perfMeasureStartNSTime__) / COUNT + " ns");
         perfMeasureStartNSTime__ = System.nanoTime();
           int countlocal = COUNT;
        for (int i = countlocal; i >= 0; i--)
            switch(code[i]) {
                case 0:
                    a.run();
                    break;
                case 1:
                    b.run();
                    break;
            }
        System.out.println("switch-case: " + (double)(System.nanoTime() - perfMeasureStartNSTime__) / COUNT + " ns");
        
        System.out.println();
        System.out.println("Done!");
    }

    public static int[] prepare() {
        int[] code = new int[COUNT+1];
        for (int i = 0; i < code.length; i++)
            code[i] = i % 2;
        return code;
    }

    public static void run(Runnable[] array, int[] code) {
        int countlocal = COUNT;
        for (int i = countlocal; i >= 0; i--)
            array[code[i]].run();
    }
    public static final int COUNT = 1000000;

    private static class A implements Runnable {

        public int counter;

        @Override
        public void run() {
            counter++;
        }

    }

    private static class B implements Runnable {

        public int counter;

        @Override
        public void run() {
            counter++;
        }

    }
}
