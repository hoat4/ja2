package javainterpreter.test;

/**
 *
 * @author Attila
 */
public class Num {

    public int value;
    private int number;

    public void increment() {
        ++number;
    }

    public void decrement() {
        --number;
    }
    public void sqrt() {
        final SqrtSession sqrt = new SqrtSession(number);
        sqrt.setEndListener(new Runnable() {
            @Override
            public void run() {
                number = sqrt.result.number;
            }
        });
        sqrt.run();
    }


    public void publish() {
        value = number;
    }

    public void unpublish() {
        value = (int)(Math.random()*100000);
    }

}
