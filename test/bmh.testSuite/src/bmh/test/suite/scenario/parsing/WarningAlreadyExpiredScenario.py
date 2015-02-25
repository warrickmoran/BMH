'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class WarningAlreadyExpiredScenario(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = ('Validation Fails due to Message Expiration. ' 
        'Notification that an Expired Warning has never been broadcast is displayed in AlertViz.')

    def __init__(self):
        '''
        Constructor
        '''
        super(WarningAlreadyExpiredScenario, self).__init__('Warning Already Expired', 
            self._EXPECTED_RESULT, 'EXPIRED_MSG_WARN', False)
        