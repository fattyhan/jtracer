package com.jdjr.tracer.constant;

import org.apache.log4j.Logger;

/**
 * Created by hanxiaofei on 2018/8/14.
 */
public enum TracerLoggerEnum {
    INSTANCE;

    TracerLoggerEnum() {
    }

    public Logger LOG(){
        Logger logger = Logger.getLogger(TracerLoggerEnum.class);
        return logger;
    }
}
