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
