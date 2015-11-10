-- This script should not be manually executed. This script will be executed
-- automatically by the DR5092_transform_data.sh delta script.

DROP FUNCTION IF EXISTS dr5092_channel_xfer_pre();
DROP FUNCTION IF EXISTS dr5092_channel_xfer();
DROP FUNCTION IF EXISTS dr5092_channel_xfer_RUN();

CREATE FUNCTION dr5092_channel_xfer_pre() RETURNS BOOLEAN AS $$
DECLARE
   count integer; 
BEGIN
   -- Determine if the dac ports table exists.
   SELECT COUNT(*) FROM information_schema.tables
      WHERE table_catalog LIKE 'bmh%' AND
      table_name = 'dac_ports' INTO count;
   if count = 0 THEN
      RETURN FALSE;
   END IF;
   RETURN TRUE;
END
$$ LANGUAGE plpgsql;

CREATE FUNCTION dr5092_channel_xfer() RETURNS BOOLEAN AS $$
DECLARE
   prow dac_ports%ROWTYPE;

   all_dac_ports_cursor CURSOR FOR SELECT p.* FROM dac_ports p
      ORDER BY dac_id, "position";
BEGIN
   DROP TABLE IF EXISTS dac_channel;

   CREATE TABLE dac_channel
   (
      channel integer NOT NULL,
      level numeric(3,1) NOT NULL,
      port integer NOT NULL,
      dac_id integer NOT NULL,
      CONSTRAINT dac_channel_pkey PRIMARY KEY (channel, dac_id),
      CONSTRAINT fk_dac_channel_to_dac FOREIGN KEY (dac_id)
         REFERENCES dac_address (id) MATCH SIMPLE
         ON UPDATE NO ACTION ON DELETE NO ACTION,
      CONSTRAINT uk_port UNIQUE (port)
    )
   WITH (
      OIDS=FALSE
   );
   ALTER TABLE dac_channel
      OWNER TO awips;

   FOR prow IN all_dac_ports_cursor LOOP
      INSERT INTO dac_channel VALUES (prow.position, 6.0, prow.dataport, prow.dac_id);
   END LOOP;

   RETURN true;
END
$$ LANGUAGE plpgsql;

CREATE FUNCTION dr5092_channel_xfer_RUN() RETURNS BOOLEAN AS $$
DECLARE
   pre boolean;
BEGIN
   SELECT dr5092_channel_xfer_pre() INTO pre;
   IF pre = false THEN
      RETURN TRUE;
   END IF;

   PERFORM dr5092_channel_xfer();
   RETURN TRUE;
END
$$ LANGUAGE plpgsql;

BEGIN TRANSACTION;
   SELECT dr5092_channel_xfer_RUN();
COMMIT;

DROP FUNCTION dr5092_channel_xfer_pre();
DROP FUNCTION dr5092_channel_xfer();
DROP FUNCTION dr5092_channel_xfer_RUN();
