-- This script should not be manually executed. This script will be executed
-- automatically by the updateTransmitterGroups.sh delta script.

DROP FUNCTION IF EXISTS dr5068_amplitude_update();

CREATE FUNCTION dr5068_amplitude_update() RETURNS BOOLEAN AS $$
DECLARE
   count integer;
BEGIN
   -- Determine if 'audiodbtarget' column still exists
   SELECT COUNT(*) FROM information_schema.columns
    WHERE table_catalog LIKE 'bmh%' AND table_name = 'transmitter_group' 
      AND column_name = 'audiodbtarget' INTO count;
   IF count = 1 THEN
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS audioamplitude;
      ALTER TABLE transmitter_group 
        ADD COLUMN audioamplitude smallint NOT NULL DEFAULT 20676;
      UPDATE transmitter_group SET audioamplitude = ROUND( POW(10.0, audiodbtarget / 20.0) * 32767);
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS audiodbtarget;
   END IF;

   -- Determine if 'alertdbtarget' column still exists
   SELECT COUNT(*) FROM information_schema.columns
    WHERE table_catalog LIKE 'bmh%' AND table_name = 'transmitter_group' 
      AND column_name = 'alertdbtarget' INTO count;
   IF count = 1 THEN
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS alertamplitude;
      ALTER TABLE transmitter_group 
        ADD COLUMN alertamplitude smallint NOT NULL DEFAULT 12183;
      UPDATE transmitter_group SET alertamplitude = ROUND( POW(10.0, alertdbtarget / 20.0) * 32767);
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS alertdbtarget;
   END IF;

   -- Determine if 'samedbtarget' column still exists
   SELECT COUNT(*) FROM information_schema.columns
    WHERE table_catalog LIKE 'bmh%' AND table_name = 'transmitter_group' 
      AND column_name = 'samedbtarget' INTO count;
   IF count = 1 THEN
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS sameamplitude;
      ALTER TABLE transmitter_group 
        ADD COLUMN sameamplitude smallint NOT NULL DEFAULT 7198;
      UPDATE transmitter_group SET sameamplitude = ROUND( POW(10.0, samedbtarget / 20.0) * 32767);
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS samedbtarget;
   END IF;

   -- Determine if 'transferhighdbtarget' column still exists
   SELECT COUNT(*) FROM information_schema.columns
    WHERE table_catalog LIKE 'bmh%' AND table_name = 'transmitter_group' 
      AND column_name = 'transferhighdbtarget' INTO count;
   IF count = 1 THEN
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS transferhighamplitude;
      ALTER TABLE transmitter_group 
        ADD COLUMN transferhighamplitude smallint NOT NULL DEFAULT 11298;
      UPDATE transmitter_group SET transferhighamplitude = ROUND( POW(10.0, transferhighdbtarget / 20.0) * 32767);
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS transferhighdbtarget;
   END IF;

   -- Determine if 'transferlowdbtarget' column still exists
   SELECT COUNT(*) FROM information_schema.columns
    WHERE table_catalog LIKE 'bmh%' AND table_name = 'transmitter_group' 
      AND column_name = 'transferlowdbtarget' INTO count;
   IF count = 1 THEN
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS transferlowamplitude;
      ALTER TABLE transmitter_group 
        ADD COLUMN transferlowamplitude smallint NOT NULL DEFAULT 11829;
      UPDATE transmitter_group SET transferlowamplitude = ROUND( POW(10.0, transferlowdbtarget / 20.0) * 32767);
      ALTER TABLE transmitter_group DROP COLUMN IF EXISTS transferlowdbtarget;
   END IF;

   RETURN TRUE;
END
$$ LANGUAGE plpgsql;

BEGIN TRANSACTION;
   SELECT dr5068_amplitude_update();
COMMIT;

DROP FUNCTION dr5068_amplitude_update();

