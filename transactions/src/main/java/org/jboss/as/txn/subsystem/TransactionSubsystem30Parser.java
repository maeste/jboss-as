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
package org.jboss.as.txn.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 * The {@link XMLElementReader} that handles the version 3.0 of Transaction subsystem xml.
 */
class TransactionSubsystem30Parser extends TransactionSubsystem14Parser {

    public static final TransactionSubsystem30Parser INSTANCE = new TransactionSubsystem30Parser();

    private TransactionSubsystem30Parser() {
        super(Namespace.TRANSACTIONS_3_0);
    }


    @Override
    protected void readElement(final XMLExtendedStreamReader reader, final Element element, final List<ModelNode> operations, final ModelNode subsystemOperation, final ModelNode logStoreOperation) throws XMLStreamException {
        switch (element) {
            case RECOVERY_ENVIRONMENT: {
                parseRecoveryEnvironmentElement(reader, subsystemOperation);
                break;
            }
            case CORE_ENVIRONMENT: {
                parseCoreEnvironmentElement(reader, subsystemOperation);
                break;
            }
            case COORDINATOR_ENVIRONMENT: {
                parseCoordinatorEnvironmentElement(reader, subsystemOperation);
                break;
            }
            case OBJECT_STORE: {
                parseObjectStoreEnvironmentElementAndEnrichOperation(reader, subsystemOperation);
                break;
            }
            case JTS: {
                parseJts(reader, subsystemOperation);
                break;
            }
            case USEHORNETQSTORE: {
                if (choiceObjectStoreEncountered) {
                    throw unexpectedElement(reader);
                }
                choiceObjectStoreEncountered = true;

                parseUsehornetqstore(reader, logStoreOperation, subsystemOperation);
                subsystemOperation.get(CommonAttributes.USEHORNETQSTORE).set(true);
                break;
            }
            case JDBC_STORE: {
                if (choiceObjectStoreEncountered) {
                    throw unexpectedElement(reader);
                }
                choiceObjectStoreEncountered = true;

                parseJdbcStoreElementAndEnrichOperation(reader, logStoreOperation, subsystemOperation);
                subsystemOperation.get(CommonAttributes.USE_JDBC_STORE).set(true);
                break;
            }
            case LLR_RESOURCES:
                parseLLRs(reader, operations);
                break;
            default: {
                throw unexpectedElement(reader);
            }
        }
    }

    private void parseLLRs(XMLExtendedStreamReader reader, List<ModelNode> operations) throws XMLStreamException {

        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case LLR_RESPOURCE:
                    parseLLR(reader, operations);
                    break;
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    private void parseLLR(XMLExtendedStreamReader reader, List<ModelNode> operations) throws XMLStreamException {
        final ModelNode address = new ModelNode();
        address.add(ModelDescriptionConstants.SUBSYSTEM, TransactionExtension.SUBSYSTEM_NAME);
        address.protect();

        final ModelNode llrAddress = address.clone();

        final ModelNode llrOperation = new ModelNode();
        llrOperation.get(OP).set(ADD);

        String jndiName = null;
//        String tableName = "xids";
        for (Attribute attribute : Attribute.values()) {
            switch (attribute) {
                case JNDI_NAME: {
                    jndiName = rawAttributeText(reader, LLRResourceResourceDefinition.JNDI_NAME.getXmlName(), null);
                    if (jndiName != null) {
                        LLRResourceResourceDefinition.JNDI_NAME.parseAndSetParameter(jndiName, llrOperation, reader);
                    } else {
                      throw missingRequired(reader, LLRResourceResourceDefinition.JNDI_NAME.getXmlName());
                    }
                    break;
                }

                default:
                    break;
            }
        }

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case END_ELEMENT: {
                    if (Element.LLR_RESPOURCE.forName(reader.getLocalName()) == Element.LLR_RESPOURCE && jndiName != null) {
                        llrAddress.add(LLRResourceResourceDefinition.LLR_RESOURCE, jndiName);
                        llrAddress.protect();
                        llrOperation.get(OP_ADDR).set(llrAddress);

                        operations.add(llrOperation);
                        return;
                    } else {
                        if (Element.LLR_RESPOURCE.forName(reader.getLocalName()) == Element.UNKNOWN) {
                            throw unexpectedElement(reader);
                        }
                    }
                    break;
                }
                case START_ELEMENT: {
                    switch (Element.forName(reader.getLocalName())) {
                        case TABLE: {
                            for (Attribute attribute : Attribute.values()) {
                                switch (attribute) {
                                    case NAME: {
                                        String value = rawAttributeText(reader, LLRResourceResourceDefinition.TABLE_NAME.getXmlName(), null);
                                        if (value != null) {
                                            LLRResourceResourceDefinition.TABLE_NAME.parseAndSetParameter(value, llrOperation, reader);
                                        }
                                        break;
                                    }

                                    default:
                                        break;
                                }
                            }
                            break;
                        }
                    }
                }

            }
        }
    }


    /**
     * Reads and trims the text for the given attribute and returns it or {@code defaultValue} if there is no
     * value for the attribute
     *
     * @param reader        source for the attribute text
     * @param attributeName the name of the attribute
     * @param defaultValue  value to return if there is no value for the attribute
     * @return the string representing raw attribute text or {@code defaultValue} if there is none
     */
    private String rawAttributeText(XMLStreamReader reader, String attributeName, String defaultValue) {
        return reader.getAttributeValue("", attributeName) == null
                ? defaultValue :
                reader.getAttributeValue("", attributeName).trim();
    }
}
