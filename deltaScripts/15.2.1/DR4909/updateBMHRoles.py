'''
Created on Sep 25, 2015

@author: bkowal
'''

import sys
import os.path
import xml.etree.ElementTree as ET
import xml.dom.minidom as minidom

DEMO_PERMISSION = 'bmh.dialog.demoMessage'
ADMIN_ROLE = 'bmh.admin'
COMMON_STATIC_SITE = os.path.join(os.sep + 'awips2', 'edex', 'data', \
                                  'utility', 'common_static', 'site')
BMH_ROLES = os.path.join('roles', 'bmhRoles.xml')

for siteDir in os.listdir(COMMON_STATIC_SITE):
    bmhRolesPath = os.path.join(COMMON_STATIC_SITE, siteDir, BMH_ROLES)
    
    if os.path.exists(bmhRolesPath):
        print 'Determining if bmh roles file: ' + bmhRolesPath + ' needs to be updated ...'
        xmlTree = ET.parse(bmhRolesPath)
        
        permissionFound = xmlTree.getroot().find('./permission[@id="' + DEMO_PERMISSION +'"]') is not None
            
        if not permissionFound:
            permissionCount = len(xmlTree.getroot().findall('./permission'))
            print 'Merging the ' + DEMO_PERMISSION + ' permission into the bmh roles file ...'
            
            # Need to add the new permission
            newPermission = ET.Element('permission', {'id' : DEMO_PERMISSION})
            description = ET.SubElement(newPermission, 'description')
            description.text = 'This permission allows the user to access the Demo Message dialog.'
            
            xmlTree.getroot().insert(permissionCount + 1, newPermission)
            
        # Determine if the new permission needs to be added to the admin role.
        adminNode = xmlTree.getroot().find('./role[@roleId="' + ADMIN_ROLE + '"]')
        if adminNode is None:
            print 'Error: roles file is inconsistent; failed to find the ' + ADMIN_ROLE + ' role!'
            sys.exit(1)
            
        rolePermissionNode = adminNode.find('./[rolePermission="' + DEMO_PERMISSION + '"]')
        if rolePermissionNode is None:
            'Adding the ' + DEMO_PERMISSION + ' permission to the ' + ADMIN_ROLE + ' role ...'
            rolePermissionNode = ET.SubElement(adminNode, 'rolePermission')
            rolePermissionNode.text = DEMO_PERMISSION
        
        # for some reason minidom adds a lot of extra empty lines and Python still leaves it as is.    
        roughString = ET.tostring(xmlTree.getroot(), 'UTF-8')
        reparsed = minidom.parseString(roughString)
        middleString = reparsed.toprettyxml(encoding='UTF-8', indent='  ')
        splitXMLStr = [line for line in middleString.split('\n') if line.strip() != '']
        cleanXMLStr = None
        for splitXMLStrComponent in splitXMLStr:
            if cleanXMLStr is None:
                cleanXMLStr = splitXMLStrComponent
            else:
                cleanXMLStr += '\n' + splitXMLStrComponent
        
        with open(bmhRolesPath, 'w') as outFile:
            outFile.write(cleanXMLStr)
        print 'Updates to bmh roles file: ' + bmhRolesPath + ' are complete.'
