package io.github.mfauzil.patientapp.service;

import io.github.mfauzil.patientapp.dto.AddressDto;
import io.github.mfauzil.patientapp.dto.PatientDto;
import io.github.mfauzil.patientapp.entity.*;
import io.github.mfauzil.patientapp.exception.DuplicatePidException;
import io.github.mfauzil.patientapp.exception.PatientNotFoundException;
import io.github.mfauzil.patientapp.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository repository;

    @InjectMocks
    private PatientService service;

    private PatientDto dto(String pid) {
        return new PatientDto(null, pid, "Ali", "Rahman",
                LocalDate.of(1990, 1, 1), Gender.MALE, "0412345678",
                new AddressDto("12 George St", "Sydney", AustralianState.NSW, "2000"));
    }

    private Patient entity(Long id, String pid) {
        Address a = new Address();
        a.setAddressLine("12 George St");
        a.setSuburb("Sydney");
        a.setState(AustralianState.NSW);
        a.setPostcode("2000");

        Patient p = new Patient();
        p.setId(id);
        p.setPid(pid);
        p.setFirstName("Ali");
        p.setLastName("Rahman");
        p.setDateOfBirth(LocalDate.of(1990, 1, 1));
        p.setGender(Gender.MALE);
        p.setPhoneNo("0412345678");
        p.setAddress(a);
        return p;
    }

    @Test
    void save_new_patient() {
        when(repository.existsByPid("P001")).thenReturn(false);
        when(repository.save(any(Patient.class))).thenReturn(entity(1L, "P001"));

        PatientDto hasil = service.create(dto("P001"));

        assertThat(hasil.id()).isEqualTo(1L);
        assertThat(hasil.pid()).isEqualTo("P001");
        verify(repository).save(any(Patient.class));
    }

    @Test
    void reject_used_pid() {
        when(repository.existsByPid("P001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto("P001")))
                .isInstanceOf(DuplicatePidException.class)
                .hasMessageContaining("P001");

        verify(repository, never()).save(any());
    }

    @Test
    void getById_return_patient() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L, "P001")));

        assertThat(service.getById(1L).pid()).isEqualTo("P001");
    }

    @Test
    void getById_throw_error_if_not_have() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void update_patient() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L, "P001")));
        when(repository.findByPid("P001")).thenReturn(Optional.of(entity(1L, "P001")));
        when(repository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientDto masuk = new PatientDto(null, "P001", "Ali", "Rahmanto",
                LocalDate.of(1990, 1, 1), Gender.MALE, "0499111222",
                new AddressDto("9 Collins St", "Melbourne", AustralianState.VIC, "3000"));

        PatientDto hasil = service.update(1L, masuk);

        assertThat(hasil.lastName()).isEqualTo("Rahmanto");
        assertThat(hasil.address().suburb()).isEqualTo("Melbourne");
    }

    @Test
    void update_throw_error_if_not_exist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, dto("P001")))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void update_refuse_other_pid() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L, "P001")));
        when(repository.findByPid("P002")).thenReturn(Optional.of(entity(2L, "P002")));

        assertThatThrownBy(() -> service.update(1L, dto("P002")))
                .isInstanceOf(DuplicatePidException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_patient() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_throw_error_if_not_exist() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(PatientNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    void search_with_keyword() {
        Page<Patient> page = new PageImpl<>(List.of(entity(1L, "P001")));
        when(repository.search(eq("rahman"), any(Pageable.class))).thenReturn(page);

        Page<PatientDto> hasil = service.search("rahman", PageRequest.of(0, 10));

        assertThat(hasil.getContent()).hasSize(1);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void search_without_keyword_take_all() {
        Page<Patient> page = new PageImpl<>(List.of(entity(1L, "P001")));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        service.search("   ", PageRequest.of(0, 10));

        verify(repository).findAll(any(Pageable.class));
        verify(repository, never()).search(anyString(), any(Pageable.class));
    }

    @Test
    void search_trim_keyword() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(repository.search(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.search("  rahman  ", PageRequest.of(0, 10));

        verify(repository).search(captor.capture(), any(Pageable.class));
        assertThat(captor.getValue()).isEqualTo("rahman");
    }
}