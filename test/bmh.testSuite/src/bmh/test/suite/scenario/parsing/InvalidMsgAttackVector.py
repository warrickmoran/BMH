'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class InvalidMsgAttackVector(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'The file is never processed because it is larger than the maximum file size allowed.'

    def __init__(self):
        '''
        Constructor
        '''
        super(InvalidMsgAttackVector, self).__init__('1 GB Message', 
            self._EXPECTED_RESULT, 'MSG_LARGE_FILE', False)