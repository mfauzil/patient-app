package io.github.mfauzil.patientapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mfauzil.patientapp.dto.AddressDto;
import io.github.mfauzil.patientapp.dto.PatientDto;
import io.github.mfauzil.patientapp.entity.AustralianState;
import io.github.mfauzil.patientapp.entity.Gender;
import io.github.mfauzil.patientapp.exception.DuplicatePidException;
import io.github.mfauzil.patientapp.exception.PatientNotFoundException;
import io.github.mfauzil.patientapp.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@ActiveProfiles("test")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientService service;

    private PatientDto dto(Long id, String pid) {
        return new PatientDto(id, pid, "Ali", "Rahman",
                LocalDate.of(1990, 1, 1), Gender.MALE, "0412345678",
                new AddressDto("12 George St", "Sydney", AustralianState.NSW, "2000"));
    }

    @Test
    void GET_list_patients() throws Exception {
        when(service.search(isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(dto(1L, "P001")), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].pid").value("P001"))
                .andExpect(jsonPath("$.content[0].address.suburb").value("Sydney"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.first").value(true));
    }

    @Test
    void GET_list_patients_by_keyword() throws Exception {
        when(service.search(eq("rahman"), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 5), 0));

        mockMvc.perform(get("/api/patients")
                        .param("keyword", "rahman")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(service).search(eq("rahman"), argThat(p ->
                p.getPageNumber() == 1 && p.getPageSize() == 5));
    }

    @Test
    void GET_one_patient() throws Exception {
        when(service.getById(1L)).thenReturn(dto(1L, "P001"));

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pid").value("P001"))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-01-01"));
    }

    @Test
    void GET_patient_not_found() throws Exception {
        when(service.getById(99L)).thenThrow(new PatientNotFoundException(99L));

        mockMvc.perform(get("/api/patients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Patient not found with id 99"));
    }

    @Test
    void POST_new_patient() throws Exception {
        when(service.create(any())).thenReturn(dto(1L, "P001"));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto(null, "P001"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void POST_data_not_valid() throws Exception {
        PatientDto rusak = new PatientDto(null, "", "", "Rahman",
                LocalDate.of(2999, 1, 1), Gender.MALE, "abc",
                new AddressDto("12 George St", "Sydney", AustralianState.NSW, "20"));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rusak)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.pid").exists())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists())
                .andExpect(jsonPath("$.fieldErrors.phoneNo").exists())
                .andExpect(jsonPath("$.fieldErrors['address.postcode']").exists());

        verify(service, never()).create(any());
    }

    @Test
    void POST_duplicate_PID() throws Exception {
        when(service.create(any())).thenThrow(new DuplicatePidException("P001"));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto(null, "P001"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void PUT_update_patient() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(dto(1L, "P001"));

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto(1L, "P001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pid").value("P001"));
    }

    @Test
    void DELETE_patient() throws Exception {
        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void DELETE_patient_not_found() throws Exception {
        doThrow(new PatientNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete("/api/patients/99"))
                .andExpect(status().isNotFound());
    }
}