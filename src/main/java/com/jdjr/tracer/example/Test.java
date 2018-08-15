package com.jdjr.tracer.example;
import com.jdjr.tracer.constant.InvokeType;
import com.jdjr.tracer.constant.Segment;
import com.jdjr.tracer.weave.Trace;

/**
 * Created by hanxiaofei on 2018/8/13.
 */
public class Test{
    @Trace(appName = "lambda",keyword = "businessNo",invokeType = InvokeType.RPC,segmentId = Segment.ONE)
    public  String regiestMember(String name, int age, TestParams testParams){
        DBSink();
        return "han";
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new MyThread());
        Thread t2 = new Thread(new MyThread());
        t1.start();
//        while (true){
//              if (!t1.isAlive()){
//                  t2.start();
//                  break;
//              }
//        }
    }

    @Trace(appName = "lambda", invokeType = InvokeType.RPC_END,segmentId = Segment.THREE)
    public boolean DBSink(){
        return true;
    }

    static class MyThread extends Thread {
        TestParams testParams = new TestParams();
        @Override
        public  void run() {
            for (int i = 0; i <1 ; i++) {
                testParams.setBusinessNo(i+"");
                testParams.setCompony("JD");
                new Test().regiestMember(null, 27, testParams);
            }
        }
    }

}
