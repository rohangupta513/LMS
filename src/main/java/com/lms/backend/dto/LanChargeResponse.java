package com.lms.backend.dto;

import com.lms.backend.entity.LanCharge;
import java.time.LocalDate;

public record LanChargeResponse(
    Long lan,
    Integer dpd,
    Double penalCharges,
    Double otherFees,
    LocalDate lastCalculatedDate
) {
  public static LanChargeResponse fromEntity(LanCharge charge) {
    if (charge == null) return null;
    return new LanChargeResponse(
        charge.getLan(),
        charge.getDpd(),
        charge.getPenalCharges(),
        charge.getOtherFees(),
        charge.getLastCalculatedDate()
    );
  }
}
