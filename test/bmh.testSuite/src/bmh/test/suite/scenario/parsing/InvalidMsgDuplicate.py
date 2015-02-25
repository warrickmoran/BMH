'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgDuplicate(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Validation fails because the message is a duplicate.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgDuplicate, self).__init__('Invalid Message Duplicate', 
            self._EXPECTED_RESULT, 'MSG_INCLUDES_MRD')