'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgActive(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to Unrecognized Active/Inactive Character. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgActive, self).__init__('Invalid Message Active/Inactive Character', 
            self._EXPECTED_RESULT, 'MSG_INVALID_ACTIVE', False)