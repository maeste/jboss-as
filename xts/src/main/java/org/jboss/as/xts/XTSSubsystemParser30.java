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

package org.jboss.as.xts;

import static org.jboss.as.xts.XTSSubsystemDefinition.ENVIRONMENT_HOST;
import static org.jboss.as.xts.XTSSubsystemDefinition.ENVIRONMENT_PATH;
import static org.jboss.as.xts.XTSSubsystemDefinition.ENVIRONMENT_PORT;
import static org.jboss.as.xts.XTSSubsystemDefinition.ENVIRONMENT_PROTOCOL;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;


class XTSSubsystemParser30 extends XTSSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {


    /**
     * {@inheritDoc}          XMLExtendedStreamReader reader
     */
    @Override
    protected void writeXtsEnvironment(XMLExtendedStreamWriter writer, ModelNode node) throws XMLStreamException {
        if (node.hasDefined(ENVIRONMENT_PROTOCOL.getName()) ||
                node.hasDefined(ENVIRONMENT_HOST.getName()) ||
                node.hasDefined(ENVIRONMENT_PORT.getName()) ||
                node.hasDefined(ENVIRONMENT_PATH.getName()) ) {
            writer.writeStartElement(Element.XTS_ENVIRONMENT.getLocalName());
            ENVIRONMENT_PROTOCOL.marshallAsAttribute(node, writer);
            ENVIRONMENT_HOST.marshallAsAttribute(node, writer);
            ENVIRONMENT_PORT.marshallAsAttribute(node, writer);
            ENVIRONMENT_PATH.marshallAsAttribute(node, writer);
            writer.writeEndElement();
        }
    }


    /**
     * Handle the xts-environment element
     *
     * @param reader
     * @param subsystem
     * @return ModelNode for the core-environment
     * @throws javax.xml.stream.XMLStreamException
     */
    @Override
    protected void parseXTSEnvironmentElement(XMLExtendedStreamReader reader, ModelNode subsystem) throws XMLStreamException {

        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            ParseUtils.requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case PROTOCOL:
                    ENVIRONMENT_PROTOCOL.parseAndSetParameter(value, subsystem, reader);
                    break;
                case HOST:
                    ENVIRONMENT_HOST.parseAndSetParameter(value, subsystem, reader);
                    break;
                case PORT:
                    ENVIRONMENT_PORT.parseAndSetParameter(value, subsystem, reader);
                    break;
                case PATH:
                    ENVIRONMENT_PATH.parseAndSetParameter(value, subsystem, reader);
                    break;
                default:
                    throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }
        // Handle elements
        ParseUtils.requireNoContent(reader);
    }

}
