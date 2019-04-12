# Liquigen
>
Liquibase Changeset Generation for IntelliJ IDEA
>
## How to Use
This plugin acts as a front-end for several Liquibase command-line actions.

Currently the following actions are supported:
- Generate a Liquibase XML changelog to recreate a selected Data Source from scratch
- Generate a Liquibase XML changelog to create selected database objects for a single Data Source
- Compare two Data Sources - a reference and a target - and generate a Liquibase XML changelog
to transform the target Data Source to the reference

### Generate XML from Data Source
Select a Data Source, then select <kbd>Liquibase</kbd> > <kbd>Generate Changeset</kbd>

<img src="https://raw.githubusercontent.com/iman89/liquigen/master/doc/liquigen_generate_data_source.png" width="735" height="490" />

### Generate XML from Database Objects
Select a single or several Database Objects, then select <kbd>Liquibase</kbd> > <kbd>Generate Changeset</kbd>

If Liquibase does not support generating XML changeset for selected objects, the resulting XML will be empty.

<img src="https://raw.githubusercontent.com/iman89/liquigen/master/doc/liquigen_generate_db_object.png" width="735" height="490" />

### Compare two Data Sources
- Select a Data Source, then select <kbd>Liquibase</kbd> > <kbd>Generate Diff</kbd>
- Select a reference Data Source in the pop-up

or

- Select two Data Sources, then select <kbd>Liquibase</kbd> > <kbd>Generate Diff</kbd> on any of them

Generated diff will be based upon which transformations are needed to transform target Data Source to a reference Data Source.

This operation is not symmetric.

<img src="https://raw.githubusercontent.com/iman89/liquigen/master/doc/liquigen_generate_diff.png" width="735" height="490" />

>
## How to Install
- Using IDE built-in plugin system on Windows:
  - <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "Liquigen"</kbd> > <kbd>Install Plugin</kbd>
- Manually:
  - Download the [latest release](https://github.com/iman89/liquigen/releases) and install it manually using <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Install plugin from disk...</kbd>
>
## How to Build
To build the plugin from source code, please execute:
```
gradlew buildPlugin
```
You can find the distribution file in `build/distributions` folder.

**Caution!** This plugin is configured to build against a bundled distribution of **IntelliJ Ultimate 2019.1**

This can result in an extremely heavy and lengthy download of dependencies.
If you want to build this plugin against your local IntelliJ IDE installation, please consult
[Gradle IntelliJ plugin](https://github.com/JetBrains/gradle-intellij-plugin) documentation.
>
## Download
Download the [latest release](https://github.com/iman89/liquigen/releases).
