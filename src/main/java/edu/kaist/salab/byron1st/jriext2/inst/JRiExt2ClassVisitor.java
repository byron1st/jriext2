package edu.kaist.salab.byron1st.jriext2.inst;

import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by byron1st on 2016. 1. 8..
 */
class JRiExt2ClassVisitor extends ClassVisitor implements Opcodes{
    private ETType ettype;
    private boolean isDebugMode;

    JRiExt2ClassVisitor(ClassVisitor cv, ETType ettype) {
        super(ASM5, cv);
        this.ettype = ettype;
        this.isDebugMode = isDebugMode;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if(ettype.getMethodName().equals(name) && ettype.getMethodDesc().equals(desc)) {
            return new JRiExt2MethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc, ettype);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
