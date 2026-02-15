package it.manage.orders.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class ManageOrdersUser extends it.auth.security.core.entity.AppUser {

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "manageOrdersUser")
    private Set<Order> orders;
}
