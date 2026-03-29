-- V402: Agregar cantidad_recibida a inv_transferencia_contenedores para tracking de recepcion parcial
ALTER TABLE inv_transferencia_contenedores ADD COLUMN cantidad_recibida NUMERIC(18,6) DEFAULT 0;
