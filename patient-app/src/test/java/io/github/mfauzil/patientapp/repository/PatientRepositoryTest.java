package io.github.mfauzil.patientapp.repository;

import io.github.mfauzil.patientapp.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PatientRepositoryTest {

    @Autowired
    private PatientRepository repository;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        save("P001", "Ali", "Rahman");
        save("P002", "Budi", "Rahman");
        save("P003", "Sarah", "Wilson");
        for (int i = 4; i <= 25; i++) save("P%03d".formatted(i), "First" + i, "Last" + i);
    }

    private void save(String pid, String first, String last) {
        Address a = new Address();
        a.setAddressLine("12 George St");
        a.setSuburb("Sydney");
        a.setState(AustralianState.NSW);
        a.setPostcode("2000");

        Patient p = new Patient();
        p.setPid(pid);
        p.setFirstName(first);
        p.setLastName(last);
        p.setDateOfBirth(LocalDate.of(1990, 1, 1));
        p.setGender(Gender.MALE);
        p.setPhoneNo("0412345678");
        p.setAddress(a);
        repository.save(p);
    }

    @Test
    void pagination_separate_per_page() {
        Page<Patient> page = repository.findAll(PageRequest.of(0, 10, Sort.by("lastName")));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.isFirst()).isTrue();
    }

    @Test
    void last_page_show_the_rest() {
        Page<Patient> page = repository.findAll(PageRequest.of(2, 10, Sort.by("lastName")));

        assertThat(page.getContent()).hasSize(5);
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void search_base_on_last_name() {
        Page<Patient> page = repository.search("rahman", PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void search_ignore_case_sensitivity() {
        assertThat(repository.search("RAHMAN", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
        assertThat(repository.search("rAhMaN", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
    }

    @Test
    void search_by_pid() {
        Page<Patient> page = repository.search("P003", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getLastName()).isEqualTo("Wilson");
    }

    @Test
    void search_by_full_name() {
        Page<Patient> page = repository.search("ali rahman", PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void search_by_part_of_name() {
        Page<Patient> page = repository.search("ahma", PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void exist_by_PID() {
        assertThat(repository.existsByPid("P001")).isTrue();
        assertThat(repository.existsByPid("P999")).isFalse();
    }
}