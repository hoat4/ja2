/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ja2.vm;

import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import ja2.JavaInterpreter;
import static ja2.JavaInterpreter.LOG_MISC;
import static ja2.JavaInterpreter.convertString;
import static ja2.JavaInterpreter.debugger;
import static ja2.JavaInterpreter.error;
import static ja2.JavaInterpreter.logMethodInvoke;
import static ja2.JavaInterpreter.throwException;
import ja2.JavaType;
import static ja2.JavaType.VOID;
import ja2.callback.VmCallback;
import ja2.clazz.BadClassFileException;
import ja2.clazz.ClassLoadHelper;
import ja2.clazz.ConstantPoolElemType;
import static ja2.clazz.ConstantPoolElemType.CLASSREF;
import static ja2.clazz.ConstantPoolElemType.STRINGREF;
import static ja2.clazz.ConstantPoolElemType.UTF8;
import ja2.clazz.JavaObject;
import ja2.clazz.JavaObject.Evaluation;
import ja2.clazz.JavaObject.JClassInstance;
import ja2.member.FieldAccessFlag;
import ja2.member.MethodInfo;

/**
 *
 * @author attila
 */
public class Bytecodes {

    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        if (obj instanceof Character)
            return (char) obj;
        return ((Number) obj).intValue();
    }

    private static class anewarray implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int count = toInt(operandStack.pop());
            JavaType elemtype = ctx.typeFromConstantPool(ctx.in.readU2());
            if (count < 0)
                error(ctx.thread, "java.lang.NegativeArraySizeException",
                        String.valueOf(count));
            if (ctx.logging)
                ctx.log(2, elemtype.toString() + "[" + count + "]");
            operandStack.push(new JavaObject.JArray(count, elemtype, ctx.thread));
        }

    }

    private static class _astore implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object value = operandStack.pop();
            int index = toInt(operandStack.pop());
            JavaObject.JArray array
                    = (JavaObject.JArray) operandStack.pop();
            array.array[index] = value;
        }

    }

    private static class _return implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            ctx.thread.popMethod(operandStack.pop());
        }

    }

    private static class _aload implements BytecodeInstruction {

        @Override
        @SuppressWarnings({"CallToThreadDumpStack", "null"})
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int index = toInt(operandStack.pop());
            JavaObject.JArray array
                    = (JavaObject.JArray) operandStack.pop();

            if (array == null) {
                error(ctx.thread, "java/lang/NullPointerException", "array");
                return;
            }
            if (array.array.length < index) {
                System.out.println("arrayindex" + ctx.method.name + ctx.method.clazz.name);
                System.out.flush();
                error(ctx.thread, "java/lang/ArrayIndexOutOfBoundsException", "index: " + index);
            }
            //System.err.println("array:" + array.array.length);
            if (index < 0 || index >= array.array.length) {
                System.out.println("arrayerror(in " + ctx.method.name + ctx.method.clazz.name);
                System.out.flush();
                Thread.dumpStack();
                error(ctx.thread, "java/lang/ArrayIndexOutOfBoundsException", index + ", but array's length is " + array.array.length + " (" + array.elemType + "[])");
            }
            if (ctx.logging) {
                ctx.log(2, array.elemType + "[" + index + "]");
                ctx.log(4, array.array[index]);
            }
            operandStack.push(array.array[index]);
        }

    }

    private static class nop implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
        }

    }

    private static class pop implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.pop();
        }

    }

    private static class pop2 implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.pop();
            operandStack.pop();
        }

    }

    private static class aconst_null implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(null);
        }

    }

    private static class arraylength implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            JavaObject.JArray array = (JavaObject.JArray) operandStack.pop();
            if (array == null) {
                error(ctx.thread, "java/lang/NullPointerException", "array");
                return;
            }
            if (ctx.logging)
                ctx.log(2, array.array.length);
            operandStack.push(array.array.length);
        }

    }

    private static class _load implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int val = ctx.wideMode ? ctx.in.readU2() : ctx.in.read();
            operandStack.push(ctx.localVariables[val]);
            if (ctx.logging) {
                ctx.log(2, "#" + val);
                ctx.log(2, operandStack.peek());
            }
        }

    }

    private static class _store implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int val = ctx.wideMode ? ctx.in.readU2() : ctx.in.read();
            if (ctx.logging) {
                ctx.log(2, "#" + val);
                ctx.log(4, operandStack.peek());
            }
            ctx.localVariables[val] = operandStack.pop();
        }

    }

    private static class _athrow_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            JavaObject.JClassInstance ex
                    = (JavaObject.JClassInstance) operandStack.pop();
            if (ex == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "The throwing exception is null");
            throwException(ex);
        }

    }

    private static class ldc implements BytecodeInstruction {// és ldc_w meg ldc2_w

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int index;
            if (instructionCode == 18)
                index = ctx.in.read();
            else
                index = ctx.in.readU2();
            ConstantPoolElemType type
                    = ctx.constantPoolElementTypes[index];
            Object value = ctx.constantPool[index];
            boolean instantResult = true;
            Object result = null;
            if (LOG_MISC)
                System.out.println(type);
            switch (type) {
                case UTF8:
                    result = convertString(ctx.thread, (String) value);

                    break;
                case STRINGREF:
                    result
                            = convertString(ctx.thread, (String) ctx.constantPool[toInt(value)]);
                    break;
                case INT:
                case FLOAT:
                case LONG:
                case DOUBLE:
                    result = value;
                    break;
                case CLASSREF:
                    instantResult = false;
                    ClassLoadHelper.loadClass((String) ctx.constantPool[toInt(value)], ctx.thread,
                            (classinfo) -> {
                                JClassInstance result1 = classinfo.classObject;
                                if (ctx.logging)
                                    ctx.log(2, result1);
                                operandStack.push(result1);
                            }, JavaInterpreter::handleError);
                    break;
                default:
                    result = null;
                    System.err.
                            println(
                                    " TODO other constant pool type @ ldc:" + type);
                // TODO other constant pool types
            }
            if (instantResult) {
                if (ctx.logging)
                    ctx.log(2, result);
                operandStack.push(result);
            }
        }

    }

    private static class bipush implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int bipushVal = (int) ctx.in.read();
            if (ctx.logging)
                ctx.log(2, bipushVal);
            operandStack.push(bipushVal);
        }

    }

    private static class _f2d_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push((double) operandStack.pop());
        }

    }

    private static class _2i implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(((Number) operandStack.pop()).intValue());
        }

    }

    private static class aload_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object val = ctx.localVariables[instructionCode - 42];
            //System.out.println(instructionCode);
            operandStack.push(val);
            if (ctx.logging)
                ctx.log(2, val);
        }

    }

    private static class iload_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(ctx.localVariables[instructionCode - 26]);
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
        }

    }

    private static class fload_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(ctx.localVariables[instructionCode - 34]);
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
        }

    }

    private static class lload_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(ctx.localVariables[instructionCode - 30]);
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
        }

    }

    private static class dload_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(ctx.localVariables[instructionCode - 38]);
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
        }

    }

    private static class astore_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            if (operandStack.size() == 0)
                error(ctx.thread, "java/lang/Error", "operand stack is empty");
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
            ctx.localVariables[instructionCode - 75]
                    = operandStack.pop();
        }

    }

    private static class istore_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
            ctx.localVariables[instructionCode - 59] = operandStack.pop();
        }

    }

    private static class fstore_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
            ctx.localVariables[instructionCode - 67] = operandStack.pop();
        }

    }

    private static class dstore_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
            ctx.localVariables[instructionCode - 71] = operandStack.pop();
        }

    }

    private static class lstore_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
            ctx.localVariables[instructionCode - 63] = operandStack.pop();
        }

    }

    private static class newarray implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int arrtype = ctx.in.read();
            int len = ((Number) operandStack.pop()).intValue();
            JavaType elemtype;
            switch (arrtype) {
                case 4://T_BOOLEAN
                    elemtype = JavaType.BOOLEAN;
                    break;
                case 5://T_CHAR
                    elemtype = JavaType.CHAR;
                    break;
                case 6://T_FLOAT
                    elemtype = JavaType.FLOAT;
                    break;
                case 7://T_DOUBLE
                    elemtype = JavaType.DOUBLE;
                    break;
                case 8://T_BYTE
                    elemtype = JavaType.BYTE;
                    break;
                case 9://T_SHORT
                    elemtype = JavaType.SHORT;
                    break;
                case 10://T_INT
                    elemtype = JavaType.INT;
                    break;
                case 11://T_LONG
                    elemtype = JavaType.LONG;
                    break;
                default:
                    ctx.ec.onError(new BadClassFileException("Not array type: "
                            + arrtype));
                    return;
            }
            operandStack.push(new JavaObject.JArray(len, elemtype, ctx.thread));

        }

    }

    private static class iconst_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int vali = instructionCode - 3;

            operandStack.push(vali);
        }

    }

    private static class castore implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object value = operandStack.pop();
            int index = ((Number) operandStack.pop()).intValue();
            JavaObject.JArray array
                    = (JavaObject.JArray) operandStack.pop();
            try {
                array.array[index] = value;
            } catch (ArrayIndexOutOfBoundsException ex2) {
                error(ctx.thread, "java/lang/ArrayIndexOutOfBoundsException", "(store char to array)arraylength: " + array.array.length + ", index: " + index);
            }
        }

    }

    private static class _iastore_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object value = ((Number) operandStack.pop()).intValue();
            int index = (int) operandStack.pop();
            JavaObject.JArray array
                    = (JavaObject.JArray) operandStack.pop();
            try {
                array.array[index] = value;
            } catch (ArrayIndexOutOfBoundsException ex2) {
                error(ctx.thread, "java/lang/ArrayIndexOutOfBoundsException", "(store int to array)arraylength: " + array.array.length + ", index: " + index);
            }
        }

    }

    private static class Return implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            ctx.thread.popMethod(VmContext.VOID);
        }

    }

    private static class getstatic implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            ctx.getField(ctx.in.readU2(), (field) -> {
                if(ctx.logging){
                    ctx.log(2, field);
                    ctx.log(4, field.clazz.staticFieldValues.get(field.name));
                }
                operandStack.push(field.clazz.staticFieldValues.get(field.name));
            }, ctx.ec);
        }

    }

    private static class wide implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            ctx.wideMode = true;
        }

    }

    private static class lconst_0 implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(0L);
        }

    }

    private static class lconst_1 implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(1L);
        }

    }

    private static class swap implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();
            operandStack.push(a);
            operandStack.push(b);
        }

    }

    private static class dup implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object value = operandStack.pop();
            if(ctx.logging)
                ctx.log(2, value);
            operandStack.push(value);
            operandStack.push(value);
        }

    }

    private static class invokestatic implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int methodRefIndex = ctx.in.readU2();
            ctx.getMethod(methodRefIndex, (method2) -> {
                if (ctx.logging)
                    ctx.log(2, method2);
                Object[] invokeArgs
                        = new Object[method2.parameterTypes.length];
                for (int i = invokeArgs.length - 1; i >= 0; i--)
                    invokeArgs[i] = operandStack.pop();
                ctx.thread.executeMethod(method2, null, invokeArgs, (result2) -> {
                    if (debugger != null)
                        debugger.continuingMethod(ctx.method);
                    if (result2 != VOID)
                        operandStack.push(result2);
                });
            }, ctx.ec);
        }

    }

    private static class invoke_______ implements BytecodeInstruction {

        @Override
        @SuppressWarnings("null")
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int methodRefIndex = ctx.in.readU2();
            if (instructionCode == 185)
                ctx.in.readU2();// TODO mit jelent invokeinterface-nél a count, és mi a 0? elég lenne 1 getMethod hívás, mert a method descriptorban benne van a paraméterek száma
            ctx.getMethod(methodRefIndex, (method3) -> {
                Object[] args2
                        = new Object[method3.parameterTypes.length];
                for (int i = args2.length - 1; i >= 0; i--)
                    args2[i] = operandStack.pop();
                JavaObject.JClassInstance this0 = (JavaObject.JClassInstance) operandStack
                        .pop();
                if (this0
                        == null)
                    error(ctx.thread, "java/lang/NullPointerException",
                            "Cannot invoke method " + method3
                            + " on null");
                VmCallback<MethodInfo> lambda = (method2) -> {
                    if (ctx.logging)
                        ctx.log(2, method2);

                    if (logMethodInvoke)
                        System.out
                                .println("invoking method " + method2
                                        + " in class " + method2.clazz
                                        + " with args "
                                        + Arrays
                                        .toString(args2));
                    ctx.thread.executeMethod(method2, this0, args2, (result2) -> {
                        if (logMethodInvoke)
                            System.out
                                    .println("Continuing with " + ctx.method
                                            + " in class " + ctx.clazz + "(invoked with "
                                            + Arrays
                                            .toString(ctx.args) + ")");
                        if (debugger != null)
                            debugger.continuingMethod(ctx.method);
                        if (result2 != VmContext.VOID)
                            operandStack.push(result2);
                    });
                };
                if (instructionCode == 183)
                    lambda.run(method3);
                else
                    ctx.getMethod(methodRefIndex, this0.classInfo, lambda, ctx.ec);
            }, ctx.ec);
        }

    }

    private static class getfield implements BytecodeInstruction {

        @Override
        @SuppressWarnings("null")
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            ctx.getField(ctx.in.readU2(), (field) -> {
                if (field.access.contains(FieldAccessFlag.STATIC))
                    error(ctx.thread, "java/lang/IncompatibleClassChangeError",
                            field.name + " is a static field in class "
                            + field.clazz);
                JClassInstance obj = (JavaObject.JClassInstance) operandStack.pop();
                if (obj == null)
                    error(ctx.thread, "java/lang/NullPointerException", "getfield " + field.toString() + " on null");
                Object val = obj.fieldValues.get(field.name);
                if (ctx.logging) {
                    ctx.log(2, field.name);
                    ctx.log(4, val);
                }
                if (val instanceof Evaluation)
                    ((Evaluation) val).evaluate(ctx, operandStack::push);
                else
                    operandStack.push(val);
            }, ctx.ec);
        }

    }

    private static class if_icmpne implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            int bd = toInt(operandStack.pop());
            int ad = toInt(operandStack.pop());
            if (ad != bd)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class if_icmpgt implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            int bd = toInt(operandStack.pop());
            int ad = toInt(operandStack.pop());
            if (ad > bd)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class putfield implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            ctx.getField(ctx.in.readU2(), (field) -> {
                if (field.access.contains(FieldAccessFlag.STATIC))
                    error(ctx.thread, "java/lang/IncompatibleClassChangeError",
                            field.name + " is a static field in class "
                            + field.clazz);
                Object val2 = operandStack.pop();
                if (ctx.logging) {
                    ctx.log(2, field);
                    ctx.log(2, val2);
                }
                ((JavaObject.JClassInstance) operandStack.pop()).fieldValues
                        .put(field.name, val2);
            }, ctx.ec);
        }

    }

    private static class putstatic implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            ctx.getField(ctx.in.readU2(), (field) -> {
                if (!field.access.contains(FieldAccessFlag.STATIC))
                    error(ctx.thread, "java/lang/IncompatibleClassChangeError",
                            field.name + " is a static field in class "
                            + field.clazz);
                if (ctx.logging) {
                    ctx.log(2, field);
                    ctx.log(4, operandStack.peek());
                }
                field.clazz.staticFieldValues.put(field.name, operandStack.pop());
            }, ctx.ec);
        }

    }

    private static class monitorenter implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            // TODO monitorenter
            operandStack.pop();
        }

    }

    private static class monitorexit implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            // TODO monitorexit
            operandStack.pop();
        }

    }

    private static class ifnull implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            if (operandStack.pop() == null)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class ifnonnull implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            if (operandStack.pop() != null)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class New implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            ClassLoadHelper.loadClass((String) ctx.constantPool[(Integer) ctx.constantPool[ctx.in.
                    readU2()]], ctx.thread, (objectClass) -> {
                        objectClass.instantiate(ctx.thread, (instance) -> {
                            operandStack.push(instance);
                            if(ctx.logging)
                                ctx.log(2, instance);
                        }, ctx.ec);
                    }, ctx.ec);
        }

    }

    private static class ifne implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            if (((Number) operandStack.pop()).intValue() != 0)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class ifge implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            Object pop = operandStack.pop();
            Number popnum = (Number) pop;
            int intValue = popnum.intValue();
            if (intValue >= 0)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class ifle implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            if (((Number) operandStack.pop()).intValue() <= 0)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class ifeq implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            if (((Number) operandStack.pop()).intValue() == 0)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class fcmp_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            float value2 = (float) operandStack.pop();
            float value1 = (float) operandStack.pop();
            if (value2 < value1)
                operandStack.push(1);
            else if (value2 == value1)
                operandStack.push(0);
            else if (value2 > value1)
                operandStack.push(-1);
            else
                operandStack.push(instructionCode == 150 ? 1 : -1);
        }

    }

    private static class fconst_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int num = instructionCode - 11;
            operandStack.push((float) num);
        }

    }

    private static class iadd implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();
            operandStack.push((toInt(a)) + (toInt(b)));
        }

    }

    private static class Goto implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            ctx.relativeJumpMinus3(ctx.in.readU2());
        }

    }

    private static class Instanceof implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            String className = (String) ctx.constantPool[(int) ctx.constantPool[ctx.in.
                    readU2()]];
            JClassInstance obj = (JavaObject.JClassInstance) operandStack.pop();
            if (ctx.logging)
                ctx.log(2, obj);
            if (obj == null) {
                if (ctx.logging)
                    ctx.log(4, "false because null");
                operandStack.push((byte) 0);
            } else
                obj.instanceOf(className, ctx.thread, (is) -> {
                    if (ctx.logging)
                        ctx.log(4, is);
                    operandStack.push((byte) (is ? 1 : 0));
                }, ctx.ec);
        }

    }

    private static class isub implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object b = operandStack.pop();
            Object a = operandStack.pop();
            if (a == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "1. number in int substract)");
            if (b == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "2. number in int substract)");
            operandStack.push(toInt(a) - toInt(b));
        }

    }

    private static class imul implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();
            if (a == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "1. number in int multiply");
            if (b == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "2. number in int multiply");
            operandStack.push(toInt(a) * toInt(b));
        }

    }

    private static class iand implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();
            if (a == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "1. number in int and");
            if (b == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "2. number in int and");
            operandStack.push(toInt(a) & toInt(b));
        }

    }

    private static class i2l implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(((Number) operandStack.pop()).
                    longValue());
        }

    }

    private static class ishl implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int ai = toInt(operandStack.pop());
            int bi = toInt(operandStack.pop());
            operandStack.push(ai << bi);
        }

    }

    private static class ishr implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int bi = toInt(operandStack.pop());
            int ai = toInt(operandStack.pop());
            operandStack.push(ai >> bi);
        }

    }

    private static class land implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();
            if (a == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "1. number in long and");
            if (b == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "2. number in long and");
            operandStack.push(((long) a) & ((long) b));
        }

    }

    private static class lcmp implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            long value2l = (long) operandStack.pop();
            long value1l = (long) operandStack.pop();
            if (value2l > value1l)
                operandStack.push(1);
            else if (value2l == value1l)
                operandStack.push(0);
            else
                operandStack.push(-1);
        }

    }

    private static class if_icmple implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            int bd = toInt(operandStack.pop());
            int ad = toInt(operandStack.pop());
            if (ad <= bd)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class iushr implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int bi = toInt(operandStack.pop());
            int ai = toInt(operandStack.pop());
            operandStack.push(ai >> bi);
        }

    }

    private static class ixor implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int bi = toInt(operandStack.pop());
            int ai = toInt(operandStack.pop());
            operandStack.push(ai ^ bi);
        }

    }

    private static class iflt implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            if (((Number) operandStack.pop()).intValue() < 0)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class ineg implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(-((Number) operandStack.pop()).intValue());
        }

    }

    private static class sipush implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push((short) ctx.in.readU2());
        }

    }

    private static class iint implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {

            int index = ctx.in.read();
            int val = ((Number) ctx.localVariables[index]).intValue();
            ctx.localVariables[index] = val + ctx.in.readSignedByte();
        }

    }

    private static class if_icmplt implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            int bd = toInt(operandStack.pop());
            int ad = toInt(operandStack.pop());
            if (ad < bd)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class if_acmpeq implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            JavaObject.JClassInstance jcia = ((JavaObject.JClassInstance) operandStack.pop());
            JavaObject.JClassInstance jcib = ((JavaObject.JClassInstance) operandStack.pop());
            int brTo = ctx.in.readU2();
            if ((jcia == null && jcib == null) || (!(jcia == null || jcib == null) && jcia.id == jcib.id))
                ctx.relativeJumpMinus3(brTo);
        }

    }

    private static class if_acmpne implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            JClassInstance jcia = ((JavaObject.JClassInstance) operandStack.pop());
            JClassInstance jcib = ((JavaObject.JClassInstance) operandStack.pop());
            int brTo = ctx.in.readU2();
            if (jcia != jcib)
                ctx.relativeJumpMinus3(brTo);
        }

    }

    private static class _checkcast_ implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            JClassInstance jcia = (JavaObject.JClassInstance) operandStack.peek();
            int read = ctx.in.readU2();
            String other = (String) ctx.constantPool[(int) ctx.constantPool[read]];
            if (ctx.logging) {
                ctx.log(2, jcia);
                ctx.log(2, other);
            }
            if (jcia != null)
                jcia.instanceOf(other, ctx.thread, (is) -> {
                    if (!is)
                        error(ctx.thread, "java/lang/ClassCastException", "can't cast " + jcia.toString() + " to " + other);
                }, ctx.ec);
        }

    }

    private static class lushr implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int bi = toInt(operandStack.pop());
            long lai = (long) (operandStack.pop());
            operandStack.push(lai >> bi);
        }

    }

    private static class if_icmpge implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            int bd = toInt(operandStack.pop());
            int ad = toInt(operandStack.pop());
            if (ad >= bd) {
                if (ctx.logging)
                    ctx.log(2, ad + " >= " + bd);
                ctx.relativeJumpMinus3(position);
            } else if (ctx.logging)
                ctx.log(2, ad + " < " + bd);
        }

    }

    private static class ior implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();
            if (a == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "1. number in int add");
            if (b == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "2. number in int add");
            operandStack.push((toInt(a)) | (toInt(b)));
        }

    }

    private static class if_icmpeq implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            int bd = toInt(operandStack.pop());
            int ad = toInt(operandStack.pop());
            if (ad == bd)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class d2f implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push((float) operandStack.pop());
        }

    }

    private static class dup_x1 implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();
            operandStack.push(a);
            operandStack.push(b);
            operandStack.push(a);
        }

    }

    private static class _2f implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push(((Number) operandStack.pop()).floatValue());
            if (ctx.logging)
                ctx.log(2, operandStack.peek());
        }

    }

    private static class fmul implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();
            if (a == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "1. number in float multiply");
            if (b == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "2. number in float multiply");
            operandStack.push((float) a * (float) b);
        }

    }

    private static class ladd implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();
            if (a == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "1. number in long add");
            if (b == null)
                error(ctx.thread, "java/lang/NullPointerException",
                        "2. number in long add");
            operandStack.push((long) a + (long) b);
        }

    }

    private static class lshl implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int b = (int) operandStack.pop();
            long a = (long) operandStack.pop();
            operandStack.push(a << b);
        }

    }

    private static class ifgt implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int position = ctx.in.readU2();
            Object pop = operandStack.pop();
            Number popnum = (Number) pop;
            int intValue = popnum.intValue();
            if (intValue > 0)
                ctx.relativeJumpMinus3(position);
        }

    }

    private static class irem implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            Object b = operandStack.pop();

            operandStack.push((toInt(b)) % (toInt(a)));
        }

    }

    private static class dup2 implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            Object a = operandStack.pop();
            if (a instanceof Long || a instanceof Double) {
                operandStack.push(a);
                operandStack.push(a);
                return;
            }
            Object b = operandStack.pop();
            operandStack.push(b);
            operandStack.push(a);
            operandStack.push(b);
            operandStack.push(a);
        }

    }

    private static class i2c implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push((char) toInt(operandStack.pop()));
        }
    }
    private static class i2b implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            operandStack.push((byte) toInt(operandStack.pop()));
        }
    }

    private static class lookupswitch implements BytecodeInstruction {

        @Override
        public void run(VmContext ctx, Deque<Object> operandStack, int instructionCode) throws IOException {
            int opcodeStart = (int) ctx.mcIn.pc-1;
            //ctx.in.skipPadding(4);
            int def = ctx.in.readInt();
            int n = ctx.in.readInt();
            int value = toInt(operandStack.pop());
            if (ctx.logging)
                ctx.log(2, "n=" + n + ",def=" + def + ",opcodeStart=" + opcodeStart);
            for (int j = 0; j < n; j++) {
                int match = ctx.in.readInt();
                int offset = ctx.in.readInt();
                if (match == value) {
                    ctx.absoluteJump(opcodeStart + offset);
                    if (ctx.logging)
                        ctx.log(4, match + " matched; jumped to " + opcodeStart + "+" + offset + "=" + (opcodeStart + offset));
                    return;
                }
            }
            ctx.absoluteJump(opcodeStart + def);
            if (ctx.logging)
                ctx.log(4, "doesn't matched anything; jumped to " + opcodeStart + "+" + def + "=" + (opcodeStart + def));
        }
    }
    public static String[] mnemonics = new String[256];
    public static BytecodeInstruction[] i = new BytecodeInstruction[256];

    static {
        i[0] = new nop();
        i[1] = new aconst_null();
        i[2] = i[3] = i[4] = i[5] = i[6] = i[7] = i[8] = new iconst_();
        i[9] = new lconst_0();
        i[10] = new lconst_1();
        i[11] = new fconst_();
        i[12] = i[13] = new fconst_();
        i[16] = new bipush();
        i[17] = new sipush();
        i[18] = i[19] = i[20] = new ldc();
        i[21] = i[22] = i[23] = i[24] = i[25] = new _load();
        i[26] = i[27] = i[28] = i[29] = new iload_();
        i[30] = i[31] = i[32] = i[33] = new lload_();
        i[34] = i[35] = i[36] = i[37] = new fload_();
        i[38] = i[39] = i[40] = i[41] = new dload_();
        i[42] = i[43] = i[44] = i[45] = new aload_();
        i[46] = i[50] = i[51] = i[52] = new _aload();
        i[54] = i[55] = i[56] = i[57] = i[58] = new _store();
        i[59] = i[60] = i[61] = i[62] = new istore_();
        i[63] = i[64] = i[65] = i[66] = new lstore_();
        i[67] = i[68] = i[69] = i[70] = new fstore_();
        i[71] = i[72] = i[73] = i[74] = new dstore_();
        i[75] = i[76] = i[77] = i[78] = i[79] = new astore_();
        i[79] = new _iastore_();
        i[83] = i[84] = new _astore();
        i[85] = new castore();
        i[87] = new pop();
        i[88] = new pop2();
        i[89] = new dup();
        i[90] = new dup_x1();
        i[92] = new dup2();
        i[95] = new swap();
        i[96] = new iadd();
        i[97] = new ladd();
        i[100] = new isub();
        i[104] = new imul();
        i[106] = new fmul();
        i[112] = new irem();
        i[116] = new ineg();
        i[120] = new ishl();
        i[121] = new lshl();
        i[122] = new ishr();
        i[124] = new iushr();
        i[125] = new lushr();
        i[126] = new iand();
        i[127] = new land();
        i[128] = new ior();
        i[130] = new ixor();
        i[132] = new iint();
        i[133] = new i2l();
        i[134] = new _2f();
        i[136] = i[139] = new _2i();
        i[141] = new _f2d_();
        i[144] = new d2f();
        i[146] = new i2c();
        i[145] = new i2b();
        i[148] = new lcmp();
        i[149] = i[150] = new fcmp_();
        i[153] = new ifeq();
        i[154] = new ifne();
        i[155] = new iflt();
        i[156] = new ifge();
        i[157] = new ifgt();
        i[158] = new ifle();
        i[159] = new if_icmpeq();
        i[160] = new if_icmpne();
        i[161] = new if_icmplt();
        i[162] = new if_icmpge();
        i[163] = new if_icmpgt();
        i[164] = new if_icmple();
        i[165] = new if_acmpeq();
        i[166] = new if_acmpne();
        i[167] = new Goto();
        i[171] = new lookupswitch();
        i[172] = i[173] = i[174] = i[175] = i[176] = new _return();
        i[177] = new Return();
        i[178] = new getstatic();
        i[179] = new putstatic();
        i[180] = new getfield();
        i[181] = new putfield();
        i[182] = i[183] = i[185] = new invoke_______();
        i[184] = new invokestatic();
        i[187] = new New();
        i[188] = new newarray();
        i[189] = new anewarray();
        i[190] = new arraylength();
        i[191] = new _athrow_();
        i[192] = new _checkcast_();
        i[193] = new Instanceof();
        i[194] = new monitorenter();
        i[195] = new monitorexit();
        i[196] = new wide();
        i[198] = new ifnull();
        i[199] = new ifnonnull();
        mnemonics[0] = "nop";
        mnemonics[1] = "aconst_null";
        mnemonics[2] = "iconst_m1(-1)";
        mnemonics[3] = "iconst_0";
        mnemonics[4] = "iconst_1";
        mnemonics[5] = "iconst_2";
        mnemonics[6] = "iconst_3";
        mnemonics[7] = "iconst_4";
        mnemonics[8] = "iconst_5";
        mnemonics[9] = "lconst_0";
        mnemonics[10] = "lconst_1";
        mnemonics[11] = "fconst_0";
        mnemonics[12] = "fconst_1";
        mnemonics[13] = "fconst_2";
        mnemonics[16] = "bipush";
        mnemonics[17] = "sipush";
        mnemonics[18] = "ldc";
        mnemonics[19] = "ldc_w";
        mnemonics[20] = "ldc2_w";
        mnemonics[21] = "iload";
        mnemonics[22] = "lload";
        mnemonics[23] = "fload";
        mnemonics[24] = "dload";
        mnemonics[25] = "aload";
        mnemonics[26] = "iload_0";
        mnemonics[27] = "iload_1";
        mnemonics[28] = "iload_2";
        mnemonics[29] = "iload_3";
        mnemonics[30] = "lload_0";
        mnemonics[31] = "lload_1";
        mnemonics[32] = "lload_2";
        mnemonics[33] = "lload_3";
        mnemonics[34] = "fload_0";
        mnemonics[35] = "fload_1";
        mnemonics[36] = "fload_2";
        mnemonics[37] = "fload_3";
        mnemonics[38] = "dload_0";
        mnemonics[39] = "dload_1";
        mnemonics[40] = "dload_2";
        mnemonics[41] = "dload_3";
        mnemonics[42] = "aload_0";
        mnemonics[43] = "aload_1";
        mnemonics[44] = "aload_2";
        mnemonics[45] = "aload_3";
        mnemonics[46] = "iaload";
        mnemonics[50] = "aaload";
        mnemonics[52] = "caload";
        mnemonics[54] = "istore";
        mnemonics[55] = "lstore";
        mnemonics[56] = "fstore";
        mnemonics[57] = "dstore";
        mnemonics[58] = "astore";
        mnemonics[59] = "istore_0";
        mnemonics[60] = "istore_1";
        mnemonics[61] = "istore_2";
        mnemonics[62] = "istore_3";
        mnemonics[63] = "lstore_0";
        mnemonics[64] = "lstore_1";
        mnemonics[65] = "lstore_2";
        mnemonics[66] = "lstore_3";
        mnemonics[67] = "fstore_0";
        mnemonics[68] = "fstore_1";
        mnemonics[69] = "fstore_2";
        mnemonics[70] = "fstore_3";
        mnemonics[71] = "dstore_0";
        mnemonics[72] = "dstore_1";
        mnemonics[73] = "dstore_2";
        mnemonics[74] = "dstore_3";
        mnemonics[75] = "astore_0";
        mnemonics[76] = "astore_1";
        mnemonics[77] = "astore_2";
        mnemonics[78] = "astore_3";
        mnemonics[79] = "iastore";
        mnemonics[83] = "aastore";
        mnemonics[84] = "bastore";
        mnemonics[85] = "castore";
        mnemonics[87] = "pop";
        mnemonics[88] = "pop2";
        mnemonics[89] = "dup";
        mnemonics[90] = "dup_x1";
        mnemonics[92] = "dup2";
        mnemonics[95] = "swap";
        mnemonics[96] = "iadd";
        mnemonics[97] = "ladd";
        mnemonics[100] = "isub";
        mnemonics[104] = "imul";
        mnemonics[106] = "fmul";
        mnemonics[112] = "irem";
        mnemonics[116] = "ineg";
        mnemonics[120] = "ishl";
        mnemonics[121] = "lshl";
        mnemonics[122] = "ishr";
        mnemonics[124] = "iushr";
        mnemonics[125] = "lushr";
        mnemonics[126] = "iand";
        mnemonics[127] = "land";
        mnemonics[128] = "ior";
        mnemonics[130] = "ixor";
        mnemonics[132] = "iint";
        mnemonics[133] = "i2l";
        mnemonics[134] = "i2f";
        mnemonics[136] = "l2i";
        mnemonics[139] = "f2i";
        mnemonics[141] = "f2d";
        mnemonics[144] = "d2f";
        mnemonics[145] = "i2b";
        mnemonics[146] = "i2c";
        mnemonics[148] = "lcmp";
        mnemonics[149] = "fcmpl";
        mnemonics[150] = "fcmpg";
        mnemonics[153] = "ifeq";
        mnemonics[154] = "ifne";
        mnemonics[155] = "iflt";
        mnemonics[156] = "ifge";
        mnemonics[158] = "ifle";
        mnemonics[159] = "if_icmpeq";
        mnemonics[160] = "if_icmpne";
        mnemonics[161] = "if_icmplt";
        mnemonics[162] = "if_icmpge";
        mnemonics[163] = "if_icmpgt";
        mnemonics[164] = "if_icmple";
        mnemonics[165] = "if_acmpeq";
        mnemonics[166] = "if_acmpne";
        mnemonics[167] = "goto";
        mnemonics[171] = "lookupswitch";
        mnemonics[172] = "ireturn";
        mnemonics[173] = "lreturn";
        mnemonics[175] = "dreturn";
        mnemonics[176] = "areturn";
        mnemonics[177] = "return";
        mnemonics[178] = "getstatic";
        mnemonics[179] = "putstatic";
        mnemonics[180] = "getfield";
        mnemonics[181] = "putfield";
        mnemonics[182] = "invokevirtual";
        mnemonics[183] = "invokespecial";
        mnemonics[184] = "invokestatic";
        mnemonics[185] = "invokeinterface";
        mnemonics[187] = "new";
        mnemonics[188] = "newarray";
        mnemonics[189] = "anewarray";
        mnemonics[190] = "arraylength";
        mnemonics[191] = "athrow";
        mnemonics[192] = "checkcast";
        mnemonics[193] = "instanceof";
        mnemonics[194] = "monitorenter";
        mnemonics[195] = "monitorexit";
        mnemonics[196] = "wide";
        mnemonics[198] = "ifnull";
        mnemonics[199] = "ifnonnull";
    }

}
