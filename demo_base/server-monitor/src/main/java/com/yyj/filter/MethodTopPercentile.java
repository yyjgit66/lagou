package com.yyj.filter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 方法和TP90等指标的对应关系
 */
public class MethodTopPercentile implements Runnable {

    private final int calculateTimeWindow;

    /**
     * 记录下当前时间窗格下所有的执行情况信息
     */
    private final MethodTopPercentileSecond[] topPercentileSeconds;

    /**
     * 创建一个新的时间窗格监控工具
     * @param calculateTimeWindow 需要监控最近多少秒的数据
     * @param reportStatusLimit 间隔多少秒发送一次通知(打印)
     * @return
     */
    public static MethodTopPercentile create(int calculateTimeWindow, int reportStatusLimit) {
        // 创建实例
        final MethodTopPercentile topPercentile = new MethodTopPercentile(calculateTimeWindow);

        // 创建单独的线程来进行每隔固定时间执行一次
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(
                topPercentile,
               500,
                TimeUnit.SECONDS.toMillis(reportStatusLimit),
                TimeUnit.MILLISECONDS);

        return topPercentile;
    }

    private MethodTopPercentile(int calculateTimeWindow) {
        this.calculateTimeWindow = calculateTimeWindow;
        topPercentileSeconds = new MethodTopPercentileSecond[calculateTimeWindow];
        for (int i = 0; i < calculateTimeWindow; i++) {
            topPercentileSeconds[i] = new MethodTopPercentileSecond();
        }
    }

    /**
     * 增加使用情况
     * @param methodName
     * @param useTime
     */
    public void increment(String methodName, long useTime) {
        final long currentTimeMillis = System.currentTimeMillis();

        // 获取当前方法所使用的时间 存放的位置
        final int idx = (int) (currentTimeMillis / 1000 % calculateTimeWindow);

        // 进行递增数据
        topPercentileSeconds[idx].increment(methodName, useTime, currentTimeMillis);
    }


    @Override
    public void run() {
        // 进行数据打印输出
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateStr = sdf.format(date) + ":";

        final long currentTimeMillis = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(calculateTimeWindow);

        // 进行统计哪些是最近时间窗格的数据
        final List<MethodTopPercentileSecond> enabledSecs = Arrays.asList(topPercentileSeconds).stream()
            .filter(s -> s.isSupportReport(currentTimeMillis)).collect(Collectors.toList());
        if (enabledSecs.isEmpty()) {
            return;
        }

        // 进行上报数据
        final Map<String, MethodTopPercentileResult> reportData = MethodTopPercentileSecond.
                doReport(enabledSecs, 90, 99);
        for (Map.Entry<String, MethodTopPercentileResult> entry : reportData.entrySet()) {
            final MethodTopPercentileResult result = entry.getValue();
            System.out.println(dateStr + entry.getKey() + "中的TP90:" + result.getTopPercentiles().get(90) + "毫秒, TP99:" + result.getTopPercentiles().get(99) + "毫秒, " +
                    "总计调用次数:" + result.getTotalCount());
        }

        System.out.println("---------------------------------------------------------------------");
    }

    /**
     * 计算当前毫秒数到下一秒还有多少的毫秒
     * @return
    */
    private int calculateNextSec() {
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, 1);
        now.set(Calendar.MILLISECOND, 0);

