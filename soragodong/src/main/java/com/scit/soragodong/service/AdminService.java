package com.scit.soragodong.service;

import com.scit.soragodong.domain.dto.AdminPostDto;
import com.scit.soragodong.domain.dto.BoardDto;
import com.scit.soragodong.domain.dto.BoardReplyDto;
import com.scit.soragodong.domain.dto.UsedDto;
import com.scit.soragodong.domain.entity.*;
import com.scit.soragodong.domain.enums.FileRefType;
import com.scit.soragodong.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final BoardRepository boardRepository;
    private final UsedRepository usedRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final FileGrpRepository fileGrpRepository;
    private final FileRepository fileRepository;
    private final BoardReplyRepository boardReplyRepository;


    @Transactional
    public Admin login(String adminId, String password) {
        // 1. 아이디 조회
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 계정입니다."));

        // 2. 비밀번호 비교 (실제 서비스에서는 PasswordEncoder 사용 권장)
        if (!admin.getAdminPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return admin;
    }


    public List<AdminPostDto> getUserTotalPosts(Integer userIdx) {
        // 1. 유저 닉네임 확보
        String nickname = userRepository.findById(userIdx)
                .map(Users::getUserNickname)
                .orElse("알 수 없음");

        // 2. 커뮤니티 글 조회 -> BoardDto -> AdminPostDto
        List<AdminPostDto> boardList = boardRepository.findByUser_UserIdx(userIdx).stream()
                .map(entity -> BoardDto.builder()
                        .boardIdx(entity.getBoardIdx())
                        .userIdx(entity.getUser().getUserIdx())

                        .boardCategory(entity.getBoardCategory())
                        .boardTitle(entity.getBoardTitle())
                        .boardContent(entity.getBoardContent())
                        .userNickname(entity.getUser().getUserNickname())
                        .isUse(entity.getIsUse())

                        .likeCount(entity.getLikeCount())
                        .viewCount(entity.getViewCount())
                        .createdAt(entity.getCreateDate())
                        .fileGrp(entity.getFileGrp() != null ? entity.getFileGrp().getFileGrpIdx() : null)
                        .build())
                .map(AdminPostDto::fromBoard)
                .toList();

        // 3. 중고거래 글 조회 -> UsedDto -> AdminPostDto
        List<AdminPostDto> usedList = usedRepository.findByUser_UserIdx(userIdx).stream()
                .map(entity -> {
                    UsedDto u = new UsedDto(
                        entity.getUsedIdx(),
                        entity.getUsedTitle(),
                        entity.getUsedContent(),
                        entity.getUsedPrice(),
                        entity.getUsedState(),
                        entity.getTradingLoc(),
                        entity.getViewCount(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt(),
                        entity.getIsUse(),
                        entity.getUser().getUserIdx()
                    );
                    // FileGrpIdx 찾기
                    fileGrpRepository.findByRefTypeAndRefId(FileRefType.USED, entity.getUsedIdx())
                            .ifPresent(fg -> u.setFileGrp(fg.getFileGrpIdx()));
                    return u;
                })
                .map(u -> AdminPostDto.fromUsed(u, nickname))
                .toList();


        // 4. 통합 및 정렬
        List<AdminPostDto> combined = new ArrayList<>();
        combined.addAll(boardList);
        combined.addAll(usedList);

        return combined.stream()
                .sorted(Comparator.comparing(AdminPostDto::getDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<AdminPostDto> findAllPosts() {
        // 1. Board 전체 조회 -> BoardDto -> AdminPostDto
        List<AdminPostDto> boardList = boardRepository.findAll().stream()
                .map(entity -> BoardDto.builder()
                        .boardIdx(entity.getBoardIdx())
                        .userIdx(entity.getUser().getUserIdx())
                        .boardCategory(entity.getBoardCategory())
                        .boardTitle(entity.getBoardTitle())
                        .boardContent(entity.getBoardContent())
                        .userNickname(entity.getUser().getUserNickname())
                        .isUse(entity.getIsUse())
                        .likeCount(entity.getLikeCount())
                        .viewCount(entity.getViewCount())
                        .createdAt(entity.getCreateDate())
                        .build())
                .map(AdminPostDto::fromBoard)
                .toList();

        // 2. Used 전체 조회 -> UsedDto -> AdminPostDto
        List<AdminPostDto> usedList = usedRepository.findAll().stream()
                .map(entity -> {
                    UsedDto u = new UsedDto(
                        entity.getUsedIdx(),
                        entity.getUsedTitle(),
                        entity.getUsedContent(),
                        entity.getUsedPrice(),
                        entity.getUsedState(),
                        entity.getTradingLoc(),
                        entity.getViewCount(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt(),
                        entity.getIsUse(),
                        entity.getUser().getUserIdx()
                    );
                    // FileGrpIdx 찾기
                    fileGrpRepository.findByRefTypeAndRefId(FileRefType.USED, entity.getUsedIdx())
                            .ifPresent(fg -> u.setFileGrp(fg.getFileGrpIdx()));
                    return u;
                })
                .map(u -> AdminPostDto.fromUsed(u, u.getUserIdx() != null
                        ? userRepository.findById(u.getUserIdx())
                        .map(Users::getUserNickname)
                        .orElse("알 수 없음")
                        : "알 수 없음"))
                .toList();

        // 3. 통합 및 정렬
        List<AdminPostDto> combined = new ArrayList<>();
        combined.addAll(boardList);
        combined.addAll(usedList);

        return combined.stream()
                .sorted(Comparator.comparing(AdminPostDto::getDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleReplyStatus(Integer replyIdx) {
        com.scit.soragodong.domain.entity.BoardReply reply = boardReplyRepository.findById(replyIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        // 상태 토글
        if (reply.getIsUse()) {
            reply.delete();
        } else {
            reply.restore();
        }
    }

    @Transactional
    public void updatePostStatus(Integer idx, String type) {
        if ("커뮤니티".equals(type)) {
            Board board = boardRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커뮤니티 게시글입니다."));
            // 상태 반전 (true -> false, false -> true)
            board.setIsUse(!board.getIsUse());
        } else if ("중고거래".equals(type)) {
            Used used = usedRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 중고거래 게시글입니다."));
            used.setIsUse(!used.getIsUse());
        } else {
            throw new IllegalArgumentException("잘못된 게시글 유형입니다.");
        }
    }

    @Transactional
    public AdminPostDto getPostDetail(Integer idx, String type) {
        if ("커뮤니티".equals(type) || "BOARD".equalsIgnoreCase(type)) {
            Board board = boardRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
            // BoardDto record 생성자에 모든 필드 맞춰서 전달
            BoardDto boardDto = new BoardDto(
                    board.getBoardIdx(),
                    board.getUser().getUserIdx(),
                    board.getUser().getProfileIdx(),
                    board.getBoardCategory(),
                    board.getBoardTitle(),
                    board.getBoardContent(),
                    board.getUser().getUserNickname(),
                    board.getIsUse(),

                    board.getLikeCount(),
                    board.getViewCount(),
                    board.getCreateDate(),
                    board.getUpdateDate(),
                    board.getFileGrp() != null ? board.getFileGrp().getFileGrpIdx() : null,
                    board.getReplyCount()
            );
            AdminPostDto dto = AdminPostDto.fromBoard(boardDto);
            if (board.getFileGrp() != null) {
                java.util.List<String> paths = fileRepository.findByFileGroupAndIsUseTrueOrderByFileOrder(board.getFileGrp())
                        .stream()
                        .map(file -> "/img/" + file.getFileIdx())
                        .collect(java.util.stream.Collectors.toList());
                dto.setImagePaths(paths);
            }

            // 댓글 조회 추가 (작성자 null 체크 등 방어적 처리)
            java.util.List<BoardReplyDto> replies = boardReplyRepository.findAllByBoard_BoardIdx(idx)
                    .stream()
                    .map(r -> BoardReplyDto.builder()
                            .replyIdx(r.getReplyIdx())
                            .boardIdx(idx)
                            .userIdx(r.getUser() != null ? r.getUser().getUserIdx() : null)
                            .userNickname(r.getUser() != null ? r.getUser().getUserNickname() : "알 수 없음")
                            .replyContent(r.getReplyContent())
                            .isUse(r.getIsUse())
                            .createdAt(r.getCreatedAt())
                            .updatedAt(r.getUpdatedAt())
                            .build())
                    .toList();
            dto.setReplies(replies);

            return dto;

        } else if ("중고거래".equals(type) || "USED_ITEM".equalsIgnoreCase(type) || "market".equals(type)) {

            // 중고거래는 file_grp_idx 없기때문에 별도로 찾기
            FileGrp fileGrp = fileGrpRepository.findByRefTypeAndRefId(FileRefType.USED, idx).orElse(null);

            Used used = usedRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
            // 닉네임은 엔티티에서 바로 가져오기
            String nickname = used.getUser().getUserNickname();
            UsedDto usedDto = new UsedDto(
                    used.getUsedIdx(),
                    used.getUsedTitle(),
                    used.getUsedContent(),
                    used.getUsedPrice(),
                    used.getUsedState(),
                    used.getTradingLoc(),
                    used.getViewCount(),
                    used.getCreatedAt(),
                    used.getUpdatedAt(),
                    used.getIsUse(),
                    used.getUser().getUserIdx()

            );
            AdminPostDto dto = AdminPostDto.fromUsed(usedDto, nickname);


            //이미지 추가
            if (fileGrp != null) {
                java.util.List<String> paths = fileRepository.findByFileGroupAndIsUseTrueOrderByFileOrder(fileGrp)
                        .stream()
                        .map(file -> "/img/" + file.getFileIdx())
                        .collect(java.util.stream.Collectors.toList());
                dto.setImagePaths(paths);
            }


            return dto;

        } else {
            throw new IllegalArgumentException("잘못된 게시글 유형입니다.");
        }
    }





    public List<AdminPostDto> getPostByTypeAndIdx(String targetType, Integer postIdx) {
        // 1. 커뮤니티 게시글(BOARD) 조회 및 변환
        if ("BOARD".equalsIgnoreCase(targetType)) {
            return boardRepository.findById(postIdx).stream()
                    .map(entity -> BoardDto.builder()
                            .boardIdx(entity.getBoardIdx())
                            .userIdx(entity.getUser().getUserIdx())
                            .boardCategory(entity.getBoardCategory())
                            .boardTitle(entity.getBoardTitle())
                            .boardContent(entity.getBoardContent())
                            .userNickname(entity.getUser().getUserNickname())
                            .isUse(entity.getIsUse())
                            .likeCount(entity.getLikeCount())
                            .viewCount(entity.getViewCount())
                            .createdAt(entity.getCreateDate())
                            .build())
                    .map(AdminPostDto::fromBoard)
                    .toList();
        }

        // 2. 중고거래 게시글(USED_ITEM) 조회 및 변환
        else if ("USED_ITEM".equalsIgnoreCase(targetType)) {
            return usedRepository.findById(postIdx).stream()
                    .map(entity -> {
                        UsedDto u = new UsedDto(
                                entity.getUsedIdx(),
                                entity.getUsedTitle(),
                                entity.getUsedContent(),
                                entity.getUsedPrice(),
                                entity.getUsedState(),
                                entity.getTradingLoc(),
                                entity.getViewCount(),
                                entity.getCreatedAt(),
                                entity.getUpdatedAt(),
                                entity.getIsUse(),
                                entity.getUser().getUserIdx()
                        );
                        // UsedDto와 닉네임을 함께 넘겨 변환
                        return AdminPostDto.fromUsed(u, entity.getUser().getUserNickname());
                    })
                    .toList();
        }

        return Collections.emptyList();
    }
	
	public List<AdminPostDto> searchPosts(String targetType, String searchWord) {
		List<AdminPostDto> allPosts = new ArrayList<>();
		
		// 검색어가 비어있는 경우 처리 (null 방지)
		boolean hasSearchWord = (searchWord != null && !searchWord.trim().isEmpty());
		
		// 1. 커뮤니티(BOARD) 조회
		if (targetType == null || "community".equals(targetType)) {
			List<Board> boards;
			if (hasSearchWord) {
				// 검색어가 있을 때
				boards = boardRepository.searchByWord(searchWord);
			} else {
				// 검색어 없이 카테고리만 선택했을 때
				boards = boardRepository.findAll();
			}
			
			allPosts.addAll(boards.stream()
					.map(entity -> BoardDto.builder()
							.boardIdx(entity.getBoardIdx())
							.userIdx(entity.getUser().getUserIdx())
							.boardCategory(entity.getBoardCategory())
							.boardTitle(entity.getBoardTitle())
							.boardContent(entity.getBoardContent())
							.userNickname(entity.getUser().getUserNickname())
							.isUse(entity.getIsUse())
							.likeCount(entity.getLikeCount())
							.viewCount(entity.getViewCount())
							.createdAt(entity.getCreateDate())
							.fileGrp(entity.getFileGrp() != null ? entity.getFileGrp().getFileGrpIdx() : null)
							.build())
					.map(AdminPostDto::fromBoard)
					.toList());
		}
		
		// 2. 중고거래(USED_ITEM) 조회
		if (targetType == null || "market".equals(targetType)) {
			List<Used> usedItems;
			if (hasSearchWord) {
				// 검색어가 있을 때
				usedItems = usedRepository.searchByWord(searchWord);
			} else {
				// 검색어 없이 카테고리만 선택했을 때
				usedItems = usedRepository.findAll();
			}
			
			allPosts.addAll(usedItems.stream()
					.map(entity -> {
						UsedDto u = new UsedDto(
								entity.getUsedIdx(),
								entity.getUsedTitle(),
								entity.getUsedContent(),
								entity.getUsedPrice(),
								entity.getUsedState(),
								entity.getTradingLoc(),
								entity.getViewCount(),
								entity.getCreatedAt(),
								entity.getUpdatedAt(),
								entity.getIsUse(),
								entity.getUser().getUserIdx()
						);
						// FileGrpIdx 찾기
						fileGrpRepository.findByRefTypeAndRefId(FileRefType.USED, entity.getUsedIdx())
								.ifPresent(fg -> u.setFileGrp(fg.getFileGrpIdx()));
						
						return AdminPostDto.fromUsed(u, entity.getUser() != null ? entity.getUser().getUserNickname() : "알 수 없음");
					})
					.toList());
		}
		
		// 최신순 정렬
		allPosts.sort((p1, p2) -> {
			if (p1.getDate() == null || p2.getDate() == null) return 0;
			return p2.getDate().compareTo(p1.getDate());
		});
		
		return allPosts;
	}
}