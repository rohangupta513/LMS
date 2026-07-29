# System Architecture and Flow Charts

Here is the high-level overview of the LMS project's architecture, API surface, and database structure.

## 1. High-Level Entity & Controller Flow
This diagram illustrates the core REST Controllers exposed to clients, the database tables they manage, and the relational structure (1:N, 1:1) between those tables.

```mermaid
flowchart LR
    subgraph "REST Controllers"
        U["UserController"]
        L["LenderController"]
        LM["LoanController"]
        LA["LoanApplicationController"]
        S["SettlementController"]
    end

    subgraph "Database Tables (Entities)"
        DB_USER[("User")]
        DB_LENDER[("Lender")]
        DB_LOAN[("Loan")]
        DB_LOAN_ACCOUNT[("LoanAccount")]
        DB_LOAN_DUE[("LoanAccountDue")]
        DB_REPAYMENT[("RepaymentScheduler")]
        DB_CREDIT[("LoanCredit")]
        DB_LAN_CHARGE[("LanCharge")]
    end

    %% Controller to DB Connections
    U -->|Manages| DB_USER
    L -->|Manages| DB_LENDER
    LM -->|Manages| DB_LOAN
    LA -->|Creates & Modifies| DB_LOAN_ACCOUNT
    S -->|Records Payments| DB_CREDIT
    S -->|Updates Ledger| DB_LOAN_DUE
    S -->|Updates Schedule| DB_REPAYMENT
    S -->|Applies Penalties| DB_LAN_CHARGE

    %% DB Relationships
    DB_USER -.->|1:N| DB_LOAN_ACCOUNT
    DB_LENDER -.->|1:N| DB_LOAN
    DB_LOAN -.->|1:N| DB_LOAN_ACCOUNT
    DB_LOAN_ACCOUNT -.->|1:1| DB_LOAN_DUE
    DB_LOAN_ACCOUNT -.->|1:1| DB_LAN_CHARGE
    DB_LOAN_ACCOUNT -.->|1:N| DB_REPAYMENT
    DB_LOAN_ACCOUNT -.->|1:N| DB_CREDIT
```

## 2. Layered Application Architecture
This sequence diagram shows how data flows vertically through the different layers of the Spring Boot application—from the client all the way to the database.

```mermaid
sequenceDiagram
    autonumber
    actor Client as User / Frontend
    participant Controller as Controller Layer (REST)
    participant ExceptionHandler as @RestControllerAdvice
    participant Service as Service Layer (Business Logic)
    participant Repository as Repository Layer (Spring Data JPA)
    participant DB as Database (MySQL / H2)

    Client->>Controller: HTTP Request (e.g. POST /api/applications/apply)
    
    alt Validation Failed
        Controller-->>ExceptionHandler: Throws MethodArgumentNotValidException
        ExceptionHandler-->>Client: 400 Bad Request (Field errors)
    else Validation Passed
        Controller->>Service: Call Service Method (DTO to Entity mapping)
        
        Note over Service: Execute Business Logic <br/>(e.g., Calculate EMI, check rules)
        
        alt Business Logic / Data Error
            Service-->>ExceptionHandler: Throws RuntimeException or DataAccessException
            ExceptionHandler-->>Client: 400 Bad Request / 500 Internal Server Error
        else Logic Successful
            Service->>Repository: save(entity) / findById(id)
            Repository->>DB: Execute SQL Query (Hibernate)
            DB-->>Repository: Result Set
            Repository-->>Service: Managed JPA Entity Objects
            
            Note over Service: Format final result
            Service-->>Controller: Return Entities or DTOs
            Controller-->>Client: HTTP 200 OK Response (JSON payload)
        end
    end
```

## 3. Explicit Function Call Chain (Example: Apply for Loan)
This sequence diagram illustrates a concrete example of how a specific API request cascades through the exact methods and functions in each architectural layer, resulting in database changes.

