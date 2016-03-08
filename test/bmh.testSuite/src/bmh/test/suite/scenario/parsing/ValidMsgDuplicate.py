'''
Created on Feb 26, 2015

@author: bkowal
'''

import datetime
import os

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class ValidMsgDuplicate(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Message duplicate1 is successfully validated. Message duplicate2' \
        ' replaces duplicate1 because it is a DUPLICATE message. Watch Broadcast Cycle Dialog to verify.'

    def __init__(self):
        '''
        Constructor
        '''
        super(ValidMsgDuplicate, self).__init__('Valid Message Duplicate', 
            self._EXPECTED_RESULT, None)
        
    def _prepareInputs(self, dataDirectory):
        self.headerTime = datetime.datetime.utcnow()
        self._expireMinutes = 15
        
        # we want to ensure that everything is based on the same date.
        self._copyMessageToDestination(os.path.join(dataDirectory, 'parsing', 'duplicate1'))
        allowedInputs = ['y', 'n']
        userResponse = None
        
        while (userResponse not in allowedInputs):
            userResponse = raw_input('duplicate1 is successfully parsed. (y | n): ')
        if userResponse == 'y':
            self._copyMessageToDestination(os.path.join(dataDirectory, 'parsing', 'duplicate2'))
        
    def _copyMessageToDestination(self, fileToCopy):
        with open(fileToCopy, 'r') as content_file:
            content = content_file.read()
            
        content = self._updateMessageHeader(content, self.headerTime)
        dest = os.path.join(self._destinationDirectory, os.path.basename(fileToCopy))
        with open(dest, 'w') as content_file:
            content_file.write(content)
        
