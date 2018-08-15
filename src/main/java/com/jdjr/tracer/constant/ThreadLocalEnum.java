package com.jdjr.tracer.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanxiaofei on 2018/8/14.
 */
public enum ThreadLocalEnum {
    INSTANCE;

    ThreadLocalEnum() {
    }
    public ThreadLocal<Map<String,String>> TL(){
        return ThreadLocal.withInitial(()->new HashMap<String,String>());
    }
}
