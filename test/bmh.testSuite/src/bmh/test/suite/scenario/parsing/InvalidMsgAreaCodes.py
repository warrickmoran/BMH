'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgAreaCodes(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to Invalid Listening Area Codes. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgAreaCodes, self).__init__('Invalid Listening Area Codes', 
            self._EXPECTED_RESULT, 'MSG_INVALID_AREA_CODES', False)