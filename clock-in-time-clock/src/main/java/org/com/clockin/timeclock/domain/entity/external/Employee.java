package org.com.clockin.timeclock.domain.entity.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
