'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class ValidMsgIncludesMrd(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Validation succeeds. The generated message has a mrd of 257 in the input_msg table.'

    def __init__(self):
        '''
        Constructor
        '''
        super(ValidMsgIncludesMrd, self).__init__('Valid Message Includes MRD', 
            self._EXPECTED_RESULT, 'MSG_INCLUDES_MRD')
        