package ru.bicev.book_catalog.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Book {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    private String title;

    private int releaseYear;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

}
