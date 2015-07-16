'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class ValidMsgIncompleteVertices(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Message logged stating that the polygon will be ignored.  Message includes statement that incomplete vertex detected. Validation succeeds.'

    def __init__(self):
        '''
        Constructor
        '''
        super(ValidMsgIncompleteVertices, self).__init__('Valid Message Invalid Polygon Incomplete Vertex', 
            self._EXPECTED_RESULT, 'MSG_INVALID_VERTICES')
