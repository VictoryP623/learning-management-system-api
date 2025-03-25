package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.WithdrawResponseDTO;
import com.example.learning_management_system_api.entity.Withdraw;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WithdrawMapper {
  WithdrawResponseDTO toResponseDTO(Withdraw withdraw);
}
