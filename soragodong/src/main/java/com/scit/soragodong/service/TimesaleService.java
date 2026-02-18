package com.scit.soragodong.service;

import com.scit.soragodong.domain.dto.FileRes;
import com.scit.soragodong.domain.dto.StoreDto;
import com.scit.soragodong.domain.dto.StoreProductDto;
import com.scit.soragodong.domain.entity.Store;
import com.scit.soragodong.domain.entity.StoreProduct;
import com.scit.soragodong.domain.enums.FileRefType;
import com.scit.soragodong.repository.StoreProductRepository;
import com.scit.soragodong.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@Transactional
public class TimesaleService {

    private final StoreProductRepository storeProductRepository;
    private final StoreRepository storeRepository;
    private final FileService fileService;

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
            // 4. 업로드된 파일 중 첫 번째 파일의 경로를 상품의 대표 이미지(productPictureIdx)로 설정
            if (!uploadedFiles.isEmpty()) {
                // FileRes record의 fileUrl(상대경로)을 가져와 업데이트
                String representativeImage = uploadedFiles.get(0).fileUrl();
                storeProduct.setProductPictureIdx(representativeImage);

                // JPA의 더티 체킹(Dirty Checking)으로 인해 명시적 save 없이도 트랜잭션 종료 시 업데이트됩니다.
                // 혹은 확실하게 하기 위해 한 번 더 호출 가능
                // storeProductRepository.save(storeProduct);
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

            // 대표 이미지 업데이트
            if (!uploadedFiles.isEmpty()) {
                product.setProductPictureIdx(uploadedFiles.get(0).fileUrl());
            }
        }
    }

    public void updateStore(StoreDto dto, List<MultipartFile> files) {
        Store store = storeRepository.findById(dto.storeIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 점포가 없다"));
        // 2. 값 변경 (Setter나 별도 메서드 사용)
        store.setStoreName(dto.storeName());
        store.setStoreAddress(dto.storeAddress());
        store.setStoreOpenTime(dto.storeOpenTime());
        store.setStoreCloseTime(dto.storeCloseTime());
        store.setEventNote(dto.eventNote());
        store.setEventStartTime(dto.eventStartTime());
        store.setEventEndTime(dto.eventEndTime());

        // 3. 사진 변경 시 파일 시스템 및 DB 연동
        if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
            List<FileRes> uploaded = fileService.upload(
                    FileRefType.STORE,
                    store.getStoreIdx(),
                    files
            );
            if (!uploaded.isEmpty()) {
                store.setStorePictureIdx(uploaded.get(0).fileUrl());
            }
        }
    }
	
	public StoreDto getStoreById(int idx) {
		Store store = storeRepository.findById(idx)
				.orElseThrow(() -> new IllegalArgumentException("해당 점포가 없다"));
		return convertToDto(store);
	}
}
