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

package org.jboss.as.connector.subsystems.jca;

import static org.jboss.as.connector.subsystems.jca.Constants.BOOTSTRAP_CONTEXTS;
import static org.jboss.as.connector.subsystems.jca.Constants.WORKMANAGER_LONG_RUNNING;
import static org.jboss.as.connector.subsystems.jca.Constants.WORKMANAGER_SHORT_RUNNING;
import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentResourceXMLParser;
import org.jboss.as.threads.BoundedQueueThreadPoolResourceDefinition;
import org.jboss.as.threads.ThreadsParser2_0;
import org.jboss.as.threads.ThreadsServices;

/**
 * @author Stefano Maestri
 */
public class JcaSubsystemParser_4_0 extends PersistentResourceXMLParser {
    protected static final JcaSubsystemParser_4_0 INSTANCE = new JcaSubsystemParser_4_0();
    private static final PersistentResourceXMLDescription xmlDescription;

    static {
        xmlDescription = builder(JcaSubsystemRootDefinition.createInstance(false), Namespace.JCA_4_0.getUriString())
                .addChild(builder(JcaArchiveValidationDefinition.INSTANCE)
                        .addAttributes(JcaArchiveValidationDefinition.ATTRIBUTES))
                .addChild(builder(JcaBeanValidationDefinition.INSTANCE)
                        .addAttributes(JcaBeanValidationDefinition.ATTRIBUTES))
                .addChild(builder(TracerDefinition.INSTANCE)
                        .addAttributes(TracerDefinition.ATTRIBUTES))
                .addChild(builder(JcaWorkManagerDefinition.createInstance(false))
                                .setXmlElementName(Constants.WORKMANAGER)
                                .addAttributes(JcaWorkManagerDefinition.ATTRIBUTES)
                                .addChild(ThreadsParser2_0.getBoundedQueueThreadPoolParser(BoundedQueueThreadPoolResourceDefinition.create(WORKMANAGER_SHORT_RUNNING, ThreadsServices.STANDARD_THREAD_FACTORY_RESOLVER, ThreadsServices.STANDARD_HANDOFF_EXECUTOR_RESOLVER,
                                        ThreadsServices.EXECUTOR.append(WORKMANAGER_SHORT_RUNNING)))
                                        .setXmlElementName(WORKMANAGER_SHORT_RUNNING).setInheritName(true))
                                .addChild(ThreadsParser2_0.getBoundedQueueThreadPoolParser(BoundedQueueThreadPoolResourceDefinition.create(WORKMANAGER_LONG_RUNNING, ThreadsServices.STANDARD_THREAD_FACTORY_RESOLVER, ThreadsServices.STANDARD_HANDOFF_EXECUTOR_RESOLVER,
                                        ThreadsServices.EXECUTOR.append(WORKMANAGER_LONG_RUNNING)))
                                        .setXmlElementName(WORKMANAGER_LONG_RUNNING).setInheritName(true))
                )

                .addChild(builder(JcaDistributedWorkManagerDefinition.createInstance(false))
                                .setXmlElementName(Constants.DISTRIBUTED_WORKMANAGER)
                                .addAttributes(JcaDistributedWorkManagerDefinition.ATTRIBUTES)
                                .addChild(ThreadsParser2_0.getBoundedQueueThreadPoolParser(BoundedQueueThreadPoolResourceDefinition.create(WORKMANAGER_SHORT_RUNNING, ThreadsServices.STANDARD_THREAD_FACTORY_RESOLVER, ThreadsServices.STANDARD_HANDOFF_EXECUTOR_RESOLVER,
                                        ThreadsServices.EXECUTOR.append(WORKMANAGER_SHORT_RUNNING)))
                                        .setXmlElementName(WORKMANAGER_SHORT_RUNNING).setInheritName(true))
                                .addChild(ThreadsParser2_0.getBoundedQueueThreadPoolParser(BoundedQueueThreadPoolResourceDefinition.create(WORKMANAGER_LONG_RUNNING, ThreadsServices.STANDARD_THREAD_FACTORY_RESOLVER, ThreadsServices.STANDARD_HANDOFF_EXECUTOR_RESOLVER,
                                        ThreadsServices.EXECUTOR.append(WORKMANAGER_LONG_RUNNING)))
                                        .setXmlElementName(WORKMANAGER_LONG_RUNNING).setInheritName(true))
                )

                .addChild(builder(JcaBootstrapContextDefinition.INSTANCE)
                        .addAttributes(JcaBootstrapContextDefinition.ATTRIBUTES)
                        .setXmlWrapperElement(BOOTSTRAP_CONTEXTS))
                .addChild(builder(JcaCachedConnectionManagerDefinition.INSTANCE)
                        .addAttributes(JcaCachedConnectionManagerDefinition.ATTRIBUTES))


                .build();
    }

    private JcaSubsystemParser_4_0() {
    }


    @Override
    public PersistentResourceXMLDescription getParserDescription() {
        return xmlDescription;
    }
}

