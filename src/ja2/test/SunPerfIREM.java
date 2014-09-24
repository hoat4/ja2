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
public class SunPerfIREM {
    private static final int COUNT = 1000000000;

    private static int result;

    public static void main(String[] args) {
        a(false);
        b(false);
        a(true);
        b(true);
        System.out.println(result);
    }

    public static void a(boolean print) {
        long perfMeasureStartNSTime__ = System.nanoTime();
        for (int i = 1; i < COUNT; i++)
            result += i % 100;
        long t = System.nanoTime();
        if (print)
            System.out.println("X % Y: " + (double)(t - perfMeasureStartNSTime__) / COUNT + " ns");
    }

    public static void b(boolean print) {
        long perfMeasureStartNSTime__ = System.nanoTime();
        for (int i = 1; i < COUNT; i++)
            result += i - (i / 100) * 100;// X: i, Y: 100
        long t = System.nanoTime();
        if (print)
            System.out.println("X - (X / Y) * Y: " + (double)(t - perfMeasureStartNSTime__) / COUNT + " ns");
    }
}
