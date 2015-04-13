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

import static org.jboss.as.connector.subsystems.jca.Constants.ARCHIVE_VALIDATION;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class JcaArchiveValidationDefinition extends PersistentResourceDefinition {
    protected static final PathElement PATH_ARCHIVE_VALIDATION = PathElement.pathElement(ARCHIVE_VALIDATION, ARCHIVE_VALIDATION);
    static final JcaArchiveValidationDefinition INSTANCE = new JcaArchiveValidationDefinition();

    public static SimpleAttributeDefinition ARCHIVE_VALIDATION_ENABLED = SimpleAttributeDefinitionBuilder.create("enabled", ModelType.BOOLEAN)
                .setAllowExpression(true)
                .setAllowNull(true)
                .setDefaultValue(new ModelNode().set(true))
                .setMeasurementUnit(MeasurementUnit.NONE)
                .setRestartAllServices()
                .setXmlName("enabled")
                .build();
    public static SimpleAttributeDefinition ARCHIVE_VALIDATION_FAIL_ON_ERROR = SimpleAttributeDefinitionBuilder.create("fail-on-error", ModelType.BOOLEAN)
                .setAllowExpression(true)
                .setAllowNull(true)
                .setDefaultValue(new ModelNode().set(true))
                .setMeasurementUnit(MeasurementUnit.NONE)
                .setRestartAllServices()
                .setXmlName("fail-on-error")
                .build();
    public static SimpleAttributeDefinition ARCHIVE_VALIDATION_FAIL_ON_WARN = SimpleAttributeDefinitionBuilder.create("fail-on-warn", ModelType.BOOLEAN)
                .setAllowExpression(true)
                .setAllowNull(true)
                .setDefaultValue(new ModelNode().set(false))
                .setMeasurementUnit(MeasurementUnit.NONE)
                .setRestartAllServices()
                .setXmlName("fail-on-warn")
                .build();

    static final AttributeDefinition[] ATTRIBUTES = {ARCHIVE_VALIDATION_ENABLED, ARCHIVE_VALIDATION_FAIL_ON_ERROR, ARCHIVE_VALIDATION_FAIL_ON_WARN};

    private JcaArchiveValidationDefinition() {
        super(PATH_ARCHIVE_VALIDATION,
                JcaExtension.getResourceDescriptionResolver(PATH_ARCHIVE_VALIDATION.getKey()),
                ArchiveValidationAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }



    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }





}
