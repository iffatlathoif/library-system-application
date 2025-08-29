package com.enigma.library_app.model;
import com.enigma.library_app.enumeration.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String loanId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "loan_date", nullable = false)
    private LocalDateTime loanDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status;

    @ManyToOne
    @JoinColumn(name = "copy_id")
    private Copy copy;

    @Column(name = "return_request_date")
    private LocalDateTime returnRequestDate;

    @Column(name = "has_been_extended", nullable = false)
    private boolean hasBeenExtended = false; // Defaultnya false

//    @OneToOne(mappedBy = "loan")
//    private Fine fine;


//    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<LoanDetail> loanDetails = new ArrayList<>();
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<LoanDetail> loanDetails;

}
