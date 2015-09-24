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

public class ColumnFamilyValue {
    private final String familyName;
    private final List<ColumnValue> columnValues;

    public ColumnFamilyValue() {
        this.familyName = null;
        this.columnValues = null;
    }

    public ColumnFamilyValue(String familyName, List<ColumnValue> columnValues) {
        this.familyName = familyName;
        this.columnValues = columnValues;
    }

    public String getFamilyName() {
        return familyName;
    }

    public List<ColumnValue> getColumnValues() {
        return columnValues;
    }

    @Override public String toString() {
        return "ColumnFamilyValue{" +
            "familyName='" + familyName + '\'' +
            ", columnValues=" + columnValues +
            '}';
    }
}
