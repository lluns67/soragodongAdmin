package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.FileGrp;
import com.scit.soragodong.domain.enums.FileRefType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileGrpRepository extends JpaRepository<FileGrp, Integer> {

    Optional<FileGrp> findByRefTypeAndRefId(FileRefType refType, Integer refId);
}
