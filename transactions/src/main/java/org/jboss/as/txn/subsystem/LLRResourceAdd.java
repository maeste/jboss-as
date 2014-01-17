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

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;


/**
 * TODO class javadoc.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
class LLRResourceAdd extends AbstractAddStepHandler {
    static LLRResourceAdd INSTANCE = new LLRResourceAdd();

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        LLRResourceResourceDefinition.JNDI_NAME.validateAndSet(operation, model);
        LLRResourceResourceDefinition.TABLE_NAME.validateAndSet(operation, model);

    }

    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model,
                                  final ServiceVerificationHandler verificationHandler, final List<ServiceController<?>> newControllers) throws OperationFailedException {
        PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));

        final String jndiName = address.getLastElement().getValue();

        // TODO Uncomment this code when the correct version of narayana is used in wildfly
/*        JTAEnvironmentBean jtaEnvironmentBean = BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class);
        List<String> jndiNames = jtaEnvironmentBean.getConnectableResourceJNDINames();

        jndiNames.add(jndiName);
        jtaEnvironmentBean.setConnectableResourceJNDINames(jndiNames);*/
    }
}
