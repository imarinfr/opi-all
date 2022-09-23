package org.lei.opi.core.definitions;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@Documented
@Inherited
public @interface Parameters {
  Parameter[] value();
}