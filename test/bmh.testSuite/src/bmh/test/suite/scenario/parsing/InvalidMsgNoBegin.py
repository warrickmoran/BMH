'''
Created on Mar 4, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgNoBegin(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to No Start Message Indicator. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgNoBegin, self).__init__('No Start Message Indicator', 
            self._EXPECTED_RESULT, 'MSG_NO_BEGIN', False)