```mermaid
sequenceDiagram
    autonumber
    
    box rgb(240, 248, 255) "Client Layer"
        actor User
    end
    
    box rgb(245, 245, 245) "API Layer"
        participant Controller as LoanAppController
    end
    
    box rgb(255, 250, 240) "Business Logic Layer"
        participant Service as LoanAppService
    end
    
    box rgb(240, 255, 240) "Data Access Layer (JPA)"
        participant UserRepo as UserRepository
        participant LoanRepo as LoanAccountRepo
    end
    
    box rgb(255, 245, 245) "Storage Layer"
        participant DB as Database
    end

    User->>Controller: POST /api/applications/apply
    Note over User,Controller: Payload: { userId, loanId, amount, tenure }
    activate Controller
    
    Controller->>Service: applyForLoan(userId, loanId, amount, tenure)
    activate Service
    
    %% Fetch User Step
    Service->>UserRepo: findById(userId)
    activate UserRepo
    UserRepo->>DB: SELECT * FROM user WHERE id = ?
    DB-->>UserRepo: ResultSet (User Row)
    UserRepo-->>Service: Optional<User>
    deactivate UserRepo
    
    %% Internal Logic Step
    Service->>Service: 1. Validate User & Loan<br/>2. Calculate EMI & Interest<br/>3. Construct Entity
    
    %% Save Loan Step
    Service->>LoanRepo: save(loanAccount)
    activate LoanRepo
    LoanRepo->>DB: INSERT INTO loan_account (amount, emi, ...)
    DB-->>LoanRepo: Generated ID (LAN)
    LoanRepo-->>Service: Saved LoanAccount Entity
    deactivate LoanRepo
    
    Service-->>Controller: LoanAccount Entity
    deactivate Service
    
    Controller-->>User: HTTP 200 OK (JSON)
    deactivate Controller
```

## 4. Comprehensive API to SQL Mapping
This flowchart provides a clean, horizontal mapping of every major REST API endpoint, tracing it through its specific Controller function, its corresponding Service function, and finally mapping it to the exact core SQL operation (INSERT, UPDATE, SELECT) that gets executed in the database.

```mermaid
flowchart LR
    subgraph "User & Lender Entities"
        A1("POST /api/users/add") --> A2["UserController.add()"] --> A3["UserService.add()"] --> A4[("INSERT")]
        B1("PUT /api/users/update") --> B2["UserController.update()"] --> B3["UserService.update()"] --> B4[("UPDATE")]
        C1("POST /api/lenders/add") --> C2["LenderController.add()"] --> C3["LenderService.add()"] --> C4[("INSERT")]
    end

    subgraph "Loan Product Configuration"
        D1("POST /api/loans/add") --> D2["LoanController.add()"] --> D3["LoanService.add()"] --> D4[("INSERT")]
        E1("PUT /api/loans/update") --> E2["LoanController.update()"] --> E3["LoanService.update()"] --> E4[("UPDATE")]
    end

    subgraph "Loan Application Lifecycle"
        F1("POST /applications/apply") --> F2["LoanApplicationController.applyForLoan()"] --> F3["LoanApplicationService.applyForLoan()"] --> F4[("INSERT")]
        G1("POST /applications/inquire") --> G2["LoanApplicationController.inquireLoan()"] --> G3["LoanApplicationService.inquireLoan()"] --> G4[("SELECT")]
        H1("POST /applications/cancel") --> H2["LoanApplicationController.cancelLoan()"] --> H3["CancellationService.cancelLoan()"] --> H4[("UPDATE")]
    end

    subgraph "Settlements & Payments"
        I1("POST /settlement/credit") --> I2["SettlementController.addCredit()"] --> I3["SettlementService.addCredit()"] --> I4[("INSERT")]
        J1("POST /settlement/verify-credit") --> J2["SettlementController.verifyCredit()"] --> J3["SettlementService.verifyCredit()"] --> J4[("UPDATE")]
        K1("POST /settlement/foreclose") --> K2["SettlementController.forecloseLoan()"] --> K3["ForeclosureService.forecloseLoan()"] --> K4[("INSERT/UPDATE")]
        L1("POST /settlement/verify-foreclosure") --> L2["SettlementController.verifyForeclosure()"] --> L3["ForeclosureService.verifyForeclosure()"] --> L4[("UPDATE")]
        M1("POST /settlement/reactivate") --> M2["SettlementController.reactivateLoan()"] --> M3["ReactivationService.reactivateLoan()"] --> M4[("UPDATE")]
    end
```

