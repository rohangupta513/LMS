import os

entities = {
    "User": """package com.lms.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID userId;
    private String userName; private String userAddress; private String userPhone; private String userKycDetails;
}""",
    "Lender": """package com.lms.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name = "lenders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Lender {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID lenderId;
    private String lenderName; private String lenderContact; private String lenderDetails;
}""",
    "Loan": """package com.lms.backend.entity;
import com.lms.backend.enums.LoanType;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name = "loans")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Loan {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID loanId;
    @ManyToOne @JoinColumn(name = "lender_id") private Lender lender;
    private Double loanAmountMin; private Double loanAmountMax;
    private Double loanInterestMin; private Double loanInterestMax;
    private Integer loanTimeMin; private Integer loanTimeMax;
    @Enumerated(EnumType.STRING) private LoanType typeOfLoan;
}""",
    "Charge": """package com.lms.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name = "charges")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Charge {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID chargeId;
    private Integer dpd; private Double chargeAmount;
}""",
    "LoanAccount": """package com.lms.backend.entity;
import com.lms.backend.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name = "loan_accounts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanAccount {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID lan;
    @ManyToOne @JoinColumn(name = "user_id") private User user;
    @ManyToOne @JoinColumn(name = "lender_id") private Lender lender;
    @ManyToOne @JoinColumn(name = "loan_id") private Loan loan;
    private Double rateOfInterest; private Double amount; private Integer timePeriod;
    @Enumerated(EnumType.STRING) private LoanStatus status;
}""",
    "LoanAccountDue": """package com.lms.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
@Entity
@Table(name = "loan_account_dues")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanAccountDue {
    @Id private UUID lan;
    @OneToOne @MapsId @JoinColumn(name = "lan") private LoanAccount loanAccount;
    private LocalDate nextDueDate; private Double nextDueAmount; private Double nextDuePrinciple; private Double netDueInterest; private Double nextDueCharges;
    private Double totalOutstandingAmount; private Double totalOutstandingPrinciple; private Double totalOutstandingInterest;
    private Double totalDerivedAmount; private Double totalDerivedPrinciple; private Double totalDerivedInterest;
    private Double totalChargesDue; private Double totalChargesDerived;
    private Boolean isSettled; private Boolean isCancelled; private Boolean isForeclosed;
}""",
    "RepaymentScheduler": """package com.lms.backend.entity;
import com.lms.backend.enums.RepaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
@Entity
@Table(name = "repayment_schedulers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RepaymentScheduler {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID rpsId;
    @ManyToOne @JoinColumn(name = "lan") private LoanAccount loanAccount;
    private LocalDate dueDate; private Double totalDue; private Double totalPrincipalDue; private Double totalInterestDue; private Double otherChargesDue;
    @Enumerated(EnumType.STRING) private RepaymentStatus status;
}""",
    "LoanCredit": """package com.lms.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
@Entity
@Table(name = "loan_credits")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanCredit {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID credId;
    @ManyToOne @JoinColumn(name = "lan") private LoanAccount loanAccount;
    private LocalDate dateOfCredit; private Double amtCredited;
    private Double totalPrincipleDerived; private Double totalInterestDerived; private Double totalChargesDerived;
    private String status;
}""",
    "SettlementAudit": """package com.lms.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
@Entity
@Table(name = "settlement_audits")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SettlementAudit {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID settId;
    @ManyToOne @JoinColumn(name = "rps_id") private RepaymentScheduler repaymentScheduler;
    @ManyToOne @JoinColumn(name = "cred_id") private LoanCredit loanCredit;
    @ManyToOne @JoinColumn(name = "lan") private LoanAccount loanAccount;
    private LocalDate dueDate; private Double dueForThisMonth; private Double dueFromPreviousMonths; private Double chargesDue; private Double totalDue;
    private LocalDate dateOfCredit; private Double amountDerived; private Double principleDerived; private Double interestDerived; private Integer dpd; private Double chargesDerived;
    private Boolean isSettled; private String status;
}"""
}

