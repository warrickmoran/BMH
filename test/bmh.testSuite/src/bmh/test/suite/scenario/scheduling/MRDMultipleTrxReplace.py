'''
Created on May 19, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class MRDMultipleTrxReplace(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(MRDMultipleTrxReplace, self).__init__('Multiple MRD Trx Replace Scenario', 
            'Message mrd_replace_me1 will be played on PABE. Message mrd_replace_me2 will be played ' \
            'on PANC. Both messages will be replaced by mrd_replacer1.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 15
        response = self._copyMessageAndConfirm(dataDirectory, 'mrd_replace_me1','A new message named mrd_replace_me1 should play on PABE.')
        if response == 'y':
            response = self._copyMessageAndConfirm(dataDirectory, 'mrd_replace_me2','A new message named mrd_replace_me2 should play on PANC.')
            if response == 'y':
                self._copySchedulingMessage(dataDirectory, 'mrd_replacer1')