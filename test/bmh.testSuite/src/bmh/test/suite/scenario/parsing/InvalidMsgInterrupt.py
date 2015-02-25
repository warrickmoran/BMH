'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgInterrupt(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to Unrecognized Interrupt Flag Character. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgInterrupt, self).__init__('Unrecognized Interrupt Character', 
            self._EXPECTED_RESULT, 'MSG_INVALID_INTERRUPT', False)