'''
Created on May 19, 2015

@author: bkowal
'''
import AbstractSchedulingScenario

class MATMultipleTrxReplaceScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(MATMultipleTrxReplaceScenario, self).__init__('MAT Multiple Trx Replace Scenario', 
            'Message mat_replace_me2 will be played on PABE and PANC. Message mat_replacer2  ' \
            'will replace mat_replace_me2 on both transmitters.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 15
        response = self._copyMessageAndConfirm(dataDirectory, 'mat_replace_me2','A new message named mat_replace_me2 should play on PABE and PANC.')
        if response == 'y':
            self._copySchedulingMessage(dataDirectory, 'mat_replacer2')