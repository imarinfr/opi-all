package org.lei.opi.core.structures;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@Documented
@Inherited
public @interface Parameters {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(value = ElementType.METHOD)
  @Documented
  @Repeatable(Parameters.class)
  @interface Parameter {
    String name();
    Class<?> type() default String.class;
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
    String desc();

  /**
   * This provides description when generating docs.
   * public String desc() default "";
   */
  /**
   * This provides params when generating docs.
   * public String[] params();
   */
}
  
  Parameter[] value();
}