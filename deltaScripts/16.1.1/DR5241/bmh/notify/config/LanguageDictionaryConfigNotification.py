##
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
# 
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
# 
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
# 
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.
##

# File auto-generated against equivalent DynamicSerialize Java class
# 
#      SOFTWARE HISTORY
# 
#     Date            Ticket#       Engineer       Description
#     ------------    ----------    -----------    --------------------------
#     Jan 12, 2016                  bkowal         Generated

class LanguageDictionaryConfigNotification(object):

    def __init__(self):
        self.national = None
        self.traceId = None
        self.language = None
        self.type = None
        self.updatedWords = None

    def getNational(self):
        return self.national

    def setNational(self, national):
        self.national = national

    def getTraceId(self):
        return self.traceId

    def setTraceId(self, traceId):
        self.traceId = traceId

    def getLanguage(self):
        return self.language

    def setLanguage(self, language):
        self.language = language

    def getType(self):
        return self.type

    def setType(self, type):
        self.type = type

    def getUpdatedWords(self):
        return self.updatedWords

    def setUpdatedWords(self, updatedWords):
        self.updatedWords = updatedWords

