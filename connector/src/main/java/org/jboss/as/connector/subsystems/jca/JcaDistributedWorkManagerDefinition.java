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

import static org.jboss.as.connector.subsystems.jca.Constants.DISTRIBUTED_WORKMANAGER;
import static org.jboss.as.connector.subsystems.jca.Constants.WORKMANAGER_LONG_RUNNING;
import static org.jboss.as.connector.subsystems.jca.Constants.WORKMANAGER_SHORT_RUNNING;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.PropertiesAttributeDefinition;
import org.jboss.as.controller.ReadResourceNameOperationStepHandler;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.operations.validation.EnumValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.threads.BoundedQueueThreadPoolResourceDefinition;
import org.jboss.as.threads.ThreadsServices;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class JcaDistributedWorkManagerDefinition extends PersistentResourceDefinition {
    protected static final PathElement PATH_DISTRIBUTED_WORK_MANAGER = PathElement.pathElement(DISTRIBUTED_WORKMANAGER);
    private final boolean registerRuntimeOnly;

    private JcaDistributedWorkManagerDefinition(final boolean registerRuntimeOnly) {
        super(PATH_DISTRIBUTED_WORK_MANAGER,
                JcaExtension.getResourceDescriptionResolver(PATH_DISTRIBUTED_WORK_MANAGER.getKey()),
                DistributedWorkManagerAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
        this.registerRuntimeOnly = registerRuntimeOnly;
    }

    public static JcaDistributedWorkManagerDefinition createInstance(final boolean registerRuntimeOnly) {
        return new JcaDistributedWorkManagerDefinition(registerRuntimeOnly);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {

        for (final AttributeDefinition ad : READONLY_ATTRIBUTES) {
            resourceRegistration.registerReadOnlyAttribute(ad, ReadResourceNameOperationStepHandler.INSTANCE);
        }

        for (final AttributeDefinition ad : RELOADREQUIRED_ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(ad, null, new ReloadRequiredWriteAttributeHandler(ad));
        }

        for (final AttributeDefinition ad : RUNTIME_ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(ad, null, JcaDistributedWorkManagerWriteHandler.INSTANCE);
        }

    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(BoundedQueueThreadPoolResourceDefinition.create(WORKMANAGER_SHORT_RUNNING, ThreadsServices.STANDARD_THREAD_FACTORY_RESOLVER, ThreadsServices.STANDARD_HANDOFF_EXECUTOR_RESOLVER,
                ThreadsServices.EXECUTOR.append(WORKMANAGER_SHORT_RUNNING), registerRuntimeOnly));
        resourceRegistration.registerSubModel(BoundedQueueThreadPoolResourceDefinition.create(WORKMANAGER_LONG_RUNNING, ThreadsServices.STANDARD_THREAD_FACTORY_RESOLVER, ThreadsServices.STANDARD_HANDOFF_EXECUTOR_RESOLVER,
                ThreadsServices.EXECUTOR.append(WORKMANAGER_LONG_RUNNING), registerRuntimeOnly));

    }

    static final SimpleAttributeDefinition NAME = SimpleAttributeDefinitionBuilder.create("name", ModelType.STRING)
            .setAllowExpression(false)
            .setAllowNull(true)
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName("name")
            .build();
    static final SimpleAttributeDefinition SELECTOR = SimpleAttributeDefinitionBuilder.create("selector", ModelType.STRING)
            .setAllowExpression(true)
            .setAllowNull(true)
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName(Attribute.SELECTOR.getLocalName())
            .setValidator(new EnumValidator<SelectorValue>(SelectorValue.class, true, true))
            .setDefaultValue(new ModelNode(SelectorValue.PING_TIME.name()))
            .build();
    static final SimpleAttributeDefinition POLICY = SimpleAttributeDefinitionBuilder.create("policy", ModelType.STRING)
            .setAllowExpression(true)
            .setAllowNull(true)
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName(Attribute.POLICY.getLocalName())
            .setValidator(new EnumValidator<PolicyValue>(PolicyValue.class, true, true))
            .setDefaultValue(new ModelNode(PolicyValue.WATERMARK.name()))
            .build();
    static final PropertiesAttributeDefinition POLICY_OPTIONS = new PropertiesAttributeDefinition.Builder("policy-options", true)
            .setWrapXmlElement(false)
            .setAllowExpression(true)
            .setXmlName(Attribute.POLICY_OPTION.getLocalName())
            .build();
    static final PropertiesAttributeDefinition SELECTOR_OPTIONS = new PropertiesAttributeDefinition.Builder("selector-options", true)
            .setWrapXmlElement(false)
            .setAllowExpression(true)
            .setXmlName(Attribute.SELECTOR_OPTION.getLocalName())
            .build();
    static final SimpleAttributeDefinition TRANSPORT_JGROPUS_STACK = SimpleAttributeDefinitionBuilder.create("transport-jgroups-stack", ModelType.STRING).setAllowExpression(true)
            .setAllowNull(true)
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName(Attribute.JGROUPS_STACK.getLocalName())
            .build();
    static final SimpleAttributeDefinition TRANSPORT_JGROPUS_CLUSTER = SimpleAttributeDefinitionBuilder.create("transport-jgroups-cluster", ModelType.STRING).setAllowExpression(true)
            .setAllowNull(true)
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName(Attribute.JGROUPS_CLUSTER.getLocalName())
            .setDefaultValue(new ModelNode("jca"))
            .build();
    static final SimpleAttributeDefinition TRANSPORT_REQUEST_TIMEOUT = SimpleAttributeDefinitionBuilder.create("transport-request-timeout", ModelType.LONG).setAllowExpression(true)
            .setAllowNull(true)
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName(Attribute.REQUEST_TIMEOUT.getLocalName())
            .setDefaultValue(new ModelNode("10000"))
            .build();


    public static AttributeDefinition[] RUNTIME_ATTRIBUTES = {
            POLICY,
            SELECTOR,
            POLICY_OPTIONS,
            SELECTOR_OPTIONS

    };

    public static AttributeDefinition[] RELOADREQUIRED_ATTRIBUTES = {
            TRANSPORT_JGROPUS_CLUSTER,
            TRANSPORT_JGROPUS_STACK,
            TRANSPORT_REQUEST_TIMEOUT
    };


    public static AttributeDefinition[] READONLY_ATTRIBUTES = {
            NAME
    };

    public static AttributeDefinition[] ATTRIBUTES = {
            POLICY,
            SELECTOR,
            POLICY_OPTIONS,
            SELECTOR_OPTIONS,
            TRANSPORT_JGROPUS_CLUSTER,
            TRANSPORT_JGROPUS_STACK,
            TRANSPORT_REQUEST_TIMEOUT,
            NAME
    };


    public enum PolicyValue {
        NEVER,
        ALWAYS,
        WATERMARK;
    }

    public enum SelectorValue {
        FIRST_AVAILABLE,
        PING_TIME,
        MAX_FREE_THREADS;
    }
}
