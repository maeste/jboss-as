/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.connector.subsystems.jca;

import org.jboss.as.connector.logging.ConnectorLogger;
import org.jboss.as.connector.services.workmanager.NamedDistributedWorkManager;
import org.jboss.as.connector.util.ConnectorServices;
import org.jboss.as.connector.util.Injection;
import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PropertiesAttributeDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.workmanager.policy.Always;
import org.jboss.jca.core.workmanager.policy.Never;
import org.jboss.jca.core.workmanager.policy.WaterMark;
import org.jboss.jca.core.workmanager.selector.FirstAvailable;
import org.jboss.jca.core.workmanager.selector.MaxFreeThreads;
import org.jboss.jca.core.workmanager.selector.PingTime;

import java.util.Map;

import static org.jboss.as.connector.logging.ConnectorLogger.ROOT_LOGGER;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;


public class JcaDistributedWorkManagerWriteHandler extends AbstractWriteAttributeHandler<JcaSubsystemConfiguration> {

    static JcaDistributedWorkManagerWriteHandler INSTANCE = new JcaDistributedWorkManagerWriteHandler();

    private JcaDistributedWorkManagerWriteHandler() {
        super(JcaDistributedWorkManagerDefinition.ATTRIBUTES);
    }

    @Override
    protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName, ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<JcaSubsystemConfiguration> jcaSubsystemConfigurationHandbackHolder) throws OperationFailedException {
        final ModelNode address = operation.require(OP_ADDR);
        final String name = PathAddress.pathAddress(address).getLastElement().getValue();


        Object wm =  context.getServiceRegistry(true).getService(ConnectorServices.WORKMANAGER_SERVICE.append(name)).getValue();

        if (wm == null || ! (wm instanceof NamedDistributedWorkManager)) {
            throw ConnectorLogger.ROOT_LOGGER.failedToFindDistributedWorkManager(name);
        }

        NamedDistributedWorkManager namedDistributedWorkManager = (NamedDistributedWorkManager) wm;

        Injection injector = new Injection();

        if (attributeName.equals(JcaDistributedWorkManagerDefinition.POLICY.getName())) {
            switch (JcaDistributedWorkManagerDefinition.PolicyValue.valueOf(resolvedValue.asString())) {
                case NEVER: {
                    namedDistributedWorkManager.setPolicy(new Never());
                    break;
                }
                case ALWAYS: {
                    namedDistributedWorkManager.setPolicy(new Always());
                    break;
                }
                case WATERMARK: {
                    namedDistributedWorkManager.setPolicy(new WaterMark());
                    break;
                }
                default: {
                    throw ROOT_LOGGER.unsupportedPolicy(resolvedValue.asString());
                }
            }
        } else if (attributeName.equals(JcaDistributedWorkManagerDefinition.SELECTOR.getName())) {
            switch (JcaDistributedWorkManagerDefinition.SelectorValue.valueOf(resolvedValue.asString())) {
                case FIRST_AVAILABLE: {
                    namedDistributedWorkManager.setSelector(new FirstAvailable());
                    break;
                }
                case MAX_FREE_THREADS: {
                    namedDistributedWorkManager.setSelector(new MaxFreeThreads());
                    break;
                }
                case PING_TIME: {
                    namedDistributedWorkManager.setSelector(new PingTime());
                    break;
                }
                default: {
                    throw ROOT_LOGGER.unsupportedSelector(resolvedValue.asString());
                }
            }
        } else if (attributeName.equals(JcaDistributedWorkManagerDefinition.POLICY_OPTIONS.getName()) && namedDistributedWorkManager.getPolicy() != null) {
            for (Map.Entry<String, String> entry : ((PropertiesAttributeDefinition) JcaDistributedWorkManagerDefinition.POLICY_OPTIONS).unwrap(context, operation).entrySet()) {
                try {
                    injector.inject(namedDistributedWorkManager.getPolicy(), entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    ROOT_LOGGER.unsupportedPolicyOption(entry.getKey());

                }

            }
        } else if (attributeName.equals(JcaDistributedWorkManagerDefinition.SELECTOR_OPTIONS.getName())) {
            for (Map.Entry<String, String> entry : ((PropertiesAttributeDefinition) JcaDistributedWorkManagerDefinition.SELECTOR_OPTIONS).unwrap(context, operation).entrySet()) {
                try {
                    injector.inject(namedDistributedWorkManager.getSelector(), entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    ROOT_LOGGER.unsupportedSelectorOption(entry.getKey());
                }

            }
        }


        return false;

    }

    @Override
    protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName, ModelNode valueToRestore, ModelNode valueToRevert, JcaSubsystemConfiguration handback) throws OperationFailedException {

        CachedConnectionManager ccm = (CachedConnectionManager) context.getServiceRegistry(true).getService(ConnectorServices.CCM_SERVICE).getValue();

        if (attributeName.equals(JcaCachedConnectionManagerDefinition.DEBUG.getName())) {
            ccm.setDebug(valueToRestore.asBoolean());
        } else if (attributeName.equals(JcaCachedConnectionManagerDefinition.ERROR.getName())) {
            ccm.setError(valueToRestore.asBoolean());
        } else if (attributeName.equals(JcaCachedConnectionManagerDefinition.IGNORE_UNKNOWN_CONNECTIONS.getName())) {
            ccm.setIgnoreUnknownConnections(valueToRestore.asBoolean());
        }

    }
}
