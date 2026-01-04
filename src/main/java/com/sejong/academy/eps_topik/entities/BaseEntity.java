package com.sejong.academy.eps_topik.entities;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @UpdateTimestamp
    private LocalDateTime updatedDateTime;
    @CreationTimestamp
    private LocalDateTime createdDateTime;
}
