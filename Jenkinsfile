#!groovy
def BN = BRANCH_NAME == "master" || BRANCH_NAME.startsWith("releases/") ? BRANCH_NAME : "master"

library "knime-pipeline@$BN"

properties([
    pipelineTriggers([
        upstream('knime-base/' + env.BRANCH_NAME.replaceAll('/', '%2F'))
    ]),
    parameters(workflowTests.getConfigurationsAsParameters() + fsTests.getFSConfigurationsAsParameters()),
    buildDiscarder(logRotator(numToKeepStr: '5')),
    disableConcurrentBuilds()
])

try {
    knimetools.defaultTychoBuild('org.knime.update.ext.dl4j')

    testConfigs = [
        WorkflowTests: {
             workflowTests.runTests(
                 dependencies: [
                     repositories: ['knime-dl4j', 'knime-wide-data', 'knime-filehandling',
                     'knime-datageneration', 'knime-textprocessing', 'knime-distance',
                     'knime-r', 'knime-js-base', 'knime-database', 'knime-kerberos'],
                 ]
             )
       },
       FilehandlingTests: {
            workflowTests.runFilehandlingTests (
                dependencies: [
                    repositories: [
                        "knime-dl4j", "knime-distance", "knime-js-base", "knime-datageneration", "knime-r", "knime-database", "knime-kerberos"
                    ]
                ],
            )
        }
    ]

    parallel testConfigs

    stage('Sonarqube analysis') {
        env.lastStage = env.STAGE_NAME
        workflowTests.runSonar()
    }
} catch (ex) {
    currentBuild.result = 'FAILURE'
    throw ex
} finally {
    notifications.notifyBuild(currentBuild.result);
}
/* vim: set shiftwidth=4 expandtab smarttab: */
