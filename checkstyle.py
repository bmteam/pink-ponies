#!/usr/bin/python

import fnmatch
import subprocess
import os
import sys
import xml.etree.ElementTree as ET

CHECKSTYLE_JAVA_STYLE_XML = "javastyle.xml"

def get_hg_status():
  """ Runs the hg status command and parses its output. Returns a list of
  changed files. """
  return filter(None, subprocess.check_output(["hg", "st", "-man"]).split("\n"))

def run_checkstyle(file_name):
  """ Runs the checkstyle command on the given file and returns
  a list of all the errors. """

  # Run checkstyle.
  output = None
  try:
    with open(os.devnull, "w") as fnull:
      cmd = ["checkstyle", "-c", CHECKSTYLE_JAVA_STYLE_XML, "-f", "xml", file_name]
      out = subprocess.check_output(cmd, stderr=fnull)
  except subprocess.CalledProcessError as e:
    output = e.output

  # If there were no errors, return an empty list.
  if output == None:
    return []

  # Parse checkstyle output.
  root = None
  try:
    root = ET.fromstring(output)
  except ET.ParseError as pe:
    return [(0, "Could not parse checkstyle output: %s\nCheckstyle output: %s" % (pe, output))]

  # There is no way it is None.
  assert root != None

  # Collect all error messages.
  result = []
  for file in root:
    for err in file:
      line = err.attrib["line"]
      message = err.attrib["message"]
      result.append((line, message))

  return result

def check_java_files(all_files):
  """ Checks all java files from the given list for stylistic mistakes. """
  files = filter(lambda path: fnmatch.fnmatch(path, '*.java'), all_files)

  errorCount = 0
  for filename in files:
    errors = run_checkstyle(filename)
    errorCount += len(errors)

    for err in errors:
      print "%s:%s - %s" % (filename, err[0], err[1])

  return errorCount

if __name__ == "__main__":
  files = get_hg_status()
  errorCount = 0
  errorCount += check_java_files(files)

  sys.exit(errorCount)