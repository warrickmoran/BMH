'''
Created on May 18, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class SAMEAlertWxrScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(SAMEAlertWxrScenario, self).__init__('SAME Alert WXR Scenario', 
            'Message same_alert_wxr_3trx is scheduled on transmitters: PABE, PAWS, and PANC. ' \
            'SAME and Alert Tones are broadcast. SAME Encoding indicates a WXR origination. ' \
            'Area for PABE is: 002050; area for PAWS and PANC is: 002170. ' \
            'Check message activity logs to verify.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 5
        self._copySchedulingMessage(dataDirectory, 'same_alert_wxr_3trx')