'''
Created on May 19, 2015

@author: bkowal
'''

import AbstractSchedulingScenario
import datetime

class SixInterruptsScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(SixInterruptsScenario, self).__init__('Six Interrupts Scenario', 
            'Six interrupt messages will be played on PABE approximately 1 minute apart. The messages are, in order: ' \
            'interrupt_high_suite, interrupt_high_suite2, interrupt_high_suite3, ' \
            'interrupt_exclusive_suite, interrupt_exclusive_suite2, interrupt_exclusive_suite3. ' \
            'As interrupts finish, the message in the highest priority suite will be broadcast ' \
            'until expiration. Use Broadcast Cycle to verify.')
        
    def _updateMessageHeader(self, content, time = None):
        if time is None:
            time = self._nextEffectiveTime
            self._nextEffectiveTime = time + datetime.timedelta(minutes=1)
        return super(SixInterruptsScenario, self)._updateMessageHeader(content, time)
    
    def _prepareInputs(self, dataDirectory):
        self._nextEffectiveTime = datetime.datetime.utcnow() + datetime.timedelta(minutes=1)
        self._expireMinutes = 12
        self._copySchedulingMessage(dataDirectory, 'interrupt_high_suite')
        self._expireMinutes = 10
        self._copySchedulingMessage(dataDirectory, 'interrupt_high_suite2')
        self._expireMinutes = 8
        self._copySchedulingMessage(dataDirectory, 'interrupt_high_suite3')
        self._expireMinutes = 6
        self._copySchedulingMessage(dataDirectory, 'interrupt_exclusive_suite')
        self._expireMinutes = 4
        self._copySchedulingMessage(dataDirectory, 'interrupt_exclusive_suite2')
        self._expireMinutes = 2
        self._copySchedulingMessage(dataDirectory, 'interrupt_exclusive_suite3')