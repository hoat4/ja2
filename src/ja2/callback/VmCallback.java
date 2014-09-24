/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.callback;

/**
 *
 * @author Attila
 */
public interface VmCallback<T> {
    public static VmCallback<Object> NOP = (ignored)->{};

    void run(T arg);
}
