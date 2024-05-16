package me.jack.lat.lwsbackend.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface UnprotectedRoute {
    // Empty annotation
}