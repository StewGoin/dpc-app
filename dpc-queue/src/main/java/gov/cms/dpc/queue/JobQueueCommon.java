package gov.cms.dpc.queue;

import gov.cms.dpc.queue.exceptions.JobQueueFailure;
import gov.cms.dpc.queue.models.JobQueueBatch;
import io.reactivex.Observable;
import org.hl7.fhir.dstu3.model.ResourceType;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class JobQueueCommon implements IJobQueue {

    // Object variables
    private final int batchSize;

    public abstract void submitJobBatches(List<JobQueueBatch> jobBatches);

    public JobQueueCommon(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public UUID createJob(UUID orgID, String providerID, List<String> patients, List<ResourceType> resourceTypes, OffsetDateTime since, OffsetDateTime transactionTime) {
        final UUID jobID = UUID.randomUUID();

        List<JobQueueBatch> jobBatches;
        if (patients.isEmpty()) {
            jobBatches = createEmptyBatch(jobID, orgID, providerID, resourceTypes, since, transactionTime);
        } else if (since != null && !transactionTime.isAfter(since)) {
            // If the since request is after the BFD transactionTime, then result will always be an empty result set
            jobBatches = createEmptyBatch(jobID, orgID, providerID, resourceTypes, since, transactionTime);
        } else {
            jobBatches = Observable.fromIterable(patients)
                    .buffer(batchSize)
                    .map(patientBatch -> this.createJobBatch(jobID, orgID, providerID, patientBatch, resourceTypes, since, transactionTime))
                    .toList()
                    .blockingGet();
        }


        // Set the priority of a job batch
        // Single patients will have first priority to support patient everything
        final int priority = patients.size() == 1 ? 1000 : 5000;
        jobBatches.forEach(batch -> batch.setPriority(priority));

        this.submitJobBatches(jobBatches);
        return jobBatches.stream().map(JobQueueBatch::getJobID).findFirst().orElseThrow(() -> new JobQueueFailure("Unable to create job. No batches to submit."));
    }

    protected JobQueueBatch createJobBatch(UUID jobID,
                                           UUID orgID,
                                           String providerID,
                                           List<String> patients,
                                           List<ResourceType> resourceTypes,
                                           OffsetDateTime since,
                                           OffsetDateTime transactionTime) {
        return new JobQueueBatch(jobID, orgID, providerID, patients, resourceTypes, since, transactionTime);
    }

    protected List<JobQueueBatch> createEmptyBatch(UUID jobID,
                                                   UUID orgID,
                                                   String providerID,
                                                   List<ResourceType> resourceTypes,
                                                   OffsetDateTime since,
                                                   OffsetDateTime transactionTime) {
        return Collections.singletonList(
                createJobBatch(jobID, orgID, providerID, Collections.emptyList(), resourceTypes, since, transactionTime)
        );
    }

    public int getBatchSize() {
        return batchSize;
    }
}
