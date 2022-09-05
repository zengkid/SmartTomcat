<!-- Keep a Changelog guide -> https://keepachangelog.com -->
# SmartTomcat Changelog

## [4.3.4]
### Changed
- Improve the Tomcat runner settings editor 
- Reuse the `<Resources>` element in the context.xml file, fixes #83

## [4.3.3]
### Changed
- Improve Tomcat server management
- Remove unnecessary `path` in Tomcat context file
- Handle exceptions in older IDE versions

## [4.3.2]
### Changed
- Fix the bug where the `temp` folder is not created

## [4.3.1]
### Added
- Support Tomcat 6 and 7
### Changed
- Use separate context file to deploy webapps (#89)
- Fixed IDEA warning during startup
- Fixed the wrong `catalina.home` value

## [4.3.0]
### Added
- Added support for redirecting the Tomcat logs to console.
- Added support for exiting the Tomcat process gracefully when stopping
### Changed
- Fixed the incorrect path of the Tomcat logs
- Improved the TomcatRunner

## [4.2.0]
### Changed
- IDEA - upgrade intellij platformVersion to latestVersion `2202.2+`
- Dependencies - upgrade `org.jetbrains.intellij` to `1.6.0`

## [4.1.0]
### Changed 
- fixed defects
- Dependencies - upgrade `org.jetbrains.intellij` to `1.5.2`

## [4.0.0]
### Added
- changelog.md
- add Dependencies plugin: `org.jetbrains.changelog 1.3.1`

### Changed
- IDEA - upgrade intellij platformVersion to `2021.1`
- Dependencies - upgrade `org.jetbrains.intellij` to `1.3.0` 
