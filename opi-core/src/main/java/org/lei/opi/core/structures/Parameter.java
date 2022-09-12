package org.lei.opi.core.structures;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@Documented
@Repeatable(Parameters.class)
public @interface Parameter {
  String name();
  Class<?> type() default String.class;
  double min() default Double.MIN_VALUE;
  double max() default Double.MAX_VALUE;
  String desc();
}