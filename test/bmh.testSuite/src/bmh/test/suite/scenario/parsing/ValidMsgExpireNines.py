'''
Created on Mar 4, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class ValidMsgExpireNines(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Validation succeeds. The expirationtime column in the input_msg table indicates that the message never expires.'

    def __init__(self):
        '''
        Constructor
        '''
        super(ValidMsgExpireNines, self).__init__('Valid Message 9s Expiration', 
            self._EXPECTED_RESULT, 'MSG_EXPIRES_9', False)