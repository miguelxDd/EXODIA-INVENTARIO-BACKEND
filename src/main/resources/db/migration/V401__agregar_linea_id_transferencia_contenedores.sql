-- V401: Agregar transferencia_linea_id a inv_transferencia_contenedores para vincular contenedor con su linea
ALTER TABLE inv_transferencia_contenedores ADD COLUMN transferencia_linea_id BIGINT REFERENCES inv_transferencia_lineas(id);
