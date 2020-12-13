package com.yyj.bean;

public class MethodInfo {
    private  String name;
    private  long   times;
    private  long   endTimes;

    public MethodInfo() {
    }

    public MethodInfo(String name, long times, long endTimes) {
        this.name = name;
        this.times = times;
        this.endTimes = endTimes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public long getEndTimes() {
        return endTimes;
    }

    public void setEndTimes(long endTimes) {
        this.endTimes = endTimes;
    }
}