## 5. Loan Cancellation Flow & Validation Checks
This flowchart details the exact validation checks and business rules enforced during the strict two-step loan cancellation process. It highlights how `CancellationService` handles edge cases to ensure financial integrity.

```mermaid
flowchart TD
    %% Step 1: Initiate Cancellation
    subgraph "Phase 1: cancelLoan() - CancellationService"
        A[Start: POST /applications/cancel] --> B{Loan Exists?}
        B -- No --> B_Err[Throw ResourceNotFoundException]
        B -- Yes --> C{Is Status ACTIVE?}
        C -- No --> C_Err[Throw: Only active loans can be cancelled]
        C -- Yes --> D{Is <= 5 Days from Start Date?}
        D -- No --> D_Err[Throw: Cancelled within 5 days only]
        D -- Yes --> E[Mark as PENDING_CANCELLATION]
        
        E --> F[Halt all PENDING dues in RepaymentScheduler]
        F --> G[Add ₹500 flat Cancellation Fee to LanCharge]
        G --> H[Update Ledger: Principal + ₹500 Due Immediately]
    end

    %% Transition
    H -->|User makes payments via Settlement API| I

    %% Step 2: Verify Cancellation
    subgraph "Phase 2: verifyCancellation() - CancellationService"
        I[Start: POST /settlement/verify-cancellation] --> J{Loan Exists?}
        J -- No --> J_Err[Throw ResourceNotFoundException]
        J -- Yes --> K{Is Status PENDING_CANCELLATION?}
        K -- No --> K_Err[Throw: Account not pending cancellation]
        K -- Yes --> L{Outstanding Amount > 0 OR Charges > 0?}
        L -- Yes --> L_Err[Throw: Dues are not fully settled]
        L -- No --> N[Mark Loan as CANCELLED]
        N --> O[Mark Ledger as isSettled = true]
    end
    
    style B_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style C_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style D_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style J_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style K_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style L_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    
    style H fill:#e6ffe6,stroke:#009900,stroke-width:2px,color:#000000
    style O fill:#e6ffe6,stroke:#009900,stroke-width:2px,color:#000000
```

## 6. Loan Foreclosure Flow & Validation Checks
This flowchart maps out the two-phase lifecycle of a loan foreclosure request, illustrating the consolidation of dues, fee application, and final verification logic.

```mermaid
flowchart TD
    %% Step 1: Initiate Foreclosure
    subgraph "Phase 1: forecloseLoan() - ForeclosureService"
        A[Start: POST /settlement/foreclose] --> B{Loan Exists?}
        B -- No --> B_Err[Throw ResourceNotFoundException]
        B -- Yes --> C{Is Status ACTIVE?}
        C -- No --> C_Err[Throw: Only Active loans can be foreclosed]
        C -- Yes --> E[Mark as PENDING_FORECLOSURE]
        
        E --> F[Consolidate all PENDING dues into one Immediate Due]
        F --> G[Add ₹1000 flat Foreclosure Fee to LanCharge]
        G --> H[Update Ledger: Total Due + ₹1000 Due Immediately]
    end

    %% Transition
    H -->|User makes payments via Settlement API| I

    %% Step 2: Verify Foreclosure
    subgraph "Phase 2: verifyForeclosure() - ForeclosureService"
        I[Start: POST /settlement/verify-foreclosure] --> J{Loan Exists?}
        J -- No --> J_Err[Throw ResourceNotFoundException]
        J -- Yes --> K{Is Status PENDING_FORECLOSURE?}
        K -- No --> K_Err[Throw: Account not pending foreclosure]
        K -- Yes --> L{Is Ledger Settled?}
        L -- No --> L_Err[Throw: Payment has not been fully settled]
        L -- Yes --> N[Mark Loan as FORECLOSED]
        N --> O[Mark Ledger as isForeclosed = true]
    end
    
    style B_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style C_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style J_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style K_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style L_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    
    style H fill:#e6ffe6,stroke:#009900,stroke-width:2px,color:#000000
    style O fill:#e6ffe6,stroke:#009900,stroke-width:2px,color:#000000
```

