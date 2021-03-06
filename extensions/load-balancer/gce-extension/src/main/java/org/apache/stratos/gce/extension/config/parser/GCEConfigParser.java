package org.apache.stratos.gce.extension.config.parser;

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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.common.exception.MalformedConfigurationFileException;
import org.apache.stratos.common.util.AxiomXpathParserUtil;
import org.apache.stratos.gce.extension.config.Constants;
import org.apache.stratos.gce.extension.config.GCEContext;
import org.apache.stratos.load.balancer.extension.api.exception.LoadBalancerExtensionException;

import javax.xml.namespace.QName;

/**
 * parse the gce-configuration.xml
 */
public class GCEConfigParser {
    private static final Log log = LogFactory.getLog(GCEConfigParser.class);
    private static GCEContext gceContext;

    /**
     * Parse the gce-configuration.xml file when the extension is starting up.
     *
     * @param documentElement axiom document element.
     * @throws MalformedConfigurationFileException
     */
    public static void parse(OMElement documentElement) throws LoadBalancerExtensionException {

        if (log.isDebugEnabled()) {
            log.debug("Parsing the configuration xml file ");
        }

        //get cep info
        OMElement cepInfoElement = AxiomXpathParserUtil.getFirstChildElement(documentElement,
                Constants.CEP_STATS_PUBLISHER_ELEMENT);
        extractCepConfiguration(cepInfoElement);

        //get GCE IaaS info
        OMElement gceIaasInfoElement = AxiomXpathParserUtil.getFirstChildElement(documentElement,
                Constants.IAAS_PROPERTIES_ELEMENT);
        extractGceIaasInformation(gceIaasInfoElement);

        //get heath check info
        OMElement healthCheckPropertiesElement = AxiomXpathParserUtil.getFirstChildElement(documentElement,
                Constants.HEALTH_CHECK_PROPERTIES_ELEMENT);
        extractHealthCheckProperties(healthCheckPropertiesElement);

        //extract other properties
        extractOtherProperties(documentElement);

        //validate all extracted properties - just a null check
        if (log.isDebugEnabled()) {
            log.debug("Validating the properties read from configuration xml file");
        }
        gceContext.validate();
    }

    /**
     * Extract cep ip and port and store in gceContext object
     *
     * @param cepInfoElement - OMElement which is containing the cep information
     */
    private static void extractCepConfiguration(OMElement cepInfoElement) {
        //Check whether the cep stat publisher enabled or not
        QName qName = new QName(Constants.CEP_STATS_PUBLISHER_ENABLED);
        String enabled = cepInfoElement.getAttributeValue(qName);

        gceContext = GCEContext.getInstance();
        gceContext.setCepStatsPublisherEnabled(Boolean.parseBoolean(enabled));

        //if stat publisher is enabled
        if (Boolean.parseBoolean(enabled)) {
            //extract rest configuration of cep

            OMElement thriftReceiverIpElement = AxiomXpathParserUtil.getFirstChildElement(cepInfoElement,
                    Constants.THRIFT_RECEIVER_IP);
            OMElement thriftReceiverPortElement = AxiomXpathParserUtil.getFirstChildElement(cepInfoElement,
                    Constants.THRIFT_RECEIVER_PORT);

            if (thriftReceiverIpElement != null) {
                //set extracted ip to gceContext object
                gceContext.setThriftReceiverIp(thriftReceiverIpElement.getText());
                if (log.isDebugEnabled()) {
                    log.debug("Thrift receiver IP: " + thriftReceiverIpElement.getText());
                }
            }

            if (thriftReceiverPortElement != null) {
                //set extracted port to gceContext object
                gceContext.setThriftReceiverPort(thriftReceiverPortElement.getText());
                if (log.isDebugEnabled()) {
                    log.debug("Thrift receiver port: " + thriftReceiverPortElement.getText());
                }
            }
        }
    }

    /**
     * extract Iaas properties from given OMElement and store it in gceContext object
     *
     * @param gceIaasInfoElement - OMElement which contains Iaas properties
     */
    private static void extractGceIaasInformation(OMElement gceIaasInfoElement) {
        OMElement projectNameElement = AxiomXpathParserUtil.getFirstChildElement(gceIaasInfoElement, Constants.PROJECT_NAME);
        OMElement projectIdElement = AxiomXpathParserUtil.getFirstChildElement(gceIaasInfoElement, Constants.PROJECT_ID);
        OMElement regionNameElement = AxiomXpathParserUtil.getFirstChildElement(gceIaasInfoElement, Constants.REGION_NAME);
        OMElement keyFilePathElement = AxiomXpathParserUtil.getFirstChildElement(gceIaasInfoElement, Constants.KEY_FILE_PATH);
        OMElement gceAccountIdElement = AxiomXpathParserUtil.getFirstChildElement(gceIaasInfoElement, Constants.GCE_ACCOUNT_ID);
        OMElement networkNameElement = AxiomXpathParserUtil.getFirstChildElement(gceIaasInfoElement, Constants.NETWORK_NAME);

        //set extracted properties to gceContext object
        if (projectNameElement != null) {
            gceContext.setProjectName(projectNameElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Project Name: " + projectNameElement.getText());
            }
        }

        if (projectIdElement != null) {
            gceContext.setProjectID(projectIdElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Project ID: " + projectIdElement.getText());
            }
        }

        if (regionNameElement != null) {
            gceContext.setRegionName(regionNameElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Region Name: " + regionNameElement.getText());
            }
        }

        if (keyFilePathElement != null) {
            gceContext.setKeyFilePath(keyFilePathElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Key file path: " + keyFilePathElement.getText());
            }
        }

        if (gceAccountIdElement != null) {
            gceContext.setGceAccountID(gceAccountIdElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("GCE Account ID: " + gceAccountIdElement.getText());
            }
        }
        if (networkNameElement != null) {
            gceContext.setNetworkName(networkNameElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Network Name: " + networkNameElement.getText());
            }
        }
    }

