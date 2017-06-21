package edu.kaist.salab.byron1st.jriext2.inst;

import edu.kaist.salab.byron1st.jriext2.ettype.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Created by byron1st on 2016. 1. 8..
 */
class JRiExt2MethodVisitor extends AdviceAdapter implements Opcodes, Symbols {
    private static ETTAttributeMethod getExecutionTime = new ETTAttributeMethod("time", false, "java/lang/System", "nanoTime()J");

    private ETType ettype;

    JRiExt2MethodVisitor(MethodVisitor mv, int access, String name, String desc, ETType ettype) {
        super(ASM5, mv, access, name, desc);
        this.ettype = ettype;
    }

    @Override
    protected void onMethodEnter() {
        if(isFeasible(methodAccess) && ettype.isEnter()) {
            insertLoggingCode();
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        if(isFeasible(methodAccess) && !ettype.isEnter()) {
            insertLoggingCode();
        }
        mv.visitEnd();
    }

    private void insertLoggingCode() {
        Label ifSystemOutNull = new Label();
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitJumpInsn(IFNULL, ifSystemOutNull);
        logBeginPrint();

        if(ettype.isEnter()) logStringValue(ENTER);
        else logStringValue(EXIT);

        logStringValue(ettype.getTypeName());
        logDelimiter();
        log(getExecutionTime, true);
        logDelimiter();
        logObjectId();
        logEndPrint();

        for (ETTAttribute ettattribute : ettype.getAttributeList()) {
            if(ettattribute instanceof ETTAttributeMethod) logMethod((ETTAttributeMethod) ettattribute);
            else if (ettattribute instanceof ETTAttributeField) logField((ETTAttributeField) ettattribute);
            else if (ettattribute instanceof ETTAttributeParameter) logParameter((ETTAttributeParameter) ettattribute);
            else if (ettattribute instanceof ETTAttributeReturn) logReturn((ETTAttributeReturn) ettattribute);
        }

        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "()V", false);
        mv.visitLabel(ifSystemOutNull);
    }

    private void logMethod(ETTAttributeMethod ettattribute) {
        logBeginPrint();
        logDelimiter();
        log(ettattribute, true);
        logEndPrint();
    }

    private void logField(ETTAttributeField ettattribute) {
        Label ifFieldNull = new Label();
        Label endLabel = new Label();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ettype.getClassName(), ettattribute.getFieldName(), ettattribute.getClassName());
        mv.visitJumpInsn(IFNULL, ifFieldNull);
        logBeginPrint();
        logDelimiter();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ettype.getClassName(), ettattribute.getFieldName(), ettattribute.getClassName());

        if(ettattribute.getNextMethod() != null) log(ettattribute.getNextMethod(), false);
        else mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + ettattribute.getClassName() + ")Ljava/lang/StringBuilder;", false);

        logEndPrint();
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(ifFieldNull);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(DDELIM + "null");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
        mv.visitLabel(endLabel);
    }

    private void logParameter(ETTAttributeParameter ettattribute) {
        //static 이면 i = 0, 아니면 1 (this가 0이라서)
        int ith = ettattribute.getIndex();
        int opcode = Type.getArgumentTypes(ettype.getMethodDesc())[ith].getOpcode(ILOAD);
        int off = 1;
        if(isStatic()) off = 0;

        logBeginPrint();
        logDelimiter();
        mv.visitVarInsn(opcode, off + ith);
        if(ettattribute.getNextMethod() != null) log(ettattribute.getNextMethod(), false);
        else mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + ettattribute.getClassName() + ")Ljava/lang/StringBuilder;", false);
        logEndPrint();
    }

    private void logReturn(ETTAttributeReturn ettattribute) {
        mv.visitVarInsn(ASTORE, 1);
        logBeginPrint();
        logDelimiter();
        mv.visitVarInsn(ALOAD, 1);
        if(ettattribute.getNextMethod() != null) log(ettattribute.getNextMethod(), false);
        else mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + ettattribute.getClassName() + ")Ljava/lang/StringBuilder;", false);
        logEndPrint();
        mv.visitVarInsn(ALOAD, 1);
    }

    /************ System.out.print를 시작해 놓고 쓰는 log들 ************/

    private void log(ETTAttributeMethod ettattribute, boolean doesCheckALOAD) {
        if(doesCheckALOAD
                && ettattribute.getClassName().equals(ettype.getClassName())
                && ettattribute.isVirtual())
            mv.visitVarInsn(ALOAD, 0);

        String returnType;
        while(true) {
            int code;
            if(ettattribute.isVirtual()) code = INVOKEVIRTUAL;
            else code = INVOKESTATIC;
            mv.visitMethodInsn(code, ettattribute.getClassName(), ettattribute.getMethodName(), ettattribute.getMethodDesc(), false);
            if(ettattribute.isFinal()) {
                returnType = ettattribute.getReturnType();
                break;
            } else ettattribute = ettattribute.getNextMethod();
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + returnType + ")Ljava/lang/StringBuilder;", false);
    }

    private void logDelimiter() {
        mv.visitLdcInsn(DDELIM);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
    }

    private void logObjectId() {
        if(isStatic()) logStringValue(STATIC + ettype.getClassName() + ">");
        else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
        }
    }

    private void logStringValue(String value) {
        mv.visitLdcInsn(value);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
    }

    private void logBeginPrint() {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
    }

    private void logEndPrint() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
    }

    private boolean isFeasible(int access) {
        return !((access & ACC_NATIVE) != 0)
                || ((access & ACC_ABSTRACT) != 0);
    }

    private boolean isStatic() {
        return (methodAccess & ACC_STATIC) != 0;
    }
}
