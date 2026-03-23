# Arquitectura de Servicios Java — Módulo de Inventario

**Stack:** Java 21 (LTS) + Spring Boot 4.0.x + PostgreSQL 18 + Angular 21  
**Build:** Maven  
**Estilo arquitectónico:** Capas limpias con Domain Services  
**Base de datos:** Modelo definido via Flyway migrations en `src/main/resources/db/migration/`

---

## 1. Estructura de módulos Maven

El inventario se construye como un módulo dentro de un proyecto multi-módulo o como un proyecto standalone. 
La estructura recomendada es monorepo con módulos Maven:

```
exodia-erp/
├── pom.xml                              (parent POM)
├── exodia-common/                       (DTOs compartidos, utils, excepciones base)
│   ├── pom.xml
│   └── src/main/java/com/exodia/comun/
├── exodia-security/                     (autenticación, JWT, contexto de usuario)
│   ├── pom.xml
│   └── src/main/java/com/exodia/seguridad/
├── exodia-inventory/                    (MÓDULO DE INVENTARIO)
│   ├── pom.xml
│   └── src/main/java/com/exodia/inventario/
└── exodia-app/                          (aplicación Spring Boot, punto de entrada)
    ├── pom.xml
    └── src/main/java/com/exodia/app/
```

Si el proyecto es standalone (solo inventario):

```
exodia-inventory/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/exodia/inventario/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/migration/          (Flyway)
    └── test/
        └── java/com/exodia/inventario/
```

---

## 2. Estructura de paquetes del módulo de inventario

