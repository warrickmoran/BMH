'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgTone(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to Unrecognized Alert Tone Character. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgTone, self).__init__('Invalid Message Tone', 
            self._EXPECTED_RESULT, 'MSG_INVALID_TONE', False)