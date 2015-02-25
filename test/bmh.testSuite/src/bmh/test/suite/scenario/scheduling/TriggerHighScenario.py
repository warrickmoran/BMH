'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

class TriggerHighScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(TriggerHighScenario, self).__init__('Trigger High Scenario', 
            'A High Suite should trigger.')
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'trigger_high')
