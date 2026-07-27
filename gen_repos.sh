#!/bin/bash
DIR="src/main/java/com/lms/backend/repository"

cat << 'INNER_EOF' > $DIR/UserRepository.java
package com.lms.backend.repository;

import com.lms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/LenderRepository.java
package com.lms.backend.repository;

import com.lms.backend.entity.Lender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LenderRepository extends JpaRepository<Lender, UUID> {
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/LoanRepository.java
package com.lms.backend.repository;

import com.lms.backend.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/ChargeRepository.java
package com.lms.backend.repository;

import com.lms.backend.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, UUID> {
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/LoanAccountRepository.java
package com.lms.backend.repository;

import com.lms.backend.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, UUID> {
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/LoanAccountDueRepository.java
package com.lms.backend.repository;

import com.lms.backend.entity.LoanAccountDue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LoanAccountDueRepository extends JpaRepository<LoanAccountDue, UUID> {
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/RepaymentSchedulerRepository.java
package com.lms.backend.repository;

import com.lms.backend.entity.RepaymentScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import com.lms.backend.enums.RepaymentStatus;

@Repository
public interface RepaymentSchedulerRepository extends JpaRepository<RepaymentScheduler, UUID> {
    List<RepaymentScheduler> findByLoanAccount_LanAndStatusOrderByDueDateAsc(UUID lan, RepaymentStatus status);
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/LoanCreditRepository.java
package com.lms.backend.repository;

import com.lms.backend.entity.LoanCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LoanCreditRepository extends JpaRepository<LoanCredit, UUID> {
}
INNER_EOF

cat << 'INNER_EOF' > $DIR/SettlementAuditRepository.java
package com.lms.backend.repository;

import com.lms.backend.entity.SettlementAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SettlementAuditRepository extends JpaRepository<SettlementAudit, UUID> {
}
INNER_EOF

chmod +x gen_repos.sh
./gen_repos.sh
