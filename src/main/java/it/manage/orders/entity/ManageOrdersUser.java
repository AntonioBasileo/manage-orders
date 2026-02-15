package it.manage.orders.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class AppUser extends it.auth.security.core.entity.AppUser {

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "appUser")
    private Set<Order> orders;
}
