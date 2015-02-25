'''
Created on Feb 25, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgEffectiveDate(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to an Invalid Effective Date. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgEffectiveDate, self).__init__('Invalid Effective Date', 
            self._EXPECTED_RESULT, 'MSG_INVALID_EFFECTIVE_DATE', False)
        