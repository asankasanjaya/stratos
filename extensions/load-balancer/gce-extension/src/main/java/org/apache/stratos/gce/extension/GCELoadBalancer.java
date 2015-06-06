package org.apache.stratos.gce.extension;

import org.apache.stratos.load.balancer.common.domain.Topology;
import org.apache.stratos.load.balancer.extension.api.LoadBalancer;
import org.apache.stratos.load.balancer.extension.api.exception.LoadBalancerExtensionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class GCELoadBalancer implements LoadBalancer {

    private static final Log log = LogFactory.getLog(GCELoadBalancer.class);

    private GCEOperations gceOperations;

    public GCELoadBalancer() {
        try {
            gceOperations = new GCEOperations();
        } catch (LoadBalancerExtensionException e) {
            log.error(e);
        } catch (GeneralSecurityException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
    }

    @Override
    public void start() throws LoadBalancerExtensionException {
        log.info("Starting GCE Load balancer instance...");

        //create a target pool by specifying the target pool name given by user

        log.info("GCE Load balancer instance started");


    }

    @Override
    public void stop() throws LoadBalancerExtensionException {

        //remove instances from target pool
        //delete forwarding rule
    }

    @Override
    public boolean configure(Topology topology) throws LoadBalancerExtensionException {

        //add members to target pool
        //change port range in forwarding rule

        return false;
    }

    @Override
    public void reload() throws LoadBalancerExtensionException {

        //remove existing members in target pool
        //add members to target pool

    }
}