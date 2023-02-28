/*
 * A decorator to describe elements that can be returned 
 * over a Communicator as JSON strings.
 * Used to define communication protocol in conjunction with 
 * @Parameter decorator.
 */
package org.lei.opi.core.definitions;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@Documented
@Repeatable(ReturnMsgs.class)
public @interface ReturnMsg {
    String name();
    Class<?> className() default String.class;
    String desc();
    boolean isList() default false;
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
}