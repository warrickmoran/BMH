'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario


class GeneralUnorderedScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(GeneralUnorderedScenario, self).__init__('General Unordered Scenario', 
            'The general suite will play five messages with names containing a number, the messages will enter the system out of ordered but should be scheduled to play in numerical order in the playlist.')
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'general3')
        self._copySchedulingMessage(dataDirectory, 'general1')
        self._copySchedulingMessage(dataDirectory, 'general5')
        self._copySchedulingMessage(dataDirectory, 'general2')
        self._copySchedulingMessage(dataDirectory, 'general4')