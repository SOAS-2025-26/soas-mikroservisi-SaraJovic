package com.currencyapp.usersservice.service;

import com.currencyapp.servicelibrary.dto.UserDto;
import com.currencyapp.servicelibrary.feign.BankAccountServiceClient;
import com.currencyapp.servicelibrary.feign.CryptoWalletServiceClient;
import com.currencyapp.usersservice.entity.User;
import com.currencyapp.usersservice.repository.UserRepository;
import com.currencyapp.util.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String OWNER_ROLE = "OWNER";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";

    private static final int PROVISIONING_MAX_ATTEMPTS = 6;
    private static final long PROVISIONING_RETRY_DELAY_MS = 1500;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankAccountServiceClient bankAccountServiceClient;
    private final CryptoWalletServiceClient cryptoWalletServiceClient;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public UserDto getUserById(Long id) {
        return toDto(findUserOrThrow(id));
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        return toDto(user);
    }

    public UserDto validateUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public UserDto createUser(UserDto dto, String actorRole) {
        if (ADMIN_ROLE.equalsIgnoreCase(actorRole) && !USER_ROLE.equalsIgnoreCase(dto.getRole())) {
            throw new BusinessException("ADMIN can only create users with role USER", HttpStatus.FORBIDDEN);
        }

        if (OWNER_ROLE.equalsIgnoreCase(dto.getRole()) && userRepository.findAll().stream()
                .anyMatch(user -> OWNER_ROLE.equalsIgnoreCase(user.getRole()))) {
            throw new BusinessException("An OWNER already exists", HttpStatus.CONFLICT);
        }

        userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
            throw new BusinessException("User with email " + dto.getEmail() + " already exists", HttpStatus.CONFLICT);
        });

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();

        User saved = userRepository.save(user);

        if (USER_ROLE.equalsIgnoreCase(saved.getRole())) {
            retryProvisioningCall("create bank account for " + saved.getEmail(),
                    () -> bankAccountServiceClient.createAccount(saved.getEmail()));
            retryProvisioningCall("create crypto wallet for " + saved.getEmail(),
                    () -> cryptoWalletServiceClient.createWallet(saved.getEmail()));
        }

        return toDto(saved);
    }

    public UserDto updateUser(Long id, UserDto dto, String actorRole) {
        User user = findUserOrThrow(id);

        if (ADMIN_ROLE.equalsIgnoreCase(actorRole)
                && (!USER_ROLE.equalsIgnoreCase(user.getRole()) || !USER_ROLE.equalsIgnoreCase(dto.getRole()))) {
            throw new BusinessException("ADMIN can only update users with role USER", HttpStatus.FORBIDDEN);
        }

        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return toDto(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);

        if (USER_ROLE.equalsIgnoreCase(user.getRole())) {
            retryProvisioningCall("delete bank account for " + user.getEmail(),
                    () -> bankAccountServiceClient.deleteAccountByEmail(user.getEmail()));
            retryProvisioningCall("delete crypto wallet for " + user.getEmail(),
                    () -> cryptoWalletServiceClient.deleteWalletByEmail(user.getEmail()));
        }
    }

    private void retryProvisioningCall(String description, Runnable call) {
        for (int attempt = 1; attempt <= PROVISIONING_MAX_ATTEMPTS; attempt++) {
            try {
                call.run();
                return;
            } catch (Exception ex) {
                if (attempt == PROVISIONING_MAX_ATTEMPTS) {
                    log.warn("Failed to {} after {} attempts: {}", description, PROVISIONING_MAX_ATTEMPTS, ex.getMessage());
                    return;
                }

                try {
                    Thread.sleep(PROVISIONING_RETRY_DELAY_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    log.warn("Interrupted while retrying {}", description);
                    return;
                }
            }
        }
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found with id " + id, HttpStatus.NOT_FOUND));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }
}
