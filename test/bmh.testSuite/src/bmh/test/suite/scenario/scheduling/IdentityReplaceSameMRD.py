'''
Created on May 18, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class IdentityReplaceSameMRD(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(IdentityReplaceSameMRD, self).__init__('Identity Replace Same MRD Scenario', 
            'Message mrd_ident_to_replace is scheduled on transmitter PABE. ' \
            'Message mrd_ident_replacement replaces mrd_ident_to_replace on PABE. ' \
            'Message mrd_ident_no_replace is scheduled on PABE besides mrd_ident_replacement. ' \
            'Check Broadcast Cycle to verify.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 8
        response = self._copyMessageAndConfirm(dataDirectory, 'mrd_ident_to_replace', 'A new message named mrd_ident_to_replace should play in the current list.')
        if response == 'y':
            self._expireMinutes = 10
            self._copyMessageAndConfirm(dataDirectory, 'mrd_ident_replacement', 'A new message named mrd_ident_replacement should replace mrd_ident_to_replace')
            if response == 'y':
                self._expireMinutes = 10
                self._copySchedulingMessage(dataDirectory, 'mrd_ident_no_replace')
