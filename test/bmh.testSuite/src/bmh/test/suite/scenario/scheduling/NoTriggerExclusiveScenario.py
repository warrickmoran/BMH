'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

import time

class NoTriggerExclusiveScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(NoTriggerExclusiveScenario, self).__init__('No Trigger Exclusive Scenario', 
            'An exclusive suite should now start playing with at least two messages named trigger_exclusive and notrigger_exclusive.')
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'notrigger_exclusive')
        print 'A message has been created named notrigger_exclusive, this message should NOT cause an exclusive suite to play.'
        print 'Please wait 30 seconds to ensure an exclusive suite does not play.'
        time.sleep(30)
        self._copySchedulingMessage(dataDirectory, 'trigger_exclusive')