package ja2.platform.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import ja2.clazz.ClassLoadHelper;
import ja2.member.MethodInfo;

/**
 *
 * @author Attila
 */
public class Debugger {

    private static final BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));
    private static List<MethodInfo> breakpoints = new ArrayList<>();
    private static boolean continueMode;

    public static void debugUI(Deque<Object> operandStack, Object[] localvars,
            MethodInfo currentMethod) {
        if (continueMode && !breakpoints.contains(currentMethod))
            return;
        continueMode = false;
        while (true) {
            try {
                switch (reader.readLine()) {
                    case "":
                        return;
                    case "operand stack":
                        for (Object object : operandStack)
                            System.out.println(object);
                        continue;
                    case "local vars":
                        int i = 0;
                        for (Object localvar : localvars) {
                            System.out.println(i + ": " + localvar);
                            i++;
                        }
                        continue;
                    case "localvar":
                        System.out.print("Local variable number: ");
                        try {
                            System.out.println(localvars[Integer.
                                    parseInt(reader.readLine())]);
                        } catch (NumberFormatException ex) {
                            System.err.println("Not number");
                        }
                        continue;
                    case "exit":
                        System.exit(0);
                        return;//unreachable
                        // TODO re-enable breakpoints
                 /*   case "breakpoint":
                        System.out.print("Method name where stop:");
                        String methodName = reader.readLine();
                        System.out.print("Method descriptor where stop:");
                        String methodDescriptor = reader.readLine();
                        System.out.print("Method class name where stop:");
                        String clazzName = reader.readLine();
                        try {
                            breakpoints.
                                    add(ClassLoadHelper.loadClass(clazzName).
                                    getMethod(methodName, methodDescriptor));
                        } catch (NoSuchMethodException ex) {
                            System.err.println("No such method");
                        }
                        break;*/
                    case "continue":
                        continueMode = true;
                        return;
                    default:
                        System.out.println("Unknown statement");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
