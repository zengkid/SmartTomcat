<!-- Keep a Changelog guide -> https://keepachangelog.com -->
# SmartTomcat Changelog

##[4.7.4]

### Fixed
- write change log in plugin.xml

## [4.7.3]

- be able to update the server.xml under the <project>/.smarttomcat/<module>/conf.

## [4.7.2]

- Add option to disable run configuration from context.

## [4.7.1]

- Improve the release CI job.
- Fix an NPE

## [4.7.0]

- Support config the catalina base directory, thanks to @meier123456

## [4.6.1]

- Stop the debug process gracefully, fix #75.

## [4.6.0]

- Support configuring the SSL port, thanks to @leopoldhub.

## [4.5.0]

- Support reading classpath from the specific module as the classpath for Tomcat runtime.

## [4.4.1]
### Added

- Added support for passing extra classpath the JVM.

## [4.4.0]
### Added

- Added support for passing extra classpath the JVM.

## [4.3.8]
### Added

- Added support for `allowLinking` and `cacheMaxSize` configurations, fix #99

### Changed

- Fixed a bug where SmartTomcat run configuration overrides the Application configuration, fix #100

## [4.3.7]
### Changed

- Fix context paths like /foo/bar may not working on Windows, fix #95

## [4.3.6]
### Changed

- Allow `Context Path` to be empty
- Support `Context Path` like `/foo/bar`
- Fix #92

## [4.3.5]
### Changed
- Remove Context elements inside the `server.xml`, fix #91
- Remove `reloadable` from the generated context xml file
- Pretty print the generated context xml file
- Improve the Tomcat configuration producer

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
