'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

class InterruptScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(InterruptScenario, self).__init__('Interrupt Scenario', 
            'An interrupt message named interrupt should play as an interrupt.')
        self._expireMinutes = 2
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'interrupt')
