/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ja2;

/**
 *
 * @author attila
 */
public class VmLifecycle {
    private final JThread thread;

    public VmLifecycle(JThread thread) {
        this.thread = thread;
    }
    
    public void run() {
        while(JavaInterpreter.currentThread.runnable&&JavaInterpreter.enable) {
            for (int i = 0; i < thread.runningThreadsRef.size(); i++) {
                thread.runningThreadsRef.get(i).tick();
            }
        }
    }
}
