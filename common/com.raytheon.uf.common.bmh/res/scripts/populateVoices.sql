/*
 * This voice should ALWAYS be available in a BMH system. If it does not, bmh edex initialization will fail.
 */
insert into tts_voice (voiceNumber, voiceName, language, male) values (101, 'Paul','ENGLISH', true);
