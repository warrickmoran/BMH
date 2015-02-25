'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario

class IdentityReplaceScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(IdentityReplaceScenario, self).__init__('Identity Replace Scenario', 
            'The general message should be replaced by a message named identity_replace.')
        
    def _prepareInputs(self, dataDirectory):
        response = self._copyMessageAndConfirm(dataDirectory, 'general', 'A new message named general should play in the current list.')
        if response == 'y':
            self._copySchedulingMessage(dataDirectory, 'identity_replace')