    /**
     * extract health check properties from given OMElement and store it in gceContext object
     *
     * @param healthCheckPropertiesElement - OMElement which contains health check properties
     */
    private static void extractHealthCheckProperties(OMElement healthCheckPropertiesElement) {
        OMElement healthCheckRequestPathElement = AxiomXpathParserUtil.getFirstChildElement(healthCheckPropertiesElement,
                Constants.HEALTH_CHECK_REQUEST_PATH);
        OMElement healthCheckPortElement = AxiomXpathParserUtil.getFirstChildElement(healthCheckPropertiesElement,
                Constants.HEALTH_CHECK_PORT);
        OMElement healthCheckTimeoutSecElement = AxiomXpathParserUtil.getFirstChildElement(healthCheckPropertiesElement,
                Constants.HEALTH_CHECK_TIME_OUT_SEC);
        OMElement healthCheckUnhealthyThresholdElement = AxiomXpathParserUtil.getFirstChildElement(healthCheckPropertiesElement,
                Constants.HEALTH_CHECK_UNHEALTHY_THRESHOLD);
        OMElement heathCheckIntervalSecElement = AxiomXpathParserUtil.getFirstChildElement(healthCheckPropertiesElement,
                Constants.HEALTH_CHECK_INTERVAL_SEC);
        OMElement healthCheckHealthyThresholdElement = AxiomXpathParserUtil.getFirstChildElement(healthCheckPropertiesElement,
                Constants.HEALTH_CHECK_HEALTHY_THRESHOLD);

        //set extracted properties to gceContext object
        if (healthCheckRequestPathElement != null) {
            gceContext.setHealthCheckRequestPath(healthCheckRequestPathElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Health check request path: " + healthCheckRequestPathElement.getText());
            }
        }

        if (healthCheckPortElement != null) {
            gceContext.setHealthCheckPort(healthCheckPortElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Health check port: " + healthCheckPortElement.getText());
            }
        }

        if (healthCheckTimeoutSecElement != null) {
            gceContext.setHealthCheckTimeOutSec(healthCheckTimeoutSecElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Health check timeout sec: " + healthCheckTimeoutSecElement.getText());
            }
        }

        if (healthCheckUnhealthyThresholdElement != null) {
            gceContext.setHealthCheckUnhealthyThreshold(healthCheckUnhealthyThresholdElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Health check unhealthy threshold: " + healthCheckUnhealthyThresholdElement.getText());
            }
        }

        if (heathCheckIntervalSecElement != null) {
            gceContext.setHealthCheckIntervalSec(heathCheckIntervalSecElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Health check interval sec: " + heathCheckIntervalSecElement.getText());
            }
        }

        if (healthCheckHealthyThresholdElement != null) {
            gceContext.setHealthCheckHealthyThreshold(healthCheckHealthyThresholdElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Health check healthy threshold: " + healthCheckHealthyThresholdElement.getText());
            }
        }
    }

    /**
     * extract all other properties which is not extracted from above methods and store in gceContext object
     *
     * @param documentElement - OMElement which contains other properties
     */
    private static void extractOtherProperties(OMElement documentElement) {
        OMElement operationTimeoutElement = AxiomXpathParserUtil.getFirstChildElement(documentElement, Constants.
                OPERATION_TIMEOUT);
        OMElement namePrefixElement = AxiomXpathParserUtil.getFirstChildElement(documentElement, Constants.NAME_PREFIX);
        OMElement gceApiUrlElement = AxiomXpathParserUtil.getFirstChildElement(documentElement, Constants.GCE_API_URL);

        //set extracted properties to gceContext object
        if (operationTimeoutElement != null) {
            gceContext.setOperationTimeout(operationTimeoutElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Operation timeout: " + operationTimeoutElement.getText());
            }
        }

        if (namePrefixElement != null) {
            gceContext.setNamePrefix(namePrefixElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("Name prefix: " + namePrefixElement.getText());
            }
        }

        if (gceApiUrlElement != null) {
            gceContext.setGceApiUrl(gceApiUrlElement.getText());
            if (log.isDebugEnabled()) {
                log.debug("GCE API URL: " + gceApiUrlElement.getText());
            }
        }
    }
}
