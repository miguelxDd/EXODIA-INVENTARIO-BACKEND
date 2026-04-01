ALTER TABLE inv_picking_lineas
    ADD COLUMN contenedor_solicitado_id BIGINT REFERENCES inv_contenedores(id);