```
com.exodia.inventario
├── config/                          ── Configuración Spring
│   ├── InventoryConfig.java
│   ├── JpaAuditConfig.java
│   ├── FlywayConfig.java
│   └── OpenApiConfig.java
│
├── domain/                          ── Capa de dominio (entidades JPA + enums)
│   ├── entity/
│   │   ├── catalog/                 ── Entidades de catálogos
│   │   │   ├── Company.java
│   │   │   ├── Warehouse.java
│   │   │   ├── WarehouseLocation.java
│   │   │   ├── Unit.java
│   │   │   ├── UnitConversion.java
│   │   │   ├── OperationType.java
│   │   │   ├── AdjustmentType.java
│   │   │   ├── ContainerStatus.java
│   │   │   └── TransferStatus.java
│   │   │
│   │   ├── core/                    ── Entidades del núcleo operativo
│   │   │   ├── Container.java
│   │   │   ├── Operation.java
│   │   │   ├── Reservation.java
│   │   │   └── LotMaster.java
│   │   │
│   │   ├── flow/                    ── Entidades de flujos de negocio
│   │   │   ├── Reception.java
│   │   │   ├── ReceptionLine.java
│   │   │   ├── Transfer.java
│   │   │   ├── TransferLine.java
│   │   │   ├── TransferContainer.java
│   │   │   ├── Adjustment.java
│   │   │   ├── AdjustmentLine.java
│   │   │   ├── PickingOrder.java
│   │   │   ├── PickingLine.java
│   │   │   ├── PhysicalCount.java
│   │   │   └── PhysicalCountLine.java
│   │   │
│   │   ├── extension/               ── Entidades de extensiones
│   │   │   ├── MinMaxConfig.java
│   │   │   ├── ShrinkageConfig.java
│   │   │   ├── ShrinkageRecord.java
│   │   │   ├── CostSnapshot.java
│   │   │   └── BarcodeSequence.java
│   │   │
│   │   └── audit/
│   │       └── AuditLog.java
│   │
│   ├── enums/                       ── Enums que mapean catálogos
│   │   ├── OperationTypeCode.java
│   │   ├── AdjustmentTypeCode.java
│   │   ├── ContainerStatusCode.java
│   │   ├── TransferStatusCode.java
│   │   ├── TransferType.java
│   │   ├── ReceptionType.java
│   │   ├── PickingType.java
│   │   ├── LotStatus.java
│   │   ├── StockStatus.java
│   │   ├── CostMethod.java
│   │   ├── LocationType.java
│   │   ├── ShrinkageType.java
│   │   ├── ReferenceType.java
│   │   └── ConversionOperationType.java
│   │
│   └── base/                        ── Clases base
│       ├── BaseEntity.java
│       └── AuditableEntity.java
│
├── repository/                      ── Capa de persistencia (Spring Data JPA)
│   ├── catalog/
│   │   ├── CompanyRepository.java
│   │   ├── WarehouseRepository.java
│   │   ├── WarehouseLocationRepository.java
│   │   ├── UnitRepository.java
│   │   ├── UnitConversionRepository.java
│   │   ├── OperationTypeRepository.java
│   │   ├── AdjustmentTypeRepository.java
│   │   ├── ContainerStatusRepository.java
│   │   └── TransferStatusRepository.java
│   │
│   ├── core/
│   │   ├── ContainerRepository.java
│   │   ├── OperationRepository.java
│   │   ├── ReservationRepository.java
│   │   └── LotMasterRepository.java
│   │
│   ├── flow/
│   │   ├── ReceptionRepository.java
│   │   ├── ReceptionLineRepository.java
│   │   ├── TransferRepository.java
│   │   ├── TransferLineRepository.java
│   │   ├── TransferContainerRepository.java
│   │   ├── AdjustmentRepository.java
│   │   ├── AdjustmentLineRepository.java
│   │   ├── PickingOrderRepository.java
│   │   ├── PickingLineRepository.java
│   │   ├── PhysicalCountRepository.java
│   │   └── PhysicalCountLineRepository.java
│   │
│   ├── extension/
│   │   ├── MinMaxConfigRepository.java
│   │   ├── ShrinkageConfigRepository.java
│   │   ├── ShrinkageRecordRepository.java
│   │   ├── CostSnapshotRepository.java
│   │   └── BarcodeSequenceRepository.java
│   │
│   ├── audit/
│   │   └── AuditLogRepository.java
│   │
│   └── projection/                  ── Proyecciones para consultas optimizadas
│       ├── ContainerStockProjection.java
│       ├── ProductWarehouseStockProjection.java
│       └── ExpiringContainerProjection.java
│
├── service/                         ── Capa de servicios de dominio
│   ├── catalog/
│   │   ├── WarehouseService.java
│   │   ├── WarehouseLocationService.java
│   │   ├── UnitService.java
│   │   └── UnitConversionService.java
│   │
│   ├── core/
│   │   ├── ContainerService.java
│   │   ├── OperationService.java
│   │   ├── ReservationService.java
│   │   ├── BarcodeService.java
│   │   └── StockQueryService.java
│   │
│   ├── flow/
│   │   ├── ReceiveInventoryService.java
│   │   ├── TransferByContainerService.java
│   │   ├── TransferByProductService.java
│   │   ├── AdjustInventoryService.java
│   │   ├── MoveContainerService.java
│   │   ├── ConvertUnitService.java
│   │   ├── PickingService.java
│   │   └── PhysicalCountService.java
│   │
│   ├── extension/
│   │   ├── MinMaxService.java
│   │   ├── ShrinkageService.java
│   │   ├── CostValuationService.java
│   │   └── InventoryLedgerService.java
│   │
│   └── audit/
│       └── AuditService.java
│
├── dto/                             ── Data Transfer Objects
│   ├── request/                     ── DTOs de entrada (desde Angular)
│   │   ├── catalog/
│   │   │   ├── CreateWarehouseRequest.java
│   │   │   ├── UpdateWarehouseRequest.java
│   │   │   ├── CreateLocationRequest.java
│   │   │   ├── CreateUnitRequest.java
│   │   │   └── CreateConversionRequest.java
│   │   ├── core/
│   │   │   ├── ReceiveInventoryRequest.java
│   │   │   ├── ReceiveLineRequest.java
│   │   │   ├── AdjustInventoryRequest.java
│   │   │   ├── AdjustLineRequest.java
│   │   │   ├── AdjustPriceRequest.java
│   │   │   ├── MoveContainerRequest.java
│   │   │   ├── ConvertUnitRequest.java
│   │   │   └── ReserveStockRequest.java
│   │   ├── transfer/
│   │   │   ├── CreateTransferByContainerRequest.java
│   │   │   ├── CreateTransferByProductRequest.java
│   │   │   ├── AddTransferContainerRequest.java
│   │   │   ├── AddTransferLineRequest.java
│   │   │   ├── ReceiveTransferRequest.java
│   │   │   └── DispatchTransferRequest.java
│   │   ├── picking/
│   │   │   ├── CreatePickingOrderRequest.java
│   │   │   ├── PickLineRequest.java
│   │   │   └── PickMultipleRequest.java
│   │   ├── count/
│   │   │   ├── CreatePhysicalCountRequest.java
│   │   │   ├── CountLineRequest.java
│   │   │   └── ApplyCountRequest.java
│   │   ├── extension/
│   │   │   ├── CreateMinMaxRequest.java
│   │   │   ├── ProcessShrinkageRequest.java
│   │   │   └── CostSnapshotRequest.java
│   │   └── query/
│   │       ├── InventoryQueryFilter.java
│   │       ├── KardexQueryFilter.java
│   │       └── StockByProductFilter.java
│   │
│   └── response/                    ── DTOs de salida (hacia Angular)
│       ├── catalog/
│       │   ├── WarehouseResponse.java
│       │   ├── LocationResponse.java
│       │   ├── UnitResponse.java
│       │   └── ConversionResponse.java
│       ├── core/
│       │   ├── ContainerResponse.java
│       │   ├── ContainerStockResponse.java
│       │   ├── OperationResponse.java
│       │   ├── ReservationResponse.java
│       │   └── StockSummaryResponse.java
│       ├── flow/
│       │   ├── ReceptionResponse.java
│       │   ├── TransferResponse.java
│       │   ├── TransferDetailResponse.java
│       │   ├── AdjustmentResponse.java
│       │   ├── PickingOrderResponse.java
│       │   └── PhysicalCountResponse.java
│       ├── extension/
│       │   ├── MinMaxResponse.java
│       │   ├── ShrinkageStatisticsResponse.java
│       │   ├── CostValuationResponse.java
│       │   └── LedgerEntryResponse.java
│       ├── report/
│       │   ├── InventoryCostReportResponse.java
│       │   └── AuxiliaryLedgerResponse.java
│       └── common/
│           ├── PageResponse.java
│           └── ApiResponse.java
│
├── controller/                      ── Capa REST (endpoints para Angular)
│   ├── catalog/
│   │   ├── WarehouseController.java
│   │   ├── WarehouseLocationController.java
│   │   ├── UnitController.java
│   │   └── UnitConversionController.java
│   │
│   ├── core/
│   │   ├── InventoryQueryController.java
│   │   ├── ContainerController.java
│   │   └── ReservationController.java
│   │
│   ├── flow/
│   │   ├── ReceptionController.java
│   │   ├── TransferController.java
│   │   ├── AdjustmentController.java
│   │   ├── MoveController.java
│   │   ├── ConversionController.java
│   │   ├── PickingController.java
│   │   └── PhysicalCountController.java
│   │
│   ├── extension/
│   │   ├── MinMaxController.java
│   │   ├── ShrinkageController.java
│   │   └── CostController.java
│   │
│   └── report/
│       └── InventoryReportController.java
│
├── mapper/                          ── MapStruct mappers
│   ├── WarehouseMapper.java
│   ├── LocationMapper.java
│   ├── UnitMapper.java
│   ├── ContainerMapper.java
│   ├── OperationMapper.java
│   ├── ReceptionMapper.java
│   ├── TransferMapper.java
│   ├── AdjustmentMapper.java
│   ├── PickingMapper.java
│   ├── PhysicalCountMapper.java
│   ├── MinMaxMapper.java
│   └── ShrinkageMapper.java
│
├── exception/                       ── Excepciones de dominio
│   ├── InventoryException.java
│   ├── InsufficientStockException.java
│   ├── ContainerNotFoundException.java
│   ├── DuplicateBarcodeException.java
│   ├── InvalidOperationException.java
│   ├── TransferStateException.java
│   ├── ConversionNotFoundException.java
│   ├── ReservationConflictException.java
│   └── handler/
│       └── InventoryExceptionHandler.java
│
├── event/                           ── Eventos de dominio (Spring Events)
│   ├── InventoryReceivedEvent.java
│   ├── StockAdjustedEvent.java
│   ├── TransferDispatchedEvent.java
│   ├── TransferReceivedEvent.java
│   ├── PickingCompletedEvent.java
│   ├── StockBelowMinimumEvent.java
│   └── ContainerExpiringEvent.java
│
├── listener/                        ── Listeners de eventos
│   ├── MinMaxAlertListener.java
│   ├── AuditEventListener.java
│   └── AccountingIntegrationListener.java
│
├── scheduler/                       ── Tareas programadas
│   ├── MinMaxRecalculationJob.java
│   ├── ExpirationAlertJob.java
│   ├── CostSnapshotJob.java
│   └── ReservationExpirationJob.java
│
├── integration/                     ── Integraciones con otros módulos
│   ├── purchasing/
│   │   └── PurchaseOrderIntegration.java
│   ├── sales/
│   │   └── SalesIntegration.java
│   ├── production/
│   │   └── ProductionIntegration.java
│   └── accounting/
│       └── AccountingIntegration.java
│
└── util/
    ├── InventoryConstants.java
    └── StockCalculator.java
```

