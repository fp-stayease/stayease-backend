package com.finalproject.stayease.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application")
public record EnvConfigProperties(String env) {

}
