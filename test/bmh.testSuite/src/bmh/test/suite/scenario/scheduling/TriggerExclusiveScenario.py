'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

class TriggerExclusiveScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(TriggerExclusiveScenario, self).__init__('Trigger Exclusive Scenario', 
            'An Exclusive Suite should trigger.')
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'trigger_exclusive')
