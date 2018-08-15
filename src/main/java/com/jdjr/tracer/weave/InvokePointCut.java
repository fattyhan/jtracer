package com.jdjr.tracer.weave;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jdjr.tracer.constant.InvokeType;
import com.jdjr.tracer.constant.Segment;
import com.jdjr.tracer.constant.ThreadLocalEnum;
import com.jdjr.tracer.constant.TracerLoggerEnum;
import com.jdjr.tracer.db.DatabaseHelper;
import com.jdjr.tracer.entity.ReturnMsg;
import com.jdjr.tracer.util.ExceptionUtil;
import com.jdjr.tracer.util.IdWorker;
import com.jdjr.tracer.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hanxiaofei on 2018/8/13.
 */
@Aspect
public class InvokePointCut {
    DatabaseHelper databaseHelper = new DatabaseHelper();
    //存储线程name与traceID的映射
    private static ThreadLocal<Map<String, String>> TN2TI = ThreadLocalEnum.INSTANCE.TL();

    @Pointcut("@annotation(Trace)")
    public void trace() {

    }

    @AfterReturning(pointcut = "execution(* *(..)) && @annotation(Trace)", returning = "rvt")
    public void after(JoinPoint joinPoint, Object rvt) {
        //--TODO 从线程栈获取内部调用链 Thread.currentThread().getStackTrace()
        MethodSignature methodSig = (MethodSignature) joinPoint.getSignature();
        Annotation[] annotations = methodSig.getMethod().getDeclaredAnnotations();
        Trace annotation = (Trace) annotations[0];
        //---将returnMsg取出回填执行结果信息
        ReturnMsg returnMsg = JSON.parseObject(TN2TI.get().get(MD5Util.generatorMD5Hash(annotation.traceId() + annotation.segmentId())), ReturnMsg.class);
        if (returnMsg != null) {
            returnMsg.setInvokeType(annotation.invokeType());
            returnMsg.setTargetReturn(rvt.toString());
            TracerLoggerEnum.INSTANCE.LOG().info(JSON.toJSONString(returnMsg));
        }
        //--如果当前的节点是尾节点那么清除映射(threadNm-traceId|traceId-ReturnMsg)
        if (annotation.invokeType().equals(InvokeType.RPC_END)) {
            //--不为空才清除，避免
            TN2TI.get().remove(Thread.currentThread().getName(), annotation.traceId());
        }
        //--入库
        returnMsg.setId(MD5Util.generatorMD5Hash(IdWorker.nextId()+""));
        databaseHelper.getDataStore().save(returnMsg);
        //--合并RPC区间的RPCUUID
        databaseHelper.unifyRPCUuid(annotation.traceId());
        TN2TI.get().remove(MD5Util.generatorMD5Hash(annotation.traceId() + annotation.segmentId()));
    }