## 7. Natural Maturity Closure Flow (Automatic Settlement)
Unlike Foreclosure and Cancellation which require explicit API requests and strict two-phase flows, **Maturity Closure** happens completely automatically as part of the standard EMI payment lifecycle. 

When the user pays their final EMI, `SettlementService` handles the automatic closure natively during credit verification.

```mermaid
flowchart TD
    %% Standard EMI Payment Process
    subgraph "Standard Payment Processing - SettlementService"
        A["Start: POST /settlement/verify-credit"] --> B["Mark specific RepaymentScheduler dues as PAID"]
        B --> C["Call internal method: updateLoanAccountDue()"]
        
        C --> D{"Are there any PENDING dues left?"}
        D -- Yes --> E["Recalculate Next Due Amount & Date"]
        D -- No --> F{"Are global penalties/fees <= 0?"}
        
        F -- No --> G["Set Next Due Amount = remaining global charges"]
        
        F -- Yes --> H{"Is Status PENDING_CANCELLATION <br> OR PENDING_FORECLOSURE?"}
        
        H -- Yes --> I["Skip status update <br/> Leave for strict verify APIs"]
        
        H -- No --> J["Mark Ledger as isSettled = true"]
        J --> K["Set all Outstanding Amounts to 0.0"]
        K --> L["Mark Loan Status as CLOSED <br/> MATURITY REACHED"]
    end
    
    style E fill:#ffffe6,stroke:#cccc00,stroke-width:2px,color:#000000
    style G fill:#ffffe6,stroke:#cccc00,stroke-width:2px,color:#000000
    style I fill:#ffffe6,stroke:#cccc00,stroke-width:2px,color:#000000
    
    style J fill:#e6ffe6,stroke:#009900,stroke-width:2px,color:#000000
    style K fill:#e6ffe6,stroke:#009900,stroke-width:2px,color:#000000
    style L fill:#e6ffe6,stroke:#009900,stroke-width:2px,color:#000000
```

## 8. Account Reactivation Flow & Reversals
This flowchart illustrates the recovery mechanism inside `ReactivationService`. If a user initiates a cancellation or foreclosure but changes their mind (or fails to pay the settlement dues), this flow cleanly reverses the applied penalties and restores the original EMI schedule.

```mermaid
flowchart TD
    %% Reactivation Process
    subgraph "Reactivation - ReactivationService"
        A[Start: POST /settlement/reactivate] --> B{Loan Exists?}
        B -- No --> B_Err[Throw ResourceNotFoundException]
        B -- Yes --> C{Is Status PENDING_CANCELLATION <br> or PENDING_FORECLOSURE?}
        C -- No --> C_Err[Throw: Account must be pending closure]
        
        C -- Yes --> D{Are Outstanding Dues <= 0?}
        D -- Yes --> D_Err[Throw: Charges already paid, cannot reverse]
        
        D -- No --> E{Was it Cancellation or Foreclosure?}
        
        %% Branch: Cancellation Reversal
        E -- PENDING_CANCELLATION --> F1[Subtract ₹500 fee from LanCharge]
        F1 --> F2[Find CANCELLED schedules and restore to PENDING]
        
        %% Branch: Foreclosure Reversal
        E -- PENDING_FORECLOSURE --> G1[Subtract ₹1000 fee from LanCharge]
        G1 --> G2[Delete the newly aggregated Immediate schedule]
        G2 --> G3[Find forcefully PAID schedules and restore to PENDING]
        
        %% Merge back to common flow
        F2 --> H[Mark Loan Status back to ACTIVE]
        G3 --> H
        
        H --> I[Call calculateDpdAndPenalties to recalculate Master Ledger]
        I --> J[Return restored LoanAccount]
    end
    
    style B_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style C_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    style D_Err fill:#ffe6e6,stroke:#e60000,stroke-width:2px,color:#000000
    
    style J fill:#e6ffe6,stroke:#009900,stroke-width:2px,color:#000000
```
