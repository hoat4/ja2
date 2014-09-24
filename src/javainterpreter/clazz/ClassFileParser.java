package javainterpreter.clazz;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javainterpreter.JavaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import static javainterpreter.Main.getBooleanConfig;
import javainterpreter.io.ByteInput;
import javainterpreter.member.FieldInfo;
import javainterpreter.member.MethodAccessFlag;
import javainterpreter.member.MethodHandleInfo;
import javainterpreter.member.MethodInfo;

/**
 *
 * @author Attila
 */
public class ClassFileParser {

    private static final boolean logClasses = getBooleanConfig("vm.log.classLoading");
    private static final boolean logCP = getBooleanConfig("vm.log.constantPool");
    private static final boolean logDeprecated = getBooleanConfig("vm.log.deprecated");
    private final ByteInput in;
    private ClassInfo clazz = new ClassInfo();

    private ClassFileParser(ClassLoadInfo classFileIn) throws IOException {
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(int ch; (ch = classFileIn.in.read()) != -1;)
            baos.write(ch);
        this.in = new ByteInput(new ByteArrayInputStream(baos.toByteArray()));*/
        this.in = new ByteInput(classFileIn.in);
        clazz.systemClass = classFileIn.systemClass;
    }

    public static ClassInfo parseClass(ClassLoadInfo in) throws IOException {
        ClassFileParser cfp = new ClassFileParser(in);
        cfp.parse();
        return cfp.clazz;
    }

    private void parse() throws IOException {
        if (in.readU4() != 0xCAFEBABE)
            throw new BadClassFileException("Not 0xCAFEBABE");
        int minorVersion = in.readU2();
        String majorVersion = getVersion();
        //System.out.println("Minor version: " + in.readU2());
        //System.out.println("Major version: " + getVersion());
        clazz.contextConstantPool = new Object[in.readU2()];
        clazz.contextConstantPoolElementTypes
                = new ConstantPoolElemType[clazz.contextConstantPool.length];
        if (logCP)
            System.out.println("Constant pool count: "
                    + clazz.contextConstantPool.length);
        getConstantPool();
        EnumSet access = ClassAccessFlag.fromBitmask(clazz.rawModifiers = in.readU2());
        clazz.access = access;
        //   System.out.print("Class access:");

        if (logClasses) {
            for (Object classAccessFlag : access)
                System.out.print((ClassAccessFlag) classAccessFlag);
            System.out.print(' ');
        }
        clazz.setName(
                (String) clazz.contextConstantPool[(int) clazz.contextConstantPool[in.
                readU2()]]);
        if (logClasses)
            System.out.print(clazz.name);
        {
            Object superClassIndex = clazz.contextConstantPool[in.
                    readU2()];
            clazz.superClassName = superClassIndex == null ? null
                    : (String) clazz.contextConstantPool[(int) superClassIndex];
        }
        processImplementedInterfaces();
        processFields();
        processMethods();
        if (logClasses) {
            System.out.print(" extends " + clazz.superClassName);
            if (clazz.implementedInterfaces.length > 0) {
                System.out.print(" implements ");
                System.out.print(clazz.implementedInterfaces[0]);
                for (int i = 1; i < clazz.implementedInterfaces.length; i++)
                    System.out.print(", " + clazz.implementedInterfaces[i]);
            }
            System.out.println();
        }
    }

    private String getVersion() throws IOException, BadClassFileException {
        int ver = in.readU2();
        switch (ver) {
            case 0x34:
                return "Java 8";
            case 0x33:
                return "J2SE 7";
            case 0x32:
                return "J2SE 6.0";
            case 0x31:
                return "J2SE 5.0";
            case 0x30:
                return "JDK 1.4";
            case 0x2F:
                return "JDK 1.3";
            case 0x2E:
                return "JDK 1.2";
            case 0x2D:
                return "JDK 1.1";
            default:
                throw new BadClassFileException("Not major version: " + ver);
        }
    }
    
