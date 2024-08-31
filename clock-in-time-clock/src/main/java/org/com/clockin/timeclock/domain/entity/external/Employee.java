package org.com.clockin.timeclock.domain.entity.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Employee {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;
    private Itinerary itinerary;
    private List<EnterprisePosition> enterprisePosition;
    private List<EmployeeManager> managers;

    public enum EnterprisePosition {
        HUMAN_RESOURCES,
        EMPLOYEE,
        MANAGER,
        CEO
    }
}
