# triplea-map-tools
Tools for editing TripleA maps.

Usage:
```
Usage: triplea-tools [COMMAND]
Commands:
  help            Displays help information about the specified command
  excel           Synchronizes the game file with an Excel spreadsheet to edit
                    the unit and territory attachments and initial placements
  cp-notes        Copy game notes into our out of the game file to a separate
                    HTML file for easier editing
  template        Plugin a matrix of values into a template
  connections     Automatically generate connections from polygons.txt
  railroads       For games that use canals through land as a railroad system,
                    this will automate much of the boilerplate
  vars            Create a few standard/convenience variables automatically
                    (like a all land territories) for use in triggers
  fix-file-names  Fix capitilzation of files in a folder, even on Windows
  tile-join       Joins image tile back into the full size original image
  tile-quantize   Quantize (standardize) the colors in an image to black
                    (borders), white (land) and blue (sea) for base tile images
  tile-split      Splits an image into equally sized square tile images
```
