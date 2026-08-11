package com.acs.crm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Person {
    @Column(name = "account_manager_name")
    private String name;
    @Column(name = "account_manager_initials")
    private String initials;

    public Person() {
    }

    public Person(String name, String initials) {
        this.name = name;
        this.initials = initials;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }
}