        final long millis = now.getTimeInMillis() - System.currentTimeMillis();
        return Math.toIntExact(millis > 0 ? millis : 0);
    }

    /**
     * 每秒中的数据信息
     */
    private static class MethodTopPercentileSecond {

        /**
         * 当前秒下面的执行情况
         */
        private final ConcurrentHashMap<String, ConcurrentHashMap<Long, AtomicLong>> invokeTimeWithCount
            = new ConcurrentHashMap<>();

        /**
         * 最后一次执行递增的时间信息
         */
        private volatile long lastIncrementTime = -1;

        public void increment(String invokeMethod, long useTime, long currentMillis) {
            if (lastIncrementTime > 0 && currentMillis - lastIncrementTime > 1000) {
                synchronized (this) {
                    // 二次检查保证只会有一次生效
                    if (currentMillis - lastIncrementTime > 1000 && invokeTimeWithCount.size() > 0) {
                        invokeTimeWithCount.clear();
                        lastIncrementTime = currentMillis;
                    }
                }
            }

            // 查询出该方法中所有使用耗时的汇总情况
            ConcurrentHashMap<Long, AtomicLong> timeWithCount =
                invokeTimeWithCount.computeIfAbsent(invokeMethod, (m) -> new ConcurrentHashMap<>());

            // 找出指定耗时中的使用情况
            final AtomicLong count = timeWithCount.computeIfAbsent(useTime, (t) -> new AtomicLong());
            count.incrementAndGet();

            lastIncrementTime = currentMillis;
        }

        /**
         * 是否支持上报操作
         * @param supportReportAfterTime 支持指定时间之后的数据回显
         * @return
         */
        public boolean isSupportReport(long supportReportAfterTime) {
            return lastIncrementTime > supportReportAfterTime;
        }

        /**
         * 进行上报操作
         * @param tpValue
         * @return
         */
        public static Map<String, MethodTopPercentileResult> doReport(
                List<MethodTopPercentileSecond> timeBuckets, Integer... tpValue) {
            if (tpValue == null || tpValue.length <= 0) {
                return Collections.emptyMap();
        }

        // 保存数量对应的关系, 对timeBuckets中的数据做合并
        final Map<String, SortedMap<Long, Long>> dataMap = new HashMap<>();
            // 每秒 方法信息集合
            for (MethodTopPercentileSecond timeBucket : timeBuckets) {
                for (Map.Entry<String, ConcurrentHashMap<Long, AtomicLong>> entry : timeBucket.invokeTimeWithCount.entrySet()) {
                    // 调用时间和对应的次数
                    final Map<Long, Long> timeWithCount = dataMap.computeIfAbsent(entry.getKey(),
                            (m) -> new TreeMap<>(Long::compareTo));

                    for (Map.Entry<Long, AtomicLong> countMapping : entry.getValue().entrySet()) {
                        long count = timeWithCount.getOrDefault(countMapping.getKey(), 0L);
                        count += countMapping.getValue().get();
                        timeWithCount.put(countMapping.getKey(), count);
                    }
                }
            }

            // 进行计算结果信息
            final HashMap<String, MethodTopPercentileResult> result = new HashMap<>();
            for (Map.Entry<String, SortedMap<Long, Long>> methodToTimesEntry : dataMap.entrySet()) {
                final String methodName = methodToTimesEntry.getKey();
                final SortedMap<Long, Long> timeWithCount = methodToTimesEntry.getValue();

                final HashMap<Integer, Long> tpValues = new HashMap<>();
                final long totalCount = timeWithCount.values().stream().mapToLong(t -> t).sum();
                // 进行计算并且保存
                for (Integer tp : tpValue) {
                    tpValues.put(tp, getTpValue(timeWithCount, totalCount, tp));
                }

                // 拼装结果
                final MethodTopPercentileResult methodResult = new MethodTopPercentileResult();
                methodResult.setMethodName(methodName);
                methodResult.setTopPercentiles(tpValues);
                methodResult.setTotalCount(totalCount);

                // 添加数据
                result.put(methodName, methodResult);
            }

            return result;
        }

        /**
         * 获取TP90, TP99等值
         */
        private static Long getTpValue(SortedMap<Long, Long> timeWithCount, long totalInvokeCount, int tpValue) {
            int roof = Math.round(totalInvokeCount * tpValue * 1.0f / 100);

            int count = 0;
            Long value = 0L;
            for (Map.Entry<Long, Long> entry : timeWithCount.entrySet()) {
                count += entry.getValue();
                if (count >= roof) {
                    value = entry.getKey();
                    break;
                }
            }
            return value;
        }

    }
}
