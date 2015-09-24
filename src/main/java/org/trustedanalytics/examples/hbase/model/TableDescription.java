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

package org.trustedanalytics.examples.hbase.model;

import java.util.List;

public class TableDescription {

    private final String tableName;
    private final List<String> columnFamilies;

    public TableDescription() {
        this.tableName = null;
        this.columnFamilies = null;
    }

    public TableDescription(String tableName) {
        this.tableName = tableName;
        this.columnFamilies = null;

    }

    public TableDescription(String tableName, List<String> columnFamilies) {
        this.tableName = tableName;
        this.columnFamilies = columnFamilies;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumnFamilies() {
        return columnFamilies;
    }

    @Override
    public String toString() {
        return "TableDescription{" +
            "tableName='" + tableName + '\'' +
            ", columnFamilies=" + columnFamilies +
            '}';
    }
}
