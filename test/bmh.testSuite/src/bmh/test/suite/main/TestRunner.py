'''
Created on Feb 25, 2015

@author: bkowal
'''

import os.path
import subprocess

import bmh.test.suite.scenario.parsing.NonWarningAlreadyExpiredScenario as NonWarningAlreadyExpiredScenario
import bmh.test.suite.scenario.parsing.InvalidMsgLanguage as InvalidMsgLanguage
import bmh.test.suite.scenario.parsing.InvalidMsgFormat as InvalidMsgFormat
import bmh.test.suite.scenario.parsing.InvalidMsgAfosId as InvalidMsgAfosId
import bmh.test.suite.scenario.parsing.InvalidMsgCreationDate as InvalidMsgCreationDate
import bmh.test.suite.scenario.parsing.InvalidMsgEffectiveDate as InvalidMsgEffectiveDate
import bmh.test.suite.scenario.parsing.InvalidMsgPeriodicity as InvalidMsgPeriodicity
import bmh.test.suite.scenario.parsing.InvalidMsgActive as InvalidMsgActive
import bmh.test.suite.scenario.parsing.InvalidMsgConfirm as InvalidMsgConfirm
import bmh.test.suite.scenario.parsing.InvalidMsgInterrupt as InvalidMsgInterrupt
import bmh.test.suite.scenario.parsing.InvalidMsgTone as InvalidMsgTone
import bmh.test.suite.scenario.parsing.InvalidMsgAreaCodes as InvalidMsgAreaCodes
import bmh.test.suite.scenario.parsing.InvalidMsgExpireDate as InvalidMsgExpireDate
import bmh.test.suite.scenario.parsing.InvalidMsgNoEnd as InvalidMsgNoEnd
import bmh.test.suite.scenario.parsing.ValidMsgIncompleteVertices as ValidMsgIncompleteVertices
import bmh.test.suite.scenario.parsing.ValidMsgGt20Vertices as ValidMsgGt20Vertices
import bmh.test.suite.scenario.parsing.ValidMsgIncludesPolygon as ValidMsgIncludesPolygon
import bmh.test.suite.scenario.parsing.WarningAlreadyExpiredScenario as WarningAlreadyExpiredScenario
import bmh.test.suite.scenario.parsing.ValidMsgIncludesMrd as ValidMsgIncludesMrd
import bmh.test.suite.scenario.parsing.ValidMsgIncludesSAMEOnly as ValidMsgIncludesSAMEOnly
import bmh.test.suite.scenario.parsing.ValidMsgIncludesTones as ValidMsgIncludesTones
import bmh.test.suite.scenario.parsing.InvalidMsgUnacceptable as InvalidMsgUnacceptable
import bmh.test.suite.scenario.parsing.InvalidMsgAttackVector as InvalidMsgAttackVector
import bmh.test.suite.scenario.parsing.InvalidMsgDuplicate as InvalidMsgDuplicate
import bmh.test.suite.scenario.parsing.InvalidMsgNonNumericMrd as InvalidMsgNonNumericMrd
import bmh.test.suite.scenario.parsing.InvalidMsgNoBegin as InvalidMsgNoBegin
import bmh.test.suite.scenario.parsing.NonWarnTriggerAlreadyExpired as NonWarnTriggerAlreadyExpired
import bmh.test.suite.scenario.parsing.WarnTriggerAlreadyExpired as WarnTriggerAlreadyExpired
import bmh.test.suite.scenario.parsing.ValidMsgExpireNines as ValidMsgExpireNines

