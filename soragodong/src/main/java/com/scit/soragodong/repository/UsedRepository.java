package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.Used;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsedRepository extends JpaRepository<Used, Integer> {


    List<Used>  findByUser_UserIdx(Integer userIdx);
	
	@Query("SELECT u FROM Used u WHERE u.usedTitle LIKE %:word% OR u.usedContent LIKE %:word% OR u.user.userNickname LIKE %:word%")
	List<Used> searchByWord(@Param("word") String word);
}
