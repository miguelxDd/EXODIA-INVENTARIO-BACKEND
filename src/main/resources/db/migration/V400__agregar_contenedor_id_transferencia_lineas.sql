-- V400: Agregar contenedor_id a inv_transferencia_lineas para transferencias POR_CONTENEDOR
ALTER TABLE inv_transferencia_lineas ADD COLUMN contenedor_id BIGINT REFERENCES inv_contenedores(id);
