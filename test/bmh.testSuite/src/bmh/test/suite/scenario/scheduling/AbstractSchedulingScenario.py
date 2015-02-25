'''
Created on Feb 26, 2015

@author: bsteffen
'''

import os

import bmh.test.suite.core.AbstractBMHScenario as AbstractBMHScenario

class AbstractSchedulingScenario(AbstractBMHScenario.AbstractBMHScenario):

    def __init__(self, name, expectedResult):
        super(AbstractSchedulingScenario, self).__init__(name, 
            expectedResult, True, True)
    
    def _copySchedulingMessage(self, dataDirectory, message):
        self._copyMessageToDestination(os.path.join(dataDirectory, "scheduling", message))
        
    def _copyMessageAndConfirm(self, dataDirectory, message, prompt):
        self._copySchedulingMessage(dataDirectory, message)
        allowedInputs = ['y', 'n']
        userResponse = None
        
        while (userResponse not in allowedInputs):
            userResponse = raw_input(prompt + ' (y | n): ')
        return userResponse