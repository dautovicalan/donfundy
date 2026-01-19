package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.User;
import hr.algebra.donfundy.dto.DonorRequest;
import hr.algebra.donfundy.dto.DonorResponse;
import hr.algebra.donfundy.exception.BusinessException;
import hr.algebra.donfundy.exception.ResourceNotFoundException;
import hr.algebra.donfundy.repository.DonorRepository;
import hr.algebra.donfundy.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DonorResponse> findAll() {
        return donorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DonorResponse findById(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.donor.not.found", new Object[]{id}));
        return mapToResponse(donor);
    }

    @Transactional(readOnly = true)
    public DonorResponse findByUserId(Long userId) {
        Donor donor = donorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.donor.not.found", new Object[]{userId}));
        return mapToResponse(donor);
    }

    @Transactional(readOnly = true)
    public DonorResponse findCurrentUserDonor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("error.user.not.found"));

        Donor donor = donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("error.donor.not.found.for.user"));

        return mapToResponse(donor);
    }

    @Transactional
    public DonorResponse create(DonorRequest request) {
        Donor donor = new Donor();
        donor.setFirstName(request.getFirstName());
        donor.setLastName(request.getLastName());
        donor.setEmail(request.getEmail());
        donor.setPhoneNumber(request.getPhoneNumber());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("error.user.not.found", new Object[]{request.getUserId()}));
            donor.setUser(user);
        }

        Donor saved = donorRepository.save(donor);
        return mapToResponse(saved);
    }

    @Transactional
    public DonorResponse update(Long id, DonorRequest request) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.donor.not.found", new Object[]{id}));

        donor.setFirstName(request.getFirstName());
        donor.setLastName(request.getLastName());
        donor.setEmail(request.getEmail());
        donor.setPhoneNumber(request.getPhoneNumber());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("error.user.not.found", new Object[]{request.getUserId()}));
            donor.setUser(user);
        }

        Donor updated = donorRepository.save(donor);
        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.donor.not.found", new Object[]{id}));
        donorRepository.delete(donor);
    }

    private DonorResponse mapToResponse(Donor donor) {
        DonorResponse response = new DonorResponse();
        response.setId(donor.getId());
        response.setUserId(donor.getUser() != null ? donor.getUser().getId() : null);
        response.setFirstName(donor.getFirstName());
        response.setLastName(donor.getLastName());
        response.setEmail(donor.getEmail());
        response.setPhoneNumber(donor.getPhoneNumber());
        return response;
    }

    public void createDonorForUser(@NonNull User savedUser) {
        Optional<Donor> optionalDonor = donorRepository.findByUserId(savedUser.getId());
        if (optionalDonor.isEmpty()){
            Donor donor = new Donor();
            donor.setFirstName(savedUser.getFirstName());
            donor.setLastName(savedUser.getLastName());
            donor.setEmail(savedUser.getEmail());
            donor.setUser(savedUser);
            donorRepository.save(donor);
        }
    }
}
