'''
Created on May 20, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class MRDInterruptReplaceDiff(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(MRDInterruptReplaceDiff, self).__init__('MRD Interrupt Replace Diff Scenario', 
            'Message mrd_replace_me6 will be broadcast on PABE. Message mrd_replacer_diff_type ' \
            'will be played as an interrupt and then it will replace mrd_replace_me6.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 10
        response = self._copyMessageAndConfirm(dataDirectory, 'mrd_replace_me6','mrd_replace_me6 plays on PABE.')
        if response == 'y':
            self._copySchedulingMessage(dataDirectory, 'mrd_replacer_diff_type')