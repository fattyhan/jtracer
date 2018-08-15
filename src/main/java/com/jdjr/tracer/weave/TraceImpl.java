package com.jdjr.tracer.weave;

import java.lang.annotation.Annotation;

/**
 * Created by hanxiaofei on 2018/8/13.
 */
public class TraceImpl implements Trace{
    private String appName;
    private String invokeType;
    private String traceId;
    private String segmentId;

    public TraceImpl(String appName,String invokeType,String traceId,String segmentId) {
        this.appName = appName;
        this.invokeType = invokeType;
        this.traceId = traceId;
        this.segmentId = segmentId;
    }

    @Override
    public String appName() {
        return this.appName;
    }

    @Override
    public String keyword() {
        return null;
    }

    @Override
    public String invokeType() {
        return this.invokeType;
    }

    @Override
    public String traceId() {
        return this.traceId;
    }

    @Override
    public String segmentId() {
        return this.segmentId;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
