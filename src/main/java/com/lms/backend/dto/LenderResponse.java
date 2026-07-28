package com.lms.backend.dto;

import com.lms.backend.entity.Lender;

public record LenderResponse(
    Long lenderId, 
    String lenderName, 
    String lenderContact, 
    String lenderDetails
) {
  public static LenderResponse fromEntity(Lender lender) {
    if (lender == null) return null;
    return new LenderResponse(
        lender.getLenderId(), 
        lender.getLenderName(), 
        lender.getLenderContact(), 
        lender.getLenderDetails()
    );
  }
}
