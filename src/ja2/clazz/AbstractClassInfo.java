package ja2.clazz;

import ja2.JavaType;

/**
 *
 * @author Attila
 */
public abstract class AbstractClassInfo {

    /**
     * When setting this, <code>asType</code> must be set!
     */
    public String name;
    public final boolean isPrimitiveClass;
    public JavaObject.JClassInstance classObject;
    public JavaType asType;

    protected AbstractClassInfo(boolean isPrimitiveClass) {
        this.isPrimitiveClass = isPrimitiveClass;
    }
}
