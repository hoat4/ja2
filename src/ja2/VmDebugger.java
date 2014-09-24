/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2;

import ja2.member.MethodCallInfo;
import java.util.Deque;
import java.util.List;
import ja2.member.MethodInfo;

/**
 *
 * @author Attila
 */
public interface VmDebugger {

    void methodInvoked(MethodCallInfo methodCall);

    void onError(MethodCallInfo methodWhereErrorOccured);

    void statementExecuted(int pc, List<String> methodLogPart, Deque<Object> operandStack, Object[] localVariables);

    public void continuingMethod(MethodInfo method);
}
