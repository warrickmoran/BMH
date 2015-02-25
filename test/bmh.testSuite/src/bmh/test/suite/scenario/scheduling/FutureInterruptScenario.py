'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario
import datetime

class FutureInterruptScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(FutureInterruptScenario, self).__init__('Future Interrupt Scenario', 
            'An interrupt message should play as an interrupt in two minutes.')
        self._expireMinutes = 2

    def _updateMessageHeader(self, content, time = None):
        if time is None:
            time = datetime.datetime.utcnow()
            time = time + datetime.timedelta(minutes=2)
        return super(FutureInterruptScenario, self)._updateMessageHeader(content, time)
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'interrupt')
