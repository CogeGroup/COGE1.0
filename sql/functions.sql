DROP FUNCTION costo;
DROP FUNCTION ricavo;
DROP FUNCTION margine;

CREATE FUNCTION costo(costoGiornaliero FLOAT, oreLavorate INT)
RETURNS FLOAT DETERMINISTIC
RETURN oreLavorate * costoGiornaliero / 8;

CREATE FUNCTION ricavo(tariffaGiornaliera FLOAT, oreLavorate INT)
RETURNS FLOAT DETERMINISTIC
RETURN oreLavorate * tariffaGiornaliera / 8;

CREATE FUNCTION margine(costo FLOAT, ricavo FLOAT)
RETURNS FLOAT DETERMINISTIC
RETURN (ricavo - costo) / ricavo;

DELIMITER $$

DROP FUNCTION IF EXISTS `coge`.`costoAggiuntivo` $$
CREATE FUNCTION `coge`.`costoAggiuntivo` (anno INT, mese INT, idCommessa INT) RETURNS INT
BEGIN
DECLARE tot INT DEFAULT 0;
DECLARE no_data INT DEFAULT 0;

BEGIN

  DECLARE EXIT HANDLER FOR 1329 SET no_data=1;

  SELECT DISTINCT(importo) into tot
		FROM CostoCommessa
		WHERE commessa_IdCommessa = idCommessa
		AND concat(anno, lpad(mese, 2, 0)) = date_format(data, '%Y%m');

END;

IF no_data=1 THEN
  RETURN 0;
END IF;

RETURN tot;

END $$

DELIMITER ;
