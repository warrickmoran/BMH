'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario
import datetime

class SixTriggersScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(SixTriggersScenario, self).__init__('Six Triggers Scenario', 
            'A High suite will trigger. After a minute a second high suite will trigger. Then a third.\n' + 
            'A series of three exclusive suites will trigger in a similar progression.\n' + 
            'Then each suite will expire and should fall back to the previous suite.')

    def _updateMessageHeader(self, content, time = None):
        if time is None:
            time = self._nextEffectiveTime
            self._nextEffectiveTime = time + datetime.timedelta(minutes=1)
        return super(SixTriggersScenario, self)._updateMessageHeader(content, time)

    def _prepareInputs(self, dataDirectory):
        self._nextEffectiveTime = datetime.datetime.utcnow() + datetime.timedelta(minutes=1)
        self._expireMinutes = 11
        self._copySchedulingMessage(dataDirectory, 'trigger_high')
        self._expireMinutes = 9
        self._copySchedulingMessage(dataDirectory, 'trigger_high_suite2')
        self._expireMinutes = 7
        self._copySchedulingMessage(dataDirectory, 'trigger_high_suite3')
        self._expireMinutes = 5
        self._copySchedulingMessage(dataDirectory, 'trigger_exclusive')
        self._expireMinutes = 3
        self._copySchedulingMessage(dataDirectory, 'trigger_exclusive_suite2')
        self._expireMinutes = 1
        self._copySchedulingMessage(dataDirectory, 'trigger_exclusive_suite3')