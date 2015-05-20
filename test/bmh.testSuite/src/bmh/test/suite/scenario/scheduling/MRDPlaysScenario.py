'''
Created on May 20, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class MRDPlaysScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(MRDPlaysScenario, self).__init__('MRD Plays Scenario', 
            'Five messages will be scheduled for playback on PABE including: ' \
            'mrd_plays1, mrd_plays2, mrd_plays3, mrd_plays4, mrd_plays5. All five ' \
            'messages will be replaced by mrd_replace_spec10 which specifies 10 MRDs ' \
            'to replace.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 15
        self._copySchedulingMessage(dataDirectory, 'mrd_plays1')
        self._copySchedulingMessage(dataDirectory, 'mrd_plays2')
        self._copySchedulingMessage(dataDirectory, 'mrd_plays3')
        self._copySchedulingMessage(dataDirectory, 'mrd_plays4')
        response = self._copyMessageAndConfirm(dataDirectory, 'mrd_plays5','5 mrd_plays messages should play on PABE.')
        if response == 'y':
            self._copySchedulingMessage(dataDirectory, 'mrd_replace_spec10')