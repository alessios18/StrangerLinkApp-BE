package org.strangerlink.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "countries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true, length = 2)
    private String code;

    @OneToMany(mappedBy = "country")
    private Set<Profile> profiles = new HashSet<>();

    @OneToMany(mappedBy = "preferredCountry")
    private Set<SearchPreference> searchPreferences = new HashSet<>();
}