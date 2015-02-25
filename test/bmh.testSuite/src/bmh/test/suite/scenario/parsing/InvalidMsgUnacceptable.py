'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgUnacceptable(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Validation fails due to the existence of unacceptable words.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgUnacceptable, self).__init__('Invalid Message Unacceptable Words', 
            self._EXPECTED_RESULT, 'MSG_INVALID_UNACCEPTABLE', False)