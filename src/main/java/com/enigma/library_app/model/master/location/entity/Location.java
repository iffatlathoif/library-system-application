package com.enigma.library_app.model.master.location.entity;

import com.enigma.library_app.model.master.book.entity.Copy;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "location")
    private List<Copy> copies;

    @ManyToOne
    @JoinColumn(name = "faculty_code")
    private Faculty faculty;
}
