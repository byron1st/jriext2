package edu.kaist.salab.byron1st.jriext2.inst;

import edu.kaist.salab.byron1st.jriext2.Symbols;
import edu.kaist.salab.byron1st.jriext2.ettype.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Created by util on 2016. 1. 8..
 */
class JRiExt2MethodVisitor extends AdviceAdapter implements Opcodes, Symbols {
    private static ETTAttributeMethod getExecutionTime = new ETTAttributeMethod("time", "java/lang/System", "nanoTime", "()J", "J", false);

    private ETType ettype;

    JRiExt2MethodVisitor(MethodVisitor mv, int access, String name, String desc, ETType ettype) {
        super(ASM5, mv, access, name, desc);
        this.ettype = ettype;
    }

    @Override
    protected void onMethodEnter() {
        // Native 또는 Abstract 메소드가 아니고,
        // 메소드 도입부에서 로깅해야 할 경우
        if(isFeasible(methodAccess) && ettype.isEnter()) {
            insertLoggingCode();
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        // Native 또는 Abstract 메소드가 아니고,
        // 메소드 마지막에 로깅해야 할 경우
        if(isFeasible(methodAccess) && !ettype.isEnter()) {
            if (ettype.getAttributeList().size() != 0 && ettype.getAttributeList().get(0) instanceof ETTAttributeReturn) {
                insertLoggingCodeReturnType();
            } else {
                insertLoggingCode();
            }
        }
        mv.visitEnd();
    }

    private void insertLoggingCode() {
        // 메타 정보 (메소드 도입부 기록 여부, Execution trace type 이름, 실행 시간, 객체 hash code 값) 기록
        logBeginPrint();
        if(ettype.isEnter()) {
            logStringValue(ENTER);
        } else {
            logStringValue(EXIT);
        }
        logDelimiter();
        logStringValue(ettype.getTypeName());
        logDelimiter();
        log(getExecutionTime, true);
        logDelimiter();
        logObjectId();

        // 추가 정보 (Attribute) 기록
        for (ETTAttribute ettattribute : ettype.getAttributeList()) {
            if(ettattribute instanceof ETTAttributeMethod) logMethod((ETTAttributeMethod) ettattribute);
            else if (ettattribute instanceof ETTAttributeField) logField((ETTAttributeField) ettattribute);
            else if (ettattribute instanceof ETTAttributeParameter) logParameter((ETTAttributeParameter) ettattribute);
        }

        logEndPrint();
        mv.visitMethodInsn(INVOKESTATIC, "java/util/JRiExtLogger", "recordExecutionTrace", "(Ljava/lang/String;)V", false);
    }

    private void insertLoggingCodeReturnType() {
        // StringBuilder 객체를 생성하기 전에, Return 하려고 준비하던 값을 일단 1번 위치에 저장.
        mv.visitVarInsn(ASTORE, 1);

        // 메타 정보 (메소드 도입부 기록 여부, Execution trace type 이름, 실행 시간, 객체 hash code 값) 기록
        logBeginPrint();
        if(ettype.isEnter()) {
            logStringValue(ENTER);
        } else {
            logStringValue(EXIT);
        }
        logDelimiter();
        logStringValue(ettype.getTypeName());
        logDelimiter();
        log(getExecutionTime, true);
        logDelimiter();
        logObjectId();

        // 추가 정보 (Attribute) 기록
        // Return type은 반드시 Return type ETTAttribute만 가져야 함.
        for (ETTAttribute ettattribute : ettype.getAttributeList()) {
            logReturn((ETTAttributeReturn) ettattribute);
        }

        logEndPrint();
        mv.visitMethodInsn(INVOKESTATIC, "java/util/JRiExtLogger", "recordExecutionTrace", "(Ljava/lang/String;)V", false);

        // 기록 작업이 모두 끝나고, 앞서 저장했던 값을 로드하여 Return 준비.
        mv.visitVarInsn(ALOAD, 1);
    }

    private void logMethod(ETTAttributeMethod ettattribute) {
        logDelimiter();
        log(ettattribute, true);
    }

    private void logField(ETTAttributeField ettattribute) {
        Label ifFieldNull = new Label();
        Label endLabel = new Label();

        // ALOAD 0은 this를 호출.
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ettype.getClassName(), ettattribute.getFieldName(), ettattribute.getClassName());

        // field가 null인지 체크.
        mv.visitJumpInsn(IFNULL, ifFieldNull);

        logDelimiter();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ettype.getClassName(), ettattribute.getFieldName(), ettattribute.getClassName());

        if(ettattribute.getNextMethod() != null) {
            // primitive type이 아닐 경우, 반드시 method chain이 있어야 함.
            log(ettattribute.getNextMethod(), false);
        } else {
            // primitive type 이면 바로 기록.
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + ettattribute.getClassName() + ")Ljava/lang/StringBuilder;", false);
        }

        // null이 아닐 경우 기록이 종료되었으므로, endLabel로 jump.
        mv.visitJumpInsn(GOTO, endLabel);

        // field가 null일 경우 여기로 jump.
        mv.visitLabel(ifFieldNull);
        logStringValue(DDELIM + "null");

        // field가 null이 아닐 경우,
        // 여기로 바로 jump 시켜서 null일 경우 하는 작업을 회피.
        mv.visitLabel(endLabel);
    }

    private void logParameter(ETTAttributeParameter ettattribute) {
        //static 이면 i = 0, 아니면 1 (this가 0이라서)
        int ith = ettattribute.getIndex();
        int opcode = Type.getArgumentTypes(ettype.getMethodDesc())[ith].getOpcode(ILOAD);
        int off = 1;
        if(isStatic()) off = 0;

        logDelimiter();
        mv.visitVarInsn(opcode, off + ith);
        if(ettattribute.getNextMethod() != null) log(ettattribute.getNextMethod(), false);
        else mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + ettattribute.getClassName() + ")Ljava/lang/StringBuilder;", false);
    }

    private void logReturn(ETTAttributeReturn ettattribute) {
        logDelimiter();
        mv.visitVarInsn(ALOAD, 1);
        if(ettattribute.getNextMethod() != null) log(ettattribute.getNextMethod(), false);
        else mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + ettattribute.getClassName() + ")Ljava/lang/StringBuilder;", false);
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
        if(isStatic()) {
            logStringValue(STATIC + ettype.getClassName() + ">");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
        }
    }

    private void logStringValue(String value) {
        mv.visitLdcInsn(value);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
    }

    /************ System.out.print를 시작 ************/

    private void logBeginPrint() {
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
    }

    private void logEndPrint() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    }

    /**
     * 해당 메소드가 Native 메소드이거나 Abstract 메소드인지 확인한다.
     * 해당 될 경우, false, 그렇지 않으면 true를 출력한다.
     * @param access 메소드의 접근 정보. ASM5 Opcodes 값이다.
     * @return Native 메소드이거나 Abstract 메소드인지 확인한 boolean 값.
     */
    private boolean isFeasible(int access) {
        return !((access & ACC_NATIVE) != 0)
                || ((access & ACC_ABSTRACT) != 0);
    }

    private boolean isStatic() {
        return (methodAccess & ACC_STATIC) != 0;
    }
}
