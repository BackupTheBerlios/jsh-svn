# ----------------------------------------------------------------------------
#
# $Source: /tmp/cvs/onzen/Makefile,v $
# $Revision: 1.6 $
# $Author: torsten $
# Contents: Makefile for jsh
# Systems: all
#			   
# ----------------------------------------------------------------------------

#---------------------------------- settings ---------------------------------

PLATFORM           = Linux
JAVA_DATA_MODEL    := $(shell if test ! -f classes/DataModel.class; then install -d classes; javac -d classes src/DataModel.java; fi; java -cp classes DataModel)

ENABLE_DEBUG       = no

#------------------------------------ paths ----------------------------------

SWT_DIR            = 
LAUNCH4J_DIR       = 

#--------------------------------- tool chain --------------------------------

JAVA               = java
JAVA_FLAGS         = -Xmx256m -enableassertions

JAVAC              = javac
JAVAC_FLAGS        = -source 1.5
# -Xlint

JAR                = jar

SWT_JAR_LINUX      = jars/linux/swt-3.6.1.jar
SWT_JAR_LINUX_64   = jars/linux/swt-3.6.1_64.jar
SWT_JAR_WINDOWS    = jars/windows/swt-3.6.1.jar
SWT_JAR_WINDOWS_64 = jars/windows/swt-3.6.1_64.jar
SWT_JAR_SOLARIS    = jars/solaris/swt-3.6.1.jar
SWT_JAR_MACOSX     = jars/macosx/swt-3.6.1.jar
SWT_JAR_MACOSX_64  = jars/macosx/swt-3.6.1_64.jar

ifneq ($(SWT_DIR),)
SWT_JAR          = $(SWT_DIR)/swt.jar
else
ifeq ($(PLATFORM),Linux)
ifeq ($(JAVA_DATA_MODEL),64)
SWT_JAR          = $(SWT_JAR_LINUX_64)
else
SWT_JAR          = $(SWT_JAR_LINUX)
endif
endif
ifeq ($(PLATFORM),Windows)
ifeq ($(JAVA_DATA_MODEL),64)
SWT_JAR          = $(SWT_JAR_WINDOWS_64)
else
SWT_JAR          = $(SWT_JAR_WINDOWS)
endif
endif
ifeq ($(PLATFORM),Solaris)
SWT_JAR          = $(SWT_JAR_SOLARIS)
endif
ifeq ($(PLATFORM),MacOSX)
ifeq ($(JAVA_DATA_MODEL),64)
SWT_JAR          = $(SWT_JAR_MACOSX_64)
else
SWT_JAR          = $(SWT_JAR_MACOSX)
endif
endif
endif
ANTL_JAR         = jars/antlr-3.2.jar
GROOVY_JAR       = jars/groovy-all-1.8.6.jar
SQLITE_JAR       = jars/sqlitejdbc.jar
MAIL_JAR         = jars/mail.jar

SOURCEPATH       = tmp:src
CLASSPATH        = classes:$(SWT_JAR):$(ANTL_JAR):$(GROOVY_JAR):$(SQLITE_JAR):$(MAIL_JAR)

SHELL = bash

#---------------------------------- commands----------------------------------

CAT        = cat
CD         = cd
CHMOD      = chmod
CP         = cp
DIFF       = diff
ECHO       = echo
ECHO_NO_LF = echo -n
GREP       = grep
INSTALL    = /usr/bin/install -c
MD5SUM     = md5sum
MKDIR      = mkdir
PERL       = /usr/bin/perl
RMDIR      = rmdir
RMF        = rm -f
RMRF       = rm -rf
SED        = sed
TAR        = tar
ZIP        = zip
UNZIP      = unzip

#------------------------ specific variables/settings ------------------------

VERSION_MAJOR := $(shell $(CAT) version|$(GREP) MAJOR|$(SED) 's/MAJOR=//g')
VERSION_MINOR := $(shell $(CAT) version|$(GREP) MINOR|$(SED) 's/MINOR=//g')

DISTRIBUTION_NAME = jsh-$(VERSION_MAJOR).$(VERSION_MINOR)

#---------------------------------- rules ------------------------------------

classes/%.class: \
  tmp/%.java
	$(INSTALL) -d classes
