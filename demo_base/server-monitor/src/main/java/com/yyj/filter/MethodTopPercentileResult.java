package com.yyj.filter;

import java.util.Map;

/**
 * 每个方法的调用结果信息
 */
public class MethodTopPercentileResult {

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 不同的TP对应的耗时情况
     */
    private Map<Integer, Long> topPercentiles;

    /**
     * 总计执行的次数
     */
    private long totalCount;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setTopPercentiles(Map<Integer, Long> topPercentiles) {
        this.topPercentiles = topPercentiles;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public String getMethodName() {
        return methodName;
    }

    public Map<Integer, Long> getTopPercentiles() {
        return topPercentiles;
    }

    public long getTotalCount() {
        return totalCount;
    }
}
