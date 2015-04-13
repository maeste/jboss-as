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

import static org.jboss.as.connector.subsystems.jca.Constants.WORKMANAGER;
import static org.jboss.as.connector.subsystems.jca.Constants.WORKMANAGER_LONG_RUNNING;
import static org.jboss.as.connector.subsystems.jca.Constants.WORKMANAGER_SHORT_RUNNING;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.threads.BoundedQueueThreadPoolResourceDefinition;
import org.jboss.as.threads.ThreadsServices;
import org.jboss.dmr.ModelType;

/**
 * @author Stefano Maestri
 */
public class JcaWorkManagerDefinition extends PersistentResourceDefinition {
    protected static final PathElement PATH_WORK_MANAGER = PathElement.pathElement(WORKMANAGER);
    private final boolean registerRuntimeOnly;


    public static SimpleAttributeDefinition NAME = SimpleAttributeDefinitionBuilder.create("name", ModelType.STRING)
            .setAllowExpression(false)
            .setAllowNull(true)
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName("name")
            .build();
    static final AttributeDefinition[] ATTRIBUTES = {NAME};


    private JcaWorkManagerDefinition(final boolean registerRuntimeOnly) {
        super(PATH_WORK_MANAGER,
                JcaExtension.getResourceDescriptionResolver(PATH_WORK_MANAGER.getKey()),
                WorkManagerAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
        this.registerRuntimeOnly = registerRuntimeOnly;
    }

    public static JcaWorkManagerDefinition createInstance(final boolean registerRuntimeOnly) {
        return new JcaWorkManagerDefinition(registerRuntimeOnly);
    }


    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(BoundedQueueThreadPoolResourceDefinition.create(WORKMANAGER_SHORT_RUNNING, ThreadsServices.STANDARD_THREAD_FACTORY_RESOLVER, ThreadsServices.STANDARD_HANDOFF_EXECUTOR_RESOLVER,
                ThreadsServices.EXECUTOR.append(WORKMANAGER_SHORT_RUNNING), registerRuntimeOnly));
        resourceRegistration.registerSubModel(BoundedQueueThreadPoolResourceDefinition.create(WORKMANAGER_LONG_RUNNING, ThreadsServices.STANDARD_THREAD_FACTORY_RESOLVER, ThreadsServices.STANDARD_HANDOFF_EXECUTOR_RESOLVER,
                ThreadsServices.EXECUTOR.append(WORKMANAGER_LONG_RUNNING), registerRuntimeOnly));

    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }

}
