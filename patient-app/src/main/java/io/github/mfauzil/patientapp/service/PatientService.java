package io.github.mfauzil.patientapp.service;

import io.github.mfauzil.patientapp.dto.PatientDto;
import io.github.mfauzil.patientapp.dto.PatientMapper;
import io.github.mfauzil.patientapp.entity.Patient;
import io.github.mfauzil.patientapp.exception.DuplicatePidException;
import io.github.mfauzil.patientapp.exception.PatientNotFoundException;
import io.github.mfauzil.patientapp.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public Page<PatientDto> search(String keyword, Pageable pageable) {
        Page<Patient> page = (keyword == null || keyword.isBlank())
                ? repository.findAll(pageable)
                : repository.search(keyword.trim(), pageable);
        return page.map(PatientMapper::toDto);
    }

    public PatientDto getById(Long id) {
        return repository.findById(id)
                .map(PatientMapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    @Transactional
    public PatientDto create(PatientDto dto) {
        if (repository.existsByPid(dto.pid())) {
            throw new DuplicatePidException(dto.pid());
        }
        Patient saved = repository.save(PatientMapper.toEntity(dto, new Patient()));
        return PatientMapper.toDto(saved);
    }

    @Transactional
    public PatientDto update(Long id, PatientDto dto) {
        Patient existing = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        repository.findByPid(dto.pid())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new DuplicatePidException(dto.pid()); });

        Patient saved = repository.save(PatientMapper.toEntity(dto, existing));
        return PatientMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        repository.deleteById(id);
    }
}