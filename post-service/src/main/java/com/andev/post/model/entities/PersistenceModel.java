package com.andev.post.model.entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class PersistenceModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include // Add this for debugging
    private Long id;

    @CreatedBy
    @Column(name = "created_by", updatable = false) // explicit column name
    @ToString.Include
    protected String createdBy;

    @LastModifiedBy
    @Column(name = "modified_by")
    @ToString.Include
    protected String modifiedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @ToString.Include
    protected LocalDateTime created;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @ToString.Include
    protected LocalDateTime updated;
}