repos = {
    "UserRepository": "import com.lms.backend.entity.User;\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\nimport java.util.UUID;\n@Repository\npublic interface UserRepository extends JpaRepository<User, UUID> {}",
    "LenderRepository": "import com.lms.backend.entity.Lender;\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\nimport java.util.UUID;\n@Repository\npublic interface LenderRepository extends JpaRepository<Lender, UUID> {}",
    "LoanRepository": """import com.lms.backend.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    @Query("SELECT l FROM Loan l WHERE " +
           "l.loanAmountMin <= :amount AND l.loanAmountMax >= :amount AND " +
           "l.loanInterestMin <= :interest AND l.loanInterestMax >= :interest AND " +
           "l.loanTimeMin <= :time AND l.loanTimeMax >= :time AND " +
           "l.typeOfLoan = :type")
    List<Loan> findByCriteria(@Param("amount") Double amount, 
                              @Param("interest") Double interest, 
                              @Param("time") Integer time, 
                              @Param("type") com.lms.backend.enums.LoanType type);
}""",
    "ChargeRepository": "import com.lms.backend.entity.Charge;\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\nimport java.util.UUID;\n@Repository\npublic interface ChargeRepository extends JpaRepository<Charge, UUID> {}",
    "LoanAccountRepository": "import com.lms.backend.entity.LoanAccount;\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\nimport java.util.UUID;\n@Repository\npublic interface LoanAccountRepository extends JpaRepository<LoanAccount, UUID> {}",
    "LoanAccountDueRepository": "import com.lms.backend.entity.LoanAccountDue;\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\nimport java.util.UUID;\n@Repository\npublic interface LoanAccountDueRepository extends JpaRepository<LoanAccountDue, UUID> {}",
    "RepaymentSchedulerRepository": """import com.lms.backend.entity.RepaymentScheduler;
import com.lms.backend.enums.RepaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
@Repository
public interface RepaymentSchedulerRepository extends JpaRepository<RepaymentScheduler, UUID> {
    List<RepaymentScheduler> findByLoanAccount_LanAndStatusOrderByDueDateAsc(UUID lan, RepaymentStatus status);
}""",
    "LoanCreditRepository": "import com.lms.backend.entity.LoanCredit;\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\nimport java.util.UUID;\n@Repository\npublic interface LoanCreditRepository extends JpaRepository<LoanCredit, UUID> {}",
    "SettlementAuditRepository": "import com.lms.backend.entity.SettlementAudit;\nimport org.springframework.data.jpa.repository.JpaRepository;\nimport org.springframework.stereotype.Repository;\nimport java.util.UUID;\n@Repository\npublic interface SettlementAuditRepository extends JpaRepository<SettlementAudit, UUID> {}"
}

services = {
    "UserService": """package com.lms.backend.service;
import com.lms.backend.entity.User;
import com.lms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class UserService {
    @Autowired private UserRepository repo;
    public User add(User u) { return repo.save(u); }
    public List<User> getAll() { return repo.findAll(); }
    public Optional<User> get(UUID id) { return repo.findById(id); }
    public void delete(UUID id) { repo.deleteById(id); }
}""",
    "LenderService": """package com.lms.backend.service;
import com.lms.backend.entity.Lender;
import com.lms.backend.repository.LenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class LenderService {
    @Autowired private LenderRepository repo;
    public Lender add(Lender u) { return repo.save(u); }
    public List<Lender> getAll() { return repo.findAll(); }
    public Optional<Lender> get(UUID id) { return repo.findById(id); }
    public void delete(UUID id) { repo.deleteById(id); }
}""",
    "LoanService": """package com.lms.backend.service;
import com.lms.backend.entity.Loan;
import com.lms.backend.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class LoanService {
    @Autowired private LoanRepository repo;
    public Loan add(Loan u) { return repo.save(u); }
    public List<Loan> getAll() { return repo.findAll(); }
    public Optional<Loan> get(UUID id) { return repo.findById(id); }
    public void delete(UUID id) { repo.deleteById(id); }
}""",
    "ChargeService": """package com.lms.backend.service;
import com.lms.backend.entity.Charge;
import com.lms.backend.repository.ChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class ChargeService {
    @Autowired private ChargeRepository repo;
    public Charge add(Charge u) { return repo.save(u); }
    public List<Charge> getAll() { return repo.findAll(); }
    public Optional<Charge> get(UUID id) { return repo.findById(id); }
    public void delete(UUID id) { repo.deleteById(id); }
}"""
}

def write_files(directory, prefix, suffix, data):
    os.makedirs(directory, exist_ok=True)
    for name, content in data.items():
        with open(os.path.join(directory, f"{name}{suffix}"), "w") as f:
            if not content.startswith("package"):
                f.write(f"package {prefix};\n\n")
            f.write(content)

write_files("src/main/java/com/lms/backend/entity", "com.lms.backend.entity", ".java", entities)
write_files("src/main/java/com/lms/backend/repository", "com.lms.backend.repository", ".java", repos)
write_files("src/main/java/com/lms/backend/service", "com.lms.backend.service", ".java", services)

print("Generated files successfully.")
