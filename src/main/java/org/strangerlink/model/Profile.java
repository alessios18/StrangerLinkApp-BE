package org.strangerlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String displayName;

    private Integer age;

    @ManyToOne(fetch = FetchType.EAGER) // Cambiato da LAZY a EAGER per evitare problemi di serializzazione
    @JoinColumn(name = "country_id")
    private Country country;

    private String gender;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String profileImageUrl;

    @ManyToMany
    @JoinTable(
            name = "profile_interests",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private Set<Interest> interests = new HashSet<>();

    // Helper methods per gestire la collezione interests
    public void addInterest(Interest interest) {
        interests.add(interest);
        interest.getProfiles().add(this);
    }

    public void removeInterest(Interest interest) {
        interests.remove(interest);
        interest.getProfiles().remove(this);
    }
}