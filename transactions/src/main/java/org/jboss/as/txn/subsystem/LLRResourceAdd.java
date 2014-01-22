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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.txn.TransactionLogger.ROOT_LOGGER;

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;


/**
 * the {@link AbstractAddStepHandler} implementations that add LLR managed resource.
 *
 * @author Stefano Maestri (c) 2011 Red Hat Inc.
 */
class LLRResourceAdd extends AbstractAddStepHandler {
    static LLRResourceAdd INSTANCE = new LLRResourceAdd();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        LLRResourceResourceDefinition.JNDI_NAME.validateAndSet(operation, model);
        LLRResourceResourceDefinition.LLR_TABLE_NAME.validateAndSet(operation, model);
        LLRResourceResourceDefinition.LLR_TABLE_BATCH_SIZE.validateAndSet(operation, model);
        LLRResourceResourceDefinition.LLR_TABLE_IMMEDIATE_CLEANUP.validateAndSet(operation, model);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model,
                                  final ServiceVerificationHandler verificationHandler, final List<ServiceController<?>> newControllers) throws OperationFailedException {
        PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        final String jndiName = address.getLastElement().getValue();
        final String tableName = LLRResourceResourceDefinition.LLR_TABLE_NAME.resolveModelAttribute(context, model).asString();
        final int batchSize =  LLRResourceResourceDefinition.LLR_TABLE_BATCH_SIZE.resolveModelAttribute(context, model).asInt();
        final boolean immediate_cleanup = LLRResourceResourceDefinition.LLR_TABLE_IMMEDIATE_CLEANUP.resolveModelAttribute(context, model).asBoolean();
        ROOT_LOGGER.infof("adding llr-resource: jndi-name=%s, table-name=%s, batch-size=%d, immediate-cleanup=%b", jndiName, tableName, batchSize, immediate_cleanup);
        // TODO Uncomment this code when the correct version of narayana is used in wildfly
/*        JTAEnvironmentBean jtaEnvironmentBean = BeanPopulatorgetDefaultInstance(JTAEnvironmentBean.class);
        List<String> jndiNames = jtaEnvironmentBean.getConnectableResourceJNDINames();

        jndiNames.add(jndiName);
        jtaEnvironmentBean.setConnectableResourceJNDINames(jndiNames);   */
    }
}
