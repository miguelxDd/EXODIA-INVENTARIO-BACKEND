package com.exodia.inventario.domain.base;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadAuditable extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @CreatedDate
    @Column(name = "creado_en", updatable = false, nullable = false)
    private OffsetDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "modificado_en", nullable = false)
    private OffsetDateTime modificadoEn;

    @CreatedBy
    @Column(name = "creado_por", updatable = false)
    private Long creadoPor;

    @LastModifiedBy
    @Column(name = "modificado_por")
    private Long modificadoPor;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Version
    @Column(name = "version")
    private Long version;
}
