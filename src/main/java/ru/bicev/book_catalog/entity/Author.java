package ru.bicev.book_catalog.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    private String firstName;

    private String lastName;

    private int birthYear;

    private String country;


}
