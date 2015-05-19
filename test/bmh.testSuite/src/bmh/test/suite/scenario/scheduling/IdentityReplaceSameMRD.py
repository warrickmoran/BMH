'''
Created on May 18, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class IdentityReplaceSameMRD(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(IdentityReplaceSameMRD, self).__init__('Identity Replace Same MRD Scenario', 
            'Message mrd_ident_to_replace followed by mrd_ident_replacement are scheduled on transmitter: PABE. ' \
            'Check Broadcast Cycle to verify.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 8
        response = self._copyMessageAndConfirm(dataDirectory, 'mrd_ident_to_replace', 'A new message named mrd_ident_to_replace should play in the current list.')
        if response == 'y':
            self._expireMinutes = 10
            self._copySchedulingMessage(dataDirectory, 'mrd_ident_replacement')