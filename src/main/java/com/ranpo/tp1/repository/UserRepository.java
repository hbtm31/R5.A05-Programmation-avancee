package com.ranpo.tp1.repository;

import com.ranpo.tp1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
