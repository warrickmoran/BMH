'''
Created on Feb 8, 2016

@author: bkowal

This delta script has been created for DR #5308. This delta script will
need to be ran on px1 or px2 after the BMH upgrade.
'''

import re
import shutil
import os.path

MESSAGE_FIELDS = [ '<bmhMessage ', '<name>', '<start>', '<messageType>', '<alertTone>', \
                  '<toneBlackoutEnabled>', '<playCount>', '<playedSameTone>' \
                  '<playedAlertTone>', '<confirm>', '<watch>', '<warning>', '</bmhMessage>' ];

MESSAGES_DIR_NAME = 'messages'
BMH_PLAYLIST_DIR = os.path.join(os.sep + 'awips2', 'bmh', 'data', 'playlist')
MESSAGE_FILE_PATTERN = re.compile('^([0-9]+)_[0-9]+\.xml$')

def isMessageField(fileLine):
    for field in MESSAGE_FIELDS:
        if (fileLine.startswith(field)):
            return True
    return False

for transmitterDir in os.listdir(BMH_PLAYLIST_DIR):
    transmitterMessagesDir = os.path.join(BMH_PLAYLIST_DIR, transmitterDir, MESSAGES_DIR_NAME)
    if os.path.exists(transmitterMessagesDir):
        for messageFile in os.listdir(transmitterMessagesDir):
            if (MESSAGE_FILE_PATTERN.match(messageFile)):
                m = MESSAGE_FILE_PATTERN.match(messageFile) 
                messageId = m.group(1)
                
                fullMessageFilePath = os.path.join(transmitterMessagesDir, messageFile)
                
                updateMessageFilePath = os.path.join(transmitterMessagesDir, messageId + '.xml')
                altMetadataFilePath = fullMessageFilePath + '.upd'
                
                originalFile = open(fullMessageFilePath, 'r')
                updatedFile = open(updateMessageFilePath, 'w')
                altFile = open(altMetadataFilePath, 'w')
                updated = False
                
                for line in originalFile:
                    if isMessageField(line.lstrip()):
                        updatedFile.write(line)
                    
                    if line.lstrip().startswith('<bmhMessage '):
                        # this is an older version of the timestamped bmh message file.
                        # however, it will not be necessary to change the root tag to
                        # <bmhMessageMetadata> because the JAXB converter in the dac
                        # transmit process does not care which root tag is in use.
                        updated = True
                    altFile.write(line)
                
                originalFile.close()
                updatedFile.close()
                altFile.close()
                if updated == False:
                    os.remove(updateMessageFilePath)
                    os.remove(altMetadataFilePath)
                    print 'No file updates necessary for: ' + fullMessageFilePath
                else:
                    shutil.move(altMetadataFilePath, fullMessageFilePath)
                    print 'Completed conversion of file: ' + fullMessageFilePath