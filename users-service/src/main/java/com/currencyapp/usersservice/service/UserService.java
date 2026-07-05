package com.currencyapp.usersservice.service;

import com.currencyapp.servicelibrary.dto.UserDto;
import com.currencyapp.servicelibrary.feign.BankAccountServiceClient;
import com.currencyapp.servicelibrary.feign.CryptoWalletServiceClient;
import com.currencyapp.usersservice.entity.User;
import com.currencyapp.usersservice.repository.UserRepository;
import com.currencyapp.util.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String OWNER_ROLE = "OWNER";
    private static final String USER_ROLE = "USER";

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

    public UserDto createUser(UserDto dto) {
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
            bankAccountServiceClient.createAccount(saved.getEmail());
            cryptoWalletServiceClient.createWallet(saved.getEmail());
        }

        return toDto(saved);
    }

    public UserDto updateUser(Long id, UserDto dto) {
        User user = findUserOrThrow(id);

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
            bankAccountServiceClient.deleteAccountByEmail(user.getEmail());
            cryptoWalletServiceClient.deleteWalletByEmail(user.getEmail());
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
