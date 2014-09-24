package ja2.test;

/**
 *
 * @author Attila
 */
public class SqrtSession {

    public Num result;
    private int number = 12;
    private Runnable end;

    public SqrtSession(int number) {
        this.number = number;
    }

    public void setEndListener(Runnable end) {
        if (end != null)
            error();
        this.end = end;
    }

    public void run() {
        result.publish();
        if (result.value == -1) {
            end.run();
            result.unpublish();
            return;
        }
        result.unpublish();
        sqrt();
        Num num = new Num();
        for (int i = 0; i < number; i++) {
            num.increment();
        }
        end.run();
    }

    private void error() {
        result = new Num();
        result.decrement();
    }

    /**
     * H.A. sqrt
     */
    private void sqrt() {
        for (int x = 1, y = number; x * x != y; number = x)
            x -= Math.signum(x * x - y);
    }
}
