#!/bin/bash
java -classpath JAAS.jar:Simple.jar:EmailHandler.jar:javax.mail.jar -Djava.security.manager -Djava.security.policy=JAAS.policy -Djava.security.auth.login.config=JAAS.config JAAS.JAASClient
