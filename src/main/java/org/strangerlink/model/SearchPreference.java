package org.strangerlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    // Valori possibili: "male", "female", "all"
    @Column(name = "preferred_gender")
    private String preferredGender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_country_id")
    private Country preferredCountry;

    // true se l'utente vuole cercare in tutti i paesi
    @Column(name = "all_countries")
    private boolean allCountries = false;
}