'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgConfirm(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to Unrecognized Confirmation Character. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgConfirm, self).__init__('Unrecognized Confirmation Character', 
            self._EXPECTED_RESULT, 'MSG_INVALID_CONFIRM', False)