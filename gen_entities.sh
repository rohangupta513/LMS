#!/bin/bash
DIR="src/main/java/com/lms/backend/entity"

cat << 'INNER_EOF' > $DIR/User.java
package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;
    private String userName;
    private String userAddress;
    private String userPhone;
    private String userKycDetails;
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/Lender.java
package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "lenders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lender {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID lenderId;
    private String lenderName;
    private String lenderContact;
    private String lenderDetails;
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/Loan.java
package com.lms.backend.entity;

import com.lms.backend.enums.LoanType;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID loanId;
    
    @ManyToOne
    @JoinColumn(name = "lender_id")
    private Lender lender;
    
    private Double loanAmountMin;
    private Double loanAmountMax;
    private Double loanInterestMin;
    private Double loanInterestMax;
    private Integer loanTimeMin;
    private Integer loanTimeMax;
    
    @Enumerated(EnumType.STRING)
    private LoanType typeOfLoan;
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/Charge.java
package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "charges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Charge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID chargeId;
    
    private Integer dpd;
    private Double chargeAmount;
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/LoanAccount.java
package com.lms.backend.entity;

import com.lms.backend.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "loan_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID lan;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "lender_id")
    private Lender lender;
    
    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;
    
    private Double rateOfInterest;
    private Double amount;
    private Integer timePeriod;
    
    @Enumerated(EnumType.STRING)
    private LoanStatus status;
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/LoanAccountDue.java
package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loan_account_dues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccountDue {
    @Id
    private UUID lan;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "lan")
    private LoanAccount loanAccount;
    
    private LocalDate nextDueDate;
    private Double nextDueAmount;
    private Double nextDuePrinciple;
    private Double netDueInterest;
    private Double nextDueCharges;
    
    private Double totalOutstandingAmount;
    private Double totalOutstandingPrinciple;
    private Double totalOutstandingInterest;
    
    private Double totalDerivedAmount;
    private Double totalDerivedPrinciple;
    private Double totalDerivedInterest;
    
    private Double totalChargesDue;
    private Double totalChargesDerived;
    
    private Boolean isSettled;
    private Boolean isCancelled;
    private Boolean isForeclosed;
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/RepaymentScheduler.java
package com.lms.backend.entity;

import com.lms.backend.enums.RepaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "repayment_schedulers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentScheduler {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID rpsId;
    
    @ManyToOne
    @JoinColumn(name = "lan")
    private LoanAccount loanAccount;
    
    private LocalDate dueDate;
    private Double totalDue;
    private Double totalPrincipalDue;
    private Double totalInterestDue;
    private Double otherChargesDue;
    
    @Enumerated(EnumType.STRING)
    private RepaymentStatus status;
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/LoanCredit.java
package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loan_credits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanCredit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID credId;
    
    @ManyToOne
    @JoinColumn(name = "lan")
    private LoanAccount loanAccount;
    
    private LocalDate dateOfCredit;
    private Double amtCredited;
    
    private Double totalPrincipleDerived;
    private Double totalInterestDerived;
    private Double totalChargesDerived;
    
    private String status;
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/SettlementAudit.java
package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "settlement_audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID settId;
    
    @ManyToOne
    @JoinColumn(name = "rps_id")
    private RepaymentScheduler repaymentScheduler;
    
    @ManyToOne
    @JoinColumn(name = "cred_id")
    private LoanCredit loanCredit;
    
    @ManyToOne
    @JoinColumn(name = "lan")
    private LoanAccount loanAccount;
    
    private LocalDate dueDate;
    private Double dueForThisMonth;
    private Double dueFromPreviousMonths;
    private Double chargesDue;
    private Double totalDue;
    
    private LocalDate dateOfCredit;
    
    private Double amountDerived;
    private Double principleDerived;
    private Double interestDerived;
    private Integer dpd;
    private Double chargesDerived;
    
    private Boolean isSettled;
    private String status;
}
INNER_EOF

chmod +x gen_entities.sh
./gen_entities.sh
