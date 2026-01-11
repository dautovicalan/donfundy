package hr.algebra.donfundy.service;

import hr.algebra.donfundy.domain.Donor;
import hr.algebra.donfundy.domain.User;
import hr.algebra.donfundy.dto.DonorRequest;
import hr.algebra.donfundy.dto.DonorResponse;
import hr.algebra.donfundy.exception.BusinessException;
import hr.algebra.donfundy.exception.ResourceNotFoundException;
import hr.algebra.donfundy.repository.DonorRepository;
import hr.algebra.donfundy.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock private DonorRepository donorRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private DonorService donorService;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    private Donor donor;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");

        donor = new Donor();
        donor.setId(10L);
        donor.setFirstName("Donor");
        donor.setLastName("One");
        donor.setEmail("donor@example.com");
        donor.setPhoneNumber("123");
        donor.setUser(user);

        // Make SecurityContextHolder safe/isolated per test
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findAll_shouldReturnMappedResponses() {
        when(donorRepository.findAll()).thenReturn(List.of(donor));

        List<DonorResponse> result = donorService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getEmail()).isEqualTo("donor@example.com");

        verify(donorRepository).findAll();
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void findById_shouldReturnResponse_whenFound() {
        when(donorRepository.findById(10L)).thenReturn(Optional.of(donor));

        DonorResponse result = donorService.findById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);

        verify(donorRepository).findById(10L);
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(donorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(donorRepository).findById(99L);
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void findByUserId_shouldReturnResponse_whenFound() {
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(donor));

        DonorResponse result = donorService.findByUserId(1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);

        verify(donorRepository).findByUserId(1L);
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void findByUserId_shouldThrow_whenNotFound() {
        when(donorRepository.findByUserId(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.findByUserId(123L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(donorRepository).findByUserId(123L);
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void findCurrentUserDonor_shouldReturnResponse_whenUserAndDonorExist() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(donor));

        DonorResponse result = donorService.findCurrentUserDonor();

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail("test@example.com");
        verify(donorRepository).findByUserId(1L);
        verifyNoMoreInteractions(donorRepository, userRepository, securityContext, authentication);
    }

    @Test
    void findCurrentUserDonor_shouldThrowBusinessException_whenUserNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("missing@example.com");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.findCurrentUserDonor())
                .isInstanceOf(BusinessException.class);

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail("missing@example.com");
        verifyNoMoreInteractions(donorRepository, userRepository, securityContext, authentication);
    }

    @Test
    void findCurrentUserDonor_shouldThrowBusinessException_whenDonorNotFoundForUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.findCurrentUserDonor())
                .isInstanceOf(BusinessException.class);

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail("test@example.com");
        verify(donorRepository).findByUserId(1L);
        verifyNoMoreInteractions(donorRepository, userRepository, securityContext, authentication);
    }

    @Test
    void create_shouldSaveAndReturnResponse_withoutUser() {
        DonorRequest req = new DonorRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setEmail("a@b.com");
        req.setPhoneNumber("555");
        req.setUserId(null);

        // capture arg to assert mapping
        when(donorRepository.save(any(Donor.class))).thenAnswer(inv -> {
            Donor d = inv.getArgument(0);
            d.setId(123L);
            return d;
        });

        DonorResponse result = donorService.create(req);

        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getUserId()).isNull();
        assertThat(result.getEmail()).isEqualTo("a@b.com");

        ArgumentCaptor<Donor> captor = ArgumentCaptor.forClass(Donor.class);
        verify(donorRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isNull();
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void create_shouldAttachUser_whenUserIdProvided() {
        DonorRequest req = new DonorRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setEmail("a@b.com");
        req.setPhoneNumber("555");
        req.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(donorRepository.save(any(Donor.class))).thenAnswer(inv -> {
            Donor d = inv.getArgument(0);
            d.setId(123L);
            return d;
        });

        DonorResponse result = donorService.create(req);

        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getUserId()).isEqualTo(1L);

        ArgumentCaptor<Donor> captor = ArgumentCaptor.forClass(Donor.class);
        verify(userRepository).findById(1L);
        verify(donorRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isNotNull();
        assertThat(captor.getValue().getUser().getId()).isEqualTo(1L);
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void create_shouldThrow_whenUserIdProvidedButUserNotFound() {
        DonorRequest req = new DonorRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setEmail("a@b.com");
        req.setPhoneNumber("555");
        req.setUserId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.create(req))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository).findById(999L);
        verify(donorRepository, never()).save(any());
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void update_shouldUpdateFieldsAndSave_whenFound() {
        DonorRequest req = new DonorRequest();
        req.setFirstName("New");
        req.setLastName("Name");
        req.setEmail("new@example.com");
        req.setPhoneNumber("999");
        req.setUserId(null);

        when(donorRepository.findById(10L)).thenReturn(Optional.of(donor));
        when(donorRepository.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));

        DonorResponse result = donorService.update(10L, req);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("new@example.com");

        verify(donorRepository).findById(10L);
        verify(donorRepository).save(donor);
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void update_shouldAttachUser_whenUserIdProvided() {
        DonorRequest req = new DonorRequest();
        req.setFirstName("New");
        req.setLastName("Name");
        req.setEmail("new@example.com");
        req.setPhoneNumber("999");
        req.setUserId(1L);

        donor.setUser(null);

        when(donorRepository.findById(10L)).thenReturn(Optional.of(donor));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(donorRepository.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));

        DonorResponse result = donorService.update(10L, req);

        assertThat(result.getUserId()).isEqualTo(1L);

        verify(donorRepository).findById(10L);
        verify(userRepository).findById(1L);
        verify(donorRepository).save(donor);
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void update_shouldThrow_whenDonorNotFound() {
        DonorRequest req = new DonorRequest();
        req.setFirstName("New");
        req.setLastName("Name");
        req.setEmail("new@example.com");
        req.setPhoneNumber("999");

        when(donorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.update(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(donorRepository).findById(99L);
        verify(donorRepository, never()).save(any());
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void update_shouldThrow_whenUserIdProvidedButUserNotFound() {
        DonorRequest req = new DonorRequest();
        req.setFirstName("New");
        req.setLastName("Name");
        req.setEmail("new@example.com");
        req.setPhoneNumber("999");
        req.setUserId(999L);

        when(donorRepository.findById(10L)).thenReturn(Optional.of(donor));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.update(10L, req))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(donorRepository).findById(10L);
        verify(userRepository).findById(999L);
        verify(donorRepository, never()).save(any());
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void delete_shouldDelete_whenFound() {
        when(donorRepository.findById(10L)).thenReturn(Optional.of(donor));

        donorService.delete(10L);

        verify(donorRepository).findById(10L);
        verify(donorRepository).delete(donor);
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(donorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(donorRepository).findById(99L);
        verify(donorRepository, never()).delete(any());
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void createDonorForUser_shouldCreate_whenDoesNotExist() {
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(donorRepository.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));

        donorService.createDonorForUser(user);

        ArgumentCaptor<Donor> captor = ArgumentCaptor.forClass(Donor.class);
        verify(donorRepository).findByUserId(1L);
        verify(donorRepository).save(captor.capture());
        Donor saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        verifyNoMoreInteractions(donorRepository, userRepository);
    }

    @Test
    void createDonorForUser_shouldDoNothing_whenAlreadyExists() {
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(donor));

        donorService.createDonorForUser(user);

        verify(donorRepository).findByUserId(1L);
        verify(donorRepository, never()).save(any());
        verifyNoMoreInteractions(donorRepository, userRepository);
    }
}