'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgGt20Vertices(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to Invalid Polygon. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgGt20Vertices, self).__init__('Invalid Polygon > 20 Vertices', 
            self._EXPECTED_RESULT, 'MSG_GT_20_VERTICES', False)