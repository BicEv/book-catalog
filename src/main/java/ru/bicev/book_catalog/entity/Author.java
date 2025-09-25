package ru.bicev.book_catalog.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "authors", uniqueConstraints = {
        @UniqueConstraint(name = "uc_author_fullname_birthyear_country", columnNames = { "first_name", "last_name",
                "birth_year", "country" })
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_year", nullable = false)
    private int birthYear;

    @Column(nullable = false)
    private String country;

}
