package it.manage.orders.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.FetchType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.util.Set;


public class AppUser extends it.auth.security.core.entity.AppUser {

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "appUser")
    private Set<Order> orders;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<String> roles;
}
