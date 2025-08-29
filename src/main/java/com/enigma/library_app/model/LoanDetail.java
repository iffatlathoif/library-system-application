package com.enigma.library_app.model;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table(name = "loan_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "loan_detail_id")
    private String loanDetailId;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @OneToMany(mappedBy = "loanDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Copy> copies = new ArrayList<>();

    private int quantity;
}
