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

def hg_get_commit_changes():
  """ Runs the hg status command and parses its output. Returns a list of
  changed files. """
  files = str(subprocess.check_output(["hg", "st", "-man"]))
  return [line for line in files.splitlines() if line != '']

def hg_get_push_changes():
  """ Returns a list of all the changed files which are to be pushed
  to the remote repository. """

  commits = ""
  try:
    commits = str(subprocess.check_output(["hg", "outgoing", "-q"]))
  except subprocess.CalledProcessError:
    pass

  commits = [line for line in commits.splitlines() if line != '']
  commits = [line.partition(':')[2] for line in commits]

  all_files = set()
  for commit in commits:
    files = str(subprocess.check_output(["hg", "st", "-man", "--change",
                                         commit]))
    files = [line for line in files.splitlines() if line != '']
    for file_name in files:
      all_files.add(file_name)
  return list(all_files)

def hg_get_all_files():
  """ Returns a list of all the files in the repository. """
  files = str(subprocess.check_output(["hg", "st", "-man", "--all"]))
  return [line for line in files.splitlines() if line != '']

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

def main(files):
  """ Main checkstyle method. """
  error_count = 0
  error_count += check_java_files(files)
  error_count += check_python_files(files)

  sys.exit(error_count)

def parse_args():
  """ Parses arguments and runs main. """
  import argparse

  parser = argparse.ArgumentParser("checkstyle")
  parser.add_argument('files', type=str, nargs='*',
                      help="files to process")
  parser.add_argument('-a', '--all', action='store_true',
                      help='all the files in the repository')
  parser.add_argument('-c', '--commit', action='store_true',
                      help='files in the last commit')
  parser.add_argument('-p', '--push', action='store_true',
                      help='files which are to be pushed')

  args = parser.parse_args()
  files = args.files

  if args.all:
    files += hg_get_all_files()

  if args.commit:
    files += hg_get_commit_changes()

  if args.push:
    files += hg_get_push_changes()

  files = list(set(files))

  main(files)

if __name__ == "__main__":
  parse_args()