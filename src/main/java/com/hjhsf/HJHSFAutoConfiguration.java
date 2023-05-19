package com.hjhsf;

import com.hjhsf.scan.HJHSFScannerRegistrar;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by huangJin on 2023/5/16.
 */


@Configuration
@EnableConfigurationProperties(HJHSFConfigServer.class)
@Import(HJHSFScannerRegistrar.class)
@ConditionalOnClass(ZooKeeper.class)
public class HJHSFAutoConfiguration {
}
