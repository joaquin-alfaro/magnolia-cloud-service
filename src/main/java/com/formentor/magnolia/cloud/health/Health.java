package com.formentor.magnolia.cloud.health;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public final class Health {

    private final Status status;

    private final Map<String, Object> details;

    /**
     * Create a new {@link Health} instance with the specified status and details.
     * @param builder the Builder to use
     */
    private Health(Builder builder) {
        this.status = builder.status;
        this.details = Collections.unmodifiableMap(builder.details);
    }

    /**
     * Return the status of the health.
     * @return the status (never {@code null})
     */
    @JsonUnwrapped
    public Status getStatus() {
        return this.status;
    }

    /**
     * Return the details of the health.
     * @return the details (or an empty map)
     */
    public Map<String, Object> getDetails() {
        return this.details;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj instanceof Health) {
            Health other = (Health) obj;
            return this.status.equals(other.status) && this.details.equals(other.details);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = this.status.hashCode();
        return 13 * hashCode + this.details.hashCode();
    }

    @Override
    public String toString() {
        return getStatus() + " " + getDetails();
    }

    /**
     * Create a new {@link Builder} instance with an {@link Status#UNKNOWN} status.
     * @return a new {@link Builder} instance
     */
    public static Builder unknown() {
        return status(Status.UNKNOWN);
    }

    /**
     * Create a new {@link Builder} instance with an {@link Status#UP} status.
     * @return a new {@link Builder} instance
     */
    public static Builder up() {
        return status(Status.UP);
    }

    /**
     * Create a new {@link Builder} instance with an {@link Status#DOWN} status and the
     * specified exception details.
     * @param ex the exception
     * @return a new {@link Builder} instance
     */
    public static Builder down(Exception ex) {
        return down().withException(ex);
    }

    /**
     * Create a new {@link Builder} instance with a {@link Status#DOWN} status.
     * @return a new {@link Builder} instance
     */
    public static Builder down() {
        return status(Status.DOWN);
    }

    /**
     * Create a new {@link Builder} instance with an {@link Status#OUT_OF_SERVICE} status.
     * @return a new {@link Builder} instance
     */
    public static Builder outOfService() {
        return status(Status.OUT_OF_SERVICE);
    }

    /**
     * Create a new {@link Builder} instance with a specific status code.
     * @param statusCode the status code
     * @return a new {@link Builder} instance
     */
    public static Builder status(String statusCode) {
        return status(new Status(statusCode));
    }

    /**
     * Create a new {@link Builder} instance with a specific {@link Status}.
     * @param status the status
     * @return a new {@link Builder} instance
     */
    public static Builder status(Status status) {
        return new Builder(status);
    }

    /**
     * Builder for creating immutable {@link Health} instances.
     */
    public static class Builder {

        private Status status;

        private Map<String, Object> details;

        /**
         * Create new Builder instance.
         */
        public Builder() {
            this.status = Status.UNKNOWN;
            this.details = new LinkedHashMap<>();
        }

        /**
         * Create new Builder instance, setting status to given {@code status}.
         * @param status the {@link Status} to use
         */
        public Builder(Status status) {
            this.status = status;
            this.details = new LinkedHashMap<>();
        }

        /**
         * Create new Builder instance, setting status to given {@code status} and details
         * to given {@code details}.
         * @param status the {@link Status} to use
         * @param details the details {@link Map} to use
         */
        public Builder(Status status, Map<String, ?> details) {
            this.status = status;
            this.details = new LinkedHashMap<>(details);
        }

        /**
         * Record detail for given {@link Exception}.
         * @param ex the exception
         * @return this {@link Builder} instance
         */
        public Builder withException(Throwable ex) {
            return withDetail("error", ex.getClass().getName() + ": " + ex.getMessage());
        }

        /**
         * Record detail using given {@code key} and {@code value}.
         * @param key the detail key
         * @param value the detail value
         * @return this {@link Builder} instance
         */
        public Builder withDetail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }

        /**
         * Record details from the given {@code details} map. Keys from the given map
         * replace any existing keys if there are duplicates.
         * @param details map of details
         * @return this {@link Builder} instance
         * @since 2.1.0
         */
        public Builder withDetails(Map<String, ?> details) {
            this.details.putAll(details);
            return this;
        }

        /**
         * Set status to {@link Status#UNKNOWN} status.
         * @return this {@link Builder} instance
         */
        public Builder unknown() {
            return status(Status.UNKNOWN);
        }

        /**
         * Set status to {@link Status#UP} status.
         * @return this {@link Builder} instance
         */
        public Builder up() {
            return status(Status.UP);
        }

        /**
         * Set status to {@link Status#DOWN} and add details for given {@link Throwable}.
         * @param ex the exception
         * @return this {@link Builder} instance
         */
        public Builder down(Throwable ex) {
            return down().withException(ex);
        }

        /**
         * Set status to {@link Status#DOWN}.
         * @return this {@link Builder} instance
         */
        public Builder down() {
            return status(Status.DOWN);
        }

        /**
         * Set status to {@link Status#OUT_OF_SERVICE}.
         * @return this {@link Builder} instance
         */
        public Builder outOfService() {
            return status(Status.OUT_OF_SERVICE);
        }

        /**
         * Set status to given {@code statusCode}.
         * @param statusCode the status code
         * @return this {@link Builder} instance
         */
        public Builder status(String statusCode) {
            return status(new Status(statusCode));
        }

        /**
         * Set status to given {@link Status} instance.
         * @param status the status
         * @return this {@link Builder} instance
         */
        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        /**
         * Create a new {@link Health} instance with the previously specified code and
         * details.
         * @return a new {@link Health} instance
         */
        public Health build() {
            return new Health(this);
        }

    }

}