'''
Created on Mar 4, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class NonWarnTriggerAlreadyExpired(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Validation Fails due to Message Expiration.'

    def __init__(self):
        '''
        Constructor
        '''
        super(NonWarnTriggerAlreadyExpired, self).__init__('Non-Warning Trigger Already Expired', 
            self._EXPECTED_RESULT, 'EXPIRED_TRIGGER_NOT_WARN', False)