---

## 3. Dependencias Maven (pom.xml del módulo de inventario)

```xml
<properties>
    <java.version>21</java.version>
    <spring-boot.version>4.0.4</spring-boot.version>
    <mapstruct.version>1.6.3</mapstruct.version>
    <lombok.version>1.18.44</lombok.version>
</properties>

<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Flyway -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- OpenAPI / Swagger -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>3.0.0</version>
    </dependency>

    <!-- Apache POI (Excel) -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.3.0</version>
    </dependency>

    <!-- iText (PDF) -->
    <dependency>
        <groupId>com.itextpdf</groupId>
        <artifactId>itext-core</artifactId>
        <version>8.0.5</version>
        <type>pom</type>
    </dependency>

    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>21</source>
                <target>21</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${mapstruct.version}</version>
                    </path>
                    <!-- Lombok debe ir ANTES de MapStruct -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 4. Clases base del dominio

### 4.1. BaseEntity.java

```java
package com.exodia.inventario.domain.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

### 4.2. AuditableEntity.java

```java
package com.exodia.inventario.domain.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
```

---

## 5. Entidades JPA — Catálogos

### 5.1. Company.java

```java
package com.exodia.inventario.domain.entity.catalog;

import com.exodia.inventario.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "inv_companies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
```

### 5.2. Warehouse.java

```java
package com.exodia.inventario.domain.entity.catalog;

import com.exodia.inventario.domain.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inv_warehouses",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_warehouse_code_company",
        columnNames = {"company_id", "code"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Warehouse extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standby_location_id")
    private WarehouseLocation standbyLocation;

    @Column(name = "is_finished_goods", nullable = false)
    @Builder.Default
    private Boolean isFinishedGoods = false;

    @Column(name = "is_consignment", nullable = false)
    @Builder.Default
    private Boolean isConsignment = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WarehouseLocation> locations = new ArrayList<>();
}
```

### 5.3. WarehouseLocation.java

```java
package com.exodia.inventario.domain.entity.catalog;

import com.exodia.inventario.domain.base.AuditableEntity;
import com.exodia.inventario.domain.enums.LocationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inv_warehouse_locations",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_location_code_warehouse",
        columnNames = {"warehouse_id", "code"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WarehouseLocation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 30)
    @Builder.Default
    private LocationType locationType = LocationType.GENERAL;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
```

### 5.4. Unit.java

```java
package com.exodia.inventario.domain.entity.catalog;

import com.exodia.inventario.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "inv_units",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_unit_code_company",
        columnNames = {"company_id", "code"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Unit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "abbreviation", length = 10)
    private String abbreviation;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
```

### 5.5. UnitConversion.java

```java
package com.exodia.inventario.domain.entity.catalog;

import com.exodia.inventario.domain.base.BaseEntity;
import com.exodia.inventario.domain.enums.ConversionOperationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inv_unit_conversions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_conversion",
        columnNames = {"company_id", "source_unit_id", "target_unit_id", "product_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UnitConversion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_unit_id", nullable = false)
    private Unit sourceUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_unit_id", nullable = false)
    private Unit targetUnit;

    @Column(name = "conversion_factor", nullable = false, precision = 18, scale = 6)
    private BigDecimal conversionFactor;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 10)
    private ConversionOperationType operationType;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    /**
     * Convierte una cantidad de la unidad origen a la unidad destino.
     */
    public BigDecimal convert(BigDecimal sourceQty) {
        return switch (operationType) {
            case MULTIPLY -> sourceQty.multiply(conversionFactor);
            case DIVIDE   -> sourceQty.divide(conversionFactor, 6, java.math.RoundingMode.HALF_UP);
        };
    }
}
```

### 5.6. OperationType.java

```java
package com.exodia.inventario.domain.entity.catalog;

import com.exodia.inventario.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inv_operation_types")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OperationType extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sign", nullable = false)
    private Short sign; // 1=entrada, -1=salida, 0=informativo

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
```

### 5.7. ContainerStatus.java

```java
package com.exodia.inventario.domain.entity.catalog;

import com.exodia.inventario.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inv_container_statuses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ContainerStatus extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "allows_picking", nullable = false)
    @Builder.Default
    private Boolean allowsPicking = false;

    @Column(name = "allows_transfer", nullable = false)
    @Builder.Default
    private Boolean allowsTransfer = false;
}
```

---

## 6. Entidades JPA — Núcleo operativo

### 6.1. Container.java

