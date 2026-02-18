package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.Used;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsedRepository extends JpaRepository<Used, Integer> {


    List<Used>  findByUser_UserIdx(Integer userIdx);
}
