package me.jack.lat.lwsbackend.annotations;

import me.jack.lat.lwsbackend.entities.User;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RestrictedRoles {
    User.Role[] value() default {User.Role.USER, User.Role.LIBRARIAN, User.Role.CHIEF_LIBRARIAN, User.Role.FINANCE_DIRECTOR};
}