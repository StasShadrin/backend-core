package ru.mentee.power.crm.domain;

import java.util.Objects;

/**
 * Represents a potential customer (lead) in the CRM system.
 * <p>
 * A lead contains contact information and current status in the sales pipeline.
 */
public class Lead {
    private String id;
    private String email;
    private String phone;
    private String company;
    private String status;

    /**
     * Constructs a new Lead with the specified attributes.
     *
     * @param id       the unique identifier of the lead
     * @param email    the email address of the lead
     * @param phone    the phone number of the lead
     * @param company  the company name associated with the lead
     * @param status   the current status of the lead (e.g., "NEW", "CONTACTED", "QUALIFIED")
     */
    public Lead(String id, String email, String phone, String company, String status) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.company = company;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCompany() {
        return company;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Lead{" +
               "id='" + id + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               ", company='" + company + '\'' +
               ", status='" + status + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lead lead = (Lead) o;
        return Objects.equals(id, lead.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
