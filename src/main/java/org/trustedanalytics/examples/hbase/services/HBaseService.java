/**
 * Copyright (c) 2016 Intel Corporation
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
package org.trustedanalytics.examples.hbase.services;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.trustedanalytics.examples.hbase.configs.HBaseConnectionFactory;
import org.trustedanalytics.examples.hbase.model.ColumnFamilyValue;
import org.trustedanalytics.examples.hbase.model.ColumnValue;
import org.trustedanalytics.examples.hbase.model.RowValue;
import org.trustedanalytics.examples.hbase.model.TableDescription;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HBaseService {

    private static final Logger LOG = LoggerFactory.getLogger(HBaseService.class);

    @Autowired
    ConversionsService conversionsService;

    @Autowired
    HBaseConnectionFactory hBaseConnectionFactory;

    @Value("${hbase.namespace}")
    private String hbaseNamespace;

    @Value("${results.pageSize}")
    private int pageSize;

    /**
     * Get list of tables in given namespace;
     */
    public List<TableDescription> listTables() throws LoginException {
        List<TableDescription> result = null;

        try (Connection connection = hBaseConnectionFactory.connect();
            Admin admin = connection.getAdmin()) {
            HTableDescriptor[] tables = admin.listTables();

            Stream<HTableDescriptor> tableDescriptorsStream = Arrays.stream(tables);

            result = tableDescriptorsStream.map(conversionsService::constructTableDescription)
                .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error while talking to HBase.", e);
        }

        return result;
    }

    public TableDescription getTableInfo(String name) throws IOException, LoginException {
        TableDescription result = null;

        try (Connection connection = hBaseConnectionFactory.connect();
            Admin admin = connection.getAdmin()) {
            HTableDescriptor tableDesc = admin.getTableDescriptor(TableName.valueOf(name));
            result = conversionsService.constructTableDescription(tableDesc);
        }

        return result;
    }

    public List<RowValue> head(String name, boolean reverse) throws IOException, LoginException {
        List<RowValue> result = new ArrayList<>();

        try (Connection connection = hBaseConnectionFactory.connect()) {
            Table table = connection.getTable(TableName.valueOf(name));

            Scan scan = new Scan();
            scan.setReversed(reverse);
            scan.setFilter(new PageFilter(pageSize));
            try (ResultScanner rs = table.getScanner(scan)) {
                for (Result r = rs.next(); r != null; r = rs.next()) {
                    result.add(conversionsService.constructRowValue(r));
                }
            }
        }

        return result;
    }


    public void createTable(TableDescription tableDescription) throws IOException, LoginException {
        try (Connection connection = hBaseConnectionFactory.connect();
            Admin admin = connection.getAdmin()) {

            HTableDescriptor table = new HTableDescriptor(TableName.valueOf(ensureNamespace(tableDescription.getTableName())));
            for (String columnFamily : tableDescription.getColumnFamilies()) {
                table.addFamily(new HColumnDescriptor(columnFamily));
            }

            admin.createTable(table);
        }
    }

    public void putRow(String name, RowValue row) throws IOException, LoginException {
        try (Connection connection = hBaseConnectionFactory.connect()) {
            Table table = connection.getTable(TableName.valueOf(name));

            Put p = new Put(Bytes.toBytes(row.getRowKey()));
            for (ColumnFamilyValue family : row.getColumnFamilies() ) {
                for (ColumnValue column : family.getColumnValues()) {
                    p.addColumn(Bytes.toBytes(family.getFamilyName()), Bytes.toBytes(column.getColumn()),Bytes.toBytes(column.getValue()));
                }
            }
            table.put(p);

        }
    }

    public RowValue getRow(String name, String rowKey) throws IOException, LoginException {
       Result r = null; 
       try (Connection connection = hBaseConnectionFactory.connect()) {
            Table table = connection.getTable(TableName.valueOf(name));
            Get get = new Get(Bytes.toBytes(rowKey));
            r = table.get(get);
        }

        return conversionsService.constructRowValue(r);
    }

    private String ensureNamespace(String tableName) {
        if (!tableName.contains(":") && !Strings.isEmpty(hbaseNamespace)) {
            tableName = String.format("%s:%s", hbaseNamespace, tableName);
        }

        return tableName;
    }

}
