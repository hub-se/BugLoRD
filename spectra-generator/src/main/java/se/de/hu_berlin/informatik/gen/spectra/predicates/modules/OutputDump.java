package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import org.objectweb.asm.*;

public class OutputDump implements Opcodes {

    public static byte[] dump() throws Exception {

        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        classWriter.visit(Opcodes.V1_8, ACC_PUBLIC | ACC_SUPER, "Output", null, "java/lang/Object", null);

        classWriter.visitSource("Output.java", null);

        {
            fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_STATIC, "Predicates", "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/Integer;LPredicate;>;", null);
            fieldVisitor.visitEnd();
        }
        {
            fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_STATIC, "nextPredicateNumber", "I", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(9, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(10, label1);
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("this", "LOutput;", null, label0, label2, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "addPredicate", "(LPredicate;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(13, label0);
            methodVisitor.visitFieldInsn(GETSTATIC, "Output", "Predicates", "Ljava/util/Map;");
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "Predicate", "id", "I");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            methodVisitor.visitInsn(POP);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(14, label1);
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("newPredicate", "LPredicate;", null, label0, label2, 0);
            methodVisitor.visitMaxs(3, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "triggerPredicate", "(I)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(17, label0);
            methodVisitor.visitFieldInsn(GETSTATIC, "Output", "Predicates", "Ljava/util/Map;");
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
            methodVisitor.visitTypeInsn(CHECKCAST, "Predicate");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitFieldInsn(GETFIELD, "Predicate", "count", "I");
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitFieldInsn(PUTFIELD, "Predicate", "count", "I");
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(18, label1);
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("id", "I", null, label0, label2, 0);
            methodVisitor.visitMaxs(3, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "outputPredicates", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(21, label0);
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ASTORE, 0);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(22, label1);
            methodVisitor.visitFieldInsn(GETSTATIC, "Output", "Predicates", "Ljava/util/Map;");
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "keySet", "()Ljava/util/Set;", true);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;", true);
            methodVisitor.visitVarInsn(ASTORE, 1);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitFrame(Opcodes.F_NEW, 2, new Object[]{"java/lang/StringBuilder", "java/util/Iterator"}, 0, new Object[]{});
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
            Label label3 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label3);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
            methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            methodVisitor.visitVarInsn(ASTORE, 2);
            Label label4 = new Label();
            methodVisitor.visitLabel(label4);
            methodVisitor.visitLineNumber(23, label4);
            methodVisitor.visitFieldInsn(GETSTATIC, "Output", "Predicates", "Ljava/util/Map;");
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
            methodVisitor.visitTypeInsn(CHECKCAST, "Predicate");
            methodVisitor.visitVarInsn(ASTORE, 3);
            Label label5 = new Label();
            methodVisitor.visitLabel(label5);
            methodVisitor.visitLineNumber(24, label5);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitFieldInsn(GETFIELD, "Predicate", "count", "I");
            Label label6 = new Label();
            methodVisitor.visitJumpInsn(IFNE, label6);
            Label label7 = new Label();
            methodVisitor.visitLabel(label7);
            methodVisitor.visitLineNumber(25, label7);
            methodVisitor.visitJumpInsn(GOTO, label2);
            methodVisitor.visitLabel(label6);
            methodVisitor.visitLineNumber(26, label6);
            methodVisitor.visitFrame(Opcodes.F_NEW, 4, new Object[]{"java/lang/StringBuilder", "java/util/Iterator", "java/lang/Integer", "Predicate"}, 0, new Object[]{});
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitLdcInsn("(");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn(",");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label8 = new Label();
            methodVisitor.visitLabel(label8);
            methodVisitor.visitLineNumber(27, label8);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitFieldInsn(GETFIELD, "Predicate", "description", "Ljava/lang/String;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn(",");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label9 = new Label();
            methodVisitor.visitLabel(label9);
            methodVisitor.visitLineNumber(28, label9);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitFieldInsn(GETFIELD, "Predicate", "file", "Ljava/lang/String;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn(":");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitFieldInsn(GETFIELD, "Predicate", "linenumber", "I");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitLdcInsn(")");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitInsn(POP);
            Label label10 = new Label();
            methodVisitor.visitLabel(label10);
            methodVisitor.visitLineNumber(29, label10);
            methodVisitor.visitJumpInsn(GOTO, label2);
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(30, label3);
            methodVisitor.visitFrame(Opcodes.F_NEW, 1, new Object[]{"java/lang/StringBuilder"}, 0, new Object[]{});
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
            Label label11 = new Label();
            methodVisitor.visitLabel(label11);
            methodVisitor.visitLineNumber(31, label11);
            methodVisitor.visitInsn(RETURN);
            Label label12 = new Label();
            methodVisitor.visitLabel(label12);
            methodVisitor.visitLocalVariable("entry", "LPredicate;", null, label5, label10, 3);
            methodVisitor.visitLocalVariable("i", "Ljava/lang/Integer;", null, label4, label10, 2);
            methodVisitor.visitLocalVariable("string", "Ljava/lang/StringBuilder;", null, label1, label12, 0);
            methodVisitor.visitMaxs(2, 4);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(6, label0);
            methodVisitor.visitTypeInsn(NEW, "java/util/HashMap");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
            methodVisitor.visitFieldInsn(PUTSTATIC, "Output", "Predicates", "Ljava/util/Map;");
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(2, 0);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}

