'''
Created on Feb 25, 2015

@author: bkowal
'''

import shutil
import re
import datetime

import os.path

class AbstractBMHScenario(object):
    '''
    classdocs
    '''

    _destinationDirectory = '/awips2/bmh/data/nwr/ready'
    
    _uniqueIdCounter = 0
    
    def __init__(self, name, expectedResult, updateHeader=True, makeUnique=False):
        '''
        Constructor
        '''        
        self._name = name
        self._expectedResult = expectedResult
        self._updateHeader = updateHeader
        self._makeUnique = makeUnique
        self._expireMinutes = 5
        
    def _prepareInputs(self, dataDirectory):
        # This function should be implemented.
        pass
        
    def execute(self, dataDirectory):
        print '(INFO) ', self._name, ' : Starting Scenario ...'
        self._prepareInputs(dataDirectory)
        self._printExpectedResult()
        self._promptUserContinue()
        print '(INFO) ', self._name, ' : Scenario Complete.'
    
    def _updateMessageHeader(self, content, time = None):
        if time is None:
            time = datetime.datetime.utcnow()
        create = time.strftime('%y%m%d%H%M')
        effective = time.strftime('%y%m%d%H%M')
        time = time + datetime.timedelta(minutes=self._expireMinutes)
        expire = time.strftime('%y%m%d%H%M')

        match = re.search('^(\x1ba[A-Z]_[A-Z]{3}[A-Z]{9})(\\d{10})(\\d{10})(.*)(\\d{10})$', content, re.MULTILINE)

        header = match.group(0);
        newheader = match.group(1) + create + effective + match.group(4) + expire;
        return content.replace(header, newheader)
        
    def _makeUniqueContent(self, content):
        uid = AbstractBMHScenario._uniqueIdCounter
        AbstractBMHScenario._uniqueIdCounter += 1
        return content.replace('\x1bb','Message Unique Identifier is %d \x1bb' % uid )
        
    def _copyMessageToDestination(self, fileToCopy):
        if self._updateHeader or self._makeUnique:
            with open(fileToCopy, 'r') as content_file:
                content = content_file.read()
            if self._makeUnique:
                content = self._makeUniqueContent(content)
            if self._updateHeader:
                content = self._updateMessageHeader(content)
            dest = os.path.join(self._destinationDirectory, os.path.basename(fileToCopy))
            with open(dest, 'w') as content_file:
                content_file.write(content)
        else:    
            shutil.copy(fileToCopy, self._destinationDirectory)
    
    def _printExpectedResult(self):
        print '--------------------------------------------------------------------------------'
        print 'EXPECTED RESULT: ', self._expectedResult
        print '--------------------------------------------------------------------------------'
    
    def _promptUserContinue(self):
        allowedInputs = ['y', 'n']
        userResponse = None
        
        while (userResponse not in allowedInputs):
            userResponse = raw_input('Enter Scenario Result (y | n): ')
        
        if (userResponse == 'y'):
            print '(INFO) ', self._name, ' : Marking Test SUCCESS'
        elif (userResponse == 'n'):
            print '(WARN) ', self._name, ' : Marking TEST FAILED'
            
    def getName(self):
        return self._name