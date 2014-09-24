/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javainterpreter.clazz;

/**
 *
 * @author Attila
 */
public class BadClassFileException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of
     * <code>BadClassFileException</code> with the specified detail message.
     * <p/>
     * @param msg the detail message.
     */
    public BadClassFileException(String msg) {
        super(msg);
    }
}
