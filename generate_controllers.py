import os

controllers = {
    "UserController": """package com.lms.backend.controller;
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
    @Autowired private UserService service;
    @PostMapping public ResponseEntity<User> add(@RequestBody User u) { return ResponseEntity.ok(service.add(u)); }
    @GetMapping public ResponseEntity<List<User>> getAll() { return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/{id}") public ResponseEntity<User> get(@PathVariable UUID id) { return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }
    @PutMapping("/{id}") public ResponseEntity<User> update(@PathVariable UUID id, @RequestBody User u) {
        return service.get(id).map(existing -> {
            existing.setUserName(u.getUserName());
            existing.setUserAddress(u.getUserAddress());
            existing.setUserPhone(u.getUserPhone());
            existing.setUserKycDetails(u.getUserKycDetails());
            return ResponseEntity.ok(service.add(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.ok().build(); }
}""",
    "LenderController": """package com.lms.backend.controller;
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
    @Autowired private LenderService service;
    @PostMapping public ResponseEntity<Lender> add(@RequestBody Lender u) { return ResponseEntity.ok(service.add(u)); }
    @GetMapping public ResponseEntity<List<Lender>> getAll() { return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/{id}") public ResponseEntity<Lender> get(@PathVariable UUID id) { return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }
    @PutMapping("/{id}") public ResponseEntity<Lender> update(@PathVariable UUID id, @RequestBody Lender u) {
        return service.get(id).map(existing -> {
            existing.setLenderName(u.getLenderName());
            existing.setLenderContact(u.getLenderContact());
            existing.setLenderDetails(u.getLenderDetails());
            return ResponseEntity.ok(service.add(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.ok().build(); }
}""",
    "LoanController": """package com.lms.backend.controller;
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
    @Autowired private LoanService service;
    @PostMapping public ResponseEntity<Loan> add(@RequestBody Loan u) { return ResponseEntity.ok(service.add(u)); }
    @GetMapping public ResponseEntity<List<Loan>> getAll() { return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/{id}") public ResponseEntity<Loan> get(@PathVariable UUID id) { return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }
    @PutMapping("/{id}") public ResponseEntity<Loan> update(@PathVariable UUID id, @RequestBody Loan u) {
        return service.get(id).map(existing -> {
            existing.setLoanAmountMin(u.getLoanAmountMin());
            existing.setLoanAmountMax(u.getLoanAmountMax());
            existing.setLoanInterestMin(u.getLoanInterestMin());
            existing.setLoanInterestMax(u.getLoanInterestMax());
            existing.setLoanTimeMin(u.getLoanTimeMin());
            existing.setLoanTimeMax(u.getLoanTimeMax());
            existing.setTypeOfLoan(u.getTypeOfLoan());
            return ResponseEntity.ok(service.add(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.ok().build(); }
}""",
    "ChargeController": """package com.lms.backend.controller;
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
    @Autowired private ChargeService service;
    @PostMapping public ResponseEntity<Charge> add(@RequestBody Charge u) { return ResponseEntity.ok(service.add(u)); }
    @GetMapping public ResponseEntity<List<Charge>> getAll() { return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/{id}") public ResponseEntity<Charge> get(@PathVariable UUID id) { return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }
    @PutMapping("/{id}") public ResponseEntity<Charge> update(@PathVariable UUID id, @RequestBody Charge u) {
        return service.get(id).map(existing -> {
            existing.setDpd(u.getDpd());
            existing.setChargeAmount(u.getChargeAmount());
            return ResponseEntity.ok(service.add(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.ok().build(); }
}"""
}

def write_files(directory, prefix, suffix, data):
    os.makedirs(directory, exist_ok=True)
    for name, content in data.items():
        with open(os.path.join(directory, f"{name}{suffix}"), "w") as f:
            if not content.startswith("package"):
                f.write(f"package {prefix};\n\n")
            f.write(content)

write_files("src/main/java/com/lms/backend/controller", "com.lms.backend.controller", ".java", controllers)

print("Generated controllers successfully.")
