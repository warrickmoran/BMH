-- This script should not be manually executed. This script will be executed
-- automatically by the DR6078_input_msg_origin_update.sh delta script.

DROP FUNCTION IF EXISTS dr6078_input_msg_origin_update();

CREATE FUNCTION dr6078_input_msg_origin_update() RETURNS BOOLEAN AS $$
DECLARE
   count integer;
BEGIN
   -- If we wanted to be thorough, we can also verify the column
   -- datatype and attributes. But, ideally custom columns are
   -- not being added to the databases.

   -- Determine if the 'origin' column already exists.
   SELECT COUNT(*) FROM information_schema.columns
    WHERE table_catalog LIKE 'bmh%' AND table_name = 'input_msg'
      AND column_name = 'origin' INTO count;
   IF count = 0 THEN
      ALTER TABLE public.input_msg
        ADD COLUMN origin character varying(8) NOT NULL DEFAULT 'UNKNOWN';
      UPDATE public.input_msg SET origin = 'DMOMSG' WHERE substring(afosid, 4, 3) = 'DMO';
   END IF;

   RETURN TRUE;
END
$$ LANGUAGE plpgsql;

BEGIN TRANSACTION;
   SELECT dr6078_input_msg_origin_update();
COMMIT;

DROP FUNCTION dr6078_input_msg_origin_update();
