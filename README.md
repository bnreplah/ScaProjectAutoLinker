# Sca Project Auto Linker

## Overview

  Allows for linking an application profile to an agent-based SCA project. 
  As a default branch is mandatory, this can also be used to set the default branch for a project.

## Usage
    java -jar ScaProjectAutoLinker.jar -dn "<Selenium Driver Class (webdriver.gecko.driver recommended)>" -dl "<Selenium Driver file name, with extension>" -vu "<Veracode username>" -vp "<Veracode password>" -vi "<Veracode API ID>" -vk "<Veracode API Key>" -w "<Workspace name>" -p "<Project name>" -a "<Application profile name>" -b <Branch to set as default (optional, script will fail if this is not set and the project doesn't have a default branch already set)>

## License

[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

See the [LICENSE](LICENSE) file for details
