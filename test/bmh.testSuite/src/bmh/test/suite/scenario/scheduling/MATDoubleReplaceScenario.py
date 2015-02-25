'''
Created on Feb 26, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

class MATDoubleReplaceScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(MATDoubleReplaceScenario, self).__init__('MAT Double Replace Scenario', 
            'Both mat_message and mat_message2 should be replaced by a message named mat_replace.')
        
    def _prepareInputs(self, dataDirectory):
        self._copySchedulingMessage(dataDirectory, 'mat_message')
        response = self._copyMessageAndConfirm(dataDirectory, 'mat_message2', 'Two new messages named mat_message and mat_message2 should play in the current list.')
        if response == 'y':
            self._copySchedulingMessage(dataDirectory, 'mat_replace')