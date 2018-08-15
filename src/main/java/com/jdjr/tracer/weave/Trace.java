package com.jdjr.tracer.weave;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hanxiaofei on 2018/8/13.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Trace {
    public String appName();
    public String keyword() default "";
    public String invokeType() default "";
    public String traceId() default "";
    public String segmentId() default "";
}