    private void getConstantPool() throws IOException {
        for (int i = 1; i < clazz.contextConstantPool.length;) {
            if (logCP) {
                System.out.print(i + ": ");
                System.out.flush();
            }
            int type = in.read();
            Object cp;
            ConstantPoolElemType ct;
            int elemlength = 1;
            switch (type) {
                case 1://UTF8
                    int length = in.readU2();
                    cp = in.readString(length);
                    ct = ConstantPoolElemType.UTF8;
                    if (logCP)
                        System.out.println("UTF8:" + cp);
                    break;
                case 3://Int
                    cp = in.readInt();
                    ct = ConstantPoolElemType.INT;
                    if (logCP)
                        System.out.println("Integer:" + cp);
                    break;
                case 4://Float
                    cp = Float.intBitsToFloat(in.readInt());
                    ct = ConstantPoolElemType.FLOAT;
                    if (logCP)
                        System.out.println("Float:" + cp);
                    break;
                case 5://Long
                    cp = in.readLong();
                    ct = ConstantPoolElemType.LONG;
                    if (logCP)
                        System.out.println("Long:" + cp);
                    elemlength = 2;
                    break;
                case 6://Double
                    cp = Double.longBitsToDouble(in.readLong());
                    ct = ConstantPoolElemType.DOUBLE;
                    if (logCP)
                        System.out.println("Double:" + cp);
                    elemlength = 2;
                    break;
                case 7:
                    cp = in.readU2();
                    ct = ConstantPoolElemType.CLASSREF;
                    if (logCP)
                        System.out.println("Class Reference: " + cp);
                    break;
                case 8:
                    cp = in.readU2();
                    ct = ConstantPoolElemType.STRINGREF;
                    if (logCP)
                        System.out.println("String Reference: " + cp);
                    break;
                case 9:
                    cp = in.readU2Pair();
                    ct = ConstantPoolElemType.FIELDREF;
                    if (logCP)
                        System.out.println("Field Reference: " + cp);
                    break;
                case 10:
                    cp = in.readU2Pair();
                    ct = ConstantPoolElemType.METHODREF;
                    if (logCP)
                        System.out.println("Method Reference: " + cp);
                    break;
                case 11:
                    cp = in.readU2Pair();
                    ct = ConstantPoolElemType.INTERFACE_METHODREF;
                    if (logCP)
                        System.out.println("Interface Method Reference: " + cp);
                    break;
                case 12:
                    cp = in.readU2Pair();
                    ct = ConstantPoolElemType.NAME_AND_TYPE_DESCRIPTOR;
                    if (logCP)
                        System.out.println("Name and Type descriptor: " + cp);
                    break;
                case 15:
                    cp = new MethodHandleInfo(in.read(), in.readU2());
                    ct = ConstantPoolElemType.METHODHANDLE;
                    if (logCP)
                        System.out.println("Method Handle: " + cp);
                    break;
                case 16:
                    cp = in.readU2();
                    ct = ConstantPoolElemType.METHODTYPE_INFO;
                    if (logCP)
                        System.out.println("Method Type Info: " + cp);
                    break;
                case 18:
                    cp = in.readU2Pair();
                    ct = ConstantPoolElemType.INVOKEDYNAMIC_INFO;
                    if (logCP)
                        System.out.println("InvokeDynamic Info: " + cp);
                    break;
                default:
                    throw new BadClassFileException("Not constant pool type: "
                            + type);
            }
            clazz.contextConstantPool[i] = cp;
            clazz.contextConstantPoolElementTypes[i] = ct;
            i += elemlength;
        }
    }

    private void processImplementedInterfaces() throws IOException {
        clazz.implementedInterfaces = new String[in.readU2()];
        for (int i = 0; i < clazz.implementedInterfaces.length; i++)
            clazz.implementedInterfaces[i]
                    = (String) clazz.contextConstantPool[(int) clazz.contextConstantPool[in.
                    readU2()]];
    }

    private void processFields() throws IOException {
        clazz.fields = new FieldInfo[in.readU2()];
        for (int i = 0; i < clazz.fields.length; i++) {
            int access = in.readU2();
            String name = (String) clazz.contextConstantPool[in.readU2()];
            String type = (String) clazz.contextConstantPool[in.readU2()];
            clazz.staticFieldValues.put(name,
                    JavaType.getType(type).defaultValue);
            FieldInfo field = new FieldInfo(access, type, name, clazz, i);
            processAttributes(null, field);

            clazz.fields[i] = field;
        }
    }

    private void processMethods() throws IOException {
        int allMethodCount = in.readU2();
        List<MethodInfo> methods = new ArrayList<>();
        for (int i = 0; i < allMethodCount; i++) {
            int access = in.readU2();
            int nameIndex = in.readU2();
            String name = (String) clazz.contextConstantPool[nameIndex];
            String descriptor = (String) clazz.contextConstantPool[in.readU2()];
            MethodInfo method = new MethodInfo(access, name, descriptor, clazz);
            processAttributes(method, null);
            switch (method.name) {
                case "<clinit>":
                    clazz.clinitMethod = method;
                    break;
                default:
                    methods.add(method);
                    break;
            }
        }
        clazz.decraledMethods = methods.toArray(new MethodInfo[methods.size()]);
    }

    private void processAttributes(MethodInfo method, FieldInfo field) throws
            IOException {
        int attributeCount = in.readU2();
        for (int j = 0; j < attributeCount; j++) {
            int an = in.readU2();
            Object ize = clazz.contextConstantPool[an];
            if (clazz.contextConstantPoolElementTypes[an]
                    == ConstantPoolElemType.STRINGREF)
                ize = clazz.contextConstantPool[(int) ize];
            String attrname = (String) ize;
            long attrlength = in.readU4();
            switch (attrname) {
                case "Code":
                    in.skip(2);//max-stack
                    method.maxLocalVariables = in.readU2();
                    short[] code = new short[(int) in.readU4()];
                    for (int i = 0; i < code.length; i++)
                        code[i] = (short) in.read();
                    //         System.out.println("Code:" + Arrays.toString(code));
                    in.skip(8 * in.readU2());// TODO exception table
                    if (method != null)  //method == null ? field : method
                        method.code = code;
                    processAttributes(null, null);
                    break;
                case "LineNumberTable":
                    in.skip(in.readU2() * 4);
                    break;
                case "LocalVariableTable":
                case "LocalVariableTypeTable":
                    in.skip(in.readU2() * 10);
                    break;
                case "ConstantValue":
                    if (field == null)
                        throw new BadClassFileException(
                                "ConstantValue attribute not in a field");
                    Object val = clazz.contextConstantPool[in.readU2()];
                    clazz.staticFieldValues.put(field.name, val);
                    break;
                case "StackMapTable":
                case "Exceptions":
                case "Signature":
                    in.skip(attrlength);
                    break;
                case "RuntimeVisibleAnnotations":
//                    System.err.println("Annotations not supproted");
                    // TODO annotations
                    in.skip(attrlength);
                    break;
                case "Deprecated":
                    if (logDeprecated) {
                        System.out.print("Deprecated: ");
                        if (field != null)
                            System.out.println(field);
                        else if (method != null)
                            System.out.println(method);
                        else
                            System.out.println(clazz);
                    }
                    break;
                default:
                    throw new BadClassFileException("Not attribute name: "
                            + attrname);
            }
        }
        //return false;
    }
}
