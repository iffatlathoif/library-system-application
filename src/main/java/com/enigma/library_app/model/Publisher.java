package com.enigma.library_app.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Publishers")
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String publisherId;

    @Column(nullable = false)
    private String name;

    private String address;

    @OneToMany(mappedBy = "publisher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Book> books;
}