```java
package com.exodia.inventario.domain.entity.core;

import com.exodia.inventario.domain.base.AuditableEntity;
import com.exodia.inventario.domain.entity.catalog.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inv_containers",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_container_barcode_company",
        columnNames = {"company_id", "barcode"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Container extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "barcode", nullable = false, length = 100)
    private String barcode;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "product_supplier_id")
    private Long productSupplierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private WarehouseLocation location;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private LotMaster lot;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "origin_id")
    private Long originId;

    @Column(name = "warranty_info", length = 500)
    private String warrantyInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private ContainerStatus status;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Relaciones inversas (lazy, solo para navegación si se necesita)
    @OneToMany(mappedBy = "container", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Operation> operations = new ArrayList<>();
}
```

### 6.2. Operation.java

```java
package com.exodia.inventario.domain.entity.core;

import com.exodia.inventario.domain.base.BaseEntity;
import com.exodia.inventario.domain.entity.catalog.*;
import com.exodia.inventario.domain.enums.ReferenceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inv_operations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Operation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    private Container container;

    @Column(name = "barcode", nullable = false, length = 100)
    private String barcode;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private WarehouseLocation location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_type_id", nullable = false)
    private OperationType operationType;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjustment_type_id")
    private AdjustmentType adjustmentType;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 50)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_line_id")
    private Long referenceLineId;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "operation_date", nullable = false)
    @Builder.Default
    private OffsetDateTime operationDate = OffsetDateTime.now();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (operationDate == null) {
            operationDate = OffsetDateTime.now();
        }
    }
}
```

### 6.3. Reservation.java

```java
package com.exodia.inventario.domain.entity.core;

import com.exodia.inventario.domain.base.BaseEntity;
import com.exodia.inventario.domain.entity.catalog.*;
import com.exodia.inventario.domain.enums.ReferenceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inv_reservations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    private Container container;

    @Column(name = "barcode", nullable = false, length = 100)
    private String barcode;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "reserved_qty", nullable = false, precision = 18, scale = 6)
    private BigDecimal reservedQty;

    @Column(name = "fulfilled_qty", nullable = false, precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal fulfilledQty = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 50)
    private ReferenceType referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "reference_line_id")
    private Long referenceLineId;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    /**
     * Cantidad pendiente por despachar.
     */
    public BigDecimal getPendingQty() {
        return reservedQty.subtract(fulfilledQty);
    }
}
```

---

## 7. Enums del dominio

### 7.1. OperationTypeCode.java

```java
package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationTypeCode {
    RECEPTION("RECEPTION"),
    PICKING("PICKING"),
    ADJUSTMENT_IN("ADJUSTMENT_IN"),
    ADJUSTMENT_OUT("ADJUSTMENT_OUT"),
    ADJUSTMENT_INFO("ADJUSTMENT_INFO"),
    TRANSFER_OUT("TRANSFER_OUT"),
    TRANSFER_IN("TRANSFER_IN"),
    MOVE_OUT("MOVE_OUT"),
    MOVE_IN("MOVE_IN"),
    CONVERSION_OUT("CONVERSION_OUT"),
    CONVERSION_IN("CONVERSION_IN"),
    PRODUCTION_IN("PRODUCTION_IN"),
    SHRINKAGE("SHRINKAGE"),
    SALE_ADJUSTMENT("SALE_ADJUSTMENT"),
    PHYSICAL_COUNT_IN("PHYSICAL_COUNT_IN"),
    PHYSICAL_COUNT_OUT("PHYSICAL_COUNT_OUT");

    private final String code;
}
```

### 7.2. ContainerStatusCode.java

```java
package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContainerStatusCode {
    AVAILABLE("AVAILABLE"),
    RESERVED("RESERVED"),
    IN_TRANSIT("IN_TRANSIT"),
    IN_STANDBY("IN_STANDBY"),
    QUARANTINE("QUARANTINE"),
    BLOCKED("BLOCKED"),
    DEPLETED("DEPLETED");

    private final String code;
}
```

### 7.3. Otros enums

```java
// TransferType.java
public enum TransferType { BY_CONTAINER, BY_PRODUCT }

// ReceptionType.java
public enum ReceptionType { MANUAL, PURCHASE_ORDER, TRANSFER, PRODUCTION, RETURN }

// PickingType.java
public enum PickingType { REQUISITION, SALE_ORDER, PRODUCTION, GENERAL }

// LocationType.java
public enum LocationType { GENERAL, STANDBY, TEMPORARY, RECEPTION, PRODUCTION }

// LotStatus.java
public enum LotStatus { ACTIVE, QUARANTINE, EXPIRED, BLOCKED, CONSUMED }

// StockStatus.java
public enum StockStatus { BELOW, IN_RANGE, ABOVE }

// CostMethod.java
public enum CostMethod { WEIGHTED_AVG, FIXED, FIFO, LIFO }

// ConversionOperationType.java
public enum ConversionOperationType { MULTIPLY, DIVIDE }

// ShrinkageType.java
public enum ShrinkageType { MANUAL, AUTOMATIC, BY_PRODUCT }

// ReferenceType.java
public enum ReferenceType {
    RECEPTION, TRANSFER, PICKING_ORDER, ADJUSTMENT,
    PURCHASE_ORDER, SALE, PRODUCTION_ORDER, PHYSICAL_COUNT
}

// TransferStatusCode.java
public enum TransferStatusCode {
    DRAFT, CONFIRMED, DISPATCHED, IN_TRANSIT,
    PARTIALLY_RECEIVED, RECEIVED, CANCELLED, FORCED_CLOSE
}
```

---

## 8. Repositorios clave con queries personalizados

### 8.1. OperationRepository.java

