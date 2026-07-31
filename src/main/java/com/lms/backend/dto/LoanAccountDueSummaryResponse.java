package com.lms.backend.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lms.backend.entity.LoanAccountDue;
import com.lms.backend.utils.DoubleRoundingSerializer;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountDueSummaryResponse {
    @JsonSerialize(using = DoubleRoundingSerializer.class)
    private Double duePrincipal;
    
    @JsonSerialize(using = DoubleRoundingSerializer.class)
    private Double dueInterest;
    
    @JsonSerialize(using = DoubleRoundingSerializer.class)
    private Double dueCharges;
    
    @JsonSerialize(using = DoubleRoundingSerializer.class)
    private Double totalDueAmount;
}
