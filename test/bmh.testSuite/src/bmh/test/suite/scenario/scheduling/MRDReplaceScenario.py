'''
Created on Feb 26, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

class MRDReplaceScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(MRDReplaceScenario, self).__init__('MRD Replace Scenario', 
            'The general message should be replaced by a message named mrd_replace.')
        
    def _prepareInputs(self, dataDirectory):
        response = self._copyMessageAndConfirm(dataDirectory, 'mrd_message','A new message named mrd_message should play in the current list.')
        if response == 'y':
            self._copySchedulingMessage(dataDirectory, 'mrd_replace')