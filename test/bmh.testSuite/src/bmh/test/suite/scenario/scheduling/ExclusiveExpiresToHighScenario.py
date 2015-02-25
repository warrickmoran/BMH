'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

class ExclusiveExpiresToHighScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(ExclusiveExpiresToHighScenario, self).__init__('Trigger Exclusive Scenario', 
            'The Exclusive suite should play until the message named trigger_exclusive expires.\nThen it should fall back to the high playlist until the message named trigger_high expires.')
        
    def _prepareInputs(self, dataDirectory):
        self._copyMessageAndConfirm(dataDirectory, 'trigger_exclusive', 'An Exclusive Suite should trigger.')
        self._expireMinutes += 1
        self._copySchedulingMessage(dataDirectory, 'trigger_high')
