'''
Created on May 19, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class MRDMultipleTrxReplaceWithAddl(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(MRDMultipleTrxReplaceWithAddl, self).__init__('Multiple MRD Trx + 1 Replace Scenario', 
            'Message mrd_replace_me3 will be played on PABE. Message mrd_replace_me4 will be played ' \
            'on PANC. Message mrd_replacer2 will replace mrd_replace_me3 and mrd_replace_me4. ' \
            'Message mrd_replacer2 will be played on PABE and PAKN. Message mrd_replacer2 will not be ' \
            'broadcast on PANC due to a change in affected areas.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 15
        response = self._copyMessageAndConfirm(dataDirectory, 'mrd_replace_me3','A new message named mrd_replace_me3 should play on PABE.')
        if response == 'y':
            response = self._copyMessageAndConfirm(dataDirectory, 'mrd_replace_me4','A new message named mrd_replace_me4 should play on PANC.')
            if response == 'y':
                self._copySchedulingMessage(dataDirectory, 'mrd_replacer2')