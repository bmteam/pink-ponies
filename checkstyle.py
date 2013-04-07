#!/usr/bin/python

import fnmatch
import subprocess
import os
import sys

import xml.etree.ElementTree as ET

if __name__ == "__main__":
  files = filter(None, subprocess.check_output(["hg", "st", "-man"]).split("\n"))
  files = filter(lambda path: fnmatch.fnmatch(path, '*.java'), files)

  errorsFound = 0

  for filename in files:
    cmd = ["checkstyle", "-c", "stylecheck.xml", "-f", "xml", filename]

    try:
      with open(os.devnull, "w") as fnull:
        out = subprocess.check_output(cmd, stderr=fnull)
    except subprocess.CalledProcessError as e:
      # Parse checkstyle output.
      try:
        root = ET.fromstring(e.output)
      except ET.ParseError as pe:
        print "Could not parse output: %s\n%s" % (pe, e.output)
        errorsFound += 1
        continue

      for file in root:
        for err in file:
          line = err.attrib["line"]
          message = err.attrib["message"]
          print "%s:%s : %s" % (filename, line, message)
          errorsFound += 1

  if errorsFound != 0:
    sys.exit(errorsFound)