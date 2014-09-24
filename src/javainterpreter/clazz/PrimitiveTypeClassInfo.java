package javainterpreter.clazz;

import javainterpreter.JavaType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javainterpreter.JavaInterpreter;

/**
 *
 * @author Attila
 */
public class PrimitiveTypeClassInfo extends AbstractClassInfo {

    public  static final PrimitiveTypeClassInfo[] array = {
        create("byte"), create("char"), create("short"), create("int"),
        create("float"), create("boolean"), create("double"), create("long")};
    /*őpublic static final PrimitiveTypeClassInfo BYTE =
     new PrimitiveTypeClassInfo("byte");
     public static final PrimitiveTypeClassInfo CHAR =
     new PrimitiveTypeClassInfo("char");
     public static final PrimitiveTypeClassInfo SHORT =
     new PrimitiveTypeClassInfo("short");
     public static final PrimitiveTypeClassInfo INT = new PrimitiveTypeClassInfo(
     "int");
     public static final PrimitiveTypeClassInfo FLOAT =
     new PrimitiveTypeClassInfo("float");
     public static final PrimitiveTypeClassInfo BOOLEAN =
     new PrimitiveTypeClassInfo("boolean");
     public static final PrimitiveTypeClassInfo DOUBLE =
     new PrimitiveTypeClassInfo("double");
     public static final PrimitiveTypeClassInfo LONG =
     new PrimitiveTypeClassInfo("long");*/

    private PrimitiveTypeClassInfo(String name) {
        super(true);
        this.name = name;
        asType = JavaType.getType(name);
    }

    public static PrimitiveTypeClassInfo get(String name) {
        for (PrimitiveTypeClassInfo primitiveTypeClassInfo : array) {
            if(primitiveTypeClassInfo.name.equals(name))
                return primitiveTypeClassInfo;
        }
        return null;
    }

    private static PrimitiveTypeClassInfo create(String name) {
        return new PrimitiveTypeClassInfo(name);
    }
}
