/*
 *
 *  JBoss, Home of Professional Open Source.
 *  Copyright 2013, Red Hat, Inc., and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 * /
 */

package org.jboss.as.connector.subsystems.datasources;

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentResourceXMLParser;

/**
 * @author Stefano Maestri
 */
public class DatasourcesSubsystemParser_4_0 extends PersistentResourceXMLParser {
    protected static final DatasourcesSubsystemParser_4_0 INSTANCE = new DatasourcesSubsystemParser_4_0();
    private static final PersistentResourceXMLDescription xmlDescription;

    static {
        xmlDescription = builder(DataSourcesSubsystemRootDefinition.createInstance(false), Namespace.DATASOURCES_4_0.getUriString())
                .addChild(builder(DataSourceDefinition.createInstance(false, false))
                        .setXmlElementName("datasource")
                        .addAttributes(Constants.DATASOURCE_ATTRIBUTE)
                        .addAttributes(Constants.DATASOURCE_PROPERTIES_ATTRIBUTES)
                        .addChild(builder(ConnectionPropertyDefinition.INSTANCE)
                                .setXmlElementName("connection-property")
                                .addAttributes(ConnectionPropertyDefinition.ATTRIBUTES)))
                .addChild(builder(XaDataSourceDefinition.createInstance(false, false))
                        .addAttributes(Constants.XA_DATASOURCE_ATTRIBUTE)
                        .addAttributes(Constants.XA_DATASOURCE_PROPERTIES_ATTRIBUTES)
                        .addChild(builder(XaDataSourcePropertyDefinition.INSTANCE)
                                .setXmlElementName("xa-datasource-property")
                                .addAttributes(XaDataSourcePropertyDefinition.ATTRIBUTES)))
                .addChild(builder(JdbcDriverDefinition.INSTANCE)
                        .setXmlWrapperElement("drivers")
                        .addAttributes(Constants.JDBC_DRIVER_ATTRIBUTES))
                .build();
    }

    private DatasourcesSubsystemParser_4_0() {
    }


    @Override
    public PersistentResourceXMLDescription getParserDescription() {
        return xmlDescription;
    }
}

