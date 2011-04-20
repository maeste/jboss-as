package org.jboss.as.connector.subsystems.datasources;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import org.jboss.as.connector.ConnectorServices;
import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.ModelQueryOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.RuntimeTask;
import org.jboss.as.controller.RuntimeTaskContext;
import org.jboss.dmr.ModelNode;
import org.jboss.jca.core.api.management.DataSource;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

public class FlushIdleConnectionInPool implements ModelQueryOperationHandler {
    static FlushIdleConnectionInPool FLUSH_IDLE = new FlushIdleConnectionInPool(false);
    static FlushIdleConnectionInPool FLUSH_ALL = new FlushIdleConnectionInPool(true);

    private static final Logger log = Logger.getLogger("org.jboss.as.datasources");

    private final boolean entirePool;

    public FlushIdleConnectionInPool(boolean entirePool) {
        super();
        this.entirePool = entirePool;
    }

    /** {@inheritDoc} */
    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler)
            throws OperationFailedException {

        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        final String jndiName = address.getLastElement().getValue();

        if (context.getRuntimeContext() != null) {
            context.getRuntimeContext().setRuntimeTask(new RuntimeTask() {
                public void execute(RuntimeTaskContext runtimeCtx) throws OperationFailedException {

                    final ServiceController<?> managementRepoService = runtimeCtx.getServiceRegistry().getService(
                            ConnectorServices.MANAGEMENT_REPOSISTORY_SERVICE);
                    if (managementRepoService != null) {
                        try {
                            final ManagementRepository repository = (ManagementRepository) managementRepoService.getValue();
                            if (repository.getDataSources() != null) {
                                for (DataSource ds : repository.getDataSources()) {
                                    if (jndiName.equalsIgnoreCase(ds.getJndiName())) {
                                        ds.getPool().flush(entirePool);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new OperationFailedException(new ModelNode().set("failed to set attribute" + e.getMessage()));
                        }
                        resultHandler.handleResultComplete();
                    } else {
                        resultHandler.handleResultComplete();
                    }
                }
            });
        } else {
            resultHandler.handleResultComplete();
        }
        return new BasicOperationResult();
    }
}