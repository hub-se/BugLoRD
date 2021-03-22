package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.util.CheckMethodAdapter;

import java.util.HashMap;

public class PredicateClassVisitor extends ClassVisitor implements Opcodes {

    private String className;

    public PredicateClassVisitor(final ClassVisitor cv) {
        super(ASM5, cv);
    }

    public void visitSource(String file, String debug) {
        super.visitSource(file,debug);
    }

    @Override
    public MethodVisitor visitMethod(final int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            CheckMethodAdapter cm = new CheckMethodAdapter(access, name, desc, mv, new HashMap<Label,Integer>() {
            });
            PredicateMethodAdapter ma = new PredicateMethodAdapter(mv, this.className, name);
            ma.aa = new AnalyzerAdapter("owner", access, name, desc, ma);
            ma.ga = new GeneratorAdapter(ma.aa, access, name, desc);


            return ma.ga;
        }
        return null;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
}
