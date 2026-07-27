#!/bin/bash
SER_DIR="src/main/java/com/lms/backend/service"
CTRL_DIR="src/main/java/com/lms/backend/controller"

# User Service
cat << 'INNER_EOF' > $SER_DIR/UserService.java
package com.lms.backend.service;

import com.lms.backend.entity.User;
import com.lms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User addUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUser(UUID id) {
        return userRepository.findById(id);
    }

    public User updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setUserName(userDetails.getUserName());
            user.setUserAddress(userDetails.getUserAddress());
            user.setUserPhone(userDetails.getUserPhone());
            user.setUserKycDetails(userDetails.getUserKycDetails());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}
INNER_EOF

# User Controller
cat << 'INNER_EOF' > $CTRL_DIR/UserController.java
package com.lms.backend.controller;

import com.lms.backend.entity.User;
import com.lms.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.addUser(user));
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
INNER_EOF

# Lender Service
cat << 'INNER_EOF' > $SER_DIR/LenderService.java
package com.lms.backend.service;

import com.lms.backend.entity.Lender;
import com.lms.backend.repository.LenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LenderService {
    @Autowired
    private LenderRepository lenderRepository;

    public Lender addLender(Lender lender) {
        return lenderRepository.save(lender);
    }

    public List<Lender> getLenders() {
        return lenderRepository.findAll();
    }

    public Optional<Lender> getLender(UUID id) {
        return lenderRepository.findById(id);
    }

    public Lender updateLender(UUID id, Lender details) {
        return lenderRepository.findById(id).map(lender -> {
            lender.setLenderName(details.getLenderName());
            lender.setLenderContact(details.getLenderContact());
            lender.setLenderDetails(details.getLenderDetails());
            return lenderRepository.save(lender);
        }).orElseThrow(() -> new RuntimeException("Lender not found"));
    }

    public void deleteLender(UUID id) {
        lenderRepository.deleteById(id);
    }
}
INNER_EOF

# Lender Controller
cat << 'INNER_EOF' > $CTRL_DIR/LenderController.java
package com.lms.backend.controller;

import com.lms.backend.entity.Lender;
import com.lms.backend.service.LenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lenders")
public class LenderController {
    @Autowired
    private LenderService lenderService;

    @PostMapping
    public ResponseEntity<Lender> addLender(@RequestBody Lender lender) {
        return ResponseEntity.ok(lenderService.addLender(lender));
    }

    @GetMapping
    public ResponseEntity<List<Lender>> getLenders() {
        return ResponseEntity.ok(lenderService.getLenders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lender> getLender(@PathVariable UUID id) {
        return lenderService.getLender(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lender> updateLender(@PathVariable UUID id, @RequestBody Lender lender) {
        return ResponseEntity.ok(lenderService.updateLender(id, lender));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLender(@PathVariable UUID id) {
        lenderService.deleteLender(id);
        return ResponseEntity.ok().build();
    }
}
INNER_EOF

# Loan Service
cat << 'INNER_EOF' > $SER_DIR/LoanService.java
package com.lms.backend.service;

import com.lms.backend.entity.Loan;
import com.lms.backend.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    public Loan addLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    public List<Loan> getLoans() {
        return loanRepository.findAll();
    }

    public Optional<Loan> getLoan(UUID id) {
        return loanRepository.findById(id);
    }

    public Loan updateLoan(UUID id, Loan details) {
        return loanRepository.findById(id).map(loan -> {
            loan.setLoanAmountMin(details.getLoanAmountMin());
            loan.setLoanAmountMax(details.getLoanAmountMax());
            loan.setLoanInterestMin(details.getLoanInterestMin());
            loan.setLoanInterestMax(details.getLoanInterestMax());
            loan.setLoanTimeMin(details.getLoanTimeMin());
            loan.setLoanTimeMax(details.getLoanTimeMax());
            loan.setTypeOfLoan(details.getTypeOfLoan());
            return loanRepository.save(loan);
        }).orElseThrow(() -> new RuntimeException("Loan not found"));
    }

    public void deleteLoan(UUID id) {
        loanRepository.deleteById(id);
    }
}
INNER_EOF

# Loan Controller
cat << 'INNER_EOF' > $CTRL_DIR/LoanController.java
package com.lms.backend.controller;

import com.lms.backend.entity.Loan;
import com.lms.backend.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<Loan> addLoan(@RequestBody Loan loan) {
        return ResponseEntity.ok(loanService.addLoan(loan));
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getLoans() {
        return ResponseEntity.ok(loanService.getLoans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoan(@PathVariable UUID id) {
        return loanService.getLoan(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(@PathVariable UUID id, @RequestBody Loan loan) {
        return ResponseEntity.ok(loanService.updateLoan(id, loan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable UUID id) {
        loanService.deleteLoan(id);
        return ResponseEntity.ok().build();
    }
}
INNER_EOF

# Charge Service
cat << 'INNER_EOF' > $SER_DIR/ChargeService.java
package com.lms.backend.service;

import com.lms.backend.entity.Charge;
import com.lms.backend.repository.ChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChargeService {
    @Autowired
    private ChargeRepository chargeRepository;

    public Charge addCharge(Charge charge) {
        return chargeRepository.save(charge);
    }

    public List<Charge> getCharges() {
        return chargeRepository.findAll();
    }

    public Optional<Charge> getCharge(UUID id) {
        return chargeRepository.findById(id);
    }

    public Charge updateCharge(UUID id, Charge details) {
        return chargeRepository.findById(id).map(charge -> {
            charge.setDpd(details.getDpd());
            charge.setChargeAmount(details.getChargeAmount());
            return chargeRepository.save(charge);
        }).orElseThrow(() -> new RuntimeException("Charge not found"));
    }

    public void deleteCharge(UUID id) {
        chargeRepository.deleteById(id);
    }
}
INNER_EOF

# Charge Controller
cat << 'INNER_EOF' > $CTRL_DIR/ChargeController.java
package com.lms.backend.controller;

import com.lms.backend.entity.Charge;
import com.lms.backend.service.ChargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/charges")
public class ChargeController {
    @Autowired
    private ChargeService chargeService;

    @PostMapping
    public ResponseEntity<Charge> addCharge(@RequestBody Charge charge) {
        return ResponseEntity.ok(chargeService.addCharge(charge));
    }

    @GetMapping
    public ResponseEntity<List<Charge>> getCharges() {
        return ResponseEntity.ok(chargeService.getCharges());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Charge> getCharge(@PathVariable UUID id) {
        return chargeService.getCharge(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Charge> updateCharge(@PathVariable UUID id, @RequestBody Charge charge) {
        return ResponseEntity.ok(chargeService.updateCharge(id, charge));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharge(@PathVariable UUID id) {
        chargeService.deleteCharge(id);
        return ResponseEntity.ok().build();
    }
}
INNER_EOF

chmod +x gen_crud.sh
./gen_crud.sh
