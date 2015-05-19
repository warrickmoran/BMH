'''
Created on May 19, 2015

@author: bkowal
'''

import AbstractSchedulingScenario
import datetime

class FutureIdentityScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):
    
    def __init__(self):
        super(FutureIdentityScenario, self).__init__('Future Identity Replace Scenario', 
            'Message ident_future_replace_me will initially be played on PABE. ' \
            'Message ident_future_replacer will be created with an effective time 10 minutes ' \
            'into the future. 10 minutes later ident_future_replacer will replace ident_future_replace_me.')
        self._futureMinutes = 10
        
    def _updateMessageHeader(self, content, time = None):
        if time is None:
            time = self._nextEffectiveTime
            self._nextEffectiveTime = time + datetime.timedelta(minutes=self._futureMinutes)
        return super(FutureIdentityScenario, self)._updateMessageHeader(content, time)
    
    def _prepareInputs(self, dataDirectory):
        self._nextEffectiveTime = datetime.datetime.utcnow()
        self._expireMinutes = 25
        self._copySchedulingMessage(dataDirectory, 'ident_future_replace_me')
        self._copySchedulingMessage(dataDirectory, 'ident_future_replacer')