'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgIncompleteVertices(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to Invalid Polygon. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgIncompleteVertices, self).__init__('Invalid Polygon Incomplete Vertex', 
            self._EXPECTED_RESULT, 'MSG_INVALID_VERTICES', False)