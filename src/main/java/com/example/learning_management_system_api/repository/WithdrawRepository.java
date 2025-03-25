package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Withdraw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawRepository extends JpaRepository<Withdraw, Long> {}
