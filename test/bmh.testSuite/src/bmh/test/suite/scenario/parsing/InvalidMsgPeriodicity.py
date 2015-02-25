'''
Created on Feb 25, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgPeriodicity(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to an Invalid Periodicity. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgPeriodicity, self).__init__('Invalid Periodicity', 
            self._EXPECTED_RESULT, 'MSG_INVALID_PERIODICITY', False)