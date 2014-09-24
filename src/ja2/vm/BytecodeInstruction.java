/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ja2.vm;

import java.io.IOException;
import java.util.Deque;

/**
 *
 * @author attila
 */
public interface BytecodeInstruction {
    void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException;
}
