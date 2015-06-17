'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class ValidMsgGt20Vertices(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Message logged stating that the polygon will be ignored.  Message includes statement that polygon has more 20 vertices. Validation succeeds.'

    def __init__(self):
        '''
        Constructor
        '''
        super(ValidMsgGt20Vertices, self).__init__('Valid Message Invalid Polygon > 20 Vertices', 
            self._EXPECTED_RESULT, 'MSG_GT_20_VERTICES')
