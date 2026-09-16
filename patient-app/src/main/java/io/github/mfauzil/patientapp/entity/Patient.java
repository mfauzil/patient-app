package io.github.mfauzil.patientapp.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "patient",
    uniqueConstraints = @UniqueConstraint(name = "uq_patient_pid", columnNames = "pid"),
    indexes = {
        @Index(name = "ix_patient_last_name", columnList = "last_name"),
        @Index(name = "ix_patient_first_name", columnList = "first_name")
    }
)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "pid", nullable = false, length = 20)
    private String pid;

    @NotBlank
    @Size(max = 60)
    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @NotBlank
    @Size(max = 60)
    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @NotNull
    @Past
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @NotBlank
    @Pattern(regexp = "^[0-9 +()-]{6,20}$", message = "invalid phone number format")
    @Column(name = "phone_no", nullable = false, length = 20)
    private String phoneNo;

    @Valid
    @NotNull
    @Embedded
    private Address address;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPid() { return pid; }
    public void setPid(String pid) { this.pid = pid; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}