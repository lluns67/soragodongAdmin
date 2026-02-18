package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Integer> {


    /**
     * 파일 그룹에서 첫 번째 파일 조회 (썸네일용)
     */

}
