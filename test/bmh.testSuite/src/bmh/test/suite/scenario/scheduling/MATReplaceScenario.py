'''
Created on Feb 26, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

class MATReplaceScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(MATReplaceScenario, self).__init__('MAT Replace Scenario', 
            'The general message should be replaced by a message named mat_replace. mat_replace is not an interrupt.')
        
    def _prepareInputs(self, dataDirectory):
        response = self._copyMessageAndConfirm(dataDirectory, 'mat_message', 'A new message named mat_message should play in the current list.')
        if response == 'y':
            self._copySchedulingMessage(dataDirectory, 'mat_replace')