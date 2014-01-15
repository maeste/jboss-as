/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.as.txn.subsystem;

import static org.jboss.as.txn.TransactionMessages.MESSAGES;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.AttributeMarshaller;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.validation.ParameterValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * TODO class javadoc.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class LLRResourceResourceDefinition extends SimpleResourceDefinition {

    private static String LLR_RESOURCE = "llr-resource";

    private static final PathElement PATH_LLR_RESOURCE = PathElement.pathElement(LLR_RESOURCE);

    static SimpleAttributeDefinition JNDI_NAME =  new SimpleAttributeDefinitionBuilder("jndi-name", ModelType.STRING)
            .setAllowExpression(true)
            .setAllowNull(false)
            .setValidator(new ParameterValidator() {
                    @Override
                    public void validateParameter(String parameterName, ModelNode value) throws OperationFailedException {
                        if (value.isDefined()) {
                            if (value.getType() != ModelType.EXPRESSION) {
                                String str = value.asString();
                                if (!str.startsWith("java:/") && !str.startsWith("java:jboss/")) {
                                    throw MESSAGES.jndiNameInvalidFormat();
                                }
                            }
                        } else {
                            throw MESSAGES.jndiNameRequired();
                        }
                    }

                    @Override
                    public void validateResolvedParameter(String parameterName, ModelNode value) throws OperationFailedException {
                        validateParameter(parameterName, value.resolve());
                    }
                })
            .build();

    static SimpleAttributeDefinition TABLE_NAME =  new SimpleAttributeDefinitionBuilder("table-name", ModelType.STRING)
                .setAllowExpression(true)
            .setAllowNull(false)
            .setXmlName("name")
            .setAttributeMarshaller(new AttributeMarshaller() {
                public void marshallAsElement(AttributeDefinition attribute, ModelNode resourceModel, boolean marshallDefault, XMLStreamWriter writer) throws XMLStreamException {
                    if (resourceModel.hasDefined(attribute.getName())) {
                        writer.writeStartElement(Element.TABLE.getLocalName());
                        writer.writeAttribute(attribute.getXmlName(), resourceModel.get(attribute.getName()).asString());
                        writer.writeEndElement();
                    }

                }
            })
            .build();


    public LLRResourceResourceDefinition() {
        super(PATH_LLR_RESOURCE,
                TransactionExtension.getResourceDescriptionResolver(LLR_RESOURCE),
                LLRResourceAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(JNDI_NAME, null, new ReloadRequiredWriteAttributeHandler());
        resourceRegistration.registerReadWriteAttribute(TABLE_NAME, null, new ReloadRequiredWriteAttributeHandler());
    }
}

