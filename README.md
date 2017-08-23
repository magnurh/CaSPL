# CaSPL-gen

A CFM-generator for benchmark testing. This tool make use of the [BeTTy](http://www.isa.us.es/betty/) framework to generate sets of Feature Models and extends them with contexts and validity Formulas to represent families of context-aware software. 

## Prerequisities

To install and run this application you need [gradle](https://gradle.org/) version 4.1 or higher. Look at their [installation guide](https://gradle.org/install/) for instructions.
This project also requires [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

## Installation

Open your terminal window and navigate to the root folder of this project (where the build.gradle file is located). Use the following command to install CaSPL-gen:

```
./gradlew build
```
CaSPL-gen is now ready to use. To run the application use the command:

```
./gradlew run
```

## Usage

The application requires a settings file in json format to define the properties of the new data set. When started up, a terminal message will appear asking for a path to this file. If you want to use the default settings simply press return. A template containing the default settings can be found under /user-settings. Edit this file or make a copy to adjust the settings. See below for detailed descriptions on the different parameters that can be set.

### User settings

The settings are put into a json file. None of the properties are required, however the app will select a default setting for any of the fields that are absent. The following example shows what property names are used and give examples on values:

```
{
  "dataset_name": "dataset",
  "sizeDataSet": 10,
  "numberOfFeatures": 50,
  "percentageCTC": 30,
  "probMand": 0,
  "probOpt": 0,
  "probAlt": 0,
  "probOr": 0,
  "maxBranchingFactor": 10,
  "maxSetChildren": 5,
  "minAttrValue": 0,
  "maxAttrValue": 100,
  "contextMaxSize": 10,
  "contextMaxValue": 10,
  "maxPercentageVFs": 20,
  "advanced": {
    "simpleMode": false,
    "maxTriesValidModel": 10,
    "requiredNumberOfPathsFromRoot": 5,
    "pathSearchDepth": 5,
    "maxTriesPathRequirement": 0,
    "hyvarrecInputScript": false,
    "hyvarrecPort": 4000
  }
}
```

A setting file may contain 15 settings that affects the dataset and the general structure of the CFMs and 7 advanced settings.

* dataset_name - Name of the dataset. Used to organise the CFMs in folders.
* sizeDataSet - The number of CFMs to be generated at execution.
* numberOfFeatures - The number of features in the CFM. Also called the size.
* percentageCTC - The maximum amount of Cross-Tree-Constraints to include in a given CFM. This is a percentage of the CFM size.
* ProbMand - The probability that a relationship is Mandatory.
* probOpt - The probability that a relationship is Optional.
* probAlt - The probability that a relationship is Alternative.
* probOr - The probability that a relationship is an Or.
* maxBranchingFactor - The maximum number of child features for a given parent feature.
* maxSetChildren - The maximum number of child features in a given Alternative- or Or-group.
* minAttrValue - The minimum domain value for attributes.
* maxAttrValue - The maximum domain value for attributes.
* contextMaxSize - The maximum number of context entities.
* contextMaxValue - The maximum domain value of a given context entity.
* maxPercentageVFs - The maximum amount of Validity Formulas to include in a given CFM. This is a percentage of the CFM size.

Note: the values of probMand, probOpt, probAlt and probOr must either sum up to 100 or they can all be set to 0, in which case BeTTy decides at random what relation types to use.
The advanced settings can be applied to influence the running-time of the generator. Two of the settings are related to using the CFMs with HyVarRec.

* simpleMode - toggles the modality. If true CaSPL-gen will not use any strategies to limit the number of void CFMs, CFMs with dead features and dead sub-trees or CFMs that can be made valid by simple configurations.
* maxTriesValidModel - How many attempts CaSPL-gen has to generate a valid basic FM structure. The SAT reasoner [sat4j](http://www.sat4j.org/) is used to check for validity. If set to 0 the validity check is switched off.
* requiredNumberOfPathsFromRoot - How many paths following mandatory-, alternative- and or-relations that are required for a basic FM to be accepted.
* pathSearchDepth - The search-depth when counting paths.
* maxTriesPathRequirement - The number of attempts CaSPL gets on generating an FM that follows the path requirements. If set to 0 the function is turned off.
* hyvarrecInputScript - If set to true a shell-script is generated that can be run to import the CFMs in the dataset to HyVarRec. To run the script successfully it is required that HyVarRec is running using Docker.
* The port selected when setting up the HyVarRec docker container.

### Output
The CFMs are saved in folders under /out/data . In order to prevent data sets being overwritten a new folder with a time-stamp is created for each execution. One folder (afm) contains the basic Feature Models (or Attributed Feature Models) generated by BeTTy and used to generate the CFMs. The structure of the CFMs is based on the input specification defined in [HyVarRec](https://github.com/HyVar/hyvar-rec). For a detailed description on how the CFM files are defined see the [input schema](https://github.com/HyVar/hyvar-rec/blob/master/spec/hyvar_input_schema.json) for HyVarRec.
