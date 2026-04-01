-- Unique constraint para evitar configs duplicadas activas.
-- Usa COALESCE para tratar bodega_id NULL como 0 y producto_id NULL como 0
-- ya que UNIQUE no considera NULLs como iguales.
CREATE UNIQUE INDEX uq_config_merma_empresa_producto_bodega_activo
    ON inv_config_merma (empresa_id, COALESCE(producto_id, 0), COALESCE(bodega_id, 0))
    WHERE activo = true;
