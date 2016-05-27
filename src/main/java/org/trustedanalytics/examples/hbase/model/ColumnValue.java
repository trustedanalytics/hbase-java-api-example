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
package org.trustedanalytics.examples.hbase.model;

public class ColumnValue {
    private final String column;
    private final String value;

    public ColumnValue() {
        this.column = null;
        this.value = null;
    }

    public ColumnValue(String column, String value) {
        this.column = column;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "ColumnValue{" +
            "column='" + column + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
