'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgExpireDate(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to Invalid Expiration Date. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgExpireDate, self).__init__('Invalid Expiration Date', 
            self._EXPECTED_RESULT, 'MSG_INVALID_EXPIRE_DATE', False)