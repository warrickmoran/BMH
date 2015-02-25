'''
Created on Mar 4, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class WarnTriggerAlreadyExpired(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = ('Validation Fails due to Message Expiration. ' 
        'Notification that an Expired Warning has never been broadcast is displayed in AlertViz.')

    def __init__(self):
        '''
        Constructor
        '''
        super(WarnTriggerAlreadyExpired, self).__init__('Warning Trigger Already Expired', 
            self._EXPECTED_RESULT, 'EXPIRED_TRIGGER_WARN', False)