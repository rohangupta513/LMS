package com.lms.backend.dto;

import com.lms.backend.entity.User;

public record UserResponse(
    Long userId, 
    String userName, 
    String userAddress, 
    String userPhone, 
    String userKycDetails
) {
  public static UserResponse fromEntity(User user) {
    if (user == null) return null;
    return new UserResponse(
        user.getUserId(), 
        user.getUserName(), 
        user.getUserAddress(), 
        user.getUserPhone(), 
        user.getUserKycDetails()
    );
  }
}