```java
package com.exodia.inventario.repository.core;

import com.exodia.inventario.domain.entity.core.Operation;
import com.exodia.inventario.repository.projection.ContainerStockProjection;
import com.exodia.inventario.repository.projection.ProductWarehouseStockProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {

    /**
     * Stock actual de un contenedor.
     */
    @Query("""
        SELECT COALESCE(SUM(o.quantity), 0)
        FROM Operation o
        WHERE o.container.id = :containerId
          AND o.isActive = true
        """)
    BigDecimal getStockByContainerId(@Param("containerId") Long containerId);

    /**
     * Stock actual por barcode.
     */
    @Query("""
        SELECT COALESCE(SUM(o.quantity), 0)
        FROM Operation o
        WHERE o.barcode = :barcode
          AND o.company.id = :companyId
          AND o.isActive = true
        """)
    BigDecimal getStockByBarcode(@Param("companyId") Long companyId,
                                 @Param("barcode") String barcode);

    /**
     * Stock por producto y bodega.
     */
    @Query("""
        SELECT COALESCE(SUM(o.quantity), 0)
        FROM Operation o
        WHERE o.productId = :productId
          AND o.warehouse.id = :warehouseId
          AND o.company.id = :companyId
          AND o.isActive = true
        """)
    BigDecimal getStockByProductAndWarehouse(@Param("companyId") Long companyId,
                                             @Param("productId") Long productId,
                                             @Param("warehouseId") Long warehouseId);

    /**
     * Consulta consolidada de stock por contenedor (vista principal de inventario).
     */
    @Query(value = """
        SELECT
            c.id              AS containerId,
            c.barcode         AS barcode,
            c.product_id      AS productId,
            c.supplier_id     AS supplierId,
            c.unit_id         AS unitId,
            c.warehouse_id    AS warehouseId,
            c.location_id     AS locationId,
            c.unit_price      AS unitPrice,
            c.lot_number      AS lotNumber,
            c.expiration_date AS expirationDate,
            cs.code           AS statusCode,
            COALESCE(SUM(o.quantity) FILTER (WHERE o.is_active), 0) AS stockQty,
            COALESCE((
                SELECT SUM(r.reserved_qty - r.fulfilled_qty)
                FROM inv_reservations r
                WHERE r.container_id = c.id
                  AND r.status IN ('PENDING','PARTIAL')
            ), 0) AS reservedQty
        FROM inv_containers c
        JOIN inv_container_statuses cs ON cs.id = c.status_id
        LEFT JOIN inv_operations o ON o.container_id = c.id AND o.is_active = true
        WHERE c.company_id = :companyId
          AND c.is_active = true
          AND (:warehouseId IS NULL OR c.warehouse_id = :warehouseId)
          AND (:productId IS NULL OR c.product_id = :productId)
          AND (:supplierId IS NULL OR c.supplier_id = :supplierId)
          AND (:barcode IS NULL OR c.barcode = :barcode)
          AND (:lotNumber IS NULL OR c.lot_number = :lotNumber)
        GROUP BY c.id, c.barcode, c.product_id, c.supplier_id, c.unit_id,
                 c.warehouse_id, c.location_id, c.unit_price, c.lot_number,
                 c.expiration_date, cs.code
        HAVING COALESCE(SUM(o.quantity) FILTER (WHERE o.is_active), 0) > 0
        """, nativeQuery = true)
    Page<ContainerStockProjection> findConsolidatedStock(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("supplierId") Long supplierId,
            @Param("barcode") String barcode,
            @Param("lotNumber") String lotNumber,
            Pageable pageable);

    /**
     * Stock agregado por producto + bodega + unidad.
     */
    @Query(value = """
        SELECT
            o.product_id    AS productId,
            o.warehouse_id  AS warehouseId,
            o.unit_id       AS unitId,
            SUM(o.quantity)  AS stockQty
        FROM inv_operations o
        WHERE o.company_id = :companyId
          AND o.is_active = true
          AND (:warehouseId IS NULL OR o.warehouse_id = :warehouseId)
          AND (:productId IS NULL OR o.product_id = :productId)
        GROUP BY o.product_id, o.warehouse_id, o.unit_id
        HAVING SUM(o.quantity) <> 0
        """, nativeQuery = true)
    List<ProductWarehouseStockProjection> findStockByProductAndWarehouse(
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId);

    /**
     * Kardex: historial de operaciones para un contenedor.
     */
    @Query("""
        SELECT o FROM Operation o
        WHERE o.company.id = :companyId
          AND o.isActive = true
          AND (:containerId IS NULL OR o.container.id = :containerId)
          AND (:barcode IS NULL OR o.barcode = :barcode)
          AND (:productId IS NULL OR o.productId = :productId)
          AND (:warehouseId IS NULL OR o.warehouse.id = :warehouseId)
          AND (:dateFrom IS NULL OR o.operationDate >= :dateFrom)
          AND (:dateTo IS NULL OR o.operationDate <= :dateTo)
        ORDER BY o.operationDate DESC, o.id DESC
        """)
    Page<Operation> findKardex(@Param("companyId") Long companyId,
                               @Param("containerId") Long containerId,
                               @Param("barcode") String barcode,
                               @Param("productId") Long productId,
                               @Param("warehouseId") Long warehouseId,
                               @Param("dateFrom") OffsetDateTime dateFrom,
                               @Param("dateTo") OffsetDateTime dateTo,
                               Pageable pageable);

    /**
     * Contenedores disponibles por producto ordenados FEFO (primero en vencer).
     * Usado por picking y transferencias.
     */
    @Query(value = """
        SELECT
            c.id              AS containerId,
            c.barcode         AS barcode,
            c.location_id     AS locationId,
            c.unit_price      AS unitPrice,
            c.expiration_date AS expirationDate,
            COALESCE(SUM(o.quantity), 0) AS stockQty,
            COALESCE(SUM(o.quantity), 0)
              - COALESCE((
                  SELECT SUM(r.reserved_qty - r.fulfilled_qty)
                  FROM inv_reservations r
                  WHERE r.container_id = c.id
                    AND r.status IN ('PENDING','PARTIAL')
              ), 0) AS availableQty
        FROM inv_containers c
        JOIN inv_container_statuses cs ON cs.id = c.status_id
        LEFT JOIN inv_operations o ON o.container_id = c.id AND o.is_active = true
        WHERE c.company_id = :companyId
          AND c.product_id = :productId
          AND c.warehouse_id = :warehouseId
          AND cs.allows_picking = true
          AND c.is_active = true
        GROUP BY c.id, c.barcode, c.location_id, c.unit_price, c.expiration_date
        HAVING COALESCE(SUM(o.quantity), 0)
               - COALESCE((
                   SELECT SUM(r.reserved_qty - r.fulfilled_qty)
                   FROM inv_reservations r
                   WHERE r.container_id = c.id
                     AND r.status IN ('PENDING','PARTIAL')
               ), 0) > 0
        ORDER BY c.expiration_date ASC NULLS LAST, c.created_at ASC
        """, nativeQuery = true)
    List<ContainerStockProjection> findAvailableContainersFEFO(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId);
}
```

