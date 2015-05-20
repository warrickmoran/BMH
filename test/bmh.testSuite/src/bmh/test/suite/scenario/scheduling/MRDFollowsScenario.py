'''
Created on May 19, 2015

@author: bkowal
'''

import AbstractSchedulingScenario

class MRDFollowsScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(MRDFollowsScenario, self).__init__('MRD Follows Scenario', 
            'Five messages will be scheduled for playback on PABE. Due to the use ' \
            'of the MRD Follows flag, the messages will be broadcast in the following ' \
            'order: mrd_follows4, mrd_follows5, mrd_follows1, mrd_follows3, mrd_follows2. '\
            'This order contradicts their numerical order in the General PABE Suite.')
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 10
        self._copySchedulingMessage(dataDirectory, 'mrd_follows1')
        self._copySchedulingMessage(dataDirectory, 'mrd_follows2')
        self._copySchedulingMessage(dataDirectory, 'mrd_follows3')
        self._copySchedulingMessage(dataDirectory, 'mrd_follows4')
        self._copySchedulingMessage(dataDirectory, 'mrd_follows5')