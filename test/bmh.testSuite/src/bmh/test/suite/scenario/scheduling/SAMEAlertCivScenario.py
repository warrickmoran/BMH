'''
Created on May 18, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class SAMEAlertCivScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(SAMEAlertCivScenario, self).__init__('SAME Alert CIV Scenario', 
            'The message is scheduled on transmitters: PABE, PAWS, and PANC.' \
            'SAME and Alert Tones are broadcast. SAME Encoding indicates a CIV origination.' \
            'Area for PABE is: 002050; area for PAWS and PANC is: 002170. ' \
            'Check message activity logs to verify.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 5
        self._copySchedulingMessage(dataDirectory, 'same_alert_civ_3trx')