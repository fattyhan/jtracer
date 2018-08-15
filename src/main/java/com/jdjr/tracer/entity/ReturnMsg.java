package com.jdjr.tracer.entity;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by hanxiaofei on 2018/8/13.
 */
@Entity("trace_tbl")
public class ReturnMsg {
    @Id
    private String id;
    private String targetMethod;
    private String targetServer;
    private String targetReturn;
    private String invokeArgs;
    private String invokeType;
    private String elapsedTime;
    private String traceId;
    private String segmentId;
    private String RPCUuid;

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getTargetServer() {
        return targetServer;
    }

    public void setTargetServer(String targetServer) {
        this.targetServer = targetServer;
    }

    public String getTargetReturn() {
        return targetReturn;
    }

    public void setTargetReturn(String targetReturn) {
        this.targetReturn = targetReturn;
    }

    public String getInvokeArgs() {
        return invokeArgs;
    }

    public void setInvokeArgs(String invokeArgs) {
        this.invokeArgs = invokeArgs;
    }

    public String getInvokeType() {
        return invokeType;
    }

    public void setInvokeType(String invokeType) {
        this.invokeType = invokeType;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public String getRPCUuid() {
        return RPCUuid;
    }

    public void setRPCUuid(String RPCUuid) {
        this.RPCUuid = RPCUuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
