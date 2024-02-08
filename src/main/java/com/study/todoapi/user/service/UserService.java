package com.study.todoapi.user.service;

import com.study.todoapi.auth.TokenProvider;
import com.study.todoapi.auth.TokenUserInfo;
import com.study.todoapi.aws.S3Service;
import com.study.todoapi.exception.DuplicatedEmailException;
import com.study.todoapi.exception.NoRegisteredArgumentsException;
import com.study.todoapi.user.dto.request.LoginRequestDTO;
import com.study.todoapi.user.dto.request.UserSignUpRequestDTO;
import com.study.todoapi.user.dto.response.LoginResponseDTO;
import com.study.todoapi.user.dto.response.UserSignUpResponseDTO;
import com.study.todoapi.user.entity.Role;
import com.study.todoapi.user.entity.User;
import com.study.todoapi.user.repoistory.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final S3Service s3Service;

    @Value("${upload.path}")
    private String rootPath;

    // 회원가입 처리
    public UserSignUpResponseDTO create(UserSignUpRequestDTO dto, String profilePath) {

        if (dto == null) {
            throw new NoRegisteredArgumentsException("회원가입 입력정보가 없습니다!");
        }
        String email = dto.getEmail();

        if (userRepository.existsByEmail(email)) {
            log.warn("이메일이 중복되었습니다!! - {}", email);
            throw new DuplicatedEmailException("중복된 이메일입니다!!");
        }

        User saved = userRepository.save(dto.toEntity(passwordEncoder, profilePath));

        log.info("회원가입 성공!! saved user - {}", saved);

        return new UserSignUpResponseDTO(saved); // 회원가입 정보를 클라이언트에게 리턴

    }


    // 이메일 중복확인
    public boolean isDuplicateEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public LoginResponseDTO authenticate(final LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(
                () -> new RuntimeException("가입된 회원이 아님")
        );
        String inputPassword = dto.getPassword();
        String encodedPassword = user.getPassword();

        if (!passwordEncoder.matches(inputPassword, encodedPassword)) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }
        String token = tokenProvider.createToken(user);
        return new LoginResponseDTO(user, token);

    }

    // 일반 회원의 프리미엄 처리
    public LoginResponseDTO promoteToPremium(TokenUserInfo userInfo) {
        User byEmail = userRepository.findByEmail(userInfo.getEmail())
                .orElseThrow(
                        () -> new NoRegisteredArgumentsException("가입된 회원이 아닙니다.")
                );
        // 이미 프리미엄 회원이거나 관리자면 예외 처리
        if (byEmail.getRole() != Role.COMMON) {
            throw new IllegalStateException("일반 회원이 아니면 승격 X");
        }
        // 등급 변경
        byEmail.setRole(Role.PREMIUM);
        User save = userRepository.save(byEmail);

        // 토큰 재발급
        String token = tokenProvider.createToken(save);
        return new LoginResponseDTO(save, token);

    }

    /**
     * 업로드한 프로필 사진을 서버에 저장 -> 저장된 경로를 리턴
     *
     * @param originalFile - 업로드된 파일의 정보 객체
     * @return - 실제로 이미지가 저장된 서버의 디렉터리 경로
     */
    public String uploadProfileImage(MultipartFile originalFile) throws IOException {

        // 루트 디렉토리가 존재하는지 확인 후 존재하지 않으면 생성한다.
        File rootDir = new File(rootPath);
        if (!rootDir.exists()) rootDir.mkdirs();

        // 파일명을 유니크하게 변경
        String uniqueFileName = UUID.randomUUID() + "_" + originalFile.getOriginalFilename();

        // 파일을 서버에 저장
        File uplaodFile = new File(rootPath + "/" + uniqueFileName);
        originalFile.transferTo(uplaodFile);

        s3Service.uploadToS3Bucket(originalFile.getBytes(),uniqueFileName);

        return uniqueFileName;
    }

    public String getProfilePath(String email) {
        // db에서 파일명을 조회
        User user = userRepository.findByEmail(email).orElseThrow();
        String profileImg = user.getProfileImg();

        return rootPath + "/" + profileImg;
    }
}