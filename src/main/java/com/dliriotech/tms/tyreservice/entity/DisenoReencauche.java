package com.dliriotech.tms.tyreservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("disenos_reencauche")
public class DisenoReencauche {
    @Id
    private Integer id;

    @Column("nombre_diseno")
    private String nombreDiseno;

    @Column("proveedor_reencauche")
    private String proveedorReencauche;
}