package ja2.io;

/**
 *
 * @author Attila
 */
public class U2Pair {

    public int a, b;
    public Object[] annotate;

    @Override
    public String toString() {
        if (annotate == null)
            return "U2Pair[a=" + a + ", b=" + b + ']';
        else
            return annotate[0] + "[" + annotate[1] + "=" + a + ", " + annotate[2] + "=" + b + "]";
    }

}
