package ja2.io;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Attila
 */
public class MethodCodeInputStream extends InputStream {

    private final short[] code;
    public int pc = 0;

    public MethodCodeInputStream(short[] code) {
        this.code = code;
    }

    public void relativeJumpMinus3(int newpc) {
       relativeJumpMinus3((short)newpc);
    }
    public void relativeJumpMinus3(short newpc) {
//        System.out.println("relativejumpminus3:"+newpc);
        pc += newpc - 3;
  //      System.out.println("currentpc:"+pc);
    }

    @Override
    public int read() throws IOException {
        if (pc == code.length)
            return -1;
        return code[pc++];
    }

}
