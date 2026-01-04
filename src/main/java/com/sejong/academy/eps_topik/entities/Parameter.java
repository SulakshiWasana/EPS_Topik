package com.sejong.academy.eps_topik.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parameter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parameter_sequence")
    @SequenceGenerator(name = "parameter_sequence", sequenceName = "parameter_sequence", allocationSize = 1)
    private Long id;
    private String name;
    private String value;
}
