package org.jboss.as.connector.subsystems.resourceadapters;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.api.workmanager.WorkManagerStatistics;

import static org.jboss.as.connector.logging.ConnectorMessages.MESSAGES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;

public class WorkManagerRuntimeAttributeReadHandler implements OperationStepHandler {

    private final WorkManagerStatistics wmStat;
    private final WorkManager wm;

    public WorkManagerRuntimeAttributeReadHandler(WorkManager wm, final WorkManagerStatistics wmStat) {
        this.wm = wm;
        this.wmStat = wmStat;
    }

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (context.isNormalServer()) {
            context.addStep(new OperationStepHandler() {
                public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
                    final String attributeName = operation.require(NAME).asString();
                    try {
                        final ModelNode result = context.getResult();

                        switch (attributeName) {
                            case Constants.WORK_ACTIVE_NAME: {
                                result.set(wmStat.getWorkActive());
                                break;
                            }
                            case Constants.WORK_FAILED_NAME: {
                                result.set(wmStat.getWorkFailed());
                                break;
                            }
                            case Constants.WORK_SUCEESSFUL_NAME: {
                                result.set(wmStat.getWorkSuccessful());
                                break;
                            }
                            case Constants.DO_WORK_ACCEPTED_NAME: {
                                result.set(wmStat.getDoWorkAccepted());
                                break;
                            }
                            case Constants.DO_WORK_REJECTED_NAME: {
                                result.set(wmStat.getDoWorkRejected());
                                break;
                            }
                            case Constants.SCHEDULED_WORK_ACCEPTED_NAME: {
                                result.set(wmStat.getScheduleWorkAccepted());
                                break;
                            }
                            case Constants.SCHEDULED_WORK_REJECTED_NAME: {
                                result.set(wmStat.getScheduleWorkRejected());
                                break;
                            }
                            case Constants.START_WORK_ACCEPTED_NAME: {
                                result.set(wmStat.getStartWorkAccepted());
                                break;
                            }
                            case Constants.START_WORK_REJECTED_NAME: {
                                result.set(wmStat.getStartWorkRejected());
                                break;
                            }
                            case Constants.WORKMANAGER_STATISTICS_ENABLED_NAME: {
                                result.set(wm.isStatisticsEnabled());
                                break;
                            }
                            case Constants.DISTRIBUTED_STATISTICS_ENABLED_NAME: {
                                result.set(((DistributedWorkManager) wm).isDistributedStatisticsEnabled());
                                break;
                            }
                            case Constants.DOWORK_DISTRIBUTION_ENABLED_NAME: {
                                result.set(((DistributedWorkManager) wm).isDoWorkDistributionEnabled());
                                break;
                            }
                            case Constants.STARTWORK_DISTRIBUTION_ENABLED_NAME: {
                                result.set(((DistributedWorkManager) wm).isStartWorkDistributionEnabled());
                                break;
                            }
                            case Constants.SCHEDULEWORK_DISTRIBUTION_ENABLED_NAME: {
                                result.set(((DistributedWorkManager) wm).isScheduleWorkDistributionEnabled());
                                break;
                            }

                        }

                    } catch (Exception e) {
                        throw new OperationFailedException(MESSAGES.failedToGetMetrics(e.getLocalizedMessage()));
                    }

                    context.stepCompleted();
                }
            }, OperationContext.Stage.RUNTIME);
        }

        context.stepCompleted();

    }

}
