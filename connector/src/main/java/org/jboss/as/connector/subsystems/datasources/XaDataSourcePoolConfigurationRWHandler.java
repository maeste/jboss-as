/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.as.connector.subsystems.datasources;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;

import org.jboss.as.connector.ConnectorServices;
import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.ModelQueryOperationHandler;
import org.jboss.as.controller.ModelUpdateOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.RuntimeTask;
import org.jboss.as.controller.RuntimeTaskContext;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.server.ServerOperationContext;
import org.jboss.dmr.ModelNode;
import org.jboss.jca.core.api.management.DataSource;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.msc.service.ServiceController;

/**
 * @author <a href="mailto:stefano.maestri@redhat.com">Stefano Maestri</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 */
class XaDataSourcePoolConfigurationRWHandler {

    static final String[] NO_LOCATION = new String[0];
    private static final String MAX_POOL_SIZE = "max-pool-size";
    private static final String MIN_POOL_SIZE = "min-pool-size";
    private static final String BLOCKING_TIMEOUT = "blocking-timeout-wait-millis";
    private static final String IDLE_TIMEOUT_MINUTES = "idle-timeout-minutes";
    private static final String BACKGROUND_VALIDATION = "background-validation";
    private static final String BACKGROUND_VALIDATION_MINUTES = "background-validation-minutes";
    private static final String POOL_PREFILL = "pool-prefill";
    private static final String POOL_USE_STRICT_MIN = "pool-use-strict-min";
    private static final String USE_FAST_FAIL = "use-fast-fail";

    static final String[] ATTRIBUTES = new String[] { MAX_POOL_SIZE, MIN_POOL_SIZE, BLOCKING_TIMEOUT, IDLE_TIMEOUT_MINUTES,
            BACKGROUND_VALIDATION, BACKGROUND_VALIDATION_MINUTES, POOL_PREFILL, POOL_USE_STRICT_MIN, USE_FAST_FAIL };

    static class XaDataSourcePoolConfigurationReadHandler implements ModelQueryOperationHandler {

        static XaDataSourcePoolConfigurationReadHandler INSTANCE = new XaDataSourcePoolConfigurationReadHandler();

        /** {@inheritDoc} */
        @Override
        public OperationResult execute(final OperationContext context, final ModelNode operation,
                final ResultHandler resultHandler) throws OperationFailedException {

            if (context.getRuntimeContext() != null) {
                context.getRuntimeContext().setRuntimeTask(new RuntimeTask() {
                    public void execute(RuntimeTaskContext context) throws OperationFailedException {
                        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
                        final String jndiName = address.getLastElement().getValue();
                        final String attributeName = operation.require(NAME).asString();

                        final ServiceController<?> managementRepoService = context.getServiceRegistry().getService(
                                ConnectorServices.MANAGEMENT_REPOSISTORY_SERVICE);
                        if (managementRepoService != null) {
                            try {
                                final ManagementRepository repository = (ManagementRepository) managementRepoService.getValue();
                                final ModelNode result = new ModelNode();
                                if (repository.getDataSources() != null) {
                                    for (DataSource ds : repository.getDataSources()) {
                                        if (jndiName.equalsIgnoreCase(ds.getJndiName())) {
                                            if (MAX_POOL_SIZE.equals(attributeName)) {
                                                result.set("" + ds.getPoolConfiguration().getMaxSize());
                                            }
                                            if (MIN_POOL_SIZE.equals(attributeName)) {
                                                result.set("" + ds.getPoolConfiguration().getMinSize());
                                            }
                                            if (BLOCKING_TIMEOUT.equals(attributeName)) {
                                                result.set("" + ds.getPoolConfiguration().getBlockingTimeout());
                                            }
                                            if (IDLE_TIMEOUT_MINUTES.equals(attributeName)) {
                                                result.set("" + ds.getPoolConfiguration().getIdleTimeout());
                                            }
                                            if (BACKGROUND_VALIDATION.equals(attributeName)) {
                                                result.set("" + ds.getPoolConfiguration().isBackgroundValidation());
                                            }
                                            if (BACKGROUND_VALIDATION_MINUTES.equals(attributeName)) {
                                                result.set("" + ds.getPoolConfiguration().getBackgroundValidationMinutes());
                                            }
                                            if (POOL_PREFILL.equals(attributeName)) {
                                                result.set("" + ds.getPoolConfiguration().isPrefill());
                                            }
                                            if (POOL_USE_STRICT_MIN.equals(attributeName)) {
                                                result.set("" + ds.getPoolConfiguration().isStrictMin());
                                            }
                                            if (USE_FAST_FAIL.equals(attributeName)) {
                                                result.set("" + ds.getPoolConfiguration().isUseFastFail());
                                            }
                                        }
                                    }
                                }
                                resultHandler.handleResultFragment(new String[0], result);
                                resultHandler.handleResultComplete();
                            } catch (Exception e) {
                                throw new OperationFailedException(new ModelNode().set("failed to get attribute"
                                        + e.getMessage()));
                            }
                        }
                    }
                });
            } else {
                resultHandler.handleResultFragment(NO_LOCATION, new ModelNode().set("no metrics available"));
                resultHandler.handleResultComplete();
            }
            return new BasicOperationResult();
        }
    }