    @Before("execution(* *(..)) && @annotation(Trace)")
    public void before(JoinPoint joinPoint) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        JSONObject jsonObject = new JSONObject();
        try {
            //注入TraceID
            MethodSignature methodSig = (MethodSignature) joinPoint.getSignature();
            annotationTraceId(methodSig);
            ReturnMsg returnMsg = new ReturnMsg();
            Annotation[] annotations = methodSig.getMethod().getDeclaredAnnotations();
            Trace annotation = (Trace) annotations[0];
            String cName = methodSig.getMethod().getDeclaringClass().getName();
            String mName = methodSig.getMethod().getName();
            jsonObject.put("targetInvoked", cName);
            jsonObject.put("method", mName);
            jsonObject.put("server", ip());
            jsonObject.put("app", annotation.appName());
            returnMsg.setTargetMethod(jsonObject.toJSONString());
            returnMsg.setInvokeArgs("Args:" + Arrays.asList(JSON.toJSONString(joinPoint.getArgs())));
            returnMsg.setSegmentId(annotation.segmentId());
            returnMsg.setTraceId(annotation.traceId());
            //--RPC类型的调用通过指定的贯通参数做加密，其它类型通过traceID关联这样来串起整个流程
            if (annotation.invokeType().equals(InvokeType.RPC)) {
                if (StringUtils.isEmpty(annotation.keyword())) {
                    //关键字未指定就用参数加密
                    returnMsg.setRPCUuid(MD5Util.generatorMD5Hash(JSON.toJSONString(joinPoint.getArgs())));
                } else {
                    String _eFlg = getParamByRex(JSON.toJSONString(joinPoint.getArgs()));
                    returnMsg.setRPCUuid(MD5Util.generatorMD5Hash(JSON.toJSONString(_eFlg)));
                }
            }
            //使用traceID与segmentId加密做key
            TN2TI.get().put(MD5Util.generatorMD5Hash(annotation.traceId() + annotation.segmentId()), JSON.toJSONString(returnMsg));
            //TracerLoggerEnum.INSTANCE.LOG().info(JSON.toJSONString(returnMsg));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Around("execution(* *(..)) && @annotation(Trace)")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSig = (MethodSignature) joinPoint.getSignature();
        Annotation[] annotations = methodSig.getMethod().getDeclaredAnnotations();
        Trace annotation = (Trace) annotations[0];
        //---将returnMsg取出回填耗时信息
        ReturnMsg returnMsg = JSON.parseObject(TN2TI.get().get(MD5Util.generatorMD5Hash(annotation.traceId() + annotation.segmentId())), ReturnMsg.class);
        final StopWatch stopWatch = new StopWatch();
        Object ret = null;
        stopWatch.start();
        ret = joinPoint.proceed();
        stopWatch.stop();
        returnMsg.setElapsedTime(stopWatch.getTime(TimeUnit.MILLISECONDS) + "");
        //回填
        //TN2TI.get().put(annotation.traceId(), JSON.toJSONString(returnMsg));
        //TracerLoggerEnum.INSTANCE.LOG().info(JSON.toJSONString(returnMsg));
        //--记录用时
        databaseHelper.updateElapsedTime(annotation.traceId(),annotation.segmentId(),returnMsg.getElapsedTime());
        return ret;
    }

    @AfterThrowing(pointcut = "execution(* *(..)) && @annotation(Trace)", throwing = "e")
    public void exceptionHolder(Throwable e) {
        ExceptionUtil.toString_02(e);
    }

    private static String ip() {
        try {
            return InetAddress.getLocalHost().getHostAddress().toString();
        } catch (UnknownHostException e) {
            return "0.0.0.0";
        }
    }

    private static void annotationTraceId(MethodSignature methodSig) {
        try {
            String traceId = IdWorker.nextId() + "";
            //将新生成的traceID与threadNm的映射关系保存起来,空增否取
            if (StringUtils.isEmpty(TN2TI.get().get(Thread.currentThread().getName()))) {
                TN2TI.get().put(Thread.currentThread().getName(), traceId);
            } else {
                traceId = TN2TI.get().get(Thread.currentThread().getName());
            }
            Method myMeth = methodSig.getMethod();
            Trace annotation2 = (Trace) methodSig.getMethod().getAnnotations()[0];
            Class<?> superCls = myMeth.getClass().getSuperclass();
            Field declaredAnnot = superCls.getDeclaredField("declaredAnnotations");
            declaredAnnot.setAccessible(true);
            Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) declaredAnnot.get(myMeth);
            map.put(Trace.class, new TraceImpl(annotation2.appName(), annotation2.invokeType(), traceId, annotation2.segmentId()));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static String getParamByRex(String json) {
        String ret = "";
        String regex = "\"businessNo\":\"(.*?)\",";
        Matcher matcher = Pattern.compile(regex).matcher(json);
        while (matcher.find()) {
            ret = matcher.group(1);
        }
        return ret;
    }
}
