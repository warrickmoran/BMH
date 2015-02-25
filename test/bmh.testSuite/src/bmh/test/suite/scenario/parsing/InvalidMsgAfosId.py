'''
Created on Feb 25, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgAfosId(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to an Invalid Afosid. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgAfosId, self).__init__('Invalid Afosid', 
            self._EXPECTED_RESULT, 'MSG_INVALID_AFOSID', False)
        