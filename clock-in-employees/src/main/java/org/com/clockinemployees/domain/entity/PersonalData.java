package org.com.clockinemployees.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "TB_PERSONAL_DATAS")
public class PersonalData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PD_ID")
    private Long id;

    @Column(name = "PD_PHONE", nullable = true)
    private String phone;

    @Column(name = "PD_STREET", nullable = false)
    private String street;

    @Column(name = "PD_HOUSE_NUMBER", nullable = false)
    private String houseNumber;

    @Column(name = "PD_COMPLEMENT", nullable = true)
    private String complement;

    @Column(name = "PD_ZIPCODE", nullable = false)
    private String zipcode;

    @Column(name = "PD_CITY", nullable = false)
    private String city;

    @Column(name = "PD_COUNTRY", nullable = false)
    private String country;

    @Column(name = "PD_STATE", nullable = false)
    private String state;

    @CreationTimestamp
    @Column(name = "PD_CREATED_AT", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "PD_UPDATED_AT", nullable = false)
    private Date updatedAt;

    @OneToOne
    @JoinColumn(name = "PD_EMPLOYEE_ID")
    private Employee employee;
}
