###jtracer是用于程序调用链监控的，其具备以下功能
1、可对所在运行系统内的方法调用进行监控，其原理是获取当前线程的线程栈，跟踪信息如下：

+--com.intellij.rt.execution.application.AppMain.main[name=main
+---java.lang.reflect.Method.invoke[name=main
+----sun.reflect.DelegatingMethodAccessorImpl.invoke[name=main
+-----sun.reflect.NativeMethodAccessorImpl.invoke[name=main
+------sun.reflect.NativeMethodAccessorImpl.invoke0[name=main
+-------com.jdjr.lambda.TraceTest.main[name=main
+--------com.jdjr.lambda.TraceTest.say[name=main
+---------java.lang.Thread.getStackTrace[name=main

2、可进行跨进程跟踪，即RPC调用跟踪。使用时需约定调用链各个相关系统的RPC区间极其区间内各节点分布，然后在需跟踪的方法上添加如下注解
@Trace(appName = "lambda",keyword = "businessNo",invokeType = InvokeType.RPC,segmentId = Segment.ONE)
该注解包含如下项：
    appName：所在应用的名称
    keyword：关键字，即贯穿调用链的唯一标识，若不声明则根据方法参数进行生成，此时需要保证参数一致
    invokeType：调用类型NATIVE|RPC|RPC_END，RPC类型的方法第一次执行时会生成PRCUuid是整个跟踪记录的主线。在RPC区间的最后一个节点需声明为RPC_END
    traceId：由插件分配，每个PRC区间只在该区间首个RPC类型方法执行时分配RPCUuid，与上游一致。区间内的方法监控用traceID保持一致。该注解值不要求用户声明
    segmentId：由用户声明，在整个调用链所处的节点是几就写几
监控示例如下：
[{
	"elapsedTime": "162",
	"id": "CA4D3DE839656E41A521D4E47AA5181E",
	"invokeArgs": "Args:[[null,27,{\"businessNo\":\"0\",\"compony\":\"JD\"}]]",
	"invokeType": "rpc",
	"rPCUuid": "B5A22CA8A85B9878EF1B23CC70F57077",
	"segmentId": "1",
	"targetMethod": "{\"app\":\"lambda\",\"server\":\"10.13.49.6\",\"targetInvoked\":\"com.jdjr.lambda.weave.Test\",\"method\":\"regiestMember\"}",
	"targetReturn": "han",
	"traceId": "258576399532675072"
}, {
	"elapsedTime": "146",
	"id": "D59C3BBD4BF14CD68EAE7516BE0B537F",
	"invokeArgs": "Args:[[]]",
	"invokeType": "rpc_end",
	"rPCUuid": "B5A22CA8A85B9878EF1B23CC70F57077",
	"segmentId": "end",
	"targetMethod": "{\"app\":\"lambda\",\"server\":\"10.13.49.6\",\"targetInvoked\":\"com.jdjr.lambda.weave.Test\",\"method\":\"DBSink\"}",
	"targetReturn": "true",
	"traceId": "258576399532675072"
}]
跨进程监控示例：
[{
	"elapsedTime": "2905",
	"invokeArgs": "Args:[0062002000010421, 258163693478596608, en_US, KBANK]",
	"rPCUuid": "2593C19C6C1A70AE7AD65B5A3EBACAB3",
	"segmentId": "1",
	"targetMethod": {
		"app": "th_dugong",
		"server": "10.13.49.6",
		"targetInvoked": "com.wangyin.th.dugong.service.impl.ThDugongOplogServiceImpl",
		"method": "BankAccRegist2"
	},
	"invokeType": "rpc",
	"targetReturn": {
		"code": "000000",
		"data": {
			"url": "https://ws06.uatebpp.kasikornbank.com/PGSRegistration.do?reg_id=20180814175513000043&langLocale=en_US"
		},
		"message": "成功"
	},
	"traceId": "258163700516638720"
}, {
	"elapsedTime": "1131",
	"invokeArgs": "Args:[0062002000010421, 258163693478596608, en_US, KBANK]",
	"rPCUuid": "2593C19C6C1A70AE7AD65B5A3EBACAB3",
	"segmentId": "end",
	"targetMethod": {
		"app": "th_channel",
		"server": "10.13.49.6",
		"targetInvoked": "com.wangyin.th.channel.biz.facade.impl.BankAccRegistFacadeImpl",
		"method": "BankAccRegist2"
	},
	"invokeType": "rpc_end",
	"targetReturn": {
		"code": "000000",
		"data": {
			"url": "https://ws06.uatebpp.kasikornbank.com/PGSRegistration.do?reg_id=20180814175513000043&langLocale=en_US"
		},
		"message": "成功"
	},
	"traceId": "258163702072725504"
}]
跨进行采集顺序：首先为本区间第一个RPC节点分配rPCUuid，在本区间最后一个节点完成时将该rPCUuid标识到本区间所有的记录中，展示时只需要根据该值进行索引并排序即可

3、耗时监控：
可监控整个调用链的每一个节点的执行耗时，为用户提供优化依据

4、数据落地，该插件集成MongoDB，用户也可自定义自己的数据sink进行存储

###缺陷
1、对代码有轻度侵入
2、对业务系统有要求（所有节点入参须有一个贯穿唯一标识）
