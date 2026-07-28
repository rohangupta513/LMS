package com.lms.backend.dto;

import com.lms.backend.entity.Charge;

public record ChargeResponse(
    Long chargeId,
    Integer dpd,
    Double chargeAmount
) {
  public static ChargeResponse fromEntity(Charge charge) {
    if (charge == null) return null;
    return new ChargeResponse(
        charge.getChargeId(),
        charge.getDpd(),
        charge.getChargeAmount()
    );
  }
}
