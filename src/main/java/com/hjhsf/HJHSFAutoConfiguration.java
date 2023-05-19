package com.hjhsf;

import com.hjhsf.scan.HJHSFScannerRegistrar;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by huangJin on 2023/5/19.
 */
@EnableConfigurationProperties(HJHSFConfigServer.class)
@ConditionalOnClass(ZooKeeper.class)
@Import(HJHSFScannerRegistrar.class)
@Configuration
public class HJHSFAutoConfiguration {

    @Bean
    HJHSFWatch hjhsfWatch() {
        return new HJHSFWatch();
    }
    @Bean
    HJHSFCommunication hjhsfCommunication(HJHSFConfigServer hjhsfConfigServer){
        return new HJHSFCommunication(hjhsfConfigServer);
    }
}

