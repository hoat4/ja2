package ja2.test;

/**
 *
 * @author Attila
 */
public class Test2 {

    public static void main(String[] args) {
       sysoutint((int)(char)(((char)Integer.MAX_VALUE)<<(char)2));
    }

    private static native void sysoutint(int value);
}
