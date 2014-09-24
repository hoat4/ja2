package ja2.test;

/**
 *
 * @author Attila
 */
public class Test2 implements Runnable {

    private int a = 1243 + 2, b = 8;
    private static boolean bool;

    static {
        System.out.println("ez a clinit");
    }

    {
        System.out.println("pingponglabda");
    }

    @Override
    public void run() {
        int i = 2;
        i++;
        i = 3;
    }

    public static void main(String[] args) {
        Num num = new Num();
        for (int i = 0; i < 4; i++) {
            num.increment();
        }
        num.sqrt();
        num.publish();
        sysoutint(num.value);
                num.unpublish();
    }

    private static native void sysoutint(int value);
}
