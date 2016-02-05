#! /awips2/python/bin/python

import dynamicserialize.dstypes.com.raytheon.uf.common.bmh.notify.config.LanguageDictionaryConfigNotification as ConfigNotification

import sys
import dynamicserialize
from qpid.messaging import *

if len(sys.argv) < 2:
  broker =  "localhost:5672" 
else:
  broker = sys.argv[1]

connection = Connection(broker)

connection.open()
session = connection.session()

sender = session.sender("amq.topic/BMH.Config")

#configNotification = ConfigNotification.LanguageDictionaryConfigNotification()
configNotification = ConfigNotification()
configNotification.setNational(True)
configNotification.setLanguage('ENGLISH')
configNotification.setType('Update')
updatedWords = [ 'winds', 'record', 'lead', 'objects' ]
configNotification.setUpdatedWords(updatedWords)

sender.send(Message(dynamicserialize.serialize(configNotification)))

connection.close()
