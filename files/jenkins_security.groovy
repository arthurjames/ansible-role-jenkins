#!/usr/bin/groovy

import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

// Disable CLI remoting, because it's insecure.
// https://support.cloudbees.com/hc/en-us/articles/234709648-Disable-Jenkins-CLI

if (Jenkins.instance.getDescriptor("jenkins.CLI").get().isEnabled()) {
    Jenkins.instance.getDesriptor("jenkins.CLI").get().setEnabled(false)
    Jenkins.instance.save()
    println "Changed CLI remoting: now disabled."
}

// Disable deprecated agent protocols
// https://github.com/jenkinsci/jenkins/commit/efdd52e9e78cc057ea49a7d338ee575d131c1959
def agentProtocolWasDisabled = false
def instance = Jenkins.getInstance()
def agentProtocolNames = new HashSet(instance.getAgentProtocols())
jenkins.AgentProtocol.all().each { agentProtocol ->
    println "Checking: " + agentProtocol.name

    if (agentProtocol.isDeprecated() && agentProtocolNames.contains(agentProtocol.name)) {
	agentProtocolNames.remove(agentProtocol.name)
	agentProtocolWasDisabled = true
	println "Changed protocols: removed protocol: " + agentProtocol.name
    }
}

if (agentProtocolWasDisabled) {
    Jenkins.instance.setAgentProtocols(agentProtocolNames)
    Jenkins.instance.save()
}

// Enable agent-master access control.
// https://wiki.jenkins.io/display/JENKINS/Slave+To+Master+Access+Control
// https://stackoverflow.com/a/41588568/1851299
if (Jenkins.instance.injector.getInstance(jenkins.security.s2m.AdminWhitelistRule.class).getMasterKillSwitch()) {
    Jenkins.instance.injector.getInstance(jenkins.security.s2m.AdminWhitelistRule.class).setMasterKillSwitch(false)
    Jenkins.instance.save()
    println "Changed agent-master access control: now disabled."
}

// Enable CSRF
// https://wiki.jenkins.io/display/JENKINS/CSRF+Protection
if (Jenkins.instance.getCrumbIssuer() == null) {
    Jenkins.instance.setCrumbIssuer(new hudson.security.csrf.DefaultCrumbIssuer(true))
    Jenkins.instance.save()
    println "Changed CSRF: enabled."
}
