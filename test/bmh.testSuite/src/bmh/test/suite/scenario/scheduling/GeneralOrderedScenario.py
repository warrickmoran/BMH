'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

import time

class GeneralOrderedScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(GeneralOrderedScenario, self).__init__('General Ordered Scenario', 
            'The general suite will play five messages with names containing a number, the messages should play in numerical order.')
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'general1')
        time.sleep(5)
        self._copySchedulingMessage(dataDirectory, 'general2')
        time.sleep(5)
        self._copySchedulingMessage(dataDirectory, 'general3')
        time.sleep(5)
        self._copySchedulingMessage(dataDirectory, 'general4')
        time.sleep(5)
        self._copySchedulingMessage(dataDirectory, 'general5')