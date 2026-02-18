package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Users, Integer> {
    @Query("SELECT COUNT(c) FROM Board c WHERE c.user.userIdx = :userIdx")
    Integer countBoardsByUserIdx(@Param("userIdx") Integer userIdx);

    @Query("SELECT COUNT(p) FROM Used p WHERE p.user.userIdx = :userIdx")
    Integer countUsedByUserIdx(@Param("userIdx") Integer userIdx);


}