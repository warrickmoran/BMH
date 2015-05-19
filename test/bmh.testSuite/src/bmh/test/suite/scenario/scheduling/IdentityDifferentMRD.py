'''
Created on May 18, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class IdentityDifferentMRD(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(IdentityDifferentMRD, self).__init__('Identity Different MRD Scenario', 
            'Message mrd_ident1 and mrd_ident2 are scheduled on transmitter: PABE. ' \
            'Check Broadcast Cycle to verify.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 4
        self._copySchedulingMessage(dataDirectory, 'mrd_ident1')
        self._expireMinutes = 6
        self._copySchedulingMessage(dataDirectory, 'mrd_ident2')