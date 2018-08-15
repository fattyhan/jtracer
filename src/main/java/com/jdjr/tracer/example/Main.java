package com.jdjr.tracer.example;

import com.alibaba.fastjson.JSON;
import com.jdjr.tracer.db.DatabaseHelper;
import com.jdjr.tracer.entity.ReturnMsg;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.get;

/**
 * Created by hanxiaofei on 2018/5/17.
 */
public class Main {
    private static Integer atomicInteger = 0;
    public static void main(String[] args) {
        get("/hello", (request, response) -> {
            //---查看有多少RPC区间，通过RPCUUID或者keyword或者参数
            List<ReturnMsg> results = new ArrayList<ReturnMsg>();
            DatabaseHelper databaseHelper = new DatabaseHelper();
            List<ReturnMsg> returnMsgs = databaseHelper.getDataStore().createQuery(ReturnMsg.class)
                    .field("RPCUuid").equal("F4EA5F46DD32B666BE3F802DFE2982C2")
                    .order("segmentId")//"-segmentId"降序
                    .asList();
            //---根据每个PRC区间的起始traceID获取所有的结果
            returnMsgs.forEach(returnMsg -> {
                List<ReturnMsg> sectionRs = databaseHelper.getDataStore().createQuery(ReturnMsg.class)
                        .field("traceId").equal(returnMsg.getTraceId())
                        .order("segmentId")
                        .asList();
                //--把每个区间的累加起来
                results.addAll(sectionRs);
            });
//            atomicInteger += 1;
//            TestParams testParams = new TestParams();
//            testParams.setBusinessNo(atomicInteger+"");
//            testParams.setCompony("JD");
//            return new Test().regiestMember(null,27,testParams);
            databaseHelper.unifyRPCUuid("258503402134294528");
            return JSON.toJSONString(results);
        });
    }
}
