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

package org.trustedanalytics.examples.hbase.api;

import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.regionserver.NoSuchColumnFamilyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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

    @RequestMapping("/tables")
    @ResponseBody
    public HttpEntity<List<TableDescription>> listTables() {
        LOG.info("listTables invoked.");
        List<TableDescription> list = null;
        try {
            list = hbaseService.listTables();
        } catch (LoginException e) {
            LOG.error("Error logging in", e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tables/{name}")
    @ResponseBody
    public HttpEntity<TableDescription> getSingleTable(@PathVariable(value = "name") String name) {
        LOG.info("getSingleTable for {}.", name);
        TableDescription result = null;
        try {
            result = hbaseService.getTableInfo(name);
        } catch (TableNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }  catch (IOException e) {
            LOG.error("Error while talking to HBase", e);
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (LoginException e) {
            LOG.error("Error logging in", e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/tables/{name}/tail")
    @ResponseBody
    public HttpEntity<List<RowValue>> tail(@PathVariable(value = "name") String name) {
        LOG.info("tail for {}.", name);
        List<RowValue> result = null;
        try {
            result = hbaseService.head(name, true);
        } catch (TableNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }  catch (IOException e) {
            LOG.error("Error while talking to HBase", e);
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (LoginException e) {
            LOG.error("Error logging in", e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/tables/{name}/head")
    @ResponseBody
    public HttpEntity<List<RowValue>> head(@PathVariable(value = "name") String name) {
        LOG.info("head for {}.", name);
        List<RowValue> result = null;
        try {
            result = hbaseService.head(name, false);
        } catch (TableNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }  catch (IOException e) {
            LOG.error("Error while talking to HBase", e);
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (LoginException e) {
            LOG.error("Error logging in", e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tables", consumes = "application/json")
    @ResponseBody
    public HttpEntity<String> createTable(@RequestBody TableDescription tableDescription) {
        LOG.info("tail for {}.", tableDescription.toString());
        try {
            hbaseService.createTable(tableDescription);
        } catch (TableExistsException e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.CONFLICT);
        } catch (IOException e) {
            LOG.error("Error while talking to HBase", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (LoginException e) {
            LOG.error("Error logging in", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tables/{name}/row", consumes = "application/json")
    @ResponseBody
    public HttpEntity<String> putRow(@PathVariable(value = "name") String name, @RequestBody RowValue rowValue) {
        LOG.info("put for {}, {}.", name, rowValue);
        try {
            hbaseService.putRow(name, rowValue);
        } catch (TableExistsException | NoSuchColumnFamilyException e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.CONFLICT);
        } catch (IOException e) {
            LOG.error("Error while talking to HBase", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (LoginException e) {
            LOG.error("Error logging in", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("created", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tables/{name}/row/{rowKey}")
    @ResponseBody
    public HttpEntity<RowValue> getRow(@PathVariable(value = "name") String name, @PathVariable(value = "rowKey") String rowKey) {
        RowValue result = null;
        LOG.info("get for {}, {}.", name, rowKey);
        try {
            result = hbaseService.getRow(name, rowKey);
        } catch (TableNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (LoginException e) {
            LOG.error("Error logging in", e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
