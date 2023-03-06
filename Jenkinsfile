#!groovy
def BN = (BRANCH_NAME == 'master' || BRANCH_NAME.startsWith('releases/')) ? BRANCH_NAME : 'releases/2023-07'

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

    // We need to exclude macosx from the workflowtest configuration, so we
    // set the configs for the workflowtests here
    String[] fsConfigurations = [ ]
    if (params.USE_DEFAULT_TEST_CONFIGURATION == false) {
        // this must be a real boolean test, because if the parameter is missing completey we don't want to end up here
        for (c in (workflowTests.ALL_CONFIGURATIONS - 'macosx')) {
            if (params[c]) {
                fsConfigurations += c
            }
        }
    } else if (BRANCH_NAME.startsWith('releases/')) {
        fsConfigurations = workflowTests.DEFAULT_CONFIGURATIONS - 'macosx'
    } else {
        fsConfigurations = workflowTests.DEFAULT_FEATURE_BRANCH_CONFIGURATIONS - 'macosx'
    }

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
                configurations: fsConfigurations,
                dependencies: [
                    repositories: [
                        'knime-dl4j',
                        'knime-distance',
                        'knime-js-base',
                        'knime-textprocessing',
                        'knime-datageneration',
                        'knime-r',
                        'knime-database',
                        'knime-kerberos'
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
    notifications.notifyBuild(currentBuild.result)
}
/* vim: set shiftwidth=4 expandtab smarttab: */
