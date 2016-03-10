DROP FUNCTION IF EXISTS dr5421_natl_dict_updates();

CREATE FUNCTION dr5421_natl_dict_updates() RETURNS BOOLEAN AS $$
DECLARE
BEGIN
   DELETE FROM word WHERE word = 'winds' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'wind up' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'wind up to' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'winds up' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'winds up to' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'wind down' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'wind down to' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'winds down' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'winds down to' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'lead' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'record' AND dictionary = 'paul-nat';
   DELETE FROM word WHERE word = 'objects' AND dictionary = 'paul-nat';
   
   INSERT INTO word (id, substitute, word, dictionary) 
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph=" W IH1 N D S ">  </phoneme>', 
       'winds', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary)
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph="  W AY2 N D ">  </phoneme><break strength="none"/>up',
       'wind up', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary)
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph="  W IH1 N D ">  </phoneme><break strength="none"/>up to',
       'wind up to', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary)
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph="  W AY2 N D S ">  </phoneme><break strength="none"/>up',
       'winds up', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary)
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph="  W IH1 N D S ">  </phoneme><break strength="none"/>up to',
       'winds up to', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary)
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph="  W AY2 N D ">  </phoneme><break strength="none"/>down',
       'wind down', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary)
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph="  W IH1 N D ">  </phoneme><break strength="none"/>down to',
       'wind down to', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary)
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph="  W AY2 N D S ">  </phoneme><break strength="none"/>down',
       'winds down', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary)
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph="  W IH1 N D S ">  </phoneme><break strength="none"/>down to',
       'winds down to', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary) 
       VALUES (nextval('word_seq'), ' <phoneme alphabet="x-cmu" ph=" L IY1 D ">  </phoneme>', 
       'lead', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary) 
       VALUES (nextval('word_seq'), 'rek erd', 'record', 'paul-nat');
   INSERT INTO word (id, substitute, word, dictionary) 
       VALUES (nextval('word_seq'), 'o bjects', 'objects', 'paul-nat');

   RETURN TRUE;
END
$$ LANGUAGE plpgsql;

BEGIN TRANSACTION;
   SELECT dr5421_natl_dict_updates();
COMMIT;

DROP FUNCTION dr5421_natl_dict_updates();