ifneq ($(JAVAC),)
	$(JAVAC) $(JAVAC_FLAGS) -d classes -classpath $(CLASSPATH) -sourcepath $(SOURCEPATH) tmp/*.java
else
	@$(ECHO) "ERROR: no 'javac' command - cannot compile Java sources"
endif

classes/%.class: \
  src/%.java
	$(MAKE) $(INTERMEDIATE)
        # >/dev/null
	$(INSTALL) -d classes
ifneq ($(JAVAC),)
	$(JAVAC) $(JAVAC_FLAGS) -d classes -classpath $(CLASSPATH) -sourcepath $(SOURCEPATH) src/*.java
else
	@$(ECHO) "ERROR: no 'javac' command - cannot compile Java sources"
endif

#--------------------------------- objects -----------------------------------

SOURCES        = src/Jsh.java \
                 src/Command.java \
                 src/Exec.java \
                 src/Settings.java \
                 \
                 src/TableLayout.java \
                 src/TableLayoutData.java \
                 src/Pane.java \
                 src/ProgressBar.java \
                 src/Widgets.java \
                 src/Dialogs.java \
                 src/StringParser.java \
                 src/StringUtils.java \
                 src/Option.java \
                 src/Options.java \
                 src/OptionEnumeration.java \
                 src/OptionSpecial.java \
                 src/Dprintf.java \
                 \
                 src/DataModel.java \
\
src/ls.java

INTERMEDIATE   = tmp/Config.java \
                 tmp/SimpleJavaRecognizerLexer.java tmp/SimpleJavaRecognizerParser.java tmp/SimpleJavaRecognizer.tokens \
                 tmp/ShellLexer.java tmp/ShellParser.java tmp/Shell.tokens \
                 tmp/ShellEval.java tmp/ShellEval.tokens \

OTHER_SOURCES  = jsh.in \
                 jsh.bat.in \
                 jar.txt \

OBJECTS        = $(foreach z,$(SOURCES),$(basename $(subst src/,classes/,$z)).class) \
                 classes/Config.class \
                 classes/SimpleJavaRecognizerLexer.class classes/SimpleJavaRecognizerParser.class \
                 classes/ShellLexer.class classes/ShellParser.class \
                 classes/ShellEval.class \

JARS           = jsh.jar \
                 jsh-linux.jar \
                 jsh-linux_64.jar \
                 jsh-windows.jar \
                 jsh-windows_64.jar \
                 jsh-solaris.jar \
                 jsh-macosx.jar \
                 jsh-macosx_64.jar \

OTHER_JARS     = $(SQLITE_JAR) \
                 $(MAIL_JAR) \

TARGETS        = jsh \
                 $(if $(LAUNCH4J),jsh.exe,jsh.bat) \

#------------------------------ dependencies ---------------------------------

.PHONY: all clean distclean depend
all: $(TARGETS)

clean: \
  clean_jars
	$(RMF) $(INTERMEDIATE)
	$(RMF) $(OBJECTS)
	$(RMF) classes/*.class
	$(RMF) $(TARGETS)
	$(RMRF) tmp

distclean: \
  clean

# config
tmp/Config.java: src/Config.java.in
	$(INSTALL) -d tmp
	$(CAT) src/Config.java.in | \
        ( \
          source version; \
          while IFS="" read line; do \
            line=$${line//@VERSION_MAJOR@/$$MAJOR}; \
            line=$${line//@VERSION_MINOR@/$$MINOR}; \
            line=$${line//@VERSION_REVISION@/$(shell svnversion .)}; \
            $(ECHO) "$$line"; \
          done; \
        ) > $@       

# create simple Java source code recognizer
tmp/SimpleJavaRecognizerLexer.java tmp/SimpleJavaRecognizerParser.java: src/SimpleJavaRecognizer.g
	$(INSTALL) -d tmp
	$(JAVA) $(JAVA_FLAGS) -jar $(ANTL_JAR) -fo tmp src/SimpleJavaRecognizer.g 

# create shell command parser/lexer
tmp/ShellLexer.java tmp/ShellParser.java: src/Shell.g
	$(INSTALL) -d tmp
	$(JAVA) $(JAVA_FLAGS) -jar $(ANTL_JAR) -fo tmp src/Shell.g

# create shell command evaluator
tmp/ShellEval.java: src/ShellEval.g src/Shell.g
	$(INSTALL) -d tmp
	$(JAVA) $(JAVA_FLAGS) -jar $(ANTL_JAR) -fo tmp src/ShellEval.g 

# JARs
.PHONY: jars clean_jars
jars: $(JARS)

clean_jars:
	$(RMF) $(JARS)

# create JARs
jsh-linux.jar: jar.txt $(OBJECTS) images/*.png $(SWT_JAR_LINUX)
ifneq ($(JAR),)
	$(RMRF) tmp/jar; $(INSTALL) -d tmp/jar
	# add classes
	$(CP) classes/*.class tmp/jar
	# add SWT JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SWT_JAR_LINUX) 1>/dev/null; $(RMRF) META-INF)
	# add SQLite JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SQLITE_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add mail JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(MAIL_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add images
	$(INSTALL) -d tmp/jar/images
	$(CP) images/*.png tmp/jar/images
	# create combined JAR
	($(CD) tmp/jar; $(JAR) cmf ../../jar.txt ../../$@ *)
	$(RMRF) tmp/jar
else
	@$(ECHO) "ERROR: no 'jar' command - cannot create $@"
endif

jsh-linux_64.jar: jar.txt $(OBJECTS) images/*.png $(SWT_JAR_LINUX_64)
ifneq ($(JAR),)
	$(RMRF) tmp/jar; $(INSTALL) -d tmp/jar
	# add classes
	$(CP) classes/*.class tmp/jar
	# add SWT JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SWT_JAR_LINUX_64) 1>/dev/null; $(RMRF) META-INF)
	$(CP) classes/*.class tmp/jar
	# add SQLite JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SQLITE_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add mail JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(MAIL_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add images
	$(INSTALL) -d tmp/jar/images
	$(CP) images/*.png tmp/jar/images
	# create combined JAR
	($(CD) tmp/jar; $(JAR) cmf ../../jar.txt ../../$@ *)
	$(RMRF) tmp/jar
else
	@$(ECHO) "ERROR: no 'jar' command - cannot create $@"
endif

jsh-windows.jar: jar.txt $(OBJECTS) images/*.png $(SWT_JAR_WINDOWS)
ifneq ($(JAR),)
	$(RMRF) tmp/jar; $(INSTALL) -d tmp/jar
	# add classes
	$(CP) classes/*.class tmp/jar
	# add SWT JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SWT_JAR_WINDOWS) 1>/dev/null; $(RMRF) META-INF)
	$(CP) classes/*.class tmp/jar
	# add SQLite JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SQLITE_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add mail JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(MAIL_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add images
	$(INSTALL) -d tmp/jar/images
	$(CP) images/*.png tmp/jar/images
	# create combined JAR
	($(CD) tmp/jar; $(JAR) cmf ../../jar.txt ../../$@ *)
	$(RMRF) tmp/jar
else
	@$(ECHO) "ERROR: no 'jar' command - cannot create $@"
endif

jsh-windows_64.jar: jar.txt $(OBJECTS) images/*.png $(SWT_JAR_WINDOWS_64)
ifneq ($(JAR),)
	$(RMRF) tmp/jar; $(INSTALL) -d tmp/jar
	# add classes
	$(CP) classes/*.class tmp/jar
	# add SWT JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SWT_JAR_WINDOWS_64) 1>/dev/null; $(RMRF) META-INF)
	$(CP) classes/*.class tmp/jar
	# add SQLite JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SQLITE_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add mail JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(MAIL_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add images
	$(INSTALL) -d tmp/jar/images
	$(CP) images/*.png tmp/jar/images
	# create combined JAR
	($(CD) tmp/jar; $(JAR) cmf ../../jar.txt ../../$@ *)
	$(RMRF) tmp/jar
else
	@$(ECHO) "ERROR: no 'jar' command - cannot create $@"
endif

jsh-solaris.jar: jar.txt $(OBJECTS) images/*.png $(SWT_JAR_SOLARIS)
ifneq ($(JAR),)
	$(RMRF) tmp/jar; $(INSTALL) -d tmp/jar
	# add classes
	$(CP) classes/*.class tmp/jar
	# add SWT JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SWT_JAR_SOLARIS) 1>/dev/null; $(RMRF) META-INF)
	$(CP) classes/*.class tmp/jar
	# add SQLite JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SQLITE_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add mail JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(MAIL_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add images
	$(INSTALL) -d tmp/jar/images
	$(CP) images/*.png tmp/jar/images
	# create combined JAR
	($(CD) tmp/jar; $(JAR) cmf ../../jar.txt ../../$@ *)
	$(RMRF) tmp/jar
else
	@$(ECHO) "ERROR: no 'jar' command - cannot create $@"
endif

jsh-macosx.jar: jar.txt $(OBJECTS) images/*.png $(SWT_JAR_MACOSX)
ifneq ($(JAR),)
	$(RMRF) tmp/jar; $(INSTALL) -d tmp/jar
	# add classes
	$(CP) classes/*.class tmp/jar
	# add SWT JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SWT_JAR_MACOSX) 1>/dev/null; $(RMRF) META-INF)
	$(CP) classes/*.class tmp/jar
	# add SQLite JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SQLITE_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add mail JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(MAIL_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add images
	$(INSTALL) -d tmp/jar/images
	$(CP) images/*.png tmp/jar/images
	# create combined JAR
	($(CD) tmp/jar; $(JAR) cmf ../../jar.txt ../../$@ *)
	$(RMRF) tmp/jar
else
	@$(ECHO) "ERROR: no 'jar' command - cannot create $@"
endif

jsh-macosx_64.jar: jar.txt $(OBJECTS) images/*.png $(SWT_JAR_MACOSX_64)
ifneq ($(JAR),)
	$(RMRF) tmp/jar; $(INSTALL) -d tmp/jar
	# add classes
	$(CP) classes/*.class tmp/jar
	# add SWT JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SWT_JAR_MACOSX_64) 1>/dev/null; $(RMRF) META-INF)
	$(CP) classes/*.class tmp/jar
	# add SQLite JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(SQLITE_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add mail JAR
	($(CD) tmp/jar; $(UNZIP) ../../$(MAIL_JAR) 1>/dev/null; $(RMRF) META-INF)
	# add images
	$(INSTALL) -d tmp/jar/images
	$(CP) images/*.png tmp/jar/images
	# create combined JAR
	($(CD) tmp/jar; $(JAR) cmf ../../jar.txt ../../$@ *)
	$(RMRF) tmp/jar
else
	@$(ECHO) "ERROR: no 'jar' command - cannot create $@"
endif

# create Unix start script
jsh: jsh.in jsh-linux.jar jsh-linux_64.jar
	$(CAT) jsh.in | \
        ( \
          while IFS="" read line; do \
            line=`$(ECHO) "$$line"|$(SED) 's|\@JAVA\@|$(JAVA)|g'`; \
            line=`$(ECHO) "$$line"|$(SED) 's|\@JAVA_FLAGS\@|$(JAVA_FLAGS)|g'`; \
            $(ECHO) "$$line"; \
          done; \
        ) > $@
	$(CHMOD) 775 $@

ifneq ($(LAUNCH4J),)
# create Windows executable
jsh.exe: jsh-windows.jar jsh-windows_64.jar
	$(LAUNCH4J) jsh.xml
else
# create Windows start script
jsh.bat: jsh.bat.in jsh-windows.jar jsh-windows_64.jar
	$(CAT) jsh.bat.in | \
        ( \
          while IFS="" read line; do \
            line=`$(ECHO) "$$line"|$(SED) 's|\@JAVA\@|$(JAVA)|g'`; \
            line=`$(ECHO) "$$line"|$(SED) 's|\@JAVA_FLAGS\@|$(JAVA_FLAGS)|g'`; \
            $(ECHO) "$$line"; \
          done; \
        ) > $@
	$(CHMOD) 775 $@
endif

.PHONY: linux
linux: jsh jsh-linux.jar jsh-linux_64.jar

.PHONY: windows
windows: jsh.bat jsh-windows.jar jsh-windows_64.jar

.PHONY: solaris
solaris: jsh jsh-solaris.jar jsh-solaris_64.jar

.PHONY: macosx
macosx: jsh jsh-macosx.jar jsh-macosx_64.jar

# run
run: $(OBJECTS)
ifneq ($(JAVA),)
	$(JAVA) $(JAVA_FLAGS) -classpath $(CLASSPATH) Jsh $(OPTIONS)
else
	@$(ECHO) "ERROR: no 'java' command - cannot execute Jsh"
endif
rundebug: $(OBJECTS)
ifneq ($(JAVA),)
	$(JAVA) $(JAVA_FLAGS) -enableassertions -classpath $(CLASSPATH) Jsh --debug $(OPTIONS)
else
	@$(ECHO) "ERROR: no 'java' command - cannot execute Jsh"
endif
runxdebug: $(OBJECTS)
ifneq ($(JAVA),)
	$(JAVA) $(JAVA_FLAGS) -enableassertions -classpath $(CLASSPATH) Jsh --debug $(OPTIONS)
else
	@$(ECHO) "ERROR: no 'java' command - cannot execute Jsh"
endif

# run with JAR file
ifneq ($(JAVA),)
runjar: jsh
	./jsh
else
	@$(ECHO) "ERROR: no 'java' commadn - cannot execute jsh"
endif

.PHONY: install
#install: $(JARS) jsh
install: jsh-linux.jar jsh
	$(INSTALL) -d $(HOME)/bin
	$(INSTALL) -m 664 \
                   jsh-linux.jar \
                   jsh-linux_64.jar \
                   $(HOME)/bin
	$(INSTALL) -m 775 \
                   jsh \
                   $(HOME)/bin

# --- distribution
.PHONY: dist clean_dist $(DISTRIBUTION_NAME).tar.bz2

dist: \
  $(DISTRIBUTION_NAME).tar.bz2 \
  $(DISTRIBUTION_NAME)-linux.tar.bz2 \
  $(DISTRIBUTION_NAME)-solaris.tar.bz2 \
  $(DISTRIBUTION_NAME)-macosx.tar.bz2 \
  $(DISTRIBUTION_NAME)-windows.zip

clean_dist:
	$(RMF) $(DISTRIBUTION_NAME).tar.bz2
	$(RMF) $(DISTRIBUTION_NAME)-linux.tar.bz2
	$(RMF) $(DISTRIBUTION_NAME)-solaris.tar.bz2
	$(RMF) $(DISTRIBUTION_NAME)-macosx.tar.bz2
	$(RMF) $(DISTRIBUTION_NAME)-windows.zip

# create distribution
$(DISTRIBUTION_NAME).tar.bz2: \
  $(TARGETS) \
  \
  Makefile \
  version \
  ToDo \
  ChangeLog \
  \
  $(OTHER_SOURCES) \
  $(OTHER_JARS) \
  \
  $(SCRIPTS) \
  \
  doc/README \
  doc/COPYING \
  $(MAN)
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)
	$(INSTALL) -m 775 \
                   $(OTHER_SOURCES) \
                   tmp/$(DISTRIBUTION_NAME)
	$(INSTALL) -m 664 \
                   Makefile \
                   version \
                   ToDo \
                   ChangeLog \
                   tmp/$(DISTRIBUTION_NAME)
	#
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)/src
	$(INSTALL) -m 664 \
                   src/Config.java.in \
                   src/*.g \
                   $(sort $(SOURCES)) \
                   tmp/$(DISTRIBUTION_NAME)/src
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)/classes
	$(INSTALL) -m 664 \
                   $(sort $(OTHER_SOURCES)) \
                   tmp/$(DISTRIBUTION_NAME)
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)/images
	$(INSTALL) -m 664 \
                   images/*.png \
                   tmp/$(DISTRIBUTION_NAME)/images
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)/jars
	$(INSTALL) -m 664 \
                   $(OTHER_JARS) \
                   tmp/$(DISTRIBUTION_NAME)/jars
	#
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)/jars/linux
	$(INSTALL) -m 664 \
                   jars/linux/*.jar \
                   tmp/$(DISTRIBUTION_NAME)/jars/linux
	#
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)/jars/solaris
	$(INSTALL) -m 664 \
                   jars/solaris/*.jar \
                   tmp/$(DISTRIBUTION_NAME)/jars/solaris
	#
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)/jars/macosx
	$(INSTALL) -m 664 \
                   jars/macosx/*.jar \
                   tmp/$(DISTRIBUTION_NAME)/jars/macosx
	#
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)/jars/windows
	$(INSTALL) -m 664 \
                   jars/windows/*.jar \
                   tmp/$(DISTRIBUTION_NAME)/jars/windows
	#
	$(INSTALL) -d tmp/$(DISTRIBUTION_NAME)/doc
	$(INSTALL) -m 664 \
                   doc/README \
                   doc/COPYING \
                   tmp/$(DISTRIBUTION_NAME)/doc
	#
	($(CD) tmp; $(TAR) cjf ../$@ $(DISTRIBUTION_NAME))
	$(RMRF) tmp/$(DISTRIBUTION_NAME)
	#
ifneq ($(MD5SUM),)
	$(MD5SUM) $@
endif

$(DISTRIBUTION_NAME)-linux.tar.bz2: \
  jsh-linux.jar \
  jsh-linux_64.jar \
  jsh
	$(TAR) cjf $@ \
          jsh-linux.jar jsh-linux_64.jar \
          jsh

$(DISTRIBUTION_NAME)-solaris.tar.bz2: \
  jsh-solaris.jar \
  jsh
	$(TAR) cjf $@ \
          jsh-solaris.jar \
          jsh

$(DISTRIBUTION_NAME)-macosx.tar.bz2: \
  jsh-macosx.jar \
  jsh-macosx_64.jar \
  jsh
	$(TAR) cjf $@ \
          jsh-macosx.jar jsh-macosx_64.jar \
          jsh

$(DISTRIBUTION_NAME)-windows.zip: \
  jsh-windows.jar \
  jsh-windows_64.jar \
  jsh.bat
	$(ZIP) $@ \
          jsh-windows.jar jsh-windows_64.jar \
          jsh.bat

# end of file
