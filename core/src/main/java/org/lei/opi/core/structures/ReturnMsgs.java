package org.lei.opi.core.structures;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@Documented
@Inherited
public @interface ReturnMsgs {
  ReturnMsg[] value();
}