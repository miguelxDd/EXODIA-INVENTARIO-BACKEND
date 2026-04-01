ALTER TABLE inv_registros_merma
    ADD COLUMN motivo_codigo VARCHAR(40);

UPDATE inv_registros_merma
SET motivo_codigo = CASE
    WHEN tipo_merma = 'AUTOMATICA' THEN 'RECEPCION'
    ELSE 'OTRO'
END
WHERE motivo_codigo IS NULL;

ALTER TABLE inv_registros_merma
    ALTER COLUMN motivo_codigo SET NOT NULL;
