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

import static org.jboss.as.connector.subsystems.jca.Constants.CACHED_CONNECTION_MANAGER;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinition;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.transform.description.DiscardAttributeChecker;
import org.jboss.as.controller.transform.description.ResourceTransformationDescriptionBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class JcaCachedConnectionManagerDefinition extends PersistentResourceDefinition {
    protected static final PathElement PATH_CACHED_CONNECTION_MANAGER = PathElement.pathElement(CACHED_CONNECTION_MANAGER, CACHED_CONNECTION_MANAGER);
    static final JcaCachedConnectionManagerDefinition INSTANCE = new JcaCachedConnectionManagerDefinition();

    public static SimpleAttributeDefinition DEBUG = SimpleAttributeDefinitionBuilder.create("debug", ModelType.BOOLEAN)
            .setAllowExpression(true)
            .setAllowNull(true)
            .setDefaultValue(new ModelNode().set(false))
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName("debug")
            .build();
    public static SimpleAttributeDefinition ERROR = SimpleAttributeDefinitionBuilder.create("error", ModelType.BOOLEAN)
            .setAllowExpression(true)
            .setAllowNull(true)
            .setDefaultValue(new ModelNode().set(false))
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName("error")
            .build();
    public static SimpleAttributeDefinition IGNORE_UNKNOWN_CONNECTIONS = SimpleAttributeDefinitionBuilder.create("ignore-unknown-connections", ModelType.BOOLEAN)
            .setAllowExpression(true)
            .setAllowNull(true)
            .setDefaultValue(new ModelNode().set(false))
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName("ignore-unknown-connections")
            .build();
    public static SimpleAttributeDefinition INSTALL = SimpleAttributeDefinitionBuilder.create("install", ModelType.BOOLEAN)
            .setAllowExpression(false)
            .setAllowNull(true)
            .setDefaultValue(new ModelNode().set(false))
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .build();

    static final AttributeDefinition[] ATTRIBUTES = {DEBUG, ERROR, IGNORE_UNKNOWN_CONNECTIONS, INSTALL};

    SimpleOperationDefinition GET_NUMBER_OF_CONNECTIONS = new SimpleOperationDefinitionBuilder("get-number-of-connections", JcaExtension.getResourceDescriptionResolver(PATH_CACHED_CONNECTION_MANAGER.getKey()))
            .setRuntimeOnly()
            .build();
    SimpleOperationDefinition LIST_CONNECTIONS = new SimpleOperationDefinitionBuilder("list-connections", JcaExtension.getResourceDescriptionResolver(PATH_CACHED_CONNECTION_MANAGER.getKey()))
            .setRuntimeOnly()
            .build();


    private JcaCachedConnectionManagerDefinition() {
        super(PATH_CACHED_CONNECTION_MANAGER,
                JcaExtension.getResourceDescriptionResolver(PATH_CACHED_CONNECTION_MANAGER.getKey()),
                CachedConnectionManagerAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        ReloadRequiredWriteAttributeHandler handler = new ReloadRequiredWriteAttributeHandler(getAttributes());

        for (AttributeDefinition attr : getAttributes()) {

            if (!attr.equals(INSTALL)) {
                resourceRegistration.registerReadWriteAttribute(attr, null, JcaCachedConnectionManagerWriteHandler.INSTANCE);
            } else {
                resourceRegistration.registerReadWriteAttribute(attr, null, handler);
            }
        }

    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }


    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GET_NUMBER_OF_CONNECTIONS, GetNumberOfConnectionsHandler.INSTANCE);
        resourceRegistration.registerOperationHandler(LIST_CONNECTIONS, ListOfConnectionsHandler.INSTANCE);

    }

    static void registerTransformers110(ResourceTransformationDescriptionBuilder parentBuilder) {

        ResourceTransformationDescriptionBuilder builder = parentBuilder.addChildResource(PATH_CACHED_CONNECTION_MANAGER);
        builder.getAttributeBuilder().setDiscard(DiscardAttributeChecker.ALWAYS, IGNORE_UNKNOWN_CONNECTIONS);

    }


}
