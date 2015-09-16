#!/usr/bin/env python

import subprocess
import signal
import os
import sys
#import psutil
from optparse import OptionParser

# Argument definition
usage = "usage: %prog [options] arg"
parser = OptionParser(usage)
parser.add_option("-i", "--install-file", dest="filename", help="AEM install file")
parser.add_option("-r", "--runmode", dest="runmode",help="Run mode for the installation")
parser.add_option("-p", "--port", dest="port", help="Port for instance")

options, args = parser.parse_args()
filename = options.filename
runmode = options.runmode
port = options.port

print options
print options.filename, options.runmode, options.port

# Starts AEM installer
# Waits for connection on 5007, and then checks that the returned
# success message has been recieved.
installProcess = subprocess.Popen([
    'java', 
    '-jar', options.filename, 
    '-v',
    '-listener-port', '50007', 
    '-r', options.runmode, 
    'nosamplecontent', 
    '-p', options.port
])

print 'starting AEM (pid): %s' % (installProcess.pid)

# Starting listener
import socket
HOST = ''
PORT = 50007
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((HOST, PORT))
s.listen(1)
conn, addr = s.accept()

successfulStart = False
data = None
while not data:
    data = conn.recv(1024)
#    if not data:
#      break
#    else:
    print '**********************************'
    print str(data).strip()
    print '**********************************'
    if str(data).strip() == 'started':
      successfulStart = True

    #  break
    # conn.sendall(data)
conn.close()
print successfulStart


#Post install hook
postInstallHook = "postInstallHook.py"
if os.path.isfile(postInstallHook):
    print "Executing post install hook"
    returncode = subprocess.call(["python", postInstallHook])
    print returncode
else:
    print "No install hook found"


print "Stopping instance"

# If the success message was recieved, attempt to close all associated processes.
if successfulStart == True:
#  parentAEMprocess= psutil.Process(installProcess.pid)
#  for childProcess in parentAEMprocess.get_children():
#    os.kill(childProcess.pid,signal.SIGINT)
#
#  os.kill(parentAEMprocess.pid, signal.SIGINT)

  installProcess.kill()
  installProcess.wait()
  sys.exit(0)
else:
  installProcess.kill()
  sys.exit(1)
