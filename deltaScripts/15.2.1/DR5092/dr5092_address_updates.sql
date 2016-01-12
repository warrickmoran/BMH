-- This script should not be manually executed. This script will be executed
-- automatically by the DR5092_transform_data.sh delta script.

DROP FUNCTION IF EXISTS dr5092_address_update();

CREATE FUNCTION dr5092_address_update() RETURNS BOOLEAN AS $$
DECLARE
   count integer;
BEGIN
   -- If we wanted to be thorough, we can also verify the column
   -- datatype and attributes. But, ideally custom columns are
   -- not being added to the databases.

   -- Determine if the 'netmask' column already exists.
   SELECT COUNT(*) FROM information_schema.columns
    WHERE table_catalog LIKE 'bmh%' AND table_name = 'dac_address' 
      AND column_name = 'netmask' INTO count;
   IF count = 0 THEN
      ALTER TABLE IF EXISTS dac_address 
        ADD column netmask CHARACTER VARYING(39) NOT NULL DEFAULT '255.255.255.0';
   END IF;

   -- Determine if the 'gateway' column already exists.
   SELECT COUNT(*) FROM information_schema.columns
    WHERE table_catalog LIKE 'bmh%' AND table_name = 'dac_address' 
      AND column_name = 'gateway' INTO count;
   IF count = 0 THEN
      ALTER TABLE IF EXISTS dac_address 
        ADD column gateway CHARACTER VARYING(39) NOT NULL DEFAULT '10.2.69.254';
   END IF;

   -- Determine if the 'broadcastBuffer' column already exists.
   SELECT COUNT(*) FROM information_schema.columns
    WHERE table_catalog LIKE 'bmh%' AND table_name = 'dac_address' 
      AND column_name = 'broadcastbuffer' INTO count;
   IF count = 0 THEN
      ALTER TABLE IF EXISTS dac_address 
        ADD column broadcastbuffer integer NOT NULL DEFAULT 5;
   END IF;

   RETURN TRUE;
END
$$ LANGUAGE plpgsql;

BEGIN TRANSACTION;
   SELECT dr5092_address_update();
COMMIT;

DROP FUNCTION dr5092_address_update();
