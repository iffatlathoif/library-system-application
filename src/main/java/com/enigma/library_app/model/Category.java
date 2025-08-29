package com.enigma.library_app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", length = 10)
    private Long id;

    @Column(name = "category_name", length = 100, unique = true)
    private String name;
}
