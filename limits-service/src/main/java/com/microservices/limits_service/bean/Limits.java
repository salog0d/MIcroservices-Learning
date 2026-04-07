package com.microservices.limits_service.bean;

public class Limits {
    
    private int minimun;
    private int maximum;

    public Limits(int min, int max){
        super();
        this.minimun = min;
        this.maximum = max;
    }

    public int getMinimum(){
        return this.minimun;
    }

    public void setMinimum(int min){
        this.minimun = min;
    }

    public int getMaximum(){
        return this.maximum;
    }

    public void setMaximum(int max){
        this.maximum = max;
    }
}
