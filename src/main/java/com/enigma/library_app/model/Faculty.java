package com.enigma.library_app.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;

import java.util.List;

@Builder
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Faculties")
public class Faculty {
    @Id
    @Column(name = "faculty_code")
    private String facultyCode;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Member> members;
}
