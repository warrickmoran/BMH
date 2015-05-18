'''
Created on May 18, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class NonSAMEMultipleTrx(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(NonSAMEMultipleTrx, self).__init__('Non SAME Multiple Trx Scenario', 
            'Message multiple_trx_no_tones is scheduled on transmitters: PABE, PAWS, and PANC. No Tones are broadcast.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 5
        self._copySchedulingMessage(dataDirectory, 'multiple_trx_no_tones')