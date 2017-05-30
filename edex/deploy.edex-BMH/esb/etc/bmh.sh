# TODO: specify required unique ports for the BMH EDEX instance.

export BMH_HOME=/awips2/bmh
export BMH_DATA=/awips2/bmh/data
export LOG_CONF=logback-bmh.xml

export INIT_MEM=128 # in Meg
export MAX_MEM=384 # in Meg

export JAVA_SECURITY_OPTION=-Djava.security.properties=/awips2/edex/conf/java.security.allow-md5
