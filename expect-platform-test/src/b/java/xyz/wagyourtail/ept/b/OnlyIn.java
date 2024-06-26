package xyz.wagyourtail.ept.b;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface OnlyIn {

    Type env();

    enum Type {
        CLIENT,
        SERVER,
        COMBINED
    }

}
