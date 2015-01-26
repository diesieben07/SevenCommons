package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
class ArrayAccessVariable implements ASMVariable {

    private final ASMVariable array;
    private final CodePiece index;

    ArrayAccessVariable(ASMVariable array, CodePiece index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public CodePiece get() {
        return CodePieces.arrayGet(array.get(), index, getType());
    }

    @Override
    public CodePiece set(CodePiece newValue) {
        return CodePieces.arraySet(array.get(), index, newValue, getType());
    }

    @Override
    public CodePiece getAndSet(CodePiece newValue) {
        return get().append(set(newValue));
    }

    @Override
    public CodePiece setAndGet(CodePiece newValue) {
        return set(newValue).append(get());
    }

    @Override
    public AnnotationNode getRawAnnotation(Class<? extends Annotation> ann) {
        return array.getRawAnnotation(ann);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> ann) {
        return array.getAnnotation(ann);
    }

    @Override
    public AnnotationNode getRawAnnotation(String internalName) {
        return array.getRawAnnotation(internalName);
    }

    @Override
    public boolean hasGetterModifier(int modifier) {
        return array.hasGetterModifier(modifier);
    }

    @Override
    public boolean hasSetterModifier(int modifier) {
        return array.hasSetterModifier(modifier);
    }

    @Override
    public Type getType() {
        Type arrayType = array.getType();
        int dim = arrayType.getDimensions() - 1;
        return dim == 0 ? arrayType.getElementType() : ASMUtils.asArray(arrayType.getElementType(), dim);
    }

    @Override
    public String name() {
        return "elementIn_" + array.name();
    }

    @Override
    public String rawName() {
        return array.rawName();
    }

    @Override
    public String setterName() {
        return array.setterName();
    }

    @Override
    public boolean isStatic() {
        return array.isStatic();
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isField() {
        return false;
    }

    @Override
    public boolean isMethod() {
        return false;
    }
}
