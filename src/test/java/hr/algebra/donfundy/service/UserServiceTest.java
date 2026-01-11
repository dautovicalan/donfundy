package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.User;
import hr.algebra.donfundy.domain.enums.Role;
import hr.algebra.donfundy.dto.RegisterRequest;
import hr.algebra.donfundy.exception.ValidationException;
import hr.algebra.donfundy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;
    @Mock private DonorService donorService;

    @InjectMocks private UserService userService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setRepeatPassword("password");
    }

    @Test
    void save_shouldThrowValidationException_whenEmailAlreadyExists() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.save(request))
                .isInstanceOf(ValidationException.class);

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, donorService);
    }

    @Test
    void save_shouldThrowValidationException_whenPasswordMismatch() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        request.setRepeatPassword("different");

        assertThatThrownBy(() -> userService.save(request))
                .isInstanceOf(ValidationException.class);

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, donorService);
    }

    @Test
    void save_shouldEncodePassword_saveUser_andCreateDonorForUser() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password"))
                .thenReturn("hashed");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    u.setId(1L);
                    return u;
                });

        userService.save(request);

        // verify created user fields
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getFirstName()).isEqualTo("Test");
        assertThat(saved.getLastName()).isEqualTo("User");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.USER);

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password");

        // donor creation is called with the saved user returned by repo
        verify(donorService).createDonorForUser(any(User.class));

        verifyNoMoreInteractions(userRepository, passwordEncoder, donorService);
    }

    @Test
    void save_shouldNotCreateDonor_whenRepositorySaveThrows() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password"))
                .thenReturn("hashed");
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("db error"));

        assertThatThrownBy(() -> userService.save(request))
                .isInstanceOf(RuntimeException.class);

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verifyNoInteractions(donorService);
    }
}