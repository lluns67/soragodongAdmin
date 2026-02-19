package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.File;
import com.scit.soragodong.domain.entity.FileGrp;
import com.scit.soragodong.domain.enums.FileRefType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Integer> {

    List<File> findByFileGroupAndIsUseTrueOrderByFileOrder(FileGrp group);

    /**
     * 파일 그룹에서 첫 번째 파일 조회 (썸네일용)
     */
    Optional<File> findFirstByFileGroupAndIsUseTrueOrderByFileOrder(FileGrp fileGroup);
	
	List<File> findByFileGroup_RefTypeAndFileGroup_RefId(FileRefType fileRefType, Integer noticeIdx);
}

