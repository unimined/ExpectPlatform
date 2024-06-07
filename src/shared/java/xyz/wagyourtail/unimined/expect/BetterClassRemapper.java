package xyz.wagyourtail.unimined.expect;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class BetterClassRemapper extends ClassRemapper {

    public BetterClassRemapper(ClassVisitor classVisitor, Remapper remapper) {
        super(classVisitor, remapper);
    }

    @Override
    protected AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor annotationVisitor) {
        return new AnnotationRemapper(api, Type.getType(descriptor).getInternalName(), annotationVisitor, remapper) {
            @Override
            public void visitEnum(String name, String descriptor, String value) {
                super.visitEnum(name, descriptor, remapper.mapFieldName(Type.getType(descriptor).getInternalName(), value, descriptor));
            }
        };
    }

    @Override
    protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
        return new MethodRemapper(api, methodVisitor, remapper) {

            @Override
            protected AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor annotationVisitor) {
                return BetterClassRemapper.this.createAnnotationRemapper(descriptor, annotationVisitor);
            }

        };
    }

    @Override
    protected FieldVisitor createFieldRemapper(FieldVisitor fieldVisitor) {
        return new FieldRemapper(api, fieldVisitor, remapper) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return BetterClassRemapper.this.createAnnotationRemapper(descriptor, super.visitAnnotation(descriptor, visible));
            }
        };
    }
}
