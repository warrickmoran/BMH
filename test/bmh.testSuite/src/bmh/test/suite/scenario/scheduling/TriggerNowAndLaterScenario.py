'''
Created on Feb 25, 2015

@author: bsteffen
'''

import AbstractSchedulingScenario
import datetime
import time

class TriggerNowAndLaterScenario(AbstractSchedulingScenario.AbstractSchedulingScenario):

    def __init__(self):
        super(TriggerNowAndLaterScenario, self).__init__('Trigger Now And Later Scenario', 
            'A second message named trigger_high should cause a high suite to trigger after 2 minutes.')

    def _updateMessageHeader(self, content, time = None):
        if time is None:
            time = self._effectiveTime
        return super(TriggerNowAndLaterScenario, self)._updateMessageHeader(content, time)
        
    def _prepareInputs(self, dataDirectory):
        self._expireMinutes = 2
        self._effectiveTime = datetime.datetime.utcnow()
        self._copySchedulingMessage(dataDirectory, 'trigger_high')
        print 'A message has been created named trigger_high, this message should cause a high suite to play.'
        print 'This test needs to wait for that suite to expire before continuing'
        print 'This test will resume in 3 minutes.'
        time.sleep(60)
        print 'This test will resume in 2 minutes.'
        time.sleep(60)
        print 'This test will resume in 1 minutes.'
        time.sleep(60)
        self._effectiveTime = datetime.datetime.utcnow() + datetime.timedelta(minutes=2)
        self._copySchedulingMessage(dataDirectory, 'trigger_high')
