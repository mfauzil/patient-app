package io.github.mfauzil.patientapp.dto;

import io.github.mfauzil.patientapp.entity.Address;
import io.github.mfauzil.patientapp.entity.Patient;

public final class PatientMapper {

    private PatientMapper() {}

    public static PatientDto toDto(Patient p) {
        Address a = p.getAddress();
        return new PatientDto(
                p.getId(), p.getPid(), p.getFirstName(), p.getLastName(),
                p.getDateOfBirth(), p.getGender(), p.getPhoneNo(),
                new AddressDto(a.getAddressLine(), a.getSuburb(), a.getState(), a.getPostcode())
        );
    }

    public static Patient toEntity(PatientDto dto, Patient target) {
        target.setPid(dto.pid());
        target.setFirstName(dto.firstName());
        target.setLastName(dto.lastName());
        target.setDateOfBirth(dto.dateOfBirth());
        target.setGender(dto.gender());
        target.setPhoneNo(dto.phoneNo());

        Address a = target.getAddress() != null ? target.getAddress() : new Address();
        a.setAddressLine(dto.address().addressLine());
        a.setSuburb(dto.address().suburb());
        a.setState(dto.address().state());
        a.setPostcode(dto.address().postcode());
        target.setAddress(a);

        return target;
    }
}