### 8.2. Proyecciones

```java
// ContainerStockProjection.java
package com.exodia.inventario.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ContainerStockProjection {
    Long getContainerId();
    String getBarcode();
    Long getProductId();
    Long getSupplierId();
    Long getUnitId();
    Long getWarehouseId();
    Long getLocationId();
    BigDecimal getUnitPrice();
    String getLotNumber();
    LocalDate getExpirationDate();
    String getStatusCode();
    BigDecimal getStockQty();
    BigDecimal getReservedQty();

    default BigDecimal getAvailableQty() {
        BigDecimal stock = getStockQty() != null ? getStockQty() : BigDecimal.ZERO;
        BigDecimal reserved = getReservedQty() != null ? getReservedQty() : BigDecimal.ZERO;
        return stock.subtract(reserved);
    }
}

// ProductWarehouseStockProjection.java
package com.exodia.inventario.repository.projection;

import java.math.BigDecimal;

public interface ProductWarehouseStockProjection {
    Long getProductId();
    Long getWarehouseId();
    Long getUnitId();
    BigDecimal getStockQty();
}
```

### 8.3. ContainerRepository.java

```java
package com.exodia.inventario.repository.core;

import com.exodia.inventario.domain.entity.core.Container;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContainerRepository
        extends JpaRepository<Container, Long>, JpaSpecificationExecutor<Container> {

    Optional<Container> findByCompanyIdAndBarcode(Long companyId, String barcode);

    boolean existsByCompanyIdAndBarcode(Long companyId, String barcode);

    /**
     * Busca contenedor reutilizable: mismo producto, proveedor, bodega y unidad.
     */
    Optional<Container> findByCompanyIdAndBarcodeAndProductIdAndSupplierIdAndWarehouseIdAndUnitId(
            Long companyId, String barcode, Long productId,
            Long supplierId, Long warehouseId, Long unitId);
}
```

---

## 9. Servicios de dominio — los más importantes

### 9.1. OperationService.java (servicio central de creación de operaciones)

```java
package com.exodia.inventario.service.core;

import com.exodia.inventario.domain.entity.catalog.*;
import com.exodia.inventario.domain.entity.core.*;
import com.exodia.inventario.domain.enums.*;
import com.exodia.inventario.repository.catalog.OperationTypeRepository;
import com.exodia.inventario.repository.core.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servicio central para crear operaciones en el kardex.
 * TODA creación de operación de inventario DEBE pasar por aquí.
 * Esto garantiza consistencia y auditoría.
 */
@Service
@RequiredArgsConstructor
public class OperationService {

    private final OperationRepository operationRepository;
    private final OperationTypeRepository operationTypeRepository;

    @Transactional
    public Operation createOperation(
            Container container,
            OperationTypeCode typeCode,
            BigDecimal quantity,
            String comments,
            ReferenceType refType,
            Long refId,
            Long refLineId
    ) {
        OperationType opType = operationTypeRepository.findByCode(typeCode.getCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de operación no encontrado: " + typeCode.getCode()));

        Operation operation = Operation.builder()
                .company(container.getCompany())
                .container(container)
                .barcode(container.getBarcode())
                .productId(container.getProductId())
                .warehouse(container.getWarehouse())
                .location(container.getLocation())
                .unit(container.getUnit())
                .operationType(opType)
                .quantity(quantity)
                .unitPrice(container.getUnitPrice())
                .lotNumber(container.getLotNumber())
                .expirationDate(container.getExpirationDate())
                .supplierId(container.getSupplierId())
                .referenceType(refType)
                .referenceId(refId)
                .referenceLineId(refLineId)
                .comments(comments)
                .build();

        return operationRepository.save(operation);
    }

    /**
     * Versión simplificada para operaciones sin referencia cruzada.
     */
    @Transactional
    public Operation createOperation(
            Container container,
            OperationTypeCode typeCode,
            BigDecimal quantity,
            String comments
    ) {
        return createOperation(container, typeCode, quantity, comments, null, null, null);
    }
}
```

### 9.2. StockQueryService.java

```java
package com.exodia.inventario.service.core;

import com.exodia.inventario.repository.core.OperationRepository;
import com.exodia.inventario.repository.projection.ContainerStockProjection;
import com.exodia.inventario.repository.projection.ProductWarehouseStockProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Servicio de consultas de stock. Nunca modifica datos.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockQueryService {

    private final OperationRepository operationRepository;

    public BigDecimal getStockByContainerId(Long containerId) {
        return operationRepository.getStockByContainerId(containerId);
    }

    public BigDecimal getStockByBarcode(Long companyId, String barcode) {
        return operationRepository.getStockByBarcode(companyId, barcode);
    }

    public BigDecimal getStockByProductAndWarehouse(Long companyId, Long productId, Long warehouseId) {
        return operationRepository.getStockByProductAndWarehouse(companyId, productId, warehouseId);
    }

    public Page<ContainerStockProjection> getConsolidatedStock(
            Long companyId, Long warehouseId, Long productId,
            Long supplierId, String barcode, String lotNumber,
            Pageable pageable) {
        return operationRepository.findConsolidatedStock(
                companyId, warehouseId, productId, supplierId, barcode, lotNumber, pageable);
    }

    public List<ProductWarehouseStockProjection> getStockByProductAndWarehouse(
            Long companyId, Long warehouseId, Long productId) {
        return operationRepository.findStockByProductAndWarehouse(companyId, warehouseId, productId);
    }

    public Page<com.exodia.inventario.domain.entity.core.Operation> getKardex(
            Long companyId, Long containerId, String barcode,
            Long productId, Long warehouseId,
            OffsetDateTime dateFrom, OffsetDateTime dateTo,
            Pageable pageable) {
        return operationRepository.findKardex(
                companyId, containerId, barcode, productId, warehouseId,
                dateFrom, dateTo, pageable);
    }

    public List<ContainerStockProjection> getAvailableContainersFEFO(
            Long companyId, Long productId, Long warehouseId) {
        return operationRepository.findAvailableContainersFEFO(companyId, productId, warehouseId);
    }
}
```

