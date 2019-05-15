package gov.cms.dpc.queue.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hl7.fhir.dstu3.model.ResourceType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "job_result")
public class JobResult {
    public static final long serialVersionUID = 1L;

    @Embeddable
    public static class JobResultID implements Serializable {
        public static long serialVersionUID = 1L;

        private UUID jobID;

        @Column(name = "resource_type")
        private ResourceType resourceType;

        public JobResultID() {
        }

        public JobResultID(UUID jobID, ResourceType resourceType) {
            this.resourceType = resourceType;
            this.jobID = jobID;
        }

        public ResourceType getResourceType() {
            return resourceType;
        }

        public UUID getJobID() {
            return jobID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JobResultID)) return false;
            JobResultID other = (JobResultID) o;
            return Objects.equals(jobID, other.jobID) && Objects.equals(resourceType, other.resourceType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jobID, resourceType);
        }
    }

    @EmbeddedId
    private JobResultID jobResultID;

    @Column(name = "count")
    private int count;

    @Column(name = "error_count")
    private int errorCount;

    public JobResult() {
        // for hibernate
    }

    public JobResult(UUID jobID, ResourceType resourceType) {
        this.jobResultID = new JobResultID(jobID, resourceType);
        this.count = 0;
        this.errorCount = 0;
    }

    public JobResultID getJobResultID() {
        return jobResultID;
    }

    public void setJobResultID(JobResultID jobResultID) {
        this.jobResultID = jobResultID;
    }

    public UUID getJobID() {
        return jobResultID.getJobID();
    }

    public ResourceType getResourceType() {
        return jobResultID.getResourceType();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void incrementCount() {
        count = count + 1;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public void incrementErrorCount() {
        errorCount = errorCount + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobResult other = (JobResult) o;
        return new EqualsBuilder()
                .append(jobResultID, other.jobResultID)
                .append(count, other.count)
                .append(errorCount, other.errorCount)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobResultID, count, errorCount);
    }

    @Override
    public String toString() {
        return "JobResult{" +
                "jobID=" + jobResultID.jobID +
                ", resourceType=" + jobResultID.resourceType +
                ", count=" + count +
                ", errorCount=" + errorCount +
                '}';
    }
}