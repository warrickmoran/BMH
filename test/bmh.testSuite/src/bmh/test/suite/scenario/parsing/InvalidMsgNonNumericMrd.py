'''
Created on Mar 4, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgNonNumericMrd(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to Invalid MRD. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgNonNumericMrd, self).__init__('Invalid Message Non-Numeric MRD', 
            self._EXPECTED_RESULT, 'MSG_NON_NUMERIC_MRD', False)