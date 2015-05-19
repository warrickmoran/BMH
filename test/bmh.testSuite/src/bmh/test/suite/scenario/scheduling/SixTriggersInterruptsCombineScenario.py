'''
Created on May 19, 2015

@author: bkowal
'''

import AbstractSchedulingScenario
import datetime

class SixTriggersInterruptsCombineScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(SixTriggersInterruptsCombineScenario, self).__init__('Six Triggers/Interrupts Combination Scenario', 
            'A combination of six interrupt/trigger messages will be played on PABE approximately 1 minute apart. The messages are, in order: ' \
            'interrupt_high_suite, trigger_high_suite2, interrupt_high_suite3, ' \
            'trigger_exclusive, interrupt_exclusive_suite, trigger_exclusive_suite3. ' \
            'As interrupts finish, the message in the highest priority suite will be broadcast ' \
            'until expiration. Use Broadcast Cycle to verify.')
        
    def _updateMessageHeader(self, content, time = None):
        if time is None:
            time = self._nextEffectiveTime
            self._nextEffectiveTime = time + datetime.timedelta(minutes=1)
        return super(SixTriggersInterruptsCombineScenario, self)._updateMessageHeader(content, time)
    
    def _prepareInputs(self, dataDirectory):
        self._nextEffectiveTime = datetime.datetime.utcnow() + datetime.timedelta(minutes=1)
        self._expireMinutes = 12
        self._copySchedulingMessage(dataDirectory, 'interrupt_high_suite')
        self._expireMinutes = 10
        self._copySchedulingMessage(dataDirectory, 'trigger_high_suite2')
        self._expireMinutes = 8
        self._copySchedulingMessage(dataDirectory, 'interrupt_high_suite3')
        self._expireMinutes = 5
        self._copySchedulingMessage(dataDirectory, 'trigger_exclusive')
        self._expireMinutes = 5
        self._copySchedulingMessage(dataDirectory, 'interrupt_exclusive_suite')
        self._expireMinutes = 2
        self._copySchedulingMessage(dataDirectory, 'trigger_exclusive_suite3')