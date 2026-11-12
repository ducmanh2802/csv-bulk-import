package dev.manhd.csvimport.importer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;
    private String city;

    protected Customer() {
    }

    public Customer(CustomerRow row) {
        this.email = row.email();
        this.fullName = row.fullName();
        this.phone = row.phone();
        this.city = row.city();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
