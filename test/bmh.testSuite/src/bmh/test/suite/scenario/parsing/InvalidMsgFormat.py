'''
Created on Feb 25, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgFormat(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to an Unhandled Message Format. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgFormat, self).__init__('Invalid Message Format', 
            self._EXPECTED_RESULT, 'MSG_INVALID_FORMAT', False)