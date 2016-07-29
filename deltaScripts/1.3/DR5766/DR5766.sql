-- DR 5766 - BMH 1.3
DROP FUNCTION IF EXISTS dr5766_updates();
CREATE FUNCTION dr5766_updates() RETURNS boolean AS $$
DECLARE
	_tablesToUpdate text array;
	_tablesToUpdateTotalCount int;
	_currentTableUpdate text;
	_tablesCounter integer;
	_colCount integer;
BEGIN
	_tablesToUpdate := '{ msg_type, input_msg, static_msg_type }';
	_tablesCounter := 0;
	SELECT array_length(_tablesToUpdate, 1) INTO _tablesToUpdateTotalCount;
	LOOP
		_tablesCounter := _tablesCounter + 1;

		_currentTableUpdate := _tablesToUpdate[_tablesCounter];
		SELECT COUNT(*) FROM information_schema.columns 
			WHERE column_name = 'cycles' AND table_name = _currentTableUpdate
			INTO _colCount; 

		-- column addition
		IF _colCount = 0 THEN
			EXECUTE 'ALTER TABLE IF EXISTS ' || _currentTableUpdate ||
				' ADD COLUMN cycles integer';
		END IF;

		-- constraints addition
		EXECUTE 'ALTER TABLE ' || _currentTableUpdate ||
			' DROP CONSTRAINT IF EXISTS chk_' || _currentTableUpdate || '_cycles';

		EXECUTE 'ALTER TABLE ' || _currentTableUpdate ||
			' ADD CONSTRAINT chk_' || _currentTableUpdate || '_cycles' ||
			' CHECK (cycles IS NULL OR (cycles >= 2 AND cycles <= 100))';

		IF _tablesCounter = _tablesToUpdateTotalCount THEN
			EXIT;
		END IF;
	END LOOP;

	RETURN true;
END;
$$ LANGUAGE plpgsql;
SELECT dr5766_updates();
