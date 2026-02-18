package com.scit.soragodong.controller;

import com.scit.soragodong.domain.dto.StoreProductDto;
import com.scit.soragodong.domain.entity.Admin;
import com.scit.soragodong.domain.entity.Store;
import com.scit.soragodong.repository.AdminRepository;
import com.scit.soragodong.repository.StoreRepository;
import com.scit.soragodong.service.TimesaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
@Slf4j
public class OwnerController {

    private final StoreRepository storeRepository;
    private final AdminRepository adminRepository;
    private final TimesaleService timesaleService;




    @GetMapping("dashboard")
    public String ownerDashboard(Model model, Authentication auth) {
        if (auth == null) return "redirect:/login"; // 인증 정보가 없으면 로그인으로

        String adminId = auth.getName();

        // 1. 관리자 정보 조회
        Admin owner = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 점주를 찾을 수 없습니다: " + adminId));

        // 2. 해당 점주의 상점 조회
        Store store = storeRepository.findByOwner(owner)
                .orElse(null);

        model.addAttribute("store", store);

        if (store != null) {
            // 해당 상점의 상품 리스트 가져오기
            List<StoreProductDto> productList = timesaleService.getProductsByStore(store.getStoreIdx());
            model.addAttribute("productList", productList);
        }

        return "owner/dashboard";
    }

    @PostMapping("/product/add")
    public String addProduct(@ModelAttribute StoreProductDto productDto,
                             @RequestParam(value = "productPicture",required = false) List<MultipartFile> files,
                             RedirectAttributes rttr) {
        log.debug("{}, {}",productDto, files);
        try {
            // 상품 정보와 파일 리스트를 서비스에 전달
            timesaleService.insertProduct(productDto, files);

            rttr.addFlashAttribute("message", "상품 등록 성공!");
        } catch (Exception e) {
            log.error("Upload error: ", e);
            rttr.addFlashAttribute("error", "등록 실패: " + e.getMessage());
        }
        return "redirect:/owner/dashboard";
    }
    // 수정 페이지 이동
    @GetMapping("/product/edit/{productNum}")
    public String editProductPage(@PathVariable Integer productNum, Model model) {
        List<StoreProductDto> product = timesaleService.getProductsByStore(productNum);
        model.addAttribute("product", product);
        return "owner/product-edit";
    }

    // 수정 처리
    @PostMapping("/product/edit")
    public String updateProduct(@ModelAttribute StoreProductDto productDto,
                                @RequestParam(value = "productFiles", required = false) List<MultipartFile> files,
                                RedirectAttributes rttr) {
        try {
            timesaleService.updateProduct(productDto, files);
            rttr.addFlashAttribute("message", "상품 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "수정 중 오류 발생: " + e.getMessage());
        }
        return "redirect:/owner/dashboard";
    }
}