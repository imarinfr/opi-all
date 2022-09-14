package org.lei.opi.core.structures;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@Documented
@Repeatable(ReturnMsgs.class)
public @interface ReturnMsg {
  String name();
  Class<?> className() default String.class;
  boolean isList() default false;
  double min() default Double.MIN_VALUE;
  double max() default Double.MAX_VALUE;
  String desc();
}