    static class XaDataSourcePoolConfigurationWriteHandler implements ModelUpdateOperationHandler {

        static XaDataSourcePoolConfigurationWriteHandler INSTANCE = new XaDataSourcePoolConfigurationWriteHandler();

        /** {@inheritDoc} */
        @Override
        public OperationResult execute(final OperationContext context, final ModelNode operation,
                final ResultHandler resultHandler) throws OperationFailedException {

            final String name = operation.require(NAME).asString();
            // Don't require VALUE. Let validateValue decide if it's bothered
            // by and undefined value
            final ModelNode value = operation.get(VALUE);

            // TODO evaluate if a validation is needed
            // validateValue(name, value);

            final ModelNode submodel = context.getSubModel();
            final ModelNode currentValue = submodel.get(name).clone();

            final ModelNode compensating = Util.getEmptyOperation(operation.require(OP).asString(), operation.require(OP_ADDR));
            compensating.get(NAME).set(name);
            compensating.get(VALUE).set(currentValue);

            if (context.getRuntimeContext() != null) {
                context.getRuntimeContext().setRuntimeTask(new RuntimeTask() {
                    public void execute(RuntimeTaskContext runtimeCtx) throws OperationFailedException {
                        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
                        final String jndiName = address.getLastElement().getValue();

                        final ServiceController<?> managementRepoService = runtimeCtx.getServiceRegistry().getService(
                                ConnectorServices.MANAGEMENT_REPOSISTORY_SERVICE);
                        if (managementRepoService != null) {
                            try {
                                final ManagementRepository repository = (ManagementRepository) managementRepoService.getValue();
                                if (repository.getDataSources() != null) {
                                    for (DataSource ds : repository.getDataSources()) {
                                        if (jndiName.equalsIgnoreCase(ds.getJndiName())) {
                                            if (MAX_POOL_SIZE.equals(name)) {
                                                ds.getPoolConfiguration().setMaxSize(value.asInt());
                                            }
                                            if (MIN_POOL_SIZE.equals(name)) {
                                                ds.getPoolConfiguration().setMinSize(value.asInt());
                                            }
                                            if (BLOCKING_TIMEOUT.equals(name)) {
                                                ds.getPoolConfiguration().setBlockingTimeout(value.asLong());
                                            }
                                            if (IDLE_TIMEOUT_MINUTES.equals(name)) {
                                                ds.getPoolConfiguration().setIdleTimeout(value.asLong());
                                            }
                                            if (BACKGROUND_VALIDATION.equals(name)) {
                                                ds.getPoolConfiguration().setBackgroundValidation(value.asBoolean());
                                            }
                                            if (BACKGROUND_VALIDATION_MINUTES.equals(name)) {
                                                ds.getPoolConfiguration().setBackgroundValidationMinutes(value.asInt());
                                            }
                                            if (POOL_PREFILL.equals(name)) {
                                                ds.getPoolConfiguration().setPrefill(value.asBoolean());
                                            }
                                            if (POOL_USE_STRICT_MIN.equals(name)) {
                                                ds.getPoolConfiguration().setStrictMin(value.asBoolean());
                                            }
                                            if (USE_FAST_FAIL.equals(name)) {
                                                ds.getPoolConfiguration().setUseFastFail(value.asBoolean());
                                            }
                                        }
                                    }
                                }
                                submodel.get(name).set(value);

                                modelChanged(context, operation, resultHandler, name, value, currentValue);

                            } catch (Exception e) {
                                throw new OperationFailedException(new ModelNode().set("failed to set attribute"
                                        + e.getMessage()));
                            }
                        }
                    }
                });
            } else {
                resultHandler.handleResultComplete();
            }
            return new BasicOperationResult(compensating);

        }

        protected void modelChanged(final OperationContext context, final ModelNode operation,
                final ResultHandler resultHandler, final String attributeName, final ModelNode newValue,
                final ModelNode currentValue) throws OperationFailedException {

            resultHandler.handleResultComplete();
            // TODO evaluate something like that for "PerContainer" operations
            if (context.getRuntimeContext() != null) {
                boolean restartRequired = attributeName.equals(POOL_PREFILL);
                if (restartRequired && context instanceof ServerOperationContext) {
                    ServerOperationContext.class.cast(context).restartRequired();
                }
            } else {
                resultHandler.handleResultComplete();
            }
        }
    }
}
