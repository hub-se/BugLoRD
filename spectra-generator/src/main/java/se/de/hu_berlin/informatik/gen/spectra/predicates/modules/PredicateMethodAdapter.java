package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import com.google.common.collect.Sets;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.*;

class PredicateMethodAdapter extends MethodVisitor implements Opcodes {

    //private static final Set<Integer> COMPARISONS = Sets.newHashSet(IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE);
    private static final Set<Integer> COMPARISONS = Sets.newHashSet(IFEQ, IFLT, IFGT);
    private final ArrayList<Predicate> predicates = new ArrayList<>();
    private final ArrayList<Label> myLabel = new ArrayList<>();
    private final HashMap<Integer, String> variableNames = new HashMap<>();
    private final String fileName;
    private final String OutputClass = Type.getInternalName(Output.class);
    public GeneratorAdapter ga;
    public AnalyzerAdapter aa;
    private int currentLine;

    public PredicateMethodAdapter(MethodVisitor mv, String fileName, String name) {
        super(ASM5, mv);
        this.fileName = fileName;
        //System.out.println("Method: " + name);
    }

    public void visitCode() {
        mv.visitCode();
        //System.out.println("File: " + this.fileName);
    }

    public void visitLineNumber(int line, Label start) {
        //System.out.println("LineNumber: " + line);
        this.currentLine = line;
        mv.visitLineNumber(line, start);
    }

    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var); //original instr

        switch (opcode) {
            case ISTORE:
                CreateComparePredicates(var, INTEGER);
                break;
            case LSTORE:
                CreateComparePredicates(var, LONG);
                break;
            case FSTORE:
                CreateComparePredicates(var, FLOAT);
                break;
            case DSTORE:
                CreateComparePredicates(var, DOUBLE);
                break;
            case ASTORE:
                CreateNullPredicates(var);
        }
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {

        if (this.myLabel.contains(label)) {
            mv.visitJumpInsn(opcode, label); //original instr
            return;
        }

        switch (opcode) {
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE://one stack instr
                ga.dup();
                break;
            case GOTO:
            case JSR: //subroutine
            case IFNULL: //we should find a way to handle object nullness
            case IFNONNULL:
                mv.visitJumpInsn(opcode, label); //original instr
                //not sure how to handle yet
                return;
            default: //two stack instr
                ga.dup2();//TODO handle long and double
        }

        Predicate truePredicate = new Predicate("true", this.currentLine, this.fileName);
        Predicate falsePredicate = new Predicate("false", this.currentLine, this.fileName);
        createTrueFalseTrigger(truePredicate, falsePredicate, opcode);


        mv.visitJumpInsn(opcode, label); //original instr
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        Type type = Type.getReturnType(descriptor);
        switch (type.getSort()) {
            case Type.VOID:
                break; //we return null
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
            case Type.FLOAT:
            case Type.LONG:
            case Type.DOUBLE:
                insertZeroComparePredicates(name, type);
            default:
                break;
        }


    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        mv.visitLocalVariable(name, desc, signature, start, end, index);
        this.variableNames.put(index, name);
    }

    @Override
    public void visitEnd() {
        mv.visitEnd();
        this.predicates.forEach((predicate) -> {
            if (predicate.description == null) {
                predicate.description = this.variableNames.get(predicate.firstVariableId) + predicate.comparisonType + this.variableNames.get(predicate.secondVariableId);
            }
            Output.addPredicate(predicate);
        });
    }

    private void CreateNullPredicates(int var) {

        mv.visitVarInsn(ALOAD, var);

        Predicate truePredicate = new Predicate(this.currentLine, this.fileName, " was null.", var);

        this.predicates.add(truePredicate);

        Label label1 = ga.newLabel();
        Label label2 = ga.newLabel();

        ga.ifNonNull(label1);

        ga.visitLabel(label2);
        ga.push(truePredicate.id);
        mv.visitMethodInsn(INVOKESTATIC, OutputClass, "triggerPredicate", "(I)V", false);
        ga.visitJumpInsn(GOTO, label1);

        ga.visitLabel(label1);
    }

    private void createTrueFalseTrigger(Predicate truePredicate, Predicate falsePredicate, int jumpType) {
        this.predicates.add(truePredicate);
        this.predicates.add(falsePredicate);

        Label label1 = ga.newLabel();
        Label label2 = ga.newLabel();
        Label label3 = ga.newLabel();
        mv.visitJumpInsn(jumpType, label1);

        ga.visitLabel(label2);
        ga.push(truePredicate.id);
        mv.visitMethodInsn(INVOKESTATIC, OutputClass, "triggerPredicate", "(I)V", false);
        ga.visitJumpInsn(GOTO, label3);

        ga.visitLabel(label1);
        ga.push(falsePredicate.id);
        mv.visitMethodInsn(INVOKESTATIC, OutputClass, "triggerPredicate", "(I)V", false);

        ga.visitLabel(label3);
    }

    private void CreateComparePredicates(int var, int type) {
        List<?> locals = aa.locals;
        if (locals == null)
            return;
        for (int i = 0; i < locals.size(); i++) {
            Object local = locals.get(i);
            if (local == INTEGER || local == FLOAT || local == DOUBLE || local == LONG)
                CreatePredicate(var, i, type, (Integer) local);
        }
    }

    private void insertZeroComparePredicates(String name, Type typeOnStack) {
        COMPARISONS.forEach((comparison) -> {
            Predicate predicate = new Predicate(this.currentLine, this.fileName, comparison, name);
            Output.addPredicate(predicate);
            this.predicates.add(predicate);

            int numSlots = this.aa.stack != null ? this.aa.stack.size() + typeOnStack.getSize() : typeOnStack.getSize();
            Object[] newStack = new Object[numSlots + 10];
//            Arrays.fill(newStack,Opcodes.INTEGER);
            if (aa.stack != null && aa.stack.size() < 2)
                ga.visitFrame(F_NEW, 0, new Object[0], numSlots + 10, newStack);

            if (typeOnStack.getSize() == 2) {
                ga.dup2();
            } else ga.dup();
            ga.cast(typeOnStack, Type.INT_TYPE);

            Label label1 = new Label();
            this.myLabel.add(label1);
            ga.ifZCmp(comparison, label1);
//            Label label2 = new Label();
//            mv.visitLabel(label2);
            ga.push(predicate.id);
            mv.visitMethodInsn(INVOKESTATIC, OutputClass, "triggerPredicate", "(I)V", false);
            //ga.goTo(label1);
            mv.visitLabel(label1);
        });

    }

    private void CreatePredicate(int firstVariableId, int secondVariableId, int firstVariableType, int secondVariableType) {
        COMPARISONS.forEach((comparison) -> {
            Predicate predicate = new Predicate(this.currentLine, this.fileName, firstVariableId, secondVariableId, comparison);
            predicates.add(predicate);
            insertComparePredicateCountStatement(predicate.id, firstVariableId, secondVariableId, firstVariableType, secondVariableType, comparison);
        });
    }

    private void insertPrintVar(int var) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(ILOAD, var);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
    }

    private void insertComparePredicateCountStatement(int id, int firstVariableId, int secondVariableId,
                                                      int firstVariableType, int secondVariableType, Integer comparison) {

        Label label1 = ga.newLabel();
        this.myLabel.add(label1);
        Label label2 = ga.newLabel();

        mv.visitVarInsn(getTypefromStackType(firstVariableType).getOpcode(Opcodes.ILOAD), firstVariableId);
        mv.visitVarInsn(getTypefromStackType(secondVariableType).getOpcode(Opcodes.ILOAD), secondVariableId);

        int numSlots = this.aa.stack != null ? this.aa.stack.size() + 4 : 4;
        Object[] newStack = new Integer[numSlots];
        //Arrays.fill(newStack,Opcodes.INTEGER);
        ga.visitFrame(F_NEW, 0, new Integer[0], numSlots, newStack); //fake the stack with a min size

        ga.cast(getTypefromStackType(secondVariableType), getTypefromStackType(firstVariableType));

        ga.ifCmp(getTypefromStackType(firstVariableType), comparison, label1);

        mv.visitLabel(label2);
        ga.push(id);
        ga.visitMethodInsn(INVOKESTATIC, OutputClass, "triggerPredicate", "(I)V", false);

        mv.visitLabel(label1);
    }


    private Type getTypefromStackType(int stackType) {
        switch (stackType) {
            case 1:
                return Type.INT_TYPE;
            case 2:
                return Type.FLOAT_TYPE;
            case 3:
                return Type.DOUBLE_TYPE;
            case 4:
                return Type.LONG_TYPE;
            default:
                return Type.VOID_TYPE; //todo
        }
    }
}
