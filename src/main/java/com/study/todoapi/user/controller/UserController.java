package com.study.todoapi.user.controller;


import com.study.todoapi.auth.TokenUserInfo;
import com.study.todoapi.exception.DuplicatedEmailException;
import com.study.todoapi.exception.NoRegisteredArgumentsException;
import com.study.todoapi.user.dto.request.LoginRequestDTO;
import com.study.todoapi.user.dto.request.UserSignUpRequestDTO;
import com.study.todoapi.user.dto.response.LoginResponseDTO;
import com.study.todoapi.user.dto.response.UserSignUpResponseDTO;
import com.study.todoapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
//@CrossOrigin(origins = {"http://localhost:3000"})
public class UserController {

    private final UserService userService;

    // 회원가입 요청처리
    @PostMapping
    public ResponseEntity<?> signUp(
            @Validated @RequestPart("user") UserSignUpRequestDTO dto
            , @RequestPart(value="profileImage", required = false) MultipartFile profileImg
            , BindingResult result
    ) {

        log.info("/api/auth POST! - {}", dto);


        if (result.hasErrors()) {
            log.warn(result.toString());
            return ResponseEntity
                    .badRequest()
                    .body(result.getFieldError());
        }

        try {
            String uploadProfileImagePath = null;
            if (profileImg != null) {
                log.info("file-name: {}", profileImg.getOriginalFilename());
                uploadProfileImagePath = userService.uploadProfileImage(profileImg);
            }
            UserSignUpResponseDTO responseDTO = userService.create(dto, uploadProfileImagePath);
            return ResponseEntity.ok().body(responseDTO);
        } catch (NoRegisteredArgumentsException e) {
            log.warn("필수 가입 정보를 전달받지 못했습니다!!");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DuplicatedEmailException e) {
            log.warn("이메일이 중복되었습니다.");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            log.warn("파일 업로드 경로가 잘못되었거나 파일 저장에 실패했습니다.");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이메일 중복확인 요청처리
    @GetMapping("/check")
    public ResponseEntity<?> check(String email) {

        boolean flag = userService.isDuplicateEmail(email);
        log.debug("{} 중복?? - {}", email, flag);

        return ResponseEntity.ok().body(flag);
    }


    // 로그인 요청처리
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(
            @Validated @RequestBody LoginRequestDTO dto
    ) {
        try {
            LoginResponseDTO responseDTO = userService.authenticate(dto);
            log.info("login success!! by {}", responseDTO.getEmail());
            return ResponseEntity.ok().body(responseDTO);
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 일반회원을 프리미엄으로 상승시키는 요청 처리
    @PutMapping("/promote")
    // 그냥 이 권한을 가진 사람만 이 요청을 수행할수 있고
    // 이 권한이 아닌 유저는 강제로 403이 응답됨
//    @PreAuthorize("hasRole('COMMON')")
    public ResponseEntity<?> promote(
            @AuthenticationPrincipal TokenUserInfo userInfo
    ) {
        log.info("/api/auth/promote PUT! - {}", userInfo);

        try {
            LoginResponseDTO responseDTO = userService.promoteToPremium(userInfo);
            return ResponseEntity.ok().body(responseDTO);
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.warn(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }


    // 파일을 클라이언트에 전송하기
    @GetMapping("/load-profile")
    public ResponseEntity<?> loadProfile(
            @AuthenticationPrincipal TokenUserInfo userInfo
    ) {

        try {
            // 로그인한 회원의 프로필 사진 바이너리 데이터를 클라이언트에게 전송해야 함
            // 1. 해당 회원의 프로필이 저장된 경로와 파일명을 알아내야 함.
            // -> 파일명은 데이터베이스를 조회해야 함
            String profilePath = userService.getProfilePath(userInfo.getEmail());

            // 2. 얻어낸 파일경로를 통해 실제 데이터 가져오기
            File profileFile = new File(profilePath);

            if (!profileFile.exists()) return ResponseEntity.notFound().build();

            // 서버에 저장된 파일을 직렬화하여 바이트배열로 만들어서 가져옴
            byte[] fileData = FileCopyUtils.copyToByteArray(profileFile);

            // 3. 응답헤더에 이 데이터의 타입이 무엇인지를 설정해야 함.
            HttpHeaders headers = new HttpHeaders();
            MediaType mediaType = extractFileExtension(profilePath);
            if (mediaType == null) {
                return ResponseEntity.internalServerError()
                        .body("발견된 파일은 이미지가 아닙니다.");
            }
            headers.setContentType(mediaType);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }


    // 파일의 확장자를 추출하여 미디어타입을 알려주는 메서드
    private MediaType extractFileExtension(String filePath) {

        // D:/abc/def/image/sdjlfjslfajdfjlsajkl_cat.png
        String ext = filePath.substring(filePath.lastIndexOf(".") + 1);

        switch (ext.toUpperCase()) {
            case "JPEG": case "JPG":
                return MediaType.IMAGE_JPEG;
            case "PNG":
                return MediaType.IMAGE_PNG;
            case "GIF":
                return MediaType.IMAGE_GIF;
            default:
                return null;
        }
    }

}