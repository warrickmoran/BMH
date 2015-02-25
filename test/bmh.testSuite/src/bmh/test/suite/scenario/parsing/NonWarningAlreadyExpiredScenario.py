'''
Created on Feb 25, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class NonWarningAlreadyExpiredScenario(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Validation Fails due to Message Expiration.'

    def __init__(self):
        '''
        Constructor
        '''
        
        # No point in updating the file header because we want the input to be expired.
        super(NonWarningAlreadyExpiredScenario, self).__init__('Non-Warning Already Expired', 
            self._EXPECTED_RESULT, 'EXPIRED_MSG_NOT_WARN', False)