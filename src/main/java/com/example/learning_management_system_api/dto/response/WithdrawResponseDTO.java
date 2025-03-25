package com.example.learning_management_system_api.dto.response;

import lombok.Data;

@Data
public class WithdrawResponseDTO {
  public Long id;
  public Long instructorId;
  public Double amount;
}
