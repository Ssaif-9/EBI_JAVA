package com.ebi.employee.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeDto {
    private String name;
    private String salary;
    private String email;
    private String phone;
    private String address;
}
