package org.com.clockinemployees.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "TB_EMPLOYEES")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EM_ID")
    private Long id;

    @Column(name = "EM_FIRST_NAME", nullable = false, unique = false)
    private String firstName;

    @Column(name = "EM_LAST_NAME", nullable = false, unique = false)
    private String lastName;

    @Column(name = "EM_EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "EM_PASSWORD", nullable = false, unique = false)
    private String password;

    @Column(name = "EM_PROFILE_PICTURE_URL", nullable = true, unique = false)
    private String profilePictureUrl;

    @CreationTimestamp
    @Column(name = "EM_CREATED_AT", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "EM_UPDATED_AT", nullable = false)
    private Date updatedAt;

    @Column(name = "EM_DELETED_AT", nullable = true)
    private Date deletedAt;

    @OneToOne(mappedBy = "employee")
    private PersonalData personalData;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeSystemRole> employeeSystemRoles;

    @OneToMany(mappedBy = "employee")
    private List<EmployeePosition> employeePositions;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeManager> employeeManagers;

    @OneToMany(mappedBy = "manager")
    private List<EmployeeManager> managerEmployees;
}
