'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class ValidMsgIncludesSAMEOnly(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Validation succeeds. The nwrsametone column contains TRUE for the generated message in the input_msg table.'

    def __init__(self):
        '''
        Constructor
        '''
        super(ValidMsgIncludesSAMEOnly, self).__init__('Valid Message Includes SAME Tones', 
            self._EXPECTED_RESULT, 'MSG_INCLUDES_SAME_ONLY')