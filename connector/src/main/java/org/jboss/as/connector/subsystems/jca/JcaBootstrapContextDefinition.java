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

import static org.jboss.as.connector.subsystems.jca.Constants.BOOTSTRAP_CONTEXT;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReadResourceNameOperationStepHandler;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class JcaBootstrapContextDefinition extends PersistentResourceDefinition {
    protected static final PathElement PATH_BOOTSTRAP_CONTEXT = PathElement.pathElement(BOOTSTRAP_CONTEXT);
    static final JcaBootstrapContextDefinition INSTANCE = new JcaBootstrapContextDefinition();

    static final SimpleAttributeDefinition NAME = SimpleAttributeDefinitionBuilder.create("name", ModelType.STRING)
            .setAllowExpression(false)
            .setAllowNull(true)
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName("name")
            .build();
    static final SimpleAttributeDefinition WORKMANAGER = SimpleAttributeDefinitionBuilder.create("workmanager", ModelType.STRING)
            .setAllowExpression(true)
            .setAllowNull(false)
            .setMeasurementUnit(MeasurementUnit.NONE)
            .setRestartAllServices()
            .setXmlName("workmanager")
            .build();
    public static final AttributeDefinition[] ATTRIBUTES = {NAME, WORKMANAGER};

    private JcaBootstrapContextDefinition() {
        super(PATH_BOOTSTRAP_CONTEXT,
                JcaExtension.getResourceDescriptionResolver(PATH_BOOTSTRAP_CONTEXT.getKey()),
                BootstrapContextAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {

        for (final AttributeDefinition ad : ATTRIBUTES) {
            if (ad.equals(NAME)) {
                resourceRegistration.registerReadOnlyAttribute(ad, ReadResourceNameOperationStepHandler.INSTANCE);
            } else {
                resourceRegistration.registerReadWriteAttribute(ad, null, new ReloadRequiredWriteAttributeHandler(ad));
            }
        }

    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }


}
