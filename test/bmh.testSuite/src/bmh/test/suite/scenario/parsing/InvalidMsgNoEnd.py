'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgNoEnd(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to No End Message indicator. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgNoEnd, self).__init__('No End Message indicator', 
            self._EXPECTED_RESULT, 'MSG_NO_END', False)