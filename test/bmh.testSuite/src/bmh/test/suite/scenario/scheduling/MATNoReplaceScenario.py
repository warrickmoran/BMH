'''
Created on May 19, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class MATNoReplaceScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(MATNoReplaceScenario, self).__init__('MAT No Replace Scenario', 
            'Message mat_noreplace1 will be played on PABE. Message mat_noreplace2 will be ' \
            'played on PANC. Message mat_replace_denied will play on both transmitters without ' \
            'replacing any messages.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 15
        response = self._copyMessageAndConfirm(dataDirectory, 'mat_noreplace1','A new message named mat_noreplace1 should play on PABE.')
        if response == 'y':
            response = self._copyMessageAndConfirm(dataDirectory, 'mat_noreplace2','A new message named mat_noreplace2 should play on PANC.')
            if response == 'y':
                self._copySchedulingMessage(dataDirectory, 'mat_replace_denied')