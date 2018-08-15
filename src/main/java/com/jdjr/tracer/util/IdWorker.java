package com.jdjr.tracer.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * tweeter的snowflake 移植到Java.参考资料：https://github.com/twitter/snowflake
 * id构成: 42位的时间前缀 + 10位的节点标识 + 12位的sequence避免并发的数字(12位不够用时强制得到新的时间前缀)
 * id单调递增，长整型
 * 注意这里进行了小改动: snowkflake是5位的datacenter加5位的机器id; 这里变成使用10位的机器id
 * snowkflake当时间调整时将拒绝分配ID，这里改成分配UUID的tMostSignificantBits
 * 通过取服务器名中的编号来分配机器id，实现了去状态化
 * 使用：long id = IdWorker.nextId();
 */
public final class IdWorker {
    private static final long workerId;//每台机器分配不同的id
    private static final long epoch = 1472693086614L;   // 时间起始标记点，作为基准，一般取系统的最近时间
    private static final long workerIdBits = 10L;      // 机器标识位数
    private static final long maxWorkerId = -1L ^ -1L << workerIdBits;// 机器ID最大值: 1023
    private static long sequence = 0L;                   // 0，并发控制
    private static final long sequenceBits = 12L;      //毫秒内自增位

    private static final long workerIdShift = sequenceBits;                             // 12
    private static final long timestampLeftShift = sequenceBits + workerIdBits;// 22
    private static final long sequenceMask = -1L ^ -1L << sequenceBits;                 // 4095,111111111111,12位
    private static long lastTimestamp = -1L;

    static {
        String hostName = null;
        try {
            InetAddress netAddress = InetAddress.getLocalHost();
            hostName = netAddress.getHostName();
        } catch (UnknownHostException e) {
        }
        if (null != hostName && !"".equals(hostName)) {
            String hostNo = "";
            for (int i = 0; i < hostName.length(); i++) {
                if (hostName.charAt(i) >= 48 && hostName.charAt(i) <= 57) {
                    //取最后一组数字
                    if ("".equals(hostNo) || (hostName.charAt(i - 1) >= 48 && hostName.charAt(i - 1) <= 57))
                        hostNo += hostName.charAt(i);
                    else {
                        hostNo = "";
                        hostNo += hostName.charAt(i);
                    }
                }
            }
            if (null != hostNo && !"".equals(hostNo)) {
                workerId = Integer.parseInt(hostNo) % maxWorkerId;
            } else {
                workerId = 1;//TODO:当机器名不包含编号时，采用手动编号，需要在配置文件中指定

            }

        } else {
            workerId = 1;
        }
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
    }

    public static synchronized long nextId() {
        long timestamp = timeGen();
        if (lastTimestamp == timestamp) { // 如果上一个timestamp与新产生的相等，则sequence加一(0-4095循环); 对新的timestamp，sequence从0开始
            sequence = sequence + 1 & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);// 重新生成timestamp
            }
        } else {
            sequence = 0;
        }

        if (timestamp < lastTimestamp) {
            UUID uuid = UUID.randomUUID();
            return uuid.getMostSignificantBits();
        }

        lastTimestamp = timestamp;
        return timestamp - epoch << timestampLeftShift | workerId << workerIdShift | sequence;
    }

    /**
     * 等待下一个毫秒的到来, 保证返回的毫秒数在参数lastTimestamp之后
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获得系统当前毫秒数
     */
    private static long timeGen() {
        return System.currentTimeMillis();
    }
}
