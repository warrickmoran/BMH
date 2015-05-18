'''
Created on May 18, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class SAMECivScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(SAMECivScenario, self).__init__('SAME CIV Scenario', 
            'Message same_only_civ_3trx is scheduled on transmitters: PABE, PAWS, and PANC. ' \
            'SAME Tones are broadcast. SAME Encoding indicates a CIV origination. ' \
            'Area for PABE is: 002050; area for PAWS and PANC is: 002170. ' \
            'Check message activity logs to verify.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 5
        self._copySchedulingMessage(dataDirectory, 'same_only_civ_3trx')