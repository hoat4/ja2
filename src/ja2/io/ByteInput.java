package ja2.io;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Attila
 */
public class ByteInput {

    private final InputStream in;
    public long counter;

    public ByteInput(InputStream in) {
        this.in = in;
    }

    public byte readSignedByte() throws IOException {
        return (byte) read();
    }

    public int read() throws IOException {
        counter++;
        return in.read();
    }

    public int readU2() throws IOException {
        int a = read();
        int b = read();
        return (a << 8) | b;
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public long readU4() throws IOException {
        return ((read() << 24) + (read() << 16) + (read() << 8) + (read() << 0));
    }

    public void skip(long n) throws IOException {
        //  System.out.println("skip:"+Integer.toString ((int)n,16));
        for (long i = 0; i < n; i++)
            read();
    }

    public String readString(int length) throws IOException {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++)
            result[i] = (byte) read();
        return new String(result);
    }

    public int readInt() throws IOException {
        return (int) readU4();
    }

    public long readLong() throws IOException {
        return (((long) read()) << 56) + (((long) read()) << 48)
                + (((long) read()) << 40)
                + (((long) read()) << 32) + (((long) read()) << 24)
                + (((long) read()) << 16) + (((long) read())
                << 8) + (((long) read()));
    }

    public U2Pair readU2Pair() throws IOException {
        U2Pair result = new U2Pair();
        result.a = readU2();
        result.b = readU2();
        return result;
    }


    public U2Pair readU2Pair(Object[] annotate) throws IOException {
        U2Pair result = new U2Pair();
        result.a = readU2();
        result.b = readU2();
        result.annotate = annotate;
        return result;
    }
}
