package com.ftn.socialnetwork.util.validators.custom;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = CityValidator.class)
@Documented
public @interface City {

    String message() default "Invalid city address or format.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}