### 9.3. ReceiveInventoryService.java (ejemplo del flujo más importante)

```java
package com.exodia.inventario.service.flow;

import com.exodia.inventario.domain.entity.catalog.*;
import com.exodia.inventario.domain.entity.core.*;
import com.exodia.inventario.domain.entity.flow.*;
import com.exodia.inventario.domain.enums.*;
import com.exodia.inventario.dto.request.core.ReceiveInventoryRequest;
import com.exodia.inventario.dto.request.core.ReceiveLineRequest;
import com.exodia.inventario.event.InventoryReceivedEvent;
import com.exodia.inventario.exception.*;
import com.exodia.inventario.repository.catalog.*;
import com.exodia.inventario.repository.core.*;
import com.exodia.inventario.repository.flow.*;
import com.exodia.inventario.service.core.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiveInventoryService {

    private final ContainerRepository containerRepository;
    private final ContainerStatusRepository containerStatusRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseLocationRepository locationRepository;
    private final UnitRepository unitRepository;
    private final CompanyRepository companyRepository;
    private final ReceptionRepository receptionRepository;
    private final ReceptionLineRepository receptionLineRepository;

    private final OperationService operationService;
    private final BarcodeService barcodeService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Reception receiveInventory(Long companyId, ReceiveInventoryRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new InventoryException("Empresa no encontrada"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new InventoryException("Bodega no encontrada"));

        ContainerStatus availableStatus = containerStatusRepository
                .findByCode(ContainerStatusCode.AVAILABLE.getCode())
                .orElseThrow();

        // Crear cabecera de recepción
        Reception reception = Reception.builder()
                .company(company)
                .receptionNumber(generateReceptionNumber(companyId))
                .warehouse(warehouse)
                .receptionType(request.getReceptionType())
                .sourceReferenceId(request.getSourceReferenceId())
                .supplierId(request.getSupplierId())
                .status("CONFIRMED")
                .comments(request.getComments())
                .build();
        reception = receptionRepository.save(reception);

        // Procesar cada línea
        for (ReceiveLineRequest line : request.getLines()) {
            processReceptionLine(company, warehouse, reception, line, availableStatus);
        }

        // Publicar evento
        eventPublisher.publishEvent(new InventoryReceivedEvent(this, reception));

        log.info("Recepción {} creada con {} líneas en bodega {}",
                reception.getReceptionNumber(), request.getLines().size(), warehouse.getCode());

        return reception;
    }

    private void processReceptionLine(
            Company company,
            Warehouse warehouse,
            Reception reception,
            ReceiveLineRequest line,
            ContainerStatus availableStatus
    ) {
        WarehouseLocation location = locationRepository.findById(line.getLocationId())
                .orElseThrow(() -> new InventoryException("Ubicación no encontrada"));

        Unit unit = unitRepository.findById(line.getUnitId())
                .orElseThrow(() -> new InventoryException("Unidad no encontrada"));

        Container container;
        boolean barcodeGenerated = false;
        boolean barcodeReused = false;

        // Determinar barcode
        String barcode = line.getBarcode();
        if (line.isGenerateBarcode() || barcode == null || barcode.isBlank()) {
            barcode = barcodeService.generateBarcode(company.getId());
            barcodeGenerated = true;
        }

        // ¿Intentar reutilizar?
        if (!barcodeGenerated && line.isAllowReuse()) {
            var existing = containerRepository
                    .findByCompanyIdAndBarcodeAndProductIdAndSupplierIdAndWarehouseIdAndUnitId(
                            company.getId(), barcode, line.getProductId(),
                            line.getSupplierId(), warehouse.getId(), unit.getId());

            if (existing.isPresent()) {
                container = existing.get();
                container.setUnitPrice(line.getUnitPrice());
                container.setLocation(location);
                container = containerRepository.save(container);
                barcodeReused = true;
            } else {
                container = createNewContainer(company, warehouse, location, unit,
                        availableStatus, barcode, line);
            }
        } else if (!barcodeGenerated) {
            // Barcode manual sin reutilización: validar que no exista
            if (containerRepository.existsByCompanyIdAndBarcode(company.getId(), barcode)) {
                throw new DuplicateBarcodeException(barcode);
            }
            container = createNewContainer(company, warehouse, location, unit,
                    availableStatus, barcode, line);
        } else {
            container = createNewContainer(company, warehouse, location, unit,
                    availableStatus, barcode, line);
        }

        // Crear operación positiva
        Operation operation = operationService.createOperation(
                container,
                OperationTypeCode.RECEPTION,
                line.getQuantity(),
                "Recepción " + reception.getReceptionNumber()
                    + " | Bodega: " + warehouse.getName()
                    + " | Ubicación: " + location.getName(),
                ReferenceType.RECEPTION,
                reception.getId(),
                null
        );

        // Crear línea de recepción
        ReceptionLine receptionLine = ReceptionLine.builder()
                .reception(reception)
                .container(container)
                .productId(line.getProductId())
                .unit(unit)
                .location(location)
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .lotNumber(line.getLotNumber())
                .expirationDate(line.getExpirationDate())
                .barcodeGenerated(barcodeGenerated)
                .barcodeReused(barcodeReused)
                .operation(operation)
                .build();
        receptionLineRepository.save(receptionLine);
    }

    private Container createNewContainer(
            Company company, Warehouse warehouse, WarehouseLocation location,
            Unit unit, ContainerStatus status, String barcode,
            ReceiveLineRequest line
    ) {
        Container container = Container.builder()
                .company(company)
                .barcode(barcode)
                .productId(line.getProductId())
                .supplierId(line.getSupplierId())
                .productSupplierId(line.getProductSupplierId())
                .unit(unit)
                .warehouse(warehouse)
                .location(location)
                .unitPrice(line.getUnitPrice())
                .lotNumber(line.getLotNumber())
                .expirationDate(line.getExpirationDate())
                .serialNumber(line.getSerialNumber())
                .status(status)
                .build();
        return containerRepository.save(container);
    }

    private String generateReceptionNumber(Long companyId) {
        // Implementar lógica de numeración: REC-YYYYMMDD-XXXX
        return "REC-" + java.time.LocalDate.now().toString().replace("-", "")
                + "-" + String.format("%04d", System.nanoTime() % 10000);
    }
}
```

