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

import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.regionserver.NoSuchColumnFamilyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.security.auth.login.LoginException;
import java.io.IOException;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleBadRequest(Exception ex) {
        LOG.error("Handling request malformed exception", ex);
        return "Request message malformed";
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public String handleLoginException(LoginException ex) {
        LOG.error("Error logging in", ex);
        return ex.getMessage();
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleTableNotFound(TableNotFoundException ex) {
        LOG.error("Table not found", ex);
        return ex.getMessage();
    }

    @ExceptionHandler({TableExistsException.class, NoSuchColumnFamilyException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public String handleConflict(Exception ex) {
        LOG.error("Resource already exists", ex);
        return ex.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public String handleIOException(IOException ex) {
        LOG.error("Error while talking to HBase", ex);
        return "Error while talking to HBase";
    }

    @ExceptionHandler
    public ResponseEntity<String> handleGeneric(Exception ex) {
        LOG.error("Handling generic exception", ex);
        HttpStatus responseStatus = resolveAnnotatedResponseStatus(ex);
        if (responseStatus == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(ex.getMessage(), responseStatus);
    }

    private HttpStatus resolveAnnotatedResponseStatus(Throwable ex) {
        ResponseStatus responseStatus = findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.code();
        }
        else if (ex.getCause() instanceof Exception) {
            return resolveAnnotatedResponseStatus(ex.getCause());
        }
        return null;
    }

}
