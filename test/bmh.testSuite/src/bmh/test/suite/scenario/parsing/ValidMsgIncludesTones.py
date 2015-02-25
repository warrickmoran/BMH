'''
Created on Feb 26, 2015

@author: bkowal
'''

import AbstractBMHParsingScenario as AbstractBMHParsingScenario

class ValidMsgIncludesTones(AbstractBMHParsingScenario.AbstractBMHParsingScenario):
    '''
    classdocs
    '''

    _EXPECTED_RESULT = 'Validation succeeds. The nwrsametone and alerttone columns contain TRUE for the generated message in the input_msg table.'

    def __init__(self):
        '''
        Constructor
        '''
        super(ValidMsgIncludesTones, self).__init__('Valid Message Includes Tones', 
            self._EXPECTED_RESULT, 'MSG_INCLUDES_TONES')