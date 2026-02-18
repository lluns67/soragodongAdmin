package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.Admin;
import com.scit.soragodong.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface  StoreRepository extends JpaRepository<Store, Integer>{
        /**
     * 사용 중인 가게 조회
     */
    List<Store> findByIsUse(byte isUse);
    
    /**
     * 이벤트 상태별 가게 조회
     */
    List<Store> findByEventState(String eventState);
    
    /**
     * 가게명으로 검색
     */
    List<Store> findByStoreNameContaining(String storeName);

    @Query("SELECT s FROM Store s WHERE s.owner = :owner")
    Optional<Store> findByOwner(@Param("owner") Admin owner);
}
