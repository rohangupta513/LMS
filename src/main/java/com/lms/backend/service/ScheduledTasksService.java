package com.lms.backend.service;

import com.lms.backend.entity.LoanAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
/**
 * Service class encapsulating business logic for ScheduledTasksService operations.
 * Interfaces with repositories to perform database transactions.
 */
public class ScheduledTasksService {

    @Autowired
    private LoanApplicationService loanApplicationService;

    // Runs every day at 00:00:01 (1 second past midnight)
    @Scheduled(cron = "1 0 0 * * ?")
    public void calculateDailyDpdAndPenalties() {
        System.out.println("Starting daily DPD calculation at " + LocalDate.now());
        
        List<LoanAccount> allAccounts = loanApplicationService.getAllLoanAccounts();
        for (LoanAccount account : allAccounts) {
            // Only calculate for accounts that are ACTIVE
            if ("ACTIVE".equals(account.getStatus().name())) {
                try {
                    loanApplicationService.calculateDpdAndPenalties(account.getLan(), LocalDate.now());
                } catch (Exception e) {
                    System.err.println("Error calculating DPD for LAN " + account.getLan() + ": " + e.getMessage());
                }
            }
        }
        System.out.println("Finished daily DPD calculation");
    }
}
