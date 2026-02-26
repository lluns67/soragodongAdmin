package com.scit.soragodong.repository;

import com.scit.soragodong.domain.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    // 1. [기본] 전체 조회 (삭제 안 된 것)
    Page<Board> findByIsUseTrue(Pageable pageable);

    // 2. [카테고리] 카테고리 + 삭제 안 된 것
    Page<Board> findByBoardCategoryAndIsUseTrue(String boardCategory, Pageable pageable);

    // 3. [검색] (제목 포함 OR 내용 포함) AND 삭제 안 된 것
    // ★ 설명: Containing이 붙으면 알아서 LIKE %keyword% 가 됩니다.
    // ★ 논리: (삭제안됨 AND 제목포함) OR (삭제안됨 AND 내용포함)
    Page<Board> findByIsUseTrueAndBoardTitleContainingOrIsUseTrueAndBoardContentContaining(String title, String content,
                                                                                           Pageable pageable);

    // 4. [카테고리 + 검색] (제목 포함 OR 내용 포함) AND 카테고리 일치 AND 삭제 안 된 것
    Page<Board> findByIsUseTrueAndBoardCategoryAndBoardTitleContainingOrIsUseTrueAndBoardCategoryAndBoardContentContaining(
            String category1, String title, String category2, String content, Pageable pageable);

    List<Board> findByUser_UserIdx(Integer userIdx);
	
	@Query("SELECT b FROM Board b WHERE b.boardTitle LIKE %:word% OR b.boardContent LIKE %:word% OR b.user.userNickname LIKE %:word%")
	List<Board> searchByWord(@Param("word") String word);
}
