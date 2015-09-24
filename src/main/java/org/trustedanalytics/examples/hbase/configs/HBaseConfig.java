/*
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.examples.hbase.configs;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class HBaseConfig {

    @Value("${hbase.namespace}")
    private String hbaseNamespace;

    @Value("${zookeeper.quorum}")
    private String zookeeperQuorum;

    @Value("${zookeeper.clientPort}")
    private String zookeeperClientPort;

    @Bean
    protected org.apache.hadoop.conf.Configuration hbaseConfiguration() throws IOException {
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
        conf.set(HConstants.ZOOKEEPER_QUORUM, zookeeperQuorum);
        conf.set(HConstants.ZOOKEEPER_CLIENT_PORT, zookeeperClientPort);

        // fail fast
        conf.setInt(HConstants.HBASE_CLIENT_RETRIES_NUMBER, 3);
        conf.setInt(HConstants.HBASE_CLIENT_PAUSE, 1000);
        conf.setInt(HConstants.ZK_SESSION_TIMEOUT, 10000);
        conf.setInt(HConstants.ZOOKEEPER_RECOVERABLE_WAITTIME, 10000);

        return conf;
    }
}
