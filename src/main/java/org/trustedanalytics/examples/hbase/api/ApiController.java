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
package org.trustedanalytics.examples.hbase.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.trustedanalytics.examples.hbase.model.RowValue;
import org.trustedanalytics.examples.hbase.model.TableDescription;
import org.trustedanalytics.examples.hbase.services.HBaseService;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(value = "/api")
public class ApiController {

    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    @Autowired HBaseService hbaseService;

    @RequestMapping(method = RequestMethod.GET, value = "/tables")
    @ResponseBody
    public List<TableDescription> listTables() throws IOException, LoginException {
        LOG.info("listTables invoked.");
        return hbaseService.listTables();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tables/{name}")
    @ResponseBody
    public TableDescription getSingleTable(@PathVariable(value = "name") String name) throws IOException, LoginException {
        LOG.info("getSingleTable for {}.", name);
        return hbaseService.getTableInfo(name);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tables/{name}/tail")
    @ResponseBody
    public List<RowValue> tail(@PathVariable(value = "name") String name) throws IOException, LoginException {
        LOG.info("tail for {}.", name);
        return hbaseService.head(name, true);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tables/{name}/head")
    @ResponseBody
    public List<RowValue> head(@PathVariable(value = "name") String name) throws IOException, LoginException {
        LOG.info("head for {}.", name);
        return hbaseService.head(name, false);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tables", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTable(@RequestBody TableDescription tableDescription) throws LoginException, IOException {
        LOG.info("tail for {}.", tableDescription.toString());
        hbaseService.createTable(tableDescription);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tables/{name}/row", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void putRow(@PathVariable(value = "name") String name, @RequestBody RowValue rowValue) throws IOException, LoginException {
        LOG.info("put for {}, {}.", name, rowValue);
        hbaseService.putRow(name, rowValue);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tables/{name}/row/{rowKey}")
    @ResponseBody
    public RowValue getRow(@PathVariable(value = "name") String name, @PathVariable(value = "rowKey") String rowKey) throws IOException, LoginException {
        LOG.info("get for {}, {}.", name, rowKey);
        return hbaseService.getRow(name, rowKey);
    }
}