import bmh.test.suite.scenario.scheduling.TriggerHighScenario as TriggerHighScenario
import bmh.test.suite.scenario.scheduling.TriggerExclusiveScenario as TriggerExclusiveScenario
import bmh.test.suite.scenario.scheduling.IdentityReplaceScenario as IdentityReplaceScenario
import bmh.test.suite.scenario.scheduling.MATReplaceScenario as MATReplaceScenario
import bmh.test.suite.scenario.scheduling.MRDReplaceScenario as MRDReplaceScenario
import bmh.test.suite.scenario.scheduling.MATDoubleReplaceScenario as MATDoubleReplaceScenario
import bmh.test.suite.scenario.scheduling.ExclusiveExpiresToHighScenario as ExclusiveExpiresToHighScenario
import bmh.test.suite.scenario.scheduling.InterruptScenario as InterruptScenario
import bmh.test.suite.scenario.scheduling.InterruptInterruptScenario as InterruptInterruptScenario
import bmh.test.suite.scenario.scheduling.FutureInterruptScenario as FutureInterruptScenario
import bmh.test.suite.scenario.scheduling.FutureTriggerScenario as FutureTriggerScenario
import bmh.test.suite.scenario.scheduling.NoTriggerExclusiveScenario as NoTriggerExclusiveScenario
import bmh.test.suite.scenario.scheduling.PeriodicScenario as PeriodicScenario
import bmh.test.suite.scenario.scheduling.SixTriggersScenario as SixTriggersScenario
import bmh.test.suite.scenario.scheduling.TriggerNowAndLaterScenario as TriggerNowAndLaterScenario
import bmh.test.suite.scenario.scheduling.GeneralOrderedScenario as GeneralOrderedScenario
import bmh.test.suite.scenario.scheduling.GeneralReversedScenario as GeneralReversedScenario
import bmh.test.suite.scenario.scheduling.GeneralUnorderedScenario as GeneralUnorderedScenario
import bmh.test.suite.scenario.scheduling.SAMEAlertWxrScenario as SAMEAlertWxrScenario
import bmh.test.suite.scenario.scheduling.SAMEWxrScenario as SAMEWxrScenario
import bmh.test.suite.scenario.scheduling.SAMEAlertCivScenario as SAMEAlertCivScenario
import bmh.test.suite.scenario.scheduling.SAMECivScenario as SAMECivScenario
import bmh.test.suite.scenario.scheduling.NonSAMEMultipleTrx as NonSAMEMultipleTrx
import bmh.test.suite.scenario.scheduling.IdentityReplaceSameMRD as IdentityReplaceSameMRD
import bmh.test.suite.scenario.scheduling.IdentityDifferentMRD as IdentityDifferentMRD
import bmh.test.suite.scenario.scheduling.SixInterruptsScenario as SixInterruptsScenario
import bmh.test.suite.scenario.scheduling.SixTriggersInterruptsCombineScenario as SixTriggersInterruptsCombineScenario
import bmh.test.suite.scenario.scheduling.MRDMultipleTrxReplace as MRDMultipleTrxReplace
import bmh.test.suite.scenario.scheduling.MRDMultipleTrxReplaceWithAddl as MRDMultipleTrxReplaceWithAddl
import bmh.test.suite.scenario.scheduling.MATInterruptReplaceScenario as MATInterruptReplaceScenario
import bmh.test.suite.scenario.scheduling.MATMultipleTrxReplaceScenario as MATMultipleTrxReplaceScenario
import bmh.test.suite.scenario.scheduling.MATNoReplaceScenario as MATNoReplaceScenario
import bmh.test.suite.scenario.scheduling.FutureIdentityScenario as FutureIdentityScenario
import bmh.test.suite.scenario.scheduling.MRDFollowsScenario as MRDFollowsScenario
import bmh.test.suite.scenario.scheduling.MRDPlaysScenario as MRDPlaysScenario
import bmh.test.suite.scenario.scheduling.MRDInterruptReplaceSame as MRDInterruptReplaceSame
import bmh.test.suite.scenario.scheduling.MRDInterruptReplaceDiff as MRDInterruptReplaceDiff


