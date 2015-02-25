'''
Created on Feb 25, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgCreationDate(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Parsing Exception is generated due to an Invalid Creation Date. Validation fails.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgCreationDate, self).__init__('Invalid Creation Date', 
            self._EXPECTED_RESULT, 'MSG_INVALID_CREATION_DATE', False)