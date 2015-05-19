'''
Created on May 19, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class MATInterruptReplaceScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(MATInterruptReplaceScenario, self).__init__('MAT Interrupt Replace Scenario', 
            'Message mat_replace_me1 will be played on PABE. Message mat_replacer1 will play as ' \
            'an Interrupt and then replace mat_replace_me1.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 15
        response = self._copyMessageAndConfirm(dataDirectory, 'mat_replace_me1','A new message named mat_replace_me1 should play on PABE.')
        if response == 'y':
            self._copySchedulingMessage(dataDirectory, 'mat_replacer1')