class TestRunner(object):
    '''
    classdocs
    '''

    def __init__(self):
        '''
        Constructor
        '''
        self._loadScenarios()
        self._buildNameMappingForPrompt()
        
    def _loadScenarios(self):
        self._bmhScenarios = []
        
        self._bmhScenarios.append(NonWarningAlreadyExpiredScenario.NonWarningAlreadyExpiredScenario())
        self._bmhScenarios.append(InvalidMsgLanguage.InvalidMsgLanguage())
        self._bmhScenarios.append(InvalidMsgFormat.InvalidMsgFormat())
        self._bmhScenarios.append(InvalidMsgAfosId.InvalidMsgAfosId())
        self._bmhScenarios.append(InvalidMsgCreationDate.InvalidMsgCreationDate())
        self._bmhScenarios.append(InvalidMsgEffectiveDate.InvalidMsgEffectiveDate())
        self._bmhScenarios.append(InvalidMsgPeriodicity.InvalidMsgPeriodicity())
        self._bmhScenarios.append(InvalidMsgActive.InvalidMsgActive())
        self._bmhScenarios.append(InvalidMsgConfirm.InvalidMsgConfirm())
        self._bmhScenarios.append(InvalidMsgInterrupt.InvalidMsgInterrupt())
        self._bmhScenarios.append(InvalidMsgTone.InvalidMsgTone())
        self._bmhScenarios.append(InvalidMsgAreaCodes.InvalidMsgAreaCodes())
        self._bmhScenarios.append(InvalidMsgExpireDate.InvalidMsgExpireDate())
        self._bmhScenarios.append(InvalidMsgNoEnd.InvalidMsgNoEnd())
        self._bmhScenarios.append(ValidMsgIncompleteVertices.ValidMsgIncompleteVertices())
        self._bmhScenarios.append(ValidMsgGt20Vertices.ValidMsgGt20Vertices())
        self._bmhScenarios.append(ValidMsgIncludesPolygon.ValidMsgIncludesPolygon())
        self._bmhScenarios.append(WarningAlreadyExpiredScenario.WarningAlreadyExpiredScenario())
        self._bmhScenarios.append(ValidMsgIncludesMrd.ValidMsgIncludesMrd())
        self._bmhScenarios.append(ValidMsgIncludesSAMEOnly.ValidMsgIncludesSAMEOnly())
        self._bmhScenarios.append(ValidMsgIncludesTones.ValidMsgIncludesTones())
        self._bmhScenarios.append(InvalidMsgUnacceptable.InvalidMsgUnacceptable())
        self._bmhScenarios.append(InvalidMsgAttackVector.InvalidMsgAttackVector())
        self._bmhScenarios.append(InvalidMsgDuplicate.InvalidMsgDuplicate())
        self._bmhScenarios.append(InvalidMsgNonNumericMrd.InvalidMsgNonNumericMrd())
        self._bmhScenarios.append(InvalidMsgNoBegin.InvalidMsgNoBegin())
        self._bmhScenarios.append(NonWarnTriggerAlreadyExpired.NonWarnTriggerAlreadyExpired())
        self._bmhScenarios.append(WarnTriggerAlreadyExpired.WarnTriggerAlreadyExpired())
        self._bmhScenarios.append(ValidMsgExpireNines.ValidMsgExpireNines())
         
        self._bmhScenarios.append(TriggerHighScenario.TriggerHighScenario())
        self._bmhScenarios.append(TriggerExclusiveScenario.TriggerExclusiveScenario())
        self._bmhScenarios.append(IdentityReplaceScenario.IdentityReplaceScenario())
        self._bmhScenarios.append(MATReplaceScenario.MATReplaceScenario())
        self._bmhScenarios.append(MRDReplaceScenario.MRDReplaceScenario())
        self._bmhScenarios.append(MATDoubleReplaceScenario.MATDoubleReplaceScenario())
        self._bmhScenarios.append(ExclusiveExpiresToHighScenario.ExclusiveExpiresToHighScenario())
        self._bmhScenarios.append(InterruptScenario.InterruptScenario())
        self._bmhScenarios.append(InterruptInterruptScenario.InterruptInterruptScenario())
        self._bmhScenarios.append(FutureInterruptScenario.FutureInterruptScenario())
        self._bmhScenarios.append(FutureTriggerScenario.FutureTriggerScenario())
        self._bmhScenarios.append(NoTriggerExclusiveScenario.NoTriggerExclusiveScenario())
        self._bmhScenarios.append(PeriodicScenario.PeriodicScenario())
        self._bmhScenarios.append(SixTriggersScenario.SixTriggersScenario())
        self._bmhScenarios.append(TriggerNowAndLaterScenario.TriggerNowAndLaterScenario())
        self._bmhScenarios.append(GeneralOrderedScenario.GeneralOrderedScenario())
        self._bmhScenarios.append(GeneralReversedScenario.GeneralReversedScenario())
        self._bmhScenarios.append(GeneralUnorderedScenario.GeneralUnorderedScenario())
        self._bmhScenarios.append(SAMEAlertWxrScenario.SAMEAlertWxrScenario())
        self._bmhScenarios.append(SAMEWxrScenario.SAMEWxrScenario())
        self._bmhScenarios.append(SAMEAlertCivScenario.SAMEAlertCivScenario())
        self._bmhScenarios.append(SAMECivScenario.SAMECivScenario())
        self._bmhScenarios.append(NonSAMEMultipleTrx.NonSAMEMultipleTrx())
        self._bmhScenarios.append(IdentityReplaceSameMRD.IdentityReplaceSameMRD())
        self._bmhScenarios.append(IdentityDifferentMRD.IdentityDifferentMRD())
        self._bmhScenarios.append(SixInterruptsScenario.SixInterruptsScenario())
        self._bmhScenarios.append(SixTriggersInterruptsCombineScenario.SixTriggersInterruptsCombineScenario())
        self._bmhScenarios.append(MRDMultipleTrxReplace.MRDMultipleTrxReplace())
        self._bmhScenarios.append(MRDMultipleTrxReplaceWithAddl.MRDMultipleTrxReplaceWithAddl())
        self._bmhScenarios.append(MATInterruptReplaceScenario.MATInterruptReplaceScenario())
        self._bmhScenarios.append(MATMultipleTrxReplaceScenario.MATMultipleTrxReplaceScenario())
        self._bmhScenarios.append(MATNoReplaceScenario.MATNoReplaceScenario())
        self._bmhScenarios.append(FutureIdentityScenario.FutureIdentityScenario())
        self._bmhScenarios.append(MRDFollowsScenario.MRDFollowsScenario())
        self._bmhScenarios.append(MRDPlaysScenario.MRDPlaysScenario())
        self._bmhScenarios.append(MRDInterruptReplaceSame.MRDInterruptReplaceSame())
        self._bmhScenarios.append(MRDInterruptReplaceDiff.MRDInterruptReplaceDiff())

    def _buildNameMappingForPrompt(self):
        self._scenarioCount = len(self._bmhScenarios)
        if (self._scenarioCount == 0):
            return        
        
        self._scenarioLookupMap = dict()
        for i in range(0, self._scenarioCount):
            scenario = self._bmhScenarios[i]
            self._scenarioLookupMap[scenario.getName()] = scenario
            self._scenarioLookupMap[str(i)] = scenario
    
    def runTests(self):
        dataDirectory = '/awips2/bmh/testSuite/data' # the default installed location.
        if not(os.path.exists(dataDirectory)):
            dataDirectory = raw_input('Enter the location of the directory containing the scenario data: ')
        
        print 'Using Data Directory: ', dataDirectory
        
        # create the large data file if it does not exist.
        largeFilePath = os.path.join(dataDirectory, 'parsing', 'MSG_LARGE_FILE')
        if not(os.path.exists(largeFilePath)):
            # create the large file.
            subprocess.call(['fallocate', '-l', '1G', largeFilePath])
        
        # prompt user for the TEST to run.
        #dataDirectory = '/common/bkowal/git/BMH-TEST/BMH/test-data'
        print ''
        print 'Available Scenario(s)'
        print '---------------------'
        for i in range(0, self._scenarioCount):
            scenario = self._bmhScenarios[i]
            print str(i) + ")" + scenario.getName()
        scenarioToRun = self._promptScenario()
        if scenarioToRun == 'ALL':
            self._runAllTests(dataDirectory)
        elif scenarioToRun == '\q':
            print 'Exiting ...'
            exit(0)
        else:
            continuePrompt = True
            while continuePrompt:
                self._runTest(dataDirectory, scenarioToRun)
                scenarioToRun = self._promptScenario()
                if scenarioToRun == '\q':
                    continuePrompt =  False
                    print 'Exiting ...'
            
    def _promptScenario(self):
        scenarioToRun = raw_input('Enter the name or number of the scenario you would like to run (use ALL to run all scenarios | use \q to quit): ')
        
        return scenarioToRun
            
    def _runAllTests(self, dataDirectory):
        self._scenarioCount = len(self._bmhScenarios)
        if (self._scenarioCount == 0):
            return        
        
        for i in range(0, self._scenarioCount):
            scenario = self._bmhScenarios[i]
            # data directory will not be hard-coded in the final version.
            scenario.execute(dataDirectory)
    
    def _runTest(self, dataDirectory, scenarioName):
        scenario = self._scenarioLookupMap.get(scenarioName)
        if scenario is None:
            print 'No scenario exists with name: ', scenarioName
            return
        
        scenario.execute(dataDirectory)

tr = TestRunner()
tr.runTests()
