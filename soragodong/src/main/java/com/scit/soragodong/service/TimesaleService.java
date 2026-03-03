package com.scit.soragodong.service;

import com.scit.soragodong.domain.dto.FileRes;
import com.scit.soragodong.domain.dto.StoreDto;
import com.scit.soragodong.domain.dto.StoreProductDto;
import com.scit.soragodong.domain.entity.Store;
import com.scit.soragodong.domain.entity.StoreProduct;
import com.scit.soragodong.domain.enums.FileRefType;
import com.scit.soragodong.repository.FileRepository;
import com.scit.soragodong.repository.StoreProductRepository;
import com.scit.soragodong.repository.StoreRepository;
import com.scit.soragodong.util.FileUploadUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@Transactional
@Slf4j
public class TimesaleService {

    private final StoreProductRepository storeProductRepository;
    private final StoreRepository storeRepository;
    private final FileService fileService;
	private final FileRepository fileRepository;
	private final FileUploadUtil fileUploadUtil;
	
    /**
     * 모든 상품(음식) 데이터 조회
     */
    public List<StoreProductDto> getAllProducts() {
        List<StoreProduct> products = storeProductRepository.findAll();
        
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 할인 상품만 조회 (이벤트 가격이 있는 경우)
     */
    public List<StoreProductDto> getDiscountProducts() {
        List<StoreProduct> products = storeProductRepository.findByEventPriceIsNotNull();
        
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 가게의 상품 조회
     */
    public List<StoreProductDto> getProductsByStore(Integer storeIdx) {
        List<StoreProduct> products = storeProductRepository.findByStoreStoreIdx(storeIdx);

        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    /**
     * 모든 가게 데이터 조회
     */
    public List<StoreDto> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        
        return stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용 중인 가게만 조회 (isUse = 1)
     */
    public List<StoreDto> getActiveStores() {
        List<Store> stores = storeRepository.findByIsUse((byte) 1);
        
        return stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 이벤트 진행 중인 가게만 조회
     */
    public List<StoreDto> getEventStores() {
        List<Store> stores = storeRepository.findByEventState("진행중");
        
        return stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Entity -> DTO 변환
     */
    private StoreDto convertToDto(Store store) {
        return new StoreDto(
            store.getStoreIdx(),
            store.getStoreName(),
            store.getStoreAddress(),
            store.getStoreOpenTime(),
            store.getStoreCloseTime(),
            store.getEventStartTime(),
            store.getEventEndTime(),
            store.getEventState(),
            store.getEventNote(),
            store.getStorePictureIdx(),
            store.getIsUse(),
            store.getCreateAt(),
            store.getStoreLat(),
            store.getStoreLng()
        );
    }
    

    /**
     * Entity -> DTO 변환
     */
    private StoreProductDto convertToDto(StoreProduct product) {
        return new StoreProductDto(
            product.getProductNum(),
            product.getStore().getStoreIdx(),
            product.getStore().getStoreName(),
            product.getCategory(),
            product.getProductName(),
            product.getPrice(),
            product.getEventPrice(),
            product.getProductQuantity(),
            product.getProductPictureIdx()
        );
    }

    //상점 등록용
    public boolean createStore(StoreDto storeDto) {
        Store store = new Store().builder()
                .storeName(storeDto.storeName())
                .storeAddress(storeDto.storeAddress())
                .storeOpenTime(storeDto.storeOpenTime())
                .storeCloseTime(storeDto.storeCloseTime())
                .eventStartTime(storeDto.eventStartTime())
                .eventEndTime(storeDto.eventEndTime())
                .eventNote(storeDto.eventNote())
                .storePictureIdx(storeDto.storePictureIdx())
                .build();
        storeRepository.save(store);

        return true;
    }

    public void updateIsUse(Integer idx, byte b) {
        Store store = storeRepository.findById(idx)
                    .orElseThrow(()-> new RuntimeException());
        store.setIsUse(b);
        storeRepository.save(store);

    }

    public void updateStoreName(Integer idx, String name) {
        Store store = storeRepository.findById(idx)
                .orElseThrow(()-> new RuntimeException());
        store.setStoreName(name);
        storeRepository.save(store);
    }

    public void insertProduct(StoreProductDto productDto, List<MultipartFile> files) {
        // 1. 먼저 해당 storeIdx를 가진 Store 엔티티를 찾습니다.
        Store store = storeRepository.findById(productDto.storeIdx())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 점포입니다."));

        StoreProduct storeProduct = new StoreProduct().builder()
                .productNum(productDto.productNum())
                .productName(productDto.productName())
                .productQuantity(productDto.productQuantity())

                .price(productDto.price())
                .eventPrice(productDto.eventPrice())
                .category(productDto.category())
                .store(store)
                .build();
        storeProductRepository.save(storeProduct);

        if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
            // fileService.upload는 List<FileRes>를 반환합니다.
            List<FileRes> uploadedFiles = fileService.upload(
                    FileRefType.PRODUCT,
                    storeProduct.getProductNum(),
                    files
            );
            
			if (!uploadedFiles.isEmpty()) {
				// 경로(fileUrl) 대신 파일의 PK인 fileIdx를 저장합니다.
				Integer firstFileIdx = uploadedFiles.get(0).fileIdx();
				storeProduct.setProductPictureIdx(String.valueOf(firstFileIdx));
			}
        }
    }

    @Transactional
    public void updateProduct(StoreProductDto productDto, List<MultipartFile> files) {
        // 1. 기존 상품 조회
        StoreProduct product = storeProductRepository.findById(productDto.productNum())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 2. 기본 정보 업데이트
        product.setProductName(productDto.productName());
        product.setPrice(productDto.price());
        product.setEventPrice(productDto.eventPrice());
        product.setProductQuantity(productDto.productQuantity());
        product.setCategory(productDto.category());

        // 3. 새 파일이 올라왔을 경우 처리
        if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
            // 기존 파일 관리 로직(필요시 기존 파일 isUse = false 처리)
            List<FileRes> uploadedFiles = fileService.upload(
                    FileRefType.PRODUCT,
                    product.getProductNum(),
                    files
            );
			
			// 상품 수정 수정
			if (!uploadedFiles.isEmpty()) {
				Integer firstFileIdx = uploadedFiles.get(0).fileIdx();
				// productDto는 불변일 수 있으므로 엔티티(product)에 직접 세팅하세요.
				product.setProductPictureIdx(String.valueOf(firstFileIdx));
			}
        }
    }

    public void updateStore(StoreDto dto, List<MultipartFile> files) {
        Store store = storeRepository.findById(dto.storeIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 점포가 없다"));
        // 2. 값 변경 (Setter나 별도 메서드 사용)
        store.setStoreName(dto.storeName());
        store.setStoreAddress(dto.storeAddress());
		store.setStoreLat(dto.storeLat()); // 위도 저장
		store.setStoreLng(dto.storeLng()); // 경도 저장
        store.setStoreOpenTime(dto.storeOpenTime());
        store.setStoreCloseTime(dto.storeCloseTime());
        store.setEventNote(dto.eventNote());
        store.setEventStartTime(dto.eventStartTime());
        store.setEventEndTime(dto.eventEndTime());
		store.setEventState(dto.eventState());

        // 3. 사진 변경 시 파일 시스템 및 DB 연동
        if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
            List<FileRes> uploaded = fileService.upload(
                    FileRefType.STORE,
                    store.getStoreIdx(),
                    files
            );
			// 상점 정보 수정 수정
			if (!uploaded.isEmpty()) {
				Integer firstFileIdx = uploaded.get(0).fileIdx();
				store.setStorePictureIdx(String.valueOf(firstFileIdx));
			}
        }
    }
	
	public StoreDto getStoreById(int idx) {
		Store store = storeRepository.findById(idx)
				.orElseThrow(() -> new IllegalArgumentException("해당 점포가 없다"));
		return convertToDto(store);
	}
	
	@Transactional
	public void deleteProduct(Integer productNum) {
		// 1. 삭제할 상품 조회
		StoreProduct product = storeProductRepository.findById(productNum)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
		
		// 2. 물리 파일 삭제 (옵션)
		// DB에 저장된 productPictureIdx가 File 테이블의 ID라면, File 엔티티를 조회해 실제 경로를 알아내야 합니다.
		String fileIdxStr = product.getProductPictureIdx();
		if (fileIdxStr != null && !fileIdxStr.isEmpty()) {
			try {
				int fileIdx = Integer.parseInt(fileIdxStr);
				// fileRepository에서 파일 정보를 가져옴 (경로: /202602/uuid.png)
				fileRepository.findById(fileIdx).ifPresent(file -> {
					try {
						// FileUploadUtil의 deleteFile 호출
						fileUploadUtil.deleteFile(file.getFilePath());
						// DB에서도 파일 정보 삭제 혹은 isUse = false 처리
						fileRepository.delete(file);
					} catch (IOException e) {
						log.error("서버 파일 삭제 실패: {}", file.getFilePath(), e);
					}
				});
			} catch (NumberFormatException e) {
				log.warn("잘못된 파일 ID 형식입니다: {}", fileIdxStr);
			}
		}
		
		// 3. 상품 데이터 삭제
		storeProductRepository.delete(product);
	}
	
	public void updateAllStoreEventStatus() {
		// 1. 활성화된 모든 점포 리스트 조회
		List<Store> stores = storeRepository.findAllByIsUse((byte) 1);
		LocalTime now = LocalTime.now();
		
		for (Store store : stores) {
			LocalTime start = store.getEventStartTime();
			LocalTime end = store.getEventEndTime();
			
			// 시간 설정이 없는 점포는 건너뜁니다.
			if (start == null || end == null) continue;
			
			String newState;
			// 2. 시간 비교 로직
			if (now.isBefore(start)) {
				newState = "대기중";
			} else if (now.isAfter(start) && now.isBefore(end)) {
				newState = "진행중";
			} else {
				newState = "종료";
			}
			
			// 3. 상태가 변경된 경우에만 업데이트
			if (!newState.equals(store.getEventState())) {
				store.setEventState(newState);
				log.info("점포 [{}] 상태 변경: {} -> {}", store.getStoreName(), store.getEventState(), newState);
			}
		}
		// @Transactional에 의해 메서드 종료 시 자동 DB 반영
	}
}
