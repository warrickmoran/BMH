'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

class PeriodicScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(PeriodicScenario, self).__init__('Periodic Scenario', 
            'A general message should play with a periodic message playing periodically every 2 minutes.')
        self._expireMinutes = 7

        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'periodic')
        self._copySchedulingMessage(dataDirectory, 'general')
