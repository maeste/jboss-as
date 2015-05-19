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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.transform.description.ResourceTransformationDescriptionBuilder;
import org.jboss.as.controller.transform.description.TransformationDescription;
import org.jboss.as.controller.transform.description.TransformationDescriptionBuilder;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class JcaSubsystemRootDefinition extends PersistentResourceDefinition {
    protected static final PathElement PATH_SUBSYSTEM = PathElement.pathElement(SUBSYSTEM, JcaExtension.SUBSYSTEM_NAME);
    private final boolean registerRuntimeOnly;


    private JcaSubsystemRootDefinition(final boolean registerRuntimeOnly) {
        super(PATH_SUBSYSTEM,
                JcaExtension.getResourceDescriptionResolver(),
                JcaSubsystemAdd.INSTANCE,
                JcaSubSystemRemove.INSTANCE);
        this.registerRuntimeOnly = registerRuntimeOnly;
    }

    public static JcaSubsystemRootDefinition createInstance(final boolean registerRuntimeOnly) {
        return new JcaSubsystemRootDefinition(registerRuntimeOnly);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);


    }

    static final AttributeDefinition[] ATTRIBUTES = {};


    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }

    @Override
    public List<? extends PersistentResourceDefinition> getChildren() {
        final PersistentResourceDefinition[] CHILDREN = {
                JcaArchiveValidationDefinition.INSTANCE,
                JcaBeanValidationDefinition.INSTANCE,
                TracerDefinition.INSTANCE,
                JcaWorkManagerDefinition.createInstance(registerRuntimeOnly),
                JcaDistributedWorkManagerDefinition.createInstance(registerRuntimeOnly),
                JcaCachedConnectionManagerDefinition.INSTANCE,
                JcaBootstrapContextDefinition.INSTANCE
        };
        return Arrays.asList(CHILDREN);
    }


    static void registerTransformers(SubsystemRegistration subsystem) {
        ResourceTransformationDescriptionBuilder builder20 = TransformationDescriptionBuilder.Factory.createSubsystemInstance();
        builder20.discardChildResource(TracerDefinition.PATH_TRACER);
        TransformationDescription.Tools.register(builder20.build(), subsystem, ModelVersion.create(2, 0, 0));
    }
}
