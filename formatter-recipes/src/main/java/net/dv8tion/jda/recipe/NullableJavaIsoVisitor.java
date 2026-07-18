package net.dv8tion.jda.recipe;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

// TODO remove this after https://github.com/openrewrite/rewrite/issues/3321 is fixed
@NullMarked
@SuppressWarnings("NullableProblems")
public abstract class NullableJavaIsoVisitor<P> extends JavaIsoVisitor<P> {
    // This can be nullable, the "RemoveAnnotation" recipe proves us so
    @Override
    public J.@Nullable Annotation visitAnnotation(J.Annotation annotation, P p) {
        return super.visitAnnotation(annotation, p);
    }
}
