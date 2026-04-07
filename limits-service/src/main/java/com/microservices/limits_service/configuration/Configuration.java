package com.microservices.limits_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("limits-service")
public class Configuration {
    private int minimum;
    private int maximum;
    
    public int getMinimum(){
        return this.minimum;
    }

    public void setMinimum(int min){
        this.minimum = min;
    }

    public int getMaximum(){
        return this.maximum;
    }

    public void setMaximum(int max){
        this.maximum = max;
    }
}
