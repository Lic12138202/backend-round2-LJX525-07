package com.studio.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 将 app.hash.* / app.jwt.* 配置绑定为对象 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Hash hash = new Hash();
    private Jwt jwt = new Jwt();

    public Hash getHash() {
        return hash;
    }

    public void setHash(Hash hash) {
        this.hash = hash;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    /** app.hash.* */
    public static class Hash {
        /** 当前注册使用的算法：pbkdf2 | bcrypt | sha256 */
        private String algorithm = "pbkdf2";

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
    }

    /** app.jwt.* */
    public static class Jwt {
        private String secret;
        private long expireSeconds = 7200;
        private double refreshThreshold = 0.5;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpireSeconds() {
            return expireSeconds;
        }

        public void setExpireSeconds(long expireSeconds) {
            this.expireSeconds = expireSeconds;
        }

        public double getRefreshThreshold() {
            return refreshThreshold;
        }

        public void setRefreshThreshold(double refreshThreshold) {
            this.refreshThreshold = refreshThreshold;
        }
    }
}
