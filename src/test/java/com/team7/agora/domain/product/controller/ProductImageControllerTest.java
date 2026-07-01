package com.team7.agora.domain.product.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.product.dto.response.ProductImageResponse;
import com.team7.agora.domain.product.service.ProductImageService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ProductImageControllerTest {

    @Mock
    private ProductImageService productImageService;

    private MockMvc mockMvc;
    private CustomUserDetails seller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductImageController(productImageService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
        seller = new CustomUserDetails(1L, "seller@test.com", "password", UserRole.ROLE_USER, UserStatus.ACTIVE, "seller");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(seller, null, seller.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadAllAcceptsRepeatedImagesMultipartParts() throws Exception {
        MockMultipartFile first = image("first.jpg", "first");
        MockMultipartFile second = image("second.jpg", "second");
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(seller, null, seller.getAuthorities());
        when(productImageService.uploadAll(eq(1L), eq(9L), org.mockito.ArgumentMatchers.<List<MultipartFile>>any()))
            .thenReturn(List.of(
                new ProductImageResponse(7L, 9L, "/uploads/products/first.jpg", 0),
                new ProductImageResponse(8L, 9L, "/uploads/products/second.jpg", 1)
            ));

        mockMvc.perform(multipart("/api/products/{productId}/images", 9L).file(first).file(second).principal(authentication))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].imageUrl").value("/uploads/products/first.jpg"))
            .andExpect(jsonPath("$.data[1].sortOrder").value(1));

        verify(productImageService).uploadAll(eq(1L), eq(9L), org.mockito.ArgumentMatchers.<List<MultipartFile>>any());
    }

    @Test
    void uploadAllAcceptsSingleImagesMultipartPart() throws Exception {
        MockMultipartFile image = image("single.jpg", "single");
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(seller, null, seller.getAuthorities());
        when(productImageService.uploadAll(eq(1L), eq(9L), org.mockito.ArgumentMatchers.<List<MultipartFile>>any()))
            .thenReturn(List.of(new ProductImageResponse(7L, 9L, "/uploads/products/single.jpg", 0)));

        mockMvc.perform(multipart("/api/products/{productId}/images", 9L).file(image).principal(authentication))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].imageUrl").value("/uploads/products/single.jpg"));

        verify(productImageService).uploadAll(eq(1L), eq(9L), org.mockito.ArgumentMatchers.<List<MultipartFile>>any());
    }

    private MockMultipartFile image(String filename, String content) {
        return new MockMultipartFile("images", filename, "image/jpeg", content.getBytes());
    }
}
