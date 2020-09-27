package dev.imabad.mceventsuite.api.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Route {

    String endpoint();

    EndpointMethod method();

    boolean json() default true;

    boolean auth() default false;

    String permission() default "";

}
