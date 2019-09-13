package dev.imabad.mceventsuite.api.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    String prefix() default "";

}
