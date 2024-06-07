package xyz.wagyourtail.ept.a;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Env {

    EnvType value();

    enum EnvType {
        CLIENT,
        SERVER,
        JOINED
    }
}
