'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

import time

class InterruptInterruptScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(InterruptInterruptScenario, self).__init__('Interrupt an Interrupt Scenario', 
            'An interrupt message named interrupt should play as an interrupt, immediately after it plays a second interrupt named interrupt2 should play')
        self._expireMinutes = 2
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'interrupt')
        time.sleep(10)
        self._copySchedulingMessage(dataDirectory, 'interrupt2')