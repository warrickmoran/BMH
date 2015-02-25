'''
Created on Feb 25, 2015

@author: bkowal
'''

import os

import bmh.test.suite.core.AbstractBMHScenario as AbstractBMHScenario

class AbstractBMHParsingScenario(AbstractBMHScenario.AbstractBMHScenario):
    '''
    classdocs
    '''

    def __init__(self, name, expectedResult, inputFile, updateHeader=True):
        '''
        Constructor
        '''
        super(AbstractBMHParsingScenario, self).__init__(name, 
            self._EXPECTED_RESULT, updateHeader)
        self._inputFile = inputFile
        
    def _prepareInputs(self, dataDirectory):
        self._copyMessageToDestination(os.path.join(dataDirectory, 'parsing', self._inputFile))