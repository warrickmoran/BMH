'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class ValidMsgIncludesPolygon(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Message logged stating that the polygon will be ignored. Validation succeeds.'

    def __init__(self):
        '''
        Constructor
        '''
        super(ValidMsgIncludesPolygon, self).__init__('Valid Message Includes Polygon', 
            self._EXPECTED_RESULT, 'MSG_INCLUDES_POLYGON')
        