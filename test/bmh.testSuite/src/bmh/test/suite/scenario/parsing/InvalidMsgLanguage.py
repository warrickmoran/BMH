'''
Created on Feb 25, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgLanguage(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to an Unhandled Language. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgLanguage, self).__init__('Invalid Message Language', 
            self._EXPECTED_RESULT, 'MSG_INVALID_LANGUAGE', False)
        