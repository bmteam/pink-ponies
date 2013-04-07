#!/usr/bin/python

""" Provides style checking for java and python using checkstyle and pylint. """

import fnmatch
import subprocess
import os
import sys
import re
import xml.etree.ElementTree as ET

CHECKSTYLE_CONFIG = "javastyle.xml"
PYLINT_CONFIG = "pythonstyle.conf"

PYLINT_ERROR_FMT = re.compile(r"""
^(?P<file>.+?):(?P<line>[0-9]+):\ # file name and line number
\[(?P<type>[a-z])(?P<errno>\d+)?   # message type and error number, e.g. E0101
(,\ (?P<hint>.+))?\]\             # optional class or function name
(?P<msg>.*)                       # finally, the error message
""", re.IGNORECASE|re.VERBOSE)

def get_hg_status():
  """ Runs the hg status command and parses its output. Returns a list of
  changed files. """
  output = str(subprocess.check_output(["hg", "st", "-man"]))
  return [line for line in output.split("\n") if line != '']

def run_checkstyle(file_name):
  """ Runs the checkstyle command on the given file and returns
  a list of all the errors. """

  # Run checkstyle.
  output = None
  try:
    with open(os.devnull, "w") as fnull:
      cmd = ["checkstyle", "-c", CHECKSTYLE_CONFIG, "-f", "xml", file_name]
      subprocess.check_output(cmd, stderr=fnull)
  except subprocess.CalledProcessError as err:
    output = err.output

  # If there were no errors, return an empty list.
  if output == None:
    return []

  # Parse checkstyle output.
  root = None
  try:
    root = ET.fromstring(output)
  except ET.ParseError as err:
    return [(0, "Could not parse checkstyle output: %s\n\
                 Checkstyle output: %s" % (err, output))]

  # There is no way it is None.
  assert root != None
  assert len(root) == 1

  # Collect all error messages.
  result = []
  for child in root:
    for err in child:
      line = err.attrib["line"]
      message = err.attrib["message"]
      result.append((line, message))

  return result

def check_java_files(all_files):
  """ Checks all java files from the given list for stylistic mistakes. """
  files = [path for path in all_files if fnmatch.fnmatch(path, '*.java')]

  error_count = 0
  for filename in files:
    errors = run_checkstyle(filename)
    error_count += len(errors)

    for err in errors:
      print "%s:%s - %s" % (filename, err[0], err[1])

  return error_count

def run_pylint(file_name):
  """ Runs the pylint command on the given file and returns
  a list of all the errors. """

  # Run checkstyle.
  output = None
  try:
    with open(os.devnull, "w") as fnull:
      cmd = ["pylint", "--rcfile", PYLINT_CONFIG, file_name]
      subprocess.check_output(cmd, stderr=fnull)
  except subprocess.CalledProcessError as err:
    output = err.output

  # If there were no errors, return an empty list.
  if output == None:
    return []

  result = []
  for line in output.splitlines():
    match = PYLINT_ERROR_FMT.match(line)

    if match == None:
      result.append((0, "Could not parse pylint output: %s" % line))
      continue

    line, hint, msg = match.group('line', 'hint', 'msg')
    result.append((line, "in %s, %s" % (hint, msg) if hint != None else msg))

  return result

def check_python_files(all_files):
  """ Checks all python files from the given list for stylistic mistakes. """
  files = [path for path in all_files if fnmatch.fnmatch(path, '*.py')]

  error_count = 0
  for filename in files:
    errors = run_pylint(filename)
    error_count += len(errors)

    for err in errors:
      print "%s:%s - %s" % (filename, err[0], err[1])

  return error_count

def main(_):
  """ Main checkstyle method. """
  files = get_hg_status()
  error_count = 0
  error_count += check_java_files(files)
  error_count += check_python_files(files)

  sys.exit(error_count)

if __name__ == "__main__":
  main(sys.argv)