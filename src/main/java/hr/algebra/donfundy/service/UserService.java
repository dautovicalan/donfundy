package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.User;
import hr.algebra.donfundy.domain.enums.Role;
import hr.algebra.donfundy.exception.ValidationException;
import hr.algebra.donfundy.repository.UserRepository;
import hr.algebra.donfundy.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final DonorService donorService;

    @Transactional
    public void save(@Valid RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()){
            throw new ValidationException("error.email.already.exists");
        }

        if (!registerRequest.getPassword().equals(registerRequest.getRepeatPassword())) {
            throw new ValidationException("error.password.mismatch");
        }

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        donorService.createDonorForUser(savedUser);
    }
}
