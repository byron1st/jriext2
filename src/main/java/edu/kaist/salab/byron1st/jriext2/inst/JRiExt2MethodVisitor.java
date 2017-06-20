package edu.kaist.salab.byron1st.jriext2.inst;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Created by byron1st on 2016. 1. 8..
 */
class JRiExt2MethodVisitor extends AdviceAdapter implements Opcodes{
//    private static final String ENTER = "+E+";
//    private static final String EXIT = "+X+";
//    private static final String DDELIM = ",";

    private static MonitoringValueMethod getThreadID = new MonitoringValueMethod(false, "java/lang/Thread", "currentThread()Ljava/lang/Thread;", "threadID");
    private static MonitoringValueMethod getThreadName = new MonitoringValueMethod(false, "java/lang/Thread", "currentThread()Ljava/lang/Thread;", "threadName");
    private static MonitoringValueMethod getExecutionTime = new MonitoringValueMethod(false, "java/lang/System", "nanoTime()J", "time");
    static {
        getThreadID.setNextMethod(new MonitoringValueMethod(true, "java/lang/Object", "hashCode()I", "threadID"));
        getThreadName.setNextMethod(new MonitoringValueMethod(true, "java/lang/Thread", "getName()Ljava/lang/String;", "threadName"));
    }

    private MonitoringUnit monitoringUnit;

    JRiExt2MethodVisitor(MethodVisitor mv, int access, String name, String desc, MonitoringUnit monitoringUnit) {
        super(ASM5, mv, access, name, desc);
        this.monitoringUnit = monitoringUnit;
    }

    @Override
    protected void onMethodEnter() {
        if(isFeasible(methodAccess) && monitoringUnit.isEnter()) insertLoggingCode();
    }

    @Override
    protected void onMethodExit(int opcode) {
        if(isFeasible(methodAccess) && !monitoringUnit.isEnter()) insertLoggingCode();
        mv.visitEnd();
    }

    private void insertLoggingCode() {
        Label ifSystemOutNull = new Label();
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitJumpInsn(IFNULL, ifSystemOutNull);
        logBeginPrint();

        if(monitoringUnit.isEnter()) logStringValue(InstApp.ENTER);
        else logStringValue(InstApp.EXIT);

        logStringValue(monitoringUnit.getMuID());
        logDelimiter();
        log(getExecutionTime, true);
        logDelimiter();
        log(getThreadName, true);
        logDelimiter();
        log(getThreadID, true);
        logDelimiter();
        logObjectId();
        logDelimiter();
        logStringValue(monitoringUnit.getClassName() + InstApp.DDELIM + monitoringUnit.getMethodName() + monitoringUnit.getMethodDesc());
        logEndPrint();

        for (MonitoringValue monitoringValue : monitoringUnit.getMonitoringValues()) {
            if(monitoringValue instanceof MonitoringValueMethod) logMethod((MonitoringValueMethod) monitoringValue);
            else if (monitoringValue instanceof MonitoringValueField) logField((MonitoringValueField) monitoringValue);
            else if (monitoringValue instanceof MonitoringValueParameter) logParameter((MonitoringValueParameter) monitoringValue);
            else if (monitoringValue instanceof MonitoringValueReturn) logReturn((MonitoringValueReturn) monitoringValue);
        }

        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "()V", false);
        mv.visitLabel(ifSystemOutNull);
    }

    private void logMethod(MonitoringValueMethod monitoringValue) {
        logBeginPrint();
        logDelimiter();
        log(monitoringValue, true);
        logEndPrint();
    }

    private void logField(MonitoringValueField monitoringValue) {
        Label ifFieldNull = new Label();
        Label endLabel = new Label();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, monitoringUnit.getClassName(), monitoringValue.getFieldName(), monitoringValue.getClassName());
        mv.visitJumpInsn(IFNULL, ifFieldNull);
        logBeginPrint();
        logDelimiter();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, monitoringUnit.getClassName(), monitoringValue.getFieldName(), monitoringValue.getClassName());

        if(monitoringValue.getNextMethod() != null) log(monitoringValue.getNextMethod(), false);
        else mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + monitoringValue.getClassName() + ")Ljava/lang/StringBuilder;", false);

        logEndPrint();
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(ifFieldNull);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(InstApp.DDELIM + "null");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
        mv.visitLabel(endLabel);
    }

    private void logParameter(MonitoringValueParameter monitoringValue) {
        //static 이면 i = 0, 아니면 1 (this가 0이라서)
        int ith = monitoringValue.getIndex();
        int opcode = Type.getArgumentTypes(monitoringUnit.getMethodDesc())[ith].getOpcode(ILOAD);
        int off = 1;
        if(isStatic()) off = 0;

        logBeginPrint();
        logDelimiter();
        mv.visitVarInsn(opcode, off + ith);
        if(monitoringValue.getNextMethod() != null) log(monitoringValue.getNextMethod(), false);
        else mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + monitoringValue.getClassName() + ")Ljava/lang/StringBuilder;", false);
        logEndPrint();
    }

    private void logReturn(MonitoringValueReturn monitoringValue) {
        mv.visitVarInsn(ASTORE, 1);
        logBeginPrint();
        logDelimiter();
        mv.visitVarInsn(ALOAD, 1);
        if(monitoringValue.getNextMethod() != null) log(monitoringValue.getNextMethod(), false);
        else mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + monitoringValue.getClassName() + ")Ljava/lang/StringBuilder;", false);
        logEndPrint();
        mv.visitVarInsn(ALOAD, 1);
    }

    /************ System.out.print를 시작해 놓고 쓰는 log들 ************/

    private void log(MonitoringValueMethod monitoringValue, boolean doesCheckALOAD) {
        if(doesCheckALOAD
                && monitoringValue.getClassName().equals(monitoringUnit.getClassName())
                && monitoringValue.isVirtual())
            mv.visitVarInsn(ALOAD, 0);

        String returnType;
        while(true) {
            int code;
            if(monitoringValue.isVirtual()) code = INVOKEVIRTUAL;
            else code = INVOKESTATIC;
            mv.visitMethodInsn(code, monitoringValue.getClassName(), monitoringValue.getMethodName(), monitoringValue.getMethodDesc(), false);
            if(monitoringValue.isFinal()) {
                returnType = monitoringValue.getReturnType();
                break;
            } else monitoringValue = monitoringValue.getNextMethod();
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + returnType + ")Ljava/lang/StringBuilder;", false);
    }

    private void logDelimiter() {
        mv.visitLdcInsn(InstApp.DDELIM);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
    }

    private void logObjectId() {
        if(isStatic()) logStringValue(InstApp.STATIC + monitoringUnit.getClassName() + ">");
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
