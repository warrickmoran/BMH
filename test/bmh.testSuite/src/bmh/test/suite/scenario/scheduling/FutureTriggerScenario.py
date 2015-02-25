'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario
import datetime

class FutureTriggerScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(FutureTriggerScenario, self).__init__('Future Trigger Scenario', 
            'A message named high_trigger should cause the high suite to play in two minutes.')

    def _updateMessageHeader(self, content, time = None):
        if time is None:
            time = datetime.datetime.utcnow()
            time = time + datetime.timedelta(minutes=2)
        return super(FutureTriggerScenario, self)._updateMessageHeader(content, time)
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'trigger_high')