---

## 10. Mapa de servicios y su responsabilidad

| Servicio | Responsabilidad | Operaciones que genera |
|----------|----------------|----------------------|
| `OperationService` | Crea toda operación en el kardex. Punto único. | Todas |
| `StockQueryService` | Consultas de stock. Solo lectura. | Ninguna |
| `BarcodeService` | Genera/valida barcodes. | Ninguna |
| `ContainerService` | CRUD de contenedores, cambio de estado. | Ninguna directa |
| `ReservationService` | Crea/libera/expira reservas de stock. | Ninguna directa |
| `ReceiveInventoryService` | Recepción: manual, compra, traslado, producción. | RECEPTION |
| `AdjustInventoryService` | Ajustes +/- y ajuste de precio. | ADJUSTMENT_IN, ADJUSTMENT_OUT, ADJUSTMENT_INFO |
| `MoveContainerService` | Movimientos entre ubicaciones y standby. | MOVE_OUT + MOVE_IN |
| `ConvertUnitService` | Conversión de unidades. | CONVERSION_OUT + CONVERSION_IN |
| `TransferByContainerService` | Traslado legacy por barcode. | TRANSFER_OUT + TRANSFER_IN |
| `TransferByProductService` | Traslado por producto con resolución FIFO. | TRANSFER_OUT + TRANSFER_IN |
| `PickingService` | Picking por requisición, venta o general. | PICKING |
| `PhysicalCountService` | Conteo físico y generación de ajustes. | PHYSICAL_COUNT_IN, PHYSICAL_COUNT_OUT |
| `ShrinkageService` | Merma manual/automática. | SHRINKAGE |
| `MinMaxService` | Recalcula stock vs. umbrales configurados. | Ninguna (solo lectura + alertas) |
| `CostValuationService` | Calcula costo promedio y genera snapshots. | Ninguna (solo lectura) |
| `InventoryLedgerService` | Reconstruye auxiliar contable entre fechas. | Ninguna (solo lectura) |
| `AuditService` | Registra cambios en catálogos y entidades críticas. | Ninguna (escribe en audit_log) |

---

## 11. Regla de oro: flujo de una operación de inventario

Toda operación que modifique stock sigue este flujo invariable:

```
1. Controller recibe request DTO (validado con @Valid)
        ↓
2. Service de flujo (ej: ReceiveInventoryService)
   a. Valida reglas de negocio
   b. Crea/actualiza Container si aplica
   c. Llama a OperationService.createOperation(...)
   d. Actualiza documentos de flujo (Reception, Transfer, etc.)
   e. Publica evento de dominio
        ↓
3. Listeners reaccionan al evento
   - MinMaxAlertListener recalcula min/max
   - AuditEventListener registra auditoría
   - AccountingIntegrationListener crea partida contable si aplica
        ↓
4. Response DTO regresa a Angular
```

**Nunca** se modifica stock desde un controller directamente.  
**Nunca** se hace SUM(quantity) fuera de StockQueryService o OperationRepository.  
**Nunca** se crea una Operation sin pasar por OperationService.

---

## 12. Configuración Spring Boot

### application.yml

```yaml
spring:
  application:
    name: exodia-inventory

  datasource:
    url: jdbc:postgresql://localhost:5432/exodia_inventario
    username: ${DB_USERNAME:exodia}
    password: ${DB_PASSWORD:exodia}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate   # Flyway maneja el schema
    open-in-view: false     # Siempre false en producción
    properties:
      hibernate:
        # Hibernate 7.x (Spring Boot 4) auto-detecta el dialecto; no hace falta configurarlo
        default_schema: public
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
        format_sql: false

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false

server:
  port: 8080
  servlet:
    context-path: /api

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

# Custom
inventory:
  barcode:
    default-prefix: INV
    padding-length: 8
  reservation:
    default-expiration-hours: 48
  min-max:
    recalculation-cron: "0 0 */4 * * *"  # cada 4 horas
  expiration:
    alert-days: 90
    check-cron: "0 0 6 * * *"            # diario a las 6am
```

---

## 13. Resumen de archivos por capa

| Capa | Paquete | Cantidad estimada | Responsabilidad |
|------|---------|-------------------|-----------------|
| Entidades | `domain/entity/` | 30 clases | Mapeo JPA a las 30 tablas |
| Enums | `domain/enums/` | 13 enums | Valores tipados del dominio |
| Base | `domain/base/` | 2 clases | Herencia de entidades |
| Repositorios | `repository/` | 27 interfaces | Acceso a datos + queries nativos |
| Proyecciones | `repository/projection/` | 3 interfaces | DTOs para queries nativas |
| Servicios | `service/` | 18 clases | Lógica de negocio pura |
| DTOs request | `dto/request/` | ~25 records | Entrada desde Angular |
| DTOs response | `dto/response/` | ~20 records | Salida hacia Angular |
| Controllers | `controller/` | 12 clases | Endpoints REST |
| Mappers | `mapper/` | 12 interfaces | MapStruct entity↔DTO |
| Excepciones | `exception/` | 8 clases + 1 handler | Manejo de errores de dominio |
| Eventos | `event/` | 7 clases | Comunicación asíncrona interna |
| Listeners | `listener/` | 3 clases | Reacción a eventos |
| Schedulers | `scheduler/` | 4 clases | Tareas programadas |
| Integraciones | `integration/` | 4 clases | Conexión con otros módulos |
| Config | `config/` | 4 clases | Configuración Spring |

**Total estimado: ~175 archivos Java**
