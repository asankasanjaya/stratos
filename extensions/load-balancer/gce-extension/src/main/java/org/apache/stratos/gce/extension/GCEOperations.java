package org.apache.stratos.gce.extension;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.load.balancer.extension.api.exception.LoadBalancerExtensionException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class GCEOperations {

    private static final Log log = LogFactory.getLog(GCELoadBalancer.class);

    //TODO: remove all hardcoded values
    private static final String PROJECT_NAME = "My First Project";
    private static final String PROJECT_ID = "gold-access-96509";
    private static final String ZONE_NAME = "europe-west1-b";
    private static final String REGION_NAME = "europe-west1";
    private static final String RUNNING_FILTER = "status eq RUNNING";
    //auth
    private static final String KEY_FILE = "/home/sanjaya/keys/p12key-donwloaded.p12";
    private static final String ACCOUNT_ID = "164588286821-a517i85433f83e0nthc4qjmoupri" +
            "394q@developer.gserviceaccount.com";

    /**
     * Directory to store user credentials.
     */
    private static final String DATA_STORE_DIR = ".store/gce-extension";
    static Compute compute;


    /**
     * Constructor for GCE Operations Class
     *
     * @throws LoadBalancerExtensionException
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public GCEOperations() throws LoadBalancerExtensionException,
            GeneralSecurityException, IOException {

        buildComputeEngineObject();

        //Calling following  methods from here only for testing purposes
        createForwardingRule("fr1");

    }

    /**
     * Authorize and build compute engine object
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private void buildComputeEngineObject() throws GeneralSecurityException, IOException {

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        DataStoreFactory dataStoreFactory = new FileDataStoreFactory(new
                File(System.getProperty("user.home"), DATA_STORE_DIR));
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(ACCOUNT_ID)
                .setServiceAccountScopes(Collections.singleton(ComputeScopes.COMPUTE))
                .setServiceAccountPrivateKeyFromP12File(new File(KEY_FILE))
                .build();

        // Create compute engine object for listing instances
        compute = new Compute.Builder(
                httpTransport, jsonFactory, null).setApplicationName(PROJECT_NAME)
                .setHttpRequestInitializer(credential).build();


    }


    /**
     * Creating a new target pool; name should be unique
     *
     * @param targetPoolName
     */
    public void createTargetPool(String targetPoolName) {

        TargetPool targetPool = new TargetPool();
        targetPool.setName(targetPoolName);

        //TODO:REMOVE try catch
        try {
            compute.targetPools().insert(PROJECT_ID, REGION_NAME, targetPool).execute();
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("failed to create target pool: " + targetPoolName);
            }
            e.printStackTrace();
        }


    }

    /**
     * Check whether the given target pool is already exists in the given project and region.
     * Target pools are unique for regions, not for zones
     *
     * @param targetPoolName
     */
    public boolean isTargetPoolExists(String targetPoolName) {

        //TODO: remove try catch
        try {
            Compute.TargetPools.List targetPools = compute.targetPools().
                    list(PROJECT_ID, REGION_NAME);
            TargetPoolList targetPoolList = targetPools.execute();
            for (TargetPool targetPool : targetPoolList.getItems()) {
                if (targetPool.getName().equals(targetPoolName)) {
                    return true;
                }

            }

        } catch (IOException e) {
            if(log.isErrorEnabled()){
                log.error("Error caused when checking for target pools");
            }
            e.printStackTrace();
        }
        return false;

    }

    /**
     * Get a target pool already created in GCE
     *
     * @param targetPoolName
     * @return
     */
    public TargetPool getTargetPool(String targetPoolName) {
        //todo:remove try catch
        try {
            if (isTargetPoolExists(targetPoolName))
                return compute.targetPools().get(PROJECT_ID, REGION_NAME, targetPoolName).execute();
            else
                if (log.isDebugEnabled())
                    log.debug("Requested Target Pool Is not Available");

            return null;

        } catch (IOException e) {
            if (log.isErrorEnabled()){
                log.error("Exception caused when try to get target pool");
            }
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Add given set of instances to given target pool.
     *
     * @param targetPoolName
     * @param instanceReferenceList
     */

    private void addInstancesToTargetPool(String targetPoolName, List<InstanceReference>
            instanceReferenceList) {

        //todo: target pool is must be validated before call this method. May be we can add a validation check here
        log.info("Adding instances to target pool");

        //create target pools add instance request and set instance to it
        TargetPoolsAddInstanceRequest targetPoolsAddInstanceRequest = new
                TargetPoolsAddInstanceRequest();
        targetPoolsAddInstanceRequest.setInstances(instanceReferenceList);

        //todo Remove try catch
        try {
            //execute
            compute.targetPools().addInstance(PROJECT_ID, REGION_NAME,
                    targetPoolName, targetPoolsAddInstanceRequest).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeInstancesFromTargetPool(String targetPoolName, List<InstanceReference>
            instanceReferenceList) {
        TargetPoolsRemoveInstanceRequest targetPoolsRemoveInstanceRequest
                = new TargetPoolsRemoveInstanceRequest();
        targetPoolsRemoveInstanceRequest.setInstances(instanceReferenceList);
        //TODO: remove try catch
        try {
            compute.targetPools().removeInstance(PROJECT_ID, REGION_NAME,
                    targetPoolName, targetPoolsRemoveInstanceRequest).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Get list of running instances in given project and zone.(This method can be used
     * when we need to check whether a given instance is available or not in a given project
     * and zone)
     *
     * @return
     * @throws IOException
     */
    public static InstanceList getInstanceList() throws IOException {
        System.out.println("Listing running Compute Engine Instances");
        Compute.Instances.List instances = compute.instances().
                list(PROJECT_ID, ZONE_NAME).setFilter(RUNNING_FILTER);
        InstanceList instanceList = instances.execute();
        if (instanceList.getItems() == null) {
            log.info("No instances found for specified zone");
            return null;
        } else {
            return instanceList;
        }
    }

    /**
     * Get instance resource URL from given instance name
     *
     * @param instanceName
     * @return
     */
    public static String getInstanceURLFromName(String instanceName) {

        //check whether the given instance is available
        //todo:remove try catch
        String instanceURL;
        try {
            InstanceList instanceList = getInstanceList();
            for (Instance instance : instanceList.getItems()) {
                if (instance.getName().equals(instanceName)) {
                    //instance is available
                    //getInstance URL
                    instanceURL = instance.getSelfLink();
                    return instanceURL;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void createForwardingRule(String forwardingRuleName){
        ForwardingRule forwardingRule = new ForwardingRule();
        forwardingRule.setName(forwardingRuleName);
        try {
            compute.forwardingRules().insert(PROJECT_ID,REGION_NAME,forwardingRule);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * this is a sample method using for testing purposes
     */
    public void sampleMethodForAddingInstancesToTargetPool() {
        List<InstanceReference> instanceReferenceList = new ArrayList<InstanceReference>();

        //remove instance to instance reference list, we should use the instance URL
        InstanceReference instanceReference1 = new InstanceReference();
        instanceReference1.setInstance(getInstanceURLFromName("instance-2"));
        instanceReferenceList.add(instanceReference1);
        addInstancesToTargetPool("testtargetpool", instanceReferenceList);
    }

    /**
     * this is a sample method using for testing purposes
     */
    public void sampleMethodForRemovingInstancesToTargetPool() {
        List<InstanceReference> instanceReferenceList = new ArrayList<InstanceReference>();

        //remove instances to instance reference list, we should use the instance URL
        InstanceReference instanceReference1 = new InstanceReference();
        instanceReference1.setInstance(getInstanceURLFromName("instance-2"));
        instanceReferenceList.add(instanceReference1);
        removeInstancesFromTargetPool("testtargetpool", instanceReferenceList